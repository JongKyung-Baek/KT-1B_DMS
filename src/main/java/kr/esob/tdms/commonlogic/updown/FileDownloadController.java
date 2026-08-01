package kr.esob.tdms.commonlogic.updown;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import kr.esob.tdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.tdms.commonlogic.updown.runtime.DownloadRuntimeState;
import kr.esob.tdms.commonlogic.updown.runtime.DownloadRuntimeStatus;
import kr.esob.tdms.commonlogic.updown.runtime.DownloadRuntimeStore;
import kr.esob.tdms.controller.login.UserVO;
import kr.esob.tdms.util.StoragePathUtils;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
public class FileDownloadController {
    private final DownloadRuntimeStore runtimeStore;
    private final CommonUpdownV2Service updownV2Service;

    public FileDownloadController(DownloadRuntimeStore runtimeStore,
                                  CommonUpdownV2Service updownV2Service) {
        this.runtimeStore = runtimeStore;
        this.updownV2Service = updownV2Service;
    }

    /**
     * Native download client endpoint. The path value is a short-lived random
     * capability issued only after ACL approval; it is never a server file path,
     * file name, object ID, or REST sequence.
     */
    @GetMapping("/download/{ticket:[0-9a-fA-F]{32}}")
    public ResponseEntity<?> downloadFile(@PathVariable("ticket") String ticket,
                                          Authentication authentication,
                                          HttpServletRequest request) throws IOException {
        DownloadRuntimeState state = runtimeStore.claimByDownloadRequestKey(ticket);
        if (state == null) {
            log.warn("[DOWNLOAD][DENY] unknown, expired or already used capability");
            return ResponseEntity.notFound().build();
        }

        UserVO actor = currentOwner(state, authentication, request);
        if (actor == null) {
            log.warn("[DOWNLOAD][DENY] capability owner mismatch");
            markFailedQuietly(state.getWsSeq(), "Download capability owner mismatch.");
            return ResponseEntity.notFound().build();
        }

        File root = resolveDownloadRoot().getCanonicalFile();
        File file = new File(state.getTempFilePath()).getCanonicalFile();
        if (!file.toPath().startsWith(root.toPath()) || !file.isFile()) {
            log.warn("[DOWNLOAD][DENY] invalid mapped file");
            markFailedQuietly(state.getWsSeq(), "Temporary download file is unavailable.");
            return ResponseEntity.notFound().build();
        }

        FileDeletingInputStream stream = new FileDeletingInputStream(file);
        DownloadRuntimeState completedState;
        try {
            runtimeStore.update(state.getWsSeq(), runtime -> runtime.markResult("00", ""));
            completedState = runtimeStore.get(state.getWsSeq());
            if (completedState == null
                    || completedState.getStatus() != DownloadRuntimeStatus.COMPLETED) {
                throw new IllegalStateException("Download completion was not persisted.");
            }

            boolean auditSaved = updownV2Service.saveDownloadAudit(
                actor, completedState, DownloadRuntimeStatus.COMPLETED.name(), null);
            if (!auditSaved) {
                log.error("[DOWNLOAD][DENY] success audit persistence failed");
                return failClosed(state.getWsSeq(), stream,
                    "Download audit persistence failed.");
            }

            completedState = runtimeStore.markAuditSaved(state.getWsSeq());
            if (completedState == null || !completedState.isActLogSaved()) {
                throw new IllegalStateException(
                    "Download audit checkpoint was not persisted.");
            }

        } catch (Exception exception) {
            log.error("[DOWNLOAD][DENY] completion persistence failed. type={}",
                exception.getClass().getSimpleName());
            return failClosed(state.getWsSeq(), stream,
                "Download completion persistence failed.");
        }

        String originalName = safeResponseFileName(state.getOriginalFileName());
        String encodedName = URLEncoder.encode(originalName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + asciiFallback(originalName) + "\"; filename*=UTF-8''" + encodedName);
        headers.add(HttpHeaders.CACHE_CONTROL, "no-store, private");
        headers.add("X-Content-Type-Options", "nosniff");

        log.info("[DOWNLOAD][RESP] success");
        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(file.length())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(new InputStreamResource(stream));
    }

    private UserVO currentOwner(DownloadRuntimeState state,
                                Authentication authentication,
                                HttpServletRequest request) {
        if (state == null || authentication == null
                || !(authentication.getPrincipal() instanceof UserVO)
                || request == null || request.getSession(false) == null) {
            return null;
        }
        UserVO actor = (UserVO) authentication.getPrincipal();
        return state.isOwnedBy(actor.getUserCd(), request.getSession(false).getId())
            ? actor : null;
    }

    private ResponseEntity<?> failClosed(String wsSeq,
                                         FileDeletingInputStream stream,
                                         String reason) {
        closeQuietly(stream);
        markFailedQuietly(wsSeq, reason);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    private void markFailedQuietly(String wsSeq, String reason) {
        try {
            runtimeStore.update(wsSeq, state -> state.markFailed(reason));
        } catch (Exception persistenceException) {
            log.error("[DOWNLOAD][FAIL-CLOSED] failed state could not be persisted. type={}",
                persistenceException.getClass().getSimpleName());
        }
    }

    private void closeQuietly(FileDeletingInputStream stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (IOException cleanupException) {
            log.warn("[DOWNLOAD][CLEANUP] temporary file deletion failed. type={}",
                cleanupException.getClass().getSimpleName());
        }
    }

    private File resolveDownloadRoot() {
        String root = SystemConfig.getSystemConfigValue("UPDOWN_PATH");
        if (root == null || root.trim().isEmpty()) {
            root = System.getProperty("java.io.tmpdir");
        }
        return new File(root.trim());
    }

    private String safeResponseFileName(String value) {
        if (value == null || value.trim().isEmpty()) return "download.bin";
        try {
            return StoragePathUtils.fileName(value);
        } catch (IllegalArgumentException exception) {
            return "download.bin";
        }
    }

    private String asciiFallback(String value) {
        String fallback = value.replaceAll("[^A-Za-z0-9._-]", "_").replaceAll("_+", "_");
        return fallback.isEmpty() ? "download.bin" : fallback;
    }
}

package kr.esob.fdms.commonlogic.updown;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeState;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeStore;
import kr.esob.fdms.util.StoragePathUtils;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class FileDownloadController {
    private final DownloadRuntimeStore runtimeStore;

    public FileDownloadController(DownloadRuntimeStore runtimeStore) {
        this.runtimeStore = runtimeStore;
    }

    /**
     * Native download client endpoint. The path value is a short-lived random
     * capability issued only after ACL approval; it is never a server file path,
     * file name, object ID, or REST sequence.
     */
    @GetMapping("/download/{ticket:[0-9a-fA-F]{32}}")
    public ResponseEntity<?> downloadFile(@PathVariable("ticket") String ticket) throws IOException {
        DownloadRuntimeState state = runtimeStore.claimByDownloadRequestKey(ticket);
        if (state == null) {
            log.warn("[DOWNLOAD][DENY] unknown, expired or already used capability");
            return ResponseEntity.notFound().build();
        }

        File root = resolveDownloadRoot().getCanonicalFile();
        File file = new File(state.getTempFilePath()).getCanonicalFile();
        if (!file.toPath().startsWith(root.toPath()) || !file.isFile()) {
            log.warn("[DOWNLOAD][DENY] invalid mapped file");
            return ResponseEntity.notFound().build();
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
            .body(new InputStreamResource(new FileDeletingInputStream(file)));
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

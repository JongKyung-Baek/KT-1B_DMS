package kr.esob.fdms.commonlogic.updown.runtime;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import javax.inject.Inject;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kr.esob.fdms.commonlogic.updown.CommonUpdownV2Service;
import kr.esob.fdms.controller.login.UserVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DownloadRuntimeCleanupScheduler {
    private static final String EXPIRED_MESSAGE = "Download runtime expired.";
    private static final String RESTART_MESSAGE =
        "Download interrupted by application restart.";

    @Inject
    private DownloadRuntimeStore runtimeStore;

    @Inject
    private CommonUpdownV2Service updownV2Service;

    /**
     * Rows created before this JVM started belong to an earlier process. New
     * requests accepted after startup are never swept by restart recovery.
     */
    private final LocalDateTime startupCutoff = LocalDateTime.now();
    private volatile boolean startupRecoveryComplete;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverPersistedRuntimeAfterStartup() {
        try {
            recoverPersistedRuntimeNow();
        } catch (Exception exception) {
            // Do not silently fall back to memory. Registration and claim still
            // fail closed through DownloadRuntimeStore while PostgreSQL is down.
            log.error("[V2-RUNTIME-RECOVERY] deferred cause={}",
                exception.getClass().getSimpleName());
        }
    }

    // One minute retry cadence for recovery, expiry audit and temp cleanup.
    @Scheduled(fixedDelay = 60000)
    public void cleanupExpired() {
        if (!startupRecoveryComplete) {
            try {
                recoverPersistedRuntimeNow();
            } catch (Exception exception) {
                log.error("[V2-RUNTIME-RECOVERY] retry failed cause={}",
                    exception.getClass().getSimpleName());
            }
        }
        try {
            cleanupExpiredNow();
        } catch (Exception exception) {
            log.error("[V2-RUNTIME-CLEANUP] failed cause={}",
                exception.getClass().getSimpleName());
        }
    }

    /**
     * Converts pre-start non-terminal or expired rows to FAILED, saves the
     * server-side audit, then deletes their temp file and durable row.
     * Terminal-but-unaudited rows retain their original result and are retried.
     */
    public synchronized int recoverPersistedRuntimeNow() {
        int removedCount = 0;
        boolean allRecovered = true;
        List<DownloadRuntimeState> candidates =
            runtimeStore.findRestartRecoveryCandidates(startupCutoff);

        for (DownloadRuntimeState candidate : candidates) {
            DownloadRuntimeState state = runtimeStore.get(candidate.getWsSeq());
            if (state == null) {
                continue;
            }
            if (!state.isActLogSaved()
                    && (!isTerminal(state) || state.isExpired(startupCutoff))) {
                state = runtimeStore.markRestartRecoveryFailed(
                    state.getWsSeq(), startupCutoff, RESTART_MESSAGE);
            }
            if (state == null) {
                continue;
            }
            if (!persistAuditIfRequired(state)) {
                allRecovered = false;
                continue;
            }
            state = runtimeStore.get(state.getWsSeq());
            if (state == null) {
                continue;
            }
            if (!deleteIfExists(state.getTempFilePath())) {
                allRecovered = false;
                continue;
            }
            if (runtimeStore.remove(state.getWsSeq()) != null) {
                removedCount++;
            }
        }

        startupRecoveryComplete = allRecovered;
        return removedCount;
    }

    public int cleanupExpiredNow() {
        int removedCount = 0;
        List<String> expiredKeys = runtimeStore.findExpiredKeys();
        for (String key : expiredKeys) {
            DownloadRuntimeState state = runtimeStore.get(key);
            if (state == null) {
                continue;
            }
            if (!isTerminal(state)) {
                state = runtimeStore.markExpiredFailed(key, EXPIRED_MESSAGE);
            }
            if (state == null || !persistAuditIfRequired(state)) {
                continue;
            }
            state = runtimeStore.get(key);
            if (state == null) {
                continue;
            }
            if (!deleteIfExists(state.getTempFilePath())) {
                continue;
            }
            if (runtimeStore.remove(key) != null) {
                removedCount++;
            }
        }
        return removedCount;
    }

    private boolean persistAuditIfRequired(DownloadRuntimeState state) {
        if (state.isActLogSaved()) {
            return true;
        }
        UserVO actor = new UserVO();
        actor.setUserCd(state.getOwnerUserCd());
        actor.setUserId(state.getOwnerUserId());
        actor.setUserNm(state.getOwnerUserNm());
        if (!updownV2Service.saveDownloadAudit(
                actor, state, state.getStatus().name(), state.getErrorMessage())) {
            return false;
        }
        runtimeStore.markAuditSaved(state.getWsSeq());
        return true;
    }

    private boolean isTerminal(DownloadRuntimeState state) {
        return state.getStatus() == DownloadRuntimeStatus.COMPLETED
            || state.getStatus() == DownloadRuntimeStatus.FAILED;
    }

    private boolean deleteIfExists(String path) {
        if (path == null || path.trim().isEmpty()) {
            return true;
        }
        try {
            File file = new File(path);
            return !file.exists() || (file.isFile() && file.delete());
        } catch (Exception exception) {
            log.warn("[V2-RUNTIME-CLEANUP] temp delete failed cause={}",
                exception.getClass().getSimpleName());
            return false;
        }
    }
}

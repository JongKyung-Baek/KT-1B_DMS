package kr.esob.tdms.commonlogic.updown.runtime;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Durable V2 download runtime state.
 *
 * <p>The local map is only a cache. Every public operation first persists to,
 * or reloads from, PostgreSQL. A database error is deliberately propagated so
 * a capability can never be issued from memory alone.</p>
 */
@Component
public class DownloadRuntimeStore {
    private static final long DEFAULT_TTL_MINUTES = 30L;

    private final DownloadRuntimeDao runtimeDao;
    private final ConcurrentMap<String, DownloadRuntimeState> stateMap =
        new ConcurrentHashMap<String, DownloadRuntimeState>();
    private final ConcurrentMap<String, Object> stateLocks =
        new ConcurrentHashMap<String, Object>();

    @Inject
    public DownloadRuntimeStore(DownloadRuntimeDao runtimeDao) {
        if (runtimeDao == null) {
            throw new IllegalArgumentException("DownloadRuntimeDao is required.");
        }
        this.runtimeDao = runtimeDao;
    }

    @Transactional(rollbackFor = Exception.class)
    public DownloadRuntimeState registerQueued(String wsSeq, String requestNo, String docSeq,
                                               String fileNo, String fileSeq,
                                               String downloadRequestKey, String requestType,
                                               String objectType, String originalFileName,
                                               String ownerUserCd, String ownerUserId,
                                               String ownerUserNm, String ownerSessionId) {
        DownloadRuntimeState state = DownloadRuntimeState.createQueued(
            wsSeq, requestNo, docSeq, fileNo, fileSeq, downloadRequestKey,
            requestType, objectType, originalFileName, ownerUserCd, ownerUserId,
            ownerUserNm, ownerSessionId, DEFAULT_TTL_MINUTES
        );

        // PostgreSQL is written before the capability is published to callers.
        // Any insert/constraint/connectivity failure aborts issuance.
        requireSingleRow(runtimeDao.insertQueued(state), "register queued download");
        DownloadRuntimeState cached = DownloadRuntimeState.copyOf(state);
        DownloadRuntimeState existing = stateMap.putIfAbsent(wsSeq, cached);
        if (existing != null) {
            throw new IllegalStateException("wsSeq is already in use.");
        }
        return DownloadRuntimeState.copyOf(state);
    }

    public DownloadRuntimeState get(String wsSeq) {
        if (isBlank(wsSeq)) {
            return null;
        }
        DownloadRuntimeState persisted = runtimeDao.selectByWsSeq(wsSeq);
        return cachePersisted(wsSeq, persisted);
    }

    public boolean exists(String wsSeq) {
        return get(wsSeq) != null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(String wsSeq, Consumer<DownloadRuntimeState> updater) {
        if (isBlank(wsSeq) || updater == null) {
            return;
        }
        Object lock = stateLocks.computeIfAbsent(wsSeq, key -> new Object());
        synchronized (lock) {
            DownloadRuntimeState persisted = runtimeDao.selectByWsSeq(wsSeq);
            if (persisted == null) {
                stateMap.remove(wsSeq);
                return;
            }
            DownloadRuntimeState updated = DownloadRuntimeState.copyOf(persisted);
            updater.accept(updated);
            updated.extendTtl(DEFAULT_TTL_MINUTES);
            requireSingleRow(runtimeDao.updateState(updated), "update download runtime");
            DownloadRuntimeState refreshed = runtimeDao.selectByWsSeq(wsSeq);
            if (refreshed == null) {
                throw new IllegalStateException(
                    "Updated download runtime could not be reloaded.");
            }
            stateMap.put(wsSeq, DownloadRuntimeState.copyOf(refreshed));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public DownloadRuntimeState remove(String wsSeq) {
        if (isBlank(wsSeq)) {
            return null;
        }
        Object lock = stateLocks.computeIfAbsent(wsSeq, key -> new Object());
        synchronized (lock) {
            DownloadRuntimeState persisted = runtimeDao.selectByWsSeq(wsSeq);
            if (persisted == null) {
                stateMap.remove(wsSeq);
                stateLocks.remove(wsSeq, lock);
                return null;
            }
            requireSingleRow(runtimeDao.deleteByWsSeq(wsSeq), "remove download runtime");
            stateMap.remove(wsSeq);
            stateLocks.remove(wsSeq, lock);
            return DownloadRuntimeState.copyOf(persisted);
        }
    }

    public int size() {
        return runtimeDao.countAll();
    }

    public DownloadRuntimeState findLatestByFileNm(String fileNm) {
        if (isBlank(fileNm)) {
            return null;
        }
        DownloadRuntimeState persisted = runtimeDao.selectLatestByFileName(fileNm);
        return persisted == null ? null : cachePersisted(persisted.getWsSeq(), persisted);
    }

    public DownloadRuntimeState findLatestByDownloadRequestKey(String downloadRequestKey) {
        if (isBlank(downloadRequestKey)) {
            return null;
        }
        DownloadRuntimeState persisted =
            runtimeDao.selectLatestByDownloadRequestKey(downloadRequestKey);
        return persisted == null ? null : cachePersisted(persisted.getWsSeq(), persisted);
    }

    /**
     * PostgreSQL performs the check-and-set in one statement. The map never
     * decides whether a capability is valid or whether it has already been used.
     */
    @Transactional(rollbackFor = Exception.class)
    public DownloadRuntimeState claimByDownloadRequestKey(String downloadRequestKey) {
        if (isBlank(downloadRequestKey)) {
            return null;
        }
        DownloadRuntimeState claimed =
            runtimeDao.claimByDownloadRequestKey(downloadRequestKey);
        return claimed == null ? null : cachePersisted(claimed.getWsSeq(), claimed);
    }

    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpired() {
        int removed = 0;
        for (String wsSeq : runtimeDao.selectExpiredKeys()) {
            DownloadRuntimeState state = runtimeDao.selectByWsSeq(wsSeq);
            if (state == null || !state.isActLogSaved()) {
                continue;
            }
            int affected = runtimeDao.deleteAuditedExpiredByWsSeq(wsSeq);
            if (affected == 1) {
                stateMap.remove(wsSeq);
                stateLocks.remove(wsSeq);
                removed++;
            } else if (affected > 1) {
                throw new IllegalStateException("Unexpected expired runtime delete count.");
            }
        }
        return removed;
    }

    public List<String> findExpiredKeys() {
        return runtimeDao.selectExpiredKeys();
    }

    public List<DownloadRuntimeState> findRestartRecoveryCandidates(
            LocalDateTime startupCutoff) {
        return runtimeDao.selectRestartRecoveryCandidates(startupCutoff);
    }

    /**
     * Marks an unfinished or expired pre-start row as FAILED without extending
     * its TTL. A failed audit therefore remains immediately retryable.
     */
    @Transactional(rollbackFor = Exception.class)
    public DownloadRuntimeState markRestartRecoveryFailed(
            String wsSeq, LocalDateTime startupCutoff, String errorMessage) {
        int affected = runtimeDao.markRestartRecoveryFailed(
            wsSeq, startupCutoff, errorMessage);
        DownloadRuntimeState persisted = runtimeDao.selectByWsSeq(wsSeq);
        if (affected == 0 && persisted != null && !isTerminal(persisted)) {
            throw new IllegalStateException(
                "Unable to mark interrupted download runtime as failed.");
        }
        if (affected > 1) {
            throw new IllegalStateException(
                "Unexpected restart recovery update count.");
        }
        return cachePersisted(wsSeq, persisted);
    }

    /**
     * Same as restart recovery, but only for a currently expired non-terminal
     * row handled by the periodic cleanup lifecycle.
     */
    @Transactional(rollbackFor = Exception.class)
    public DownloadRuntimeState markExpiredFailed(String wsSeq, String errorMessage) {
        int affected = runtimeDao.markExpiredFailed(wsSeq, errorMessage);
        DownloadRuntimeState persisted = runtimeDao.selectByWsSeq(wsSeq);
        if (affected == 0 && persisted != null && !isTerminal(persisted)) {
            throw new IllegalStateException(
                "Unable to mark expired download runtime as failed.");
        }
        if (affected > 1) {
            throw new IllegalStateException(
                "Unexpected expiry recovery update count.");
        }
        return cachePersisted(wsSeq, persisted);
    }

    /**
     * Persists the audit checkpoint without extending an expired row's TTL.
     * Cleanup can therefore retry a failed temp-file deletion immediately.
     */
    @Transactional(rollbackFor = Exception.class)
    public DownloadRuntimeState markAuditSaved(String wsSeq) {
        DownloadRuntimeState current = runtimeDao.selectByWsSeq(wsSeq);
        if (current == null) {
            stateMap.remove(wsSeq);
            return null;
        }
        if (!current.isActLogSaved()) {
            requireSingleRow(runtimeDao.markAuditSaved(wsSeq),
                "mark download audit saved");
        }
        DownloadRuntimeState persisted = runtimeDao.selectByWsSeq(wsSeq);
        return cachePersisted(wsSeq, persisted);
    }

    private DownloadRuntimeState cachePersisted(
            String wsSeq, DownloadRuntimeState persisted) {
        if (persisted == null) {
            stateMap.remove(wsSeq);
            return null;
        }
        DownloadRuntimeState cached = DownloadRuntimeState.copyOf(persisted);
        stateMap.put(wsSeq, cached);
        return DownloadRuntimeState.copyOf(persisted);
    }

    private void requireSingleRow(int affectedRows, String operation) {
        if (affectedRows != 1) {
            throw new IllegalStateException("Unable to " + operation + ".");
        }
    }

    private boolean isTerminal(DownloadRuntimeState state) {
        return state.getStatus() == DownloadRuntimeStatus.COMPLETED
            || state.getStatus() == DownloadRuntimeStatus.FAILED;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

package kr.esob.fdms.commonlogic.updown.runtime;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small PostgreSQL-contract test double. Production code never falls back to
 * this implementation; it exists only to exercise store/recovery behavior
 * without a parallel integration database.
 */
public class DownloadRuntimeTestDao extends DownloadRuntimeDao {
    private final Map<String, DownloadRuntimeState> rows =
        new ConcurrentHashMap<String, DownloadRuntimeState>();

    @Override
    public synchronized int insertQueued(DownloadRuntimeState state) {
        return rows.putIfAbsent(state.getWsSeq(), DownloadRuntimeState.copyOf(state)) == null
            ? 1 : 0;
    }

    @Override
    public synchronized DownloadRuntimeState selectByWsSeq(String wsSeq) {
        return DownloadRuntimeState.copyOf(rows.get(wsSeq));
    }

    @Override
    public synchronized int updateState(DownloadRuntimeState state) {
        DownloadRuntimeState current = rows.get(state.getWsSeq());
        if (current == null) {
            return 0;
        }
        DownloadRuntimeState updated = DownloadRuntimeState.copyOf(state);
        // The production update statement deliberately cannot reset an atomic
        // public capability claim.
        updated.setDownloadClaimed(current.isDownloadClaimed());
        updated.setClaimedAt(current.getClaimedAt());
        rows.put(state.getWsSeq(), updated);
        return 1;
    }

    @Override
    public synchronized DownloadRuntimeState claimByDownloadRequestKey(String key) {
        LocalDateTime now = LocalDateTime.now();
        DownloadRuntimeState candidate = latestByKey(key);
        if (candidate == null || candidate.isDownloadClaimed()
                || candidate.getStatus() != DownloadRuntimeStatus.SENT_TO_WS
                || candidate.isExpired(now)) {
            return null;
        }
        candidate.setDownloadClaimed(true);
        candidate.setClaimedAt(now);
        candidate.setUpdatedAt(now);
        rows.put(candidate.getWsSeq(), DownloadRuntimeState.copyOf(candidate));
        return DownloadRuntimeState.copyOf(candidate);
    }

    @Override
    public synchronized DownloadRuntimeState selectLatestByFileName(String fileName) {
        DownloadRuntimeState latest = null;
        for (DownloadRuntimeState state : rows.values()) {
            if (!fileName.equals(state.getSavedFileName())
                    && !fileName.equals(state.getOriginalFileName())) {
                continue;
            }
            latest = newer(latest, state);
        }
        return DownloadRuntimeState.copyOf(latest);
    }

    @Override
    public synchronized DownloadRuntimeState selectLatestByDownloadRequestKey(String key) {
        return DownloadRuntimeState.copyOf(latestByKey(key));
    }

    @Override
    public synchronized int deleteByWsSeq(String wsSeq) {
        return rows.remove(wsSeq) == null ? 0 : 1;
    }

    @Override
    public synchronized int deleteAuditedExpiredByWsSeq(String wsSeq) {
        DownloadRuntimeState state = rows.get(wsSeq);
        if (state == null || !state.isActLogSaved()
                || !state.isExpired(LocalDateTime.now())) {
            return 0;
        }
        rows.remove(wsSeq);
        return 1;
    }

    @Override
    public synchronized List<String> selectExpiredKeys() {
        LocalDateTime now = LocalDateTime.now();
        List<String> keys = new ArrayList<String>();
        for (DownloadRuntimeState state : rows.values()) {
            if (state.isExpired(now)) {
                keys.add(state.getWsSeq());
            }
        }
        keys.sort(String::compareTo);
        return keys;
    }

    @Override
    public synchronized List<DownloadRuntimeState> selectRestartRecoveryCandidates(
            LocalDateTime startupCutoff) {
        List<DownloadRuntimeState> candidates = new ArrayList<DownloadRuntimeState>();
        for (DownloadRuntimeState state : rows.values()) {
            if (state.getCreatedAt().isBefore(startupCutoff)
                    && (!state.isActLogSaved() || state.isExpired(startupCutoff))) {
                candidates.add(DownloadRuntimeState.copyOf(state));
            }
        }
        candidates.sort(Comparator.comparing(DownloadRuntimeState::getCreatedAt));
        return candidates;
    }

    @Override
    public synchronized int markRestartRecoveryFailed(
            String wsSeq, LocalDateTime startupCutoff, String errorMessage) {
        DownloadRuntimeState state = rows.get(wsSeq);
        if (state == null || !state.getCreatedAt().isBefore(startupCutoff)) {
            return 0;
        }
        boolean terminal = state.getStatus() == DownloadRuntimeStatus.COMPLETED
            || state.getStatus() == DownloadRuntimeStatus.FAILED;
        if (terminal && !state.isExpired(startupCutoff)) {
            return 0;
        }
        state.markFailed(errorMessage);
        state.setResultCode(
            state.getResultCode() == null || state.getResultCode().isEmpty()
                ? "99" : state.getResultCode());
        state.setOptionalData(null);
        if (state.getExpireAt().isAfter(LocalDateTime.now())) {
            state.setExpireAt(LocalDateTime.now());
        }
        return 1;
    }

    @Override
    public synchronized int markExpiredFailed(String wsSeq, String errorMessage) {
        DownloadRuntimeState state = rows.get(wsSeq);
        if (state == null || !state.isExpired(LocalDateTime.now())) {
            return 0;
        }
        if (state.getStatus() == DownloadRuntimeStatus.COMPLETED
                || state.getStatus() == DownloadRuntimeStatus.FAILED) {
            return 0;
        }
        state.markFailed(errorMessage);
        state.setResultCode("99");
        state.setOptionalData(null);
        return 1;
    }

    @Override
    public synchronized int markAuditSaved(String wsSeq) {
        DownloadRuntimeState state = rows.get(wsSeq);
        if (state == null
                || (state.getStatus() != DownloadRuntimeStatus.COMPLETED
                && state.getStatus() != DownloadRuntimeStatus.FAILED)) {
            return 0;
        }
        state.markActLogSaved();
        return 1;
    }

    @Override
    public synchronized int countAll() {
        return rows.size();
    }

    public synchronized void forceExpire(String wsSeq, LocalDateTime expireAt) {
        DownloadRuntimeState state = rows.get(wsSeq);
        if (state != null) {
            state.setExpireAt(expireAt);
        }
    }

    private DownloadRuntimeState latestByKey(String key) {
        DownloadRuntimeState latest = null;
        for (DownloadRuntimeState state : rows.values()) {
            if (key.equals(state.getDownloadRequestKey())) {
                latest = newer(latest, state);
            }
        }
        return latest;
    }

    private DownloadRuntimeState newer(
            DownloadRuntimeState current, DownloadRuntimeState candidate) {
        if (current == null) {
            return candidate;
        }
        if (candidate.getUpdatedAt() != null
                && (current.getUpdatedAt() == null
                || candidate.getUpdatedAt().isAfter(current.getUpdatedAt()))) {
            return candidate;
        }
        return current;
    }
}

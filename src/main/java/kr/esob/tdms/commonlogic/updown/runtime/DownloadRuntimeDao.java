package kr.esob.tdms.commonlogic.updown.runtime;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class DownloadRuntimeDao extends AbstractDao {
    private static final String PREFIX = "sql.DownloadRuntime.";

    public int insertQueued(DownloadRuntimeState state) {
        return (Integer) insert(PREFIX + "insertQueued", state);
    }

    public DownloadRuntimeState selectByWsSeq(String wsSeq) {
        return (DownloadRuntimeState) objNotUseSession(
            PREFIX + "selectByWsSeq", singleton("wsSeq", wsSeq));
    }

    public int updateState(DownloadRuntimeState state) {
        return update(PREFIX + "updateState", state);
    }

    public DownloadRuntimeState claimByDownloadRequestKey(String downloadRequestKey) {
        return (DownloadRuntimeState) objNotUseSession(
            PREFIX + "claimByDownloadRequestKey",
            singleton("downloadRequestKey", downloadRequestKey));
    }

    public DownloadRuntimeState selectLatestByFileName(String fileName) {
        return (DownloadRuntimeState) objNotUseSession(
            PREFIX + "selectLatestByFileName", singleton("fileName", fileName));
    }

    public DownloadRuntimeState selectLatestByDownloadRequestKey(String downloadRequestKey) {
        return (DownloadRuntimeState) objNotUseSession(
            PREFIX + "selectLatestByDownloadRequestKey",
            singleton("downloadRequestKey", downloadRequestKey));
    }

    public int deleteByWsSeq(String wsSeq) {
        return delete(PREFIX + "deleteByWsSeq", singleton("wsSeq", wsSeq));
    }

    public int deleteAuditedExpiredByWsSeq(String wsSeq) {
        return delete(PREFIX + "deleteAuditedExpiredByWsSeq", singleton("wsSeq", wsSeq));
    }

    @SuppressWarnings("unchecked")
    public List<String> selectExpiredKeys() {
        return (List<String>) (List<?>) listNotUseSession(PREFIX + "selectExpiredKeys");
    }

    @SuppressWarnings("unchecked")
    public List<DownloadRuntimeState> selectRestartRecoveryCandidates(LocalDateTime startupCutoff) {
        return (List<DownloadRuntimeState>) (List<?>) listNotUseSession(
            PREFIX + "selectRestartRecoveryCandidates",
            singleton("startupCutoff", startupCutoff));
    }

    public int markRestartRecoveryFailed(String wsSeq, LocalDateTime startupCutoff, String errorMessage) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("wsSeq", wsSeq);
        params.put("startupCutoff", startupCutoff);
        params.put("errorMessage", errorMessage);
        return update(PREFIX + "markRestartRecoveryFailed", params);
    }

    public int markExpiredFailed(String wsSeq, String errorMessage) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("wsSeq", wsSeq);
        params.put("errorMessage", errorMessage);
        return update(PREFIX + "markExpiredFailed", params);
    }

    public int markAuditSaved(String wsSeq) {
        return update(PREFIX + "markAuditSaved", singleton("wsSeq", wsSeq));
    }

    public int countAll() {
        Integer count = (Integer) objNotUseSession(PREFIX + "countAll", null);
        return count == null ? 0 : count.intValue();
    }

    private Map<String, Object> singleton(String key, Object value) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(key, value);
        return params;
    }
}

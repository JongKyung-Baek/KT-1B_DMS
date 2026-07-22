package kr.esob.fdms.commonlogic.updown.runtime;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

@Component
public class DownloadRuntimeStore {
      // wsSeq(32자리 hex) -> 상태
    private final ConcurrentMap<String, DownloadRuntimeState> stateMap = new ConcurrentHashMap<>();

      // 기본 TTL: 30분 (필요 시 조정)
    private static final long DEFAULT_TTL_MINUTES = 30L;

    public DownloadRuntimeState registerQueued(String wsSeq, String requestNo, String docSeq, String fileNo, String fileSeq,
                                               String downloadRequestKey, String requestType, String objectType,
                                               String originalFileName) {
        DownloadRuntimeState state = DownloadRuntimeState.createQueued(
            wsSeq, requestNo, docSeq, fileNo, fileSeq, downloadRequestKey, requestType, objectType, originalFileName, DEFAULT_TTL_MINUTES
        );
        stateMap.put(wsSeq, state);
        return state;
    }

    public DownloadRuntimeState get(String wsSeq) { return stateMap.get(wsSeq); }

    public boolean exists(String wsSeq) { return stateMap.containsKey(wsSeq); }

    public void update(String wsSeq, Consumer<DownloadRuntimeState> updater) {
        stateMap.computeIfPresent(wsSeq, (k, v) -> {
            updater.accept(v);
            v.extendTtl(DEFAULT_TTL_MINUTES); // 활동이 있으면 TTL 갱신
            return v;
        });
    }

    public DownloadRuntimeState remove(String wsSeq) { return stateMap.remove(wsSeq); }

    public int size() { return stateMap.size(); }

    public DownloadRuntimeState findLatestByFileNm(String fileNm) {
        if (fileNm == null || fileNm.trim().isEmpty()) {
            return null;
        }
        DownloadRuntimeState found = null;
        for (DownloadRuntimeState state : stateMap.values()) {
            if (state == null) {
                continue;
            }
            boolean matched = fileNm.equals(state.getSavedFileName()) || fileNm.equals(state.getOriginalFileName());
            if (!matched) {
                continue;
            }
            if (found == null || (state.getUpdatedAt() != null && found.getUpdatedAt() != null
                    && state.getUpdatedAt().isAfter(found.getUpdatedAt()))) {
                found = state;
            }
        }
        return found;
    }

    public DownloadRuntimeState findLatestByDownloadRequestKey(String downloadRequestKey) {
        if (downloadRequestKey == null || downloadRequestKey.trim().isEmpty()) {
            return null;
        }
        DownloadRuntimeState found = null;
        for (DownloadRuntimeState state : stateMap.values()) {
            if (state == null) {
                continue;
            }
            if (!downloadRequestKey.equals(state.getDownloadRequestKey())) {
                continue;
            }
            if (found == null || (state.getUpdatedAt() != null && found.getUpdatedAt() != null
                    && state.getUpdatedAt().isAfter(found.getUpdatedAt()))) {
                found = state;
            }
        }
        return found;
    }

    public int cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        List<String> expiredKeys = new ArrayList<>();

        stateMap.forEach((k, v) -> {
            if (v.isExpired(now)) { expiredKeys.add(k); }
        });

        for (String key : expiredKeys) { stateMap.remove(key); }
        return expiredKeys.size();
    }

    public List<String> findExpiredKeys() {
        LocalDateTime now = LocalDateTime.now();
        List<String> keys = new ArrayList<>();
        stateMap.forEach((k, v) -> {
            if (v.isExpired(now)) keys.add(k);
        });
        return keys;
    }    
}

package kr.esob.fdms.commonlogic.updown.runtime;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DownloadRuntimeCleanupScheduler {
    @Inject
    private DownloadRuntimeStore runtimeStore;

    // 1분마다 만료 정리
    @Scheduled(fixedDelay = 60000)
    public void cleanupExpired() {
        List<String> expiredKeys = runtimeStore.findExpiredKeys();
        for (String key : expiredKeys) {
            DownloadRuntimeState state = runtimeStore.get(key);
            if (state != null) { deleteIfExists(state.getTempFilePath()); }
            runtimeStore.remove(key);
        }
    }

    private void deleteIfExists(String path) {
        if (path == null || path.trim().isEmpty()) return;
        try {
            File f = new File(path);
            if (f.exists() && f.isFile()) f.delete();
        } catch (Exception ignore) {}
    }    
}

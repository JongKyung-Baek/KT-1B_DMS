package kr.esob.fdms.commonlogic.updown.runtime;
import java.time.LocalDateTime;

public class DownloadRuntimeState {
    private String wsSeq;              // 32자리 hex, map key
    private String requestNo;          // 화면 시리얼
    private String docSeq;
    private String fileNo;
    private String fileSeq;
    private String downloadRequestKey;
    private String requestType;
    private String objectType;
    private String originalFileName;
    private String savedFileName;
    private boolean actLogSaved;

    private String restSequence;       // DB 조회한 REST 요청용 시퀀스
    private String tempFilePath;       // 임시파일 경로

    private DownloadRuntimeStatus status;
    private String resultCode;         // 00,02,99...
    private String optionalData;       // 성공시 경로 등
    private String errorMessage;
    private LocalDateTime sentToWsAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expireAt;

    public static DownloadRuntimeState createQueued(String wsSeq, String requestNo, String docSeq, String fileNo, String fileSeq,
                                                    String downloadRequestKey, String requestType, String objectType,
                                                    String originalFileName, long ttlMinutes){
        LocalDateTime now = LocalDateTime.now();
        DownloadRuntimeState s = new DownloadRuntimeState();
        s.wsSeq = wsSeq;
        s.requestNo = requestNo;
        s.docSeq = docSeq;
        s.fileNo = fileNo;
        s.fileSeq = fileSeq;
        s.downloadRequestKey = downloadRequestKey;
        s.requestType = requestType;
        s.objectType = objectType;
        s.originalFileName = originalFileName;
        s.status = DownloadRuntimeStatus.QUEUED;
        s.createdAt = now;
        s.updatedAt = now;
        s.expireAt = now.plusMinutes(ttlMinutes);
        return s;
    }

    public void touch(){ this.updatedAt = LocalDateTime.now();}

    public void markDownloading(String restSequence, String tempFilePath, String savedFileName) {
        this.status = DownloadRuntimeStatus.DOWNLOADING;
        this.restSequence = restSequence;
        this.tempFilePath = tempFilePath;
        this.savedFileName = savedFileName;
        touch();
    }

    public void markSentToWs(){
        this.status = DownloadRuntimeStatus.SENT_TO_WS;
        this.sentToWsAt = LocalDateTime.now();
        touch();
    }

    public void markResult(String resultCode, String optionalData) {
        this.resultCode = resultCode;
        this.optionalData = optionalData;
        this.status = "00".equals(resultCode) ? DownloadRuntimeStatus.COMPLETED : DownloadRuntimeStatus.FAILED;
        touch();
    }

    public void markFailed(String errorMessage) {
        this.status = DownloadRuntimeStatus.FAILED;
        this.errorMessage = errorMessage;
        touch();
    }

    public boolean markFailedIfWsTimedOut(long timeoutSeconds) {
        if (this.status != DownloadRuntimeStatus.SENT_TO_WS) {
            return false;
        }
        if (this.sentToWsAt == null) {
            return false;
        }
        if (this.sentToWsAt.plusSeconds(timeoutSeconds).isAfter(LocalDateTime.now())) {
            return false;
        }
        markFailed("WebSocket response timeout");
        return true;
    }

    public void markActLogSaved() {
        this.actLogSaved = true;
        touch();
    }

    public boolean isExpired(LocalDateTime now) { return expireAt != null && expireAt.isBefore(now); }

    public void extendTtl(long ttlMinutes) {
        this.expireAt = LocalDateTime.now().plusMinutes(ttlMinutes);
        touch();
    }

    public String getWsSeq() { return wsSeq; }
    public String getRequestNo() { return requestNo; }
    public String getDocSeq() { return docSeq; }
    public String getFileNo() { return fileNo; }
    public String getFileSeq() { return fileSeq; }
    public String getDownloadRequestKey() { return downloadRequestKey; }
    public String getRequestType() { return requestType; }
    public String getObjectType() { return objectType; }
    public String getOriginalFileName() { return originalFileName; }
    public String getSavedFileName() { return savedFileName; }
    public boolean isActLogSaved() { return actLogSaved; }
    public String getRestSequence() { return restSequence; }
    public String getTempFilePath() { return tempFilePath; }
    public DownloadRuntimeStatus getStatus() { return status; }
    public String getResultCode() { return resultCode; }
    public String getOptionalData() { return optionalData; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getSentToWsAt() { return sentToWsAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getExpireAt() { return expireAt; }    
}

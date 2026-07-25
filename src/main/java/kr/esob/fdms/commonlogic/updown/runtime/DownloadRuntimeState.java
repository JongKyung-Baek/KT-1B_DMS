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
    private String ownerUserCd;
    private String ownerUserId;
    private String ownerUserNm;
    private String ownerSessionId;
    private boolean downloadClaimed;
    private LocalDateTime claimedAt;

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
                                                    String originalFileName, String ownerUserCd, String ownerUserId,
                                                    String ownerUserNm,
                                                    String ownerSessionId, long ttlMinutes){
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
        s.ownerUserCd = ownerUserCd;
        s.ownerUserId = ownerUserId;
        s.ownerUserNm = ownerUserNm;
        s.ownerSessionId = ownerSessionId;
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

    public synchronized boolean claimDownload() {
        if (downloadClaimed) return false;
        downloadClaimed = true;
        claimedAt = LocalDateTime.now();
        touch();
        return true;
    }

    public boolean isOwnedBy(String userCd, String sessionId) {
        return ownerUserCd != null && ownerUserCd.equals(userCd)
            && ownerSessionId != null && ownerSessionId.equals(sessionId);
    }

    public boolean isExpired(LocalDateTime now) {
        return expireAt != null && now != null && !expireAt.isAfter(now);
    }

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
    public String getOwnerUserCd() { return ownerUserCd; }
    public String getOwnerUserId() { return ownerUserId; }
    public String getOwnerUserNm() { return ownerUserNm; }
    public String getOwnerSessionId() { return ownerSessionId; }
    public boolean isDownloadClaimed() { return downloadClaimed; }
    public LocalDateTime getClaimedAt() { return claimedAt; }
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

    // MyBatis hydration and persistence-copy setters.
    public void setWsSeq(String wsSeq) { this.wsSeq = wsSeq; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public void setDocSeq(String docSeq) { this.docSeq = docSeq; }
    public void setFileNo(String fileNo) { this.fileNo = fileNo; }
    public void setFileSeq(String fileSeq) { this.fileSeq = fileSeq; }
    public void setDownloadRequestKey(String downloadRequestKey) { this.downloadRequestKey = downloadRequestKey; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public void setSavedFileName(String savedFileName) { this.savedFileName = savedFileName; }
    public void setActLogSaved(boolean actLogSaved) { this.actLogSaved = actLogSaved; }
    public void setOwnerUserCd(String ownerUserCd) { this.ownerUserCd = ownerUserCd; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public void setOwnerUserNm(String ownerUserNm) { this.ownerUserNm = ownerUserNm; }
    public void setOwnerSessionId(String ownerSessionId) { this.ownerSessionId = ownerSessionId; }
    public void setDownloadClaimed(boolean downloadClaimed) { this.downloadClaimed = downloadClaimed; }
    public void setClaimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; }
    public void setRestSequence(String restSequence) { this.restSequence = restSequence; }
    public void setTempFilePath(String tempFilePath) { this.tempFilePath = tempFilePath; }
    public void setStatus(DownloadRuntimeStatus status) { this.status = status; }
    public void setResultCode(String resultCode) { this.resultCode = resultCode; }
    public void setOptionalData(String optionalData) { this.optionalData = optionalData; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setSentToWsAt(LocalDateTime sentToWsAt) { this.sentToWsAt = sentToWsAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }

    public static DownloadRuntimeState copyOf(DownloadRuntimeState source) {
        if (source == null) {
            return null;
        }
        DownloadRuntimeState copy = new DownloadRuntimeState();
        copy.wsSeq = source.wsSeq;
        copy.requestNo = source.requestNo;
        copy.docSeq = source.docSeq;
        copy.fileNo = source.fileNo;
        copy.fileSeq = source.fileSeq;
        copy.downloadRequestKey = source.downloadRequestKey;
        copy.requestType = source.requestType;
        copy.objectType = source.objectType;
        copy.originalFileName = source.originalFileName;
        copy.savedFileName = source.savedFileName;
        copy.actLogSaved = source.actLogSaved;
        copy.ownerUserCd = source.ownerUserCd;
        copy.ownerUserId = source.ownerUserId;
        copy.ownerUserNm = source.ownerUserNm;
        copy.ownerSessionId = source.ownerSessionId;
        copy.downloadClaimed = source.downloadClaimed;
        copy.claimedAt = source.claimedAt;
        copy.restSequence = source.restSequence;
        copy.tempFilePath = source.tempFilePath;
        copy.status = source.status;
        copy.resultCode = source.resultCode;
        copy.optionalData = source.optionalData;
        copy.errorMessage = source.errorMessage;
        copy.sentToWsAt = source.sentToWsAt;
        copy.createdAt = source.createdAt;
        copy.updatedAt = source.updatedAt;
        copy.expireAt = source.expireAt;
        return copy;
    }
}

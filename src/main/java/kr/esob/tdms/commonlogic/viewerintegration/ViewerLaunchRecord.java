package kr.esob.tdms.commonlogic.viewerintegration;

public class ViewerLaunchRecord {
    private String correlationId;
    private String viewerProvider;
    private String objectType;
    private String objectId;
    private String aclObjectType;
    private String aclObjectId;
    private String fileNo;
    private String requestNo;
    private String actorUserCd;
    private String actorUserId;
    private String actorUserNm;
    private String distributionType;
    private String drawingNo;
    private String orgFileNm;
    private String revision;
    private String expiresAt;
    private String createdAt;

    static ViewerLaunchRecord from(ViewerDocumentMetadata metadata, String expiresAt) {
        return from(metadata, expiresAt, "PDF");
    }

    static ViewerLaunchRecord from(
            ViewerDocumentMetadata metadata, String expiresAt, String viewerProvider) {
        String normalizedProvider = viewerProvider == null
                ? "" : viewerProvider.trim().toUpperCase(java.util.Locale.ROOT);
        if (!"PDF".equals(normalizedProvider) && !"STEP".equals(normalizedProvider)) {
            throw new IllegalArgumentException("Viewer provider must be PDF or STEP.");
        }
        ViewerLaunchRecord record = new ViewerLaunchRecord();
        record.correlationId = metadata.getCorrelationId();
        record.viewerProvider = normalizedProvider;
        record.objectType = metadata.getObjectType();
        record.objectId = metadata.getObjectId();
        record.aclObjectType = metadata.getAclObjectType();
        record.aclObjectId = metadata.getAclObjectId();
        record.fileNo = metadata.getFileNo();
        record.requestNo = metadata.getRequestNo();
        record.actorUserCd = metadata.getUserCd();
        record.actorUserId = metadata.getUserId();
        record.actorUserNm = metadata.getUserName();
        record.distributionType = metadata.getDistributionType();
        record.drawingNo = metadata.getDrawingNo();
        record.orgFileNm = metadata.getFileName();
        record.revision = metadata.getRevision();
        record.expiresAt = expiresAt;
        return record;
    }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getViewerProvider() { return viewerProvider; }
    public void setViewerProvider(String viewerProvider) { this.viewerProvider = viewerProvider; }
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }
    public String getAclObjectType() { return aclObjectType; }
    public void setAclObjectType(String aclObjectType) { this.aclObjectType = aclObjectType; }
    public String getAclObjectId() { return aclObjectId; }
    public void setAclObjectId(String aclObjectId) { this.aclObjectId = aclObjectId; }
    public String getFileNo() { return fileNo; }
    public void setFileNo(String fileNo) { this.fileNo = fileNo; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public String getActorUserCd() { return actorUserCd; }
    public void setActorUserCd(String actorUserCd) { this.actorUserCd = actorUserCd; }
    public String getActorUserId() { return actorUserId; }
    public void setActorUserId(String actorUserId) { this.actorUserId = actorUserId; }
    public String getActorUserNm() { return actorUserNm; }
    public void setActorUserNm(String actorUserNm) { this.actorUserNm = actorUserNm; }
    public String getDistributionType() { return distributionType; }
    public void setDistributionType(String distributionType) { this.distributionType = distributionType; }
    public String getDrawingNo() { return drawingNo; }
    public void setDrawingNo(String drawingNo) { this.drawingNo = drawingNo; }
    public String getOrgFileNm() { return orgFileNm; }
    public void setOrgFileNm(String orgFileNm) { this.orgFileNm = orgFileNm; }
    public String getRevision() { return revision; }
    public void setRevision(String revision) { this.revision = revision; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

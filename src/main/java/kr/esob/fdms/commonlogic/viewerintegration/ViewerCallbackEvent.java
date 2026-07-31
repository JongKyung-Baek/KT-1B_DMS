package kr.esob.fdms.commonlogic.viewerintegration;

public class ViewerCallbackEvent {
    private String eventId;
    private String correlationId;
    private String eventType;
    private String occurredAt;
    private String objectId;
    private String fileNo;
    private String userId;
    private String contentSha256;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getOccurredAt() { return occurredAt; }
    public void setOccurredAt(String occurredAt) { this.occurredAt = occurredAt; }
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }
    public String getFileNo() { return fileNo; }
    public void setFileNo(String fileNo) { this.fileNo = fileNo; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getContentSha256() { return contentSha256; }
    public void setContentSha256(String contentSha256) { this.contentSha256 = contentSha256; }
}

package kr.esob.fdms.commonlogic.viewerintegration;

public class ViewerIngestResponse {
    private String launchToken;
    private String expiresAt;
    private String correlationId;

    public String getLaunchToken() { return launchToken; }
    public void setLaunchToken(String launchToken) { this.launchToken = launchToken; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}

package kr.esob.fdms.commonlogic.viewerintegration;

import java.net.URI;

public class ViewerPreparedLaunch {
    private final URI launchUri;
    private final String launchToken;
    private final String correlationId;

    public ViewerPreparedLaunch(URI launchUri, String launchToken, String correlationId) {
        this.launchUri = launchUri;
        this.launchToken = launchToken;
        this.correlationId = correlationId;
    }

    public URI getLaunchUri() { return launchUri; }
    public String getLaunchToken() { return launchToken; }
    public String getCorrelationId() { return correlationId; }
}

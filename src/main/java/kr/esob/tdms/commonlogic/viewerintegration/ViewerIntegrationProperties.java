package kr.esob.tdms.commonlogic.viewerintegration;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tdms.viewer")
public class ViewerIntegrationProperties {
    public static final String INGEST_PATH = "/api/integrations/tdms/v1/documents";
    public static final String LAUNCH_PATH = "/api/integrations/tdms/v1/launch";
    public static final String CALLBACK_PATH = "/api/integrations/cv/v1/events";

    private boolean enabled;
    private String baseUrl;
    private String clientId;
    private String callbackClientId;
    private String sharedSecret;
    private String workDir;
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 30000;
    private long signatureClockSkewSeconds = 300;
    private int stateRetentionDays = 30;

    public URI ingestUri() {
        return endpoint(INGEST_PATH);
    }

    public URI launchUri() {
        return endpoint(LAUNCH_PATH);
    }

    public String effectiveCallbackClientId() {
        return hasText(callbackClientId) ? callbackClientId.trim() : trim(clientId);
    }

    public Path workDirectory() {
        requireText(workDir, "TDMS_VIEWER_WORK_DIR");
        Path path = Path.of(workDir.trim()).normalize();
        if (!path.isAbsolute()) {
            throw new IllegalStateException("TDMS_VIEWER_WORK_DIR must be an absolute path.");
        }
        return path;
    }

    public void requireOutboundConfiguration() {
        if (!enabled) {
            throw new IllegalStateException("Viewer integration is disabled.");
        }
        requireText(baseUrl, "TDMS_VIEWER_BASE_URL");
        requireText(clientId, "TDMS_VIEWER_CLIENT_ID");
        requireClientId(clientId, "TDMS_VIEWER_CLIENT_ID");
        requireText(sharedSecret, "TDMS_VIEWER_SHARED_SECRET");
        requireStrongSharedSecret();
        requirePositiveTimeouts();
        requireStateRetentionDays();
        endpoint(INGEST_PATH);
    }

    public void requireCallbackConfiguration() {
        if (!enabled) {
            throw new IllegalStateException("Viewer integration is disabled.");
        }
        requireText(callbackClientId, "TDMS_VIEWER_CALLBACK_CLIENT_ID");
        requireClientId(callbackClientId, "TDMS_VIEWER_CALLBACK_CLIENT_ID");
        requireText(sharedSecret, "TDMS_VIEWER_SHARED_SECRET");
        requireStrongSharedSecret();
        requireStateRetentionDays();
        if (signatureClockSkewSeconds < 1 || signatureClockSkewSeconds > 3600) {
            throw new IllegalStateException(
                    "TDMS_VIEWER_SIGNATURE_CLOCK_SKEW_SECONDS must be between 1 and 3600.");
        }
    }

    private URI endpoint(String path) {
        requireText(baseUrl, "TDMS_VIEWER_BASE_URL");
        URI base = URI.create(baseUrl.trim());
        String host = base.getHost();
        boolean loopbackHttp = "http".equalsIgnoreCase(base.getScheme())
                && ("localhost".equalsIgnoreCase(host)
                    || "127.0.0.1".equals(host)
                    || "::1".equals(host));
        String basePath = base.getPath();
        if (host == null || base.getUserInfo() != null || base.getQuery() != null
                || base.getFragment() != null
                || (basePath != null && !basePath.isEmpty() && !"/".equals(basePath))
                || (!"https".equalsIgnoreCase(base.getScheme()) && !loopbackHttp)) {
            throw new IllegalStateException("Viewer base URL must be an HTTPS origin.");
        }
        String normalized = base.toString();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return URI.create(normalized + path);
    }

    private void requireText(String value, String environmentName) {
        if (!hasText(value)) {
            throw new IllegalStateException(environmentName + " is required.");
        }
    }

    private void requirePositiveTimeouts() {
        if (connectTimeoutMs < 1 || readTimeoutMs < 1) {
            throw new IllegalStateException("Viewer timeouts must be positive.");
        }
    }

    private void requireStrongSharedSecret() {
        if (sharedSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("TDMS_VIEWER_SHARED_SECRET must be at least 32 UTF-8 bytes.");
        }
    }

    private void requireStateRetentionDays() {
        if (stateRetentionDays < 1 || stateRetentionDays > 3650) {
            throw new IllegalStateException(
                    "TDMS_VIEWER_STATE_RETENTION_DAYS must be between 1 and 3650.");
        }
    }

    private void requireClientId(String value, String environmentName) {
        if (!value.trim().matches("[A-Za-z0-9._:-]{1,100}")) {
            throw new IllegalStateException(environmentName + " has an invalid format.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getCallbackClientId() { return callbackClientId; }
    public void setCallbackClientId(String callbackClientId) { this.callbackClientId = callbackClientId; }
    public String getSharedSecret() { return sharedSecret; }
    public void setSharedSecret(String sharedSecret) { this.sharedSecret = sharedSecret; }
    public String getWorkDir() { return workDir; }
    public void setWorkDir(String workDir) { this.workDir = workDir; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    public long getSignatureClockSkewSeconds() { return signatureClockSkewSeconds; }
    public void setSignatureClockSkewSeconds(long value) { this.signatureClockSkewSeconds = value; }
    public int getStateRetentionDays() { return stateRetentionDays; }
    public void setStateRetentionDays(int stateRetentionDays) {
        this.stateRetentionDays = stateRetentionDays;
    }
}

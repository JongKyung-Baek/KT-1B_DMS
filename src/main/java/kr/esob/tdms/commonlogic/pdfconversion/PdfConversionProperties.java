package kr.esob.tdms.commonlogic.pdfconversion;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tdms.pdf-conversion")
public class PdfConversionProperties {
    public static final String CONVERT_PATH = "/api/integrations/tdms/v1/convert";

    private boolean enabled;
    private String baseUrl = "";
    private String clientId = "";
    private String sharedSecret = "";
    private String workDir = System.getProperty("java.io.tmpdir") + "/kt1b-pdf-conversion";
    private String outputFolder = "CONVERTED_PDF";
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 180000;
    private int pollIntervalMs = 3000;
    private int batchSize = 4;
    private int workerThreads = 2;
    private int maxAttempts = 3;
    private int retryDelaySeconds = 30;
    private int staleProcessingMinutes = 30;

    public void requireOutboundConfiguration() {
        if (!enabled) {
            throw new IllegalStateException("PDF conversion integration is disabled.");
        }
        if (isBlank(baseUrl) || isBlank(clientId) || isBlank(sharedSecret)) {
            throw new IllegalStateException("PDF conversion integration configuration is incomplete.");
        }
        if (!clientId.trim().matches("[A-Za-z0-9._:-]{1,128}")) {
            throw new IllegalStateException("PDF conversion client ID has an invalid format.");
        }
        if (sharedSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "PDF conversion shared secret must be at least 32 UTF-8 bytes.");
        }
        if (connectTimeoutMs < 1 || readTimeoutMs < 1) {
            throw new IllegalStateException("PDF conversion timeouts must be positive.");
        }
        convertUri();
    }

    public URI convertUri() {
        if (isBlank(baseUrl)) {
            throw new IllegalStateException("PDF conversion base URL is required.");
        }
        String candidate = baseUrl.trim();
        while (candidate.endsWith("/") && !candidate.endsWith("://")) {
            candidate = candidate.substring(0, candidate.length() - 1);
        }
        URI base;
        try {
            base = URI.create(candidate);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "PDF conversion base URL must be a valid HTTPS origin.", exception);
        }
        String host = base.getHost();
        boolean loopbackHttp = "http".equalsIgnoreCase(base.getScheme())
                && ("localhost".equalsIgnoreCase(host)
                    || "127.0.0.1".equals(host)
                    || "::1".equals(host));
        String path = base.getPath();
        if (host == null || base.getUserInfo() != null || base.getQuery() != null
                || base.getFragment() != null
                || (path != null && !path.isEmpty() && !path.matches("/+"))
                || (!"https".equalsIgnoreCase(base.getScheme()) && !loopbackHttp)) {
            throw new IllegalStateException(
                    "PDF conversion base URL must be an HTTPS origin.");
        }
        String normalized = base.toString();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return URI.create(normalized + CONVERT_PATH);
    }

    public Path workPath() {
        return Paths.get(workDir).toAbsolutePath().normalize();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getSharedSecret() { return sharedSecret; }
    public void setSharedSecret(String sharedSecret) { this.sharedSecret = sharedSecret; }
    public String getWorkDir() { return workDir; }
    public void setWorkDir(String workDir) { this.workDir = workDir; }
    public String getOutputFolder() { return outputFolder; }
    public void setOutputFolder(String outputFolder) { this.outputFolder = outputFolder; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    public int getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(int pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    public int getWorkerThreads() { return workerThreads; }
    public void setWorkerThreads(int workerThreads) { this.workerThreads = workerThreads; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public int getRetryDelaySeconds() { return retryDelaySeconds; }
    public void setRetryDelaySeconds(int retryDelaySeconds) { this.retryDelaySeconds = retryDelaySeconds; }
    public int getStaleProcessingMinutes() { return staleProcessingMinutes; }
    public void setStaleProcessingMinutes(int staleProcessingMinutes) { this.staleProcessingMinutes = staleProcessingMinutes; }
}

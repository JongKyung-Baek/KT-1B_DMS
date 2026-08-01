package kr.esob.tdms.controller.general.distribution.accountrequest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tdms.distribution.integration")
public class DistributionAccountIntegrationProperties {
    public static final String REQUEST_PATH =
        "/api/integrations/distribution/v1/account-requests";

    private boolean enabled;
    private String clientId;
    private String sourceSystemId;
    private String sharedSecret;
    /** Semicolon-separated clientId|sourceSystemId|secret registrations. */
    private String additionalClients;
    private long signatureClockSkewSeconds = 300;
    private int nonceRetentionDays = 2;

    RegisteredClient requireClient(String suppliedClientId) {
        if (!enabled) {
            throw DistributionAccountRequestException.unavailable(
                "Distribution-system integration is disabled.");
        }
        for (RegisteredClient registration : registrations()) {
            if (registration.clientId.equals(suppliedClientId)) {
                return registration;
            }
        }
        throw DistributionAccountRequestException.unauthorized(
            "Distribution integration client is invalid.");
    }

    private List<RegisteredClient> registrations() {
        List<RegisteredClient> result = new ArrayList<RegisteredClient>();
        if (hasText(clientId) || hasText(sourceSystemId) || hasText(sharedSecret)) {
            result.add(validated(clientId, sourceSystemId, sharedSecret));
        }
        if (hasText(additionalClients)) {
            String[] entries = additionalClients.split(";", -1);
            for (String entry : entries) {
                if (!entry.trim().isEmpty()) {
                    String[] values = entry.split("\\|", -1);
                    if (values.length != 3) {
                        throw DistributionAccountRequestException.unavailable(
                            "A distribution integration client registration is invalid.");
                    }
                    result.add(validated(values[0], values[1], values[2]));
                }
            }
        }
        if (result.isEmpty()) {
            throw DistributionAccountRequestException.unavailable(
                "No distribution integration client is registered.");
        }
        return result;
    }

    private RegisteredClient validated(String rawClientId, String rawSourceSystemId, String rawSecret) {
        String normalizedClientId = trim(rawClientId);
        String normalizedSourceSystemId = trim(rawSourceSystemId);
        String normalizedSecret = trim(rawSecret);
        if (!normalizedClientId.matches("[A-Za-z0-9._:-]{1,100}")
                || !normalizedSourceSystemId.matches("[A-Za-z0-9._:-]{1,100}")
                || normalizedSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw DistributionAccountRequestException.unavailable(
                "A distribution integration client registration is invalid.");
        }
        return new RegisteredClient(normalizedClientId, normalizedSourceSystemId, normalizedSecret);
    }

    void validateLimits() {
        if (signatureClockSkewSeconds < 1 || signatureClockSkewSeconds > 3600) {
            throw DistributionAccountRequestException.unavailable(
                "Distribution integration signature clock skew is invalid.");
        }
        if (nonceRetentionDays < 1 || nonceRetentionDays > 30) {
            throw DistributionAccountRequestException.unavailable(
                "Distribution integration nonce retention is invalid.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    static final class RegisteredClient {
        private final String clientId;
        private final String sourceSystemId;
        private final String sharedSecret;

        RegisteredClient(String clientId, String sourceSystemId, String sharedSecret) {
            this.clientId = clientId;
            this.sourceSystemId = sourceSystemId;
            this.sharedSecret = sharedSecret;
        }

        String getClientId() { return clientId; }
        String getSourceSystemId() { return sourceSystemId; }
        String getSharedSecret() { return sharedSecret; }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getSourceSystemId() { return sourceSystemId; }
    public void setSourceSystemId(String sourceSystemId) { this.sourceSystemId = sourceSystemId; }
    public String getSharedSecret() { return sharedSecret; }
    public void setSharedSecret(String sharedSecret) { this.sharedSecret = sharedSecret; }
    public String getAdditionalClients() { return additionalClients; }
    public void setAdditionalClients(String additionalClients) { this.additionalClients = additionalClients; }
    public long getSignatureClockSkewSeconds() { return signatureClockSkewSeconds; }
    public void setSignatureClockSkewSeconds(long value) { this.signatureClockSkewSeconds = value; }
    public int getNonceRetentionDays() { return nonceRetentionDays; }
    public void setNonceRetentionDays(int nonceRetentionDays) { this.nonceRetentionDays = nonceRetentionDays; }
}

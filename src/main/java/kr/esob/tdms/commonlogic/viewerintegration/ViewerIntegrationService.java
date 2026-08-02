package kr.esob.tdms.commonlogic.viewerintegration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ViewerIntegrationService {
    private static final int MAX_CALLBACK_BYTES = 16 * 1024;
    private static final String VIEW_OPENED = "VIEW_OPENED";
    private static final Set<String> CALLBACK_FIELDS = new HashSet<String>(Arrays.asList(
            "eventId", "correlationId", "eventType", "occurredAt",
            "objectId", "fileNo", "userId"));

    private final ViewerIntegrationProperties properties;
    private final StepViewerIntegrationProperties stepProperties;
    private final ViewerIntegrationClient client;
    private final ViewerIntegrationDao dao;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public ViewerIntegrationService(
                                    @Qualifier("viewerIntegrationProperties")
                                    ViewerIntegrationProperties properties,
                                    StepViewerIntegrationProperties stepProperties,
                                    ViewerIntegrationClient client,
                                    ViewerIntegrationDao dao,
                                    ObjectMapper objectMapper) {
        this(properties, stepProperties, client, dao, objectMapper, Clock.systemUTC());
    }

    ViewerIntegrationService(ViewerIntegrationProperties properties,
                             ViewerIntegrationClient client,
                             ViewerIntegrationDao dao,
                             ObjectMapper objectMapper,
                             Clock clock) {
        this(properties, new StepViewerIntegrationProperties(), client, dao, objectMapper, clock);
    }

    ViewerIntegrationService(ViewerIntegrationProperties properties,
                             StepViewerIntegrationProperties stepProperties,
                             ViewerIntegrationClient client,
                             ViewerIntegrationDao dao,
                             ObjectMapper objectMapper,
                             Clock clock) {
        this.properties = properties;
        this.stepProperties = stepProperties;
        this.client = client;
        this.dao = dao;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public ViewerPreparedLaunch prepareLaunch(Path pdf, ViewerDocumentMetadata metadata) {
        return prepareLaunch(pdf, metadata, ViewerProvider.PDF);
    }

    public ViewerPreparedLaunch prepareLaunch(Path document,
                                              ViewerDocumentMetadata metadata,
                                              ViewerProvider provider) {
        AbstractViewerIntegrationProperties selectedProperties = propertiesFor(provider);
        requireOutboundConfiguration(selectedProperties);
        normalizeMetadata(metadata);
        dao.deleteExpiredState(selectedProperties.getStateRetentionDays());
        ViewerIngestResponse response = provider == ViewerProvider.STEP
                ? client.ingest(document, metadata, provider)
                : client.ingest(document, metadata);
        if (dao.insertLaunch(ViewerLaunchRecord.from(
                metadata, response.getExpiresAt(), provider.getCode())) != 1) {
            throw new IllegalStateException("Viewer launch correlation could not be persisted.");
        }
        return new ViewerPreparedLaunch(
                selectedProperties.launchUri(), response.getLaunchToken(), response.getCorrelationId());
    }

    public Path createRequestPdf(String correlationId) {
        return createRequestDocument(correlationId, ViewerProvider.PDF);
    }

    public Path createRequestDocument(String correlationId, ViewerProvider provider) {
        AbstractViewerIntegrationProperties selectedProperties = propertiesFor(provider);
        requireOutboundConfiguration(selectedProperties);
        requireCanonicalUuid(correlationId, "Viewer correlation ID", false);
        Path workDirectory = selectedProperties.workDirectory();
        try {
            Files.createDirectories(workDirectory);
            if (!Files.isDirectory(workDirectory)) {
                throw new IllegalStateException("Viewer work directory is unavailable.");
            }
            return Files.createTempFile(
                    workDirectory, correlationId + "-", provider.getTemporarySuffix());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create a viewer request file.", exception);
        }
    }

    @Transactional
    public void acceptCallback(byte[] body,
                               String clientId,
                               String timestamp,
                               String nonce,
                               String contentHash,
                               String signature) {
        CallbackCredential credential = authenticate(
                body, clientId, timestamp, nonce, contentHash, signature);
        ViewerCallbackEvent event = parseEvent(body, credential.clockSkewSeconds);

        ViewerLaunchRecord launch = dao.selectLaunch(event.getCorrelationId());
        if (launch == null) {
            throw new ViewerCallbackIdentityException("Unknown viewer correlation identifier.");
        }
        requireProvider(credential, launch);
        requireIdentity(event, launch);
        requireOccurrenceNotBeforeLaunch(event, launch, credential.clockSkewSeconds);

        dao.deleteOldNonces();
        if (dao.insertNonce(clientId, nonce) != 1) {
            throw new ViewerCallbackAuthenticationException("Viewer callback nonce was already used.");
        }

        event.setContentSha256(contentHash);
        if (dao.insertEvent(event) == 0) {
            return;
        }
        requireSingleRow(dao.insertViewHistory(launch, event), "viewer history");
        requireSingleRow(dao.markViewed(event), "viewer launch status");
    }

    private CallbackCredential authenticate(byte[] body,
                                            String clientId,
                                            String timestamp,
                                            String nonce,
                                            String contentHash,
                                            String signature) {
        if (body == null || body.length == 0 || body.length > MAX_CALLBACK_BYTES) {
            throw new ViewerCallbackValidationException("Viewer callback body size is invalid.");
        }
        CallbackCredential credential = resolveCallbackCredential(clientId);
        long epochSeconds = parseEpochSeconds(timestamp);
        long now = Instant.now(clock).getEpochSecond();
        long skew = credential.clockSkewSeconds;
        if (epochSeconds < now - skew || epochSeconds > now + skew) {
            throw new ViewerCallbackAuthenticationException("Viewer callback timestamp is outside the allowed window.");
        }
        requireCanonicalUuid(nonce, "Viewer callback nonce", true);
        if (!ViewerCrypto.isLowerHexSha256(contentHash)
                || !ViewerCrypto.constantTimeEquals(ViewerCrypto.sha256(body), contentHash)) {
            throw new ViewerCallbackAuthenticationException("Viewer callback body hash is invalid.");
        }
        if (!ViewerCrypto.isLowerHexSha256(signature)) {
            throw new ViewerCallbackAuthenticationException("Viewer callback signature format is invalid.");
        }
        String canonical = "POST\n" + ViewerIntegrationProperties.CALLBACK_PATH + "\n"
                + clientId + "\n" + timestamp + "\n" + nonce + "\n" + contentHash;
        String expected = ViewerCrypto.hmacSha256(credential.sharedSecret, canonical);
        if (!ViewerCrypto.constantTimeEquals(expected, signature)) {
            throw new ViewerCallbackAuthenticationException("Viewer callback signature is invalid.");
        }
        return credential;
    }

    private ViewerCallbackEvent parseEvent(byte[] body, long clockSkewSeconds) {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root == null || !root.isObject() || root.size() != CALLBACK_FIELDS.size()) {
                throw new ViewerCallbackValidationException("Viewer callback JSON shape is invalid.");
            }
            Iterator<String> names = root.fieldNames();
            while (names.hasNext()) {
                if (!CALLBACK_FIELDS.contains(names.next())) {
                    throw new ViewerCallbackValidationException("Viewer callback contains an unsupported field.");
                }
            }

            ViewerCallbackEvent event = new ViewerCallbackEvent();
            event.setEventId(requiredText(root, "eventId", 36));
            event.setCorrelationId(requiredText(root, "correlationId", 64));
            event.setEventType(requiredText(root, "eventType", 40));
            event.setOccurredAt(requiredText(root, "occurredAt", 64));
            event.setObjectId(requiredText(root, "objectId", 60));
            event.setFileNo(requiredText(root, "fileNo", 60));
            event.setUserId(requiredText(root, "userId", 100));

            requireCanonicalUuid(event.getEventId(), "Viewer callback event ID", false);
            if (!VIEW_OPENED.equals(event.getEventType())) {
                throw new ViewerCallbackValidationException("Unsupported viewer callback event type.");
            }
            Instant occurredAt;
            try {
                occurredAt = Instant.parse(event.getOccurredAt());
            } catch (RuntimeException exception) {
                throw new ViewerCallbackValidationException("Viewer callback occurrence timestamp is invalid.", exception);
            }
            if (occurredAt.isAfter(Instant.now(clock).plusSeconds(clockSkewSeconds))) {
                throw new ViewerCallbackValidationException("Viewer callback occurrence timestamp is in the future.");
            }
            return event;
        } catch (ViewerCallbackValidationException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ViewerCallbackValidationException("Viewer callback JSON is invalid.", exception);
        }
    }

    private void requireIdentity(ViewerCallbackEvent event, ViewerLaunchRecord launch) {
        if (!event.getObjectId().equals(launch.getObjectId())
                || !event.getFileNo().equals(launch.getFileNo())
                || !event.getUserId().equals(launch.getActorUserId())) {
            throw new ViewerCallbackIdentityException("Viewer callback identity does not match its launch.");
        }
    }

    private void requireOccurrenceNotBeforeLaunch(
            ViewerCallbackEvent event, ViewerLaunchRecord launch, long clockSkewSeconds) {
        final Instant occurredAt;
        final Instant createdAt;
        try {
            occurredAt = Instant.parse(event.getOccurredAt());
            createdAt = Instant.parse(launch.getCreatedAt());
        } catch (RuntimeException exception) {
            throw new ViewerCallbackValidationException(
                    "Viewer launch or callback occurrence timestamp is invalid.", exception);
        }
        if (occurredAt.isBefore(createdAt.minusSeconds(
                clockSkewSeconds))) {
            throw new ViewerCallbackIdentityException(
                    "Viewer callback occurrence predates its launch.");
        }
    }

    private void normalizeMetadata(ViewerDocumentMetadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("Viewer metadata is required.");
        }
        if (metadata.getCorrelationId() == null || metadata.getCorrelationId().trim().isEmpty()) {
            metadata.setCorrelationId(UUID.randomUUID().toString());
        } else {
            requireCanonicalUuid(metadata.getCorrelationId(), "Viewer correlation ID", false);
            metadata.setCorrelationId(metadata.getCorrelationId().toLowerCase());
        }
        metadata.setObjectType(required(metadata.getObjectType(), "objectType", 30));
        metadata.setObjectId(required(metadata.getObjectId(), "objectId", 60));
        metadata.setAclObjectType(required(metadata.getAclObjectType(), "aclObjectType", 30));
        metadata.setAclObjectId(required(metadata.getAclObjectId(), "aclObjectId", 60));
        metadata.setFileNo(defaultText(metadata.getFileNo(), "*", 60));
        metadata.setFileName(required(metadata.getFileName(), "fileName", 500));
        metadata.setUserCd(required(metadata.getUserCd(), "userCd", 20));
        metadata.setUserId(required(metadata.getUserId(), "userId", 100));
        metadata.setUserName(required(metadata.getUserName(), "userName", 256));
        metadata.setAuthority(required(metadata.getAuthority(), "authority", 30));
        metadata.setRevision(defaultText(metadata.getRevision(), "", 100));
        metadata.setRequestNo(defaultText(metadata.getRequestNo(), "", 100));
        metadata.setDistributionType(defaultText(metadata.getDistributionType(), "", 100));
        metadata.setDrawingNo(defaultText(metadata.getDrawingNo(), "", 200));
    }

    private String requiredText(JsonNode root, String field, int maxLength) {
        JsonNode value = root.get(field);
        if (value == null || !value.isTextual()) {
            throw new ViewerCallbackValidationException("Viewer callback field is required: " + field);
        }
        String text = value.textValue();
        if (text == null || text.trim().isEmpty() || text.length() > maxLength) {
            throw new ViewerCallbackValidationException("Viewer callback field is invalid: " + field);
        }
        return text;
    }

    private String required(String value, String field, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > maxLength) {
            throw new IllegalArgumentException("Viewer metadata field is invalid: " + field);
        }
        return normalized;
    }

    private String defaultText(String value, String fallback, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            normalized = fallback;
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("Viewer metadata field is too long.");
        }
        return normalized;
    }

    private long parseEpochSeconds(String timestamp) {
        if (timestamp == null || !timestamp.matches("[0-9]{1,19}")) {
            throw new ViewerCallbackAuthenticationException("Viewer callback timestamp is invalid.");
        }
        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException exception) {
            throw new ViewerCallbackAuthenticationException("Viewer callback timestamp is invalid.");
        }
    }

    private void requireCanonicalUuid(String value, String label, boolean authenticationFailure) {
        try {
            UUID parsed = UUID.fromString(value);
            if (!parsed.toString().equalsIgnoreCase(value)) {
                throw new IllegalArgumentException("Non-canonical UUID");
            }
        } catch (RuntimeException exception) {
            if (authenticationFailure) {
                throw new ViewerCallbackAuthenticationException(label + " is invalid.");
            }
            throw new ViewerCallbackValidationException(label + " is invalid.", exception);
        }
    }

    private AbstractViewerIntegrationProperties propertiesFor(ViewerProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Viewer provider is required.");
        }
        return provider == ViewerProvider.STEP ? stepProperties : properties;
    }

    private void requireOutboundConfiguration(
            AbstractViewerIntegrationProperties selectedProperties) {
        try {
            selectedProperties.requireOutboundConfiguration();
        } catch (IllegalStateException exception) {
            throw new ViewerIntegrationUnavailableException("Viewer integration is not configured.", exception);
        }
    }

    private CallbackCredential resolveCallbackCredential(String clientId) {
        CallbackCredential resolved = null;
        resolved = matchingCredential(resolved, clientId, properties, ViewerProvider.PDF);
        resolved = matchingCredential(resolved, clientId, stepProperties, ViewerProvider.STEP);
        if (resolved != null) {
            return resolved;
        }
        if (!properties.isEnabled() && !stepProperties.isEnabled()) {
            throw new ViewerIntegrationUnavailableException(
                    "Viewer callback is not configured.",
                    new IllegalStateException("All viewer integrations are disabled."));
        }
        throw new ViewerCallbackAuthenticationException("Viewer callback client is invalid.");
    }

    private CallbackCredential matchingCredential(
            CallbackCredential current,
            String suppliedClientId,
            AbstractViewerIntegrationProperties candidate,
            ViewerProvider provider) {
        if (!candidate.isEnabled()
                || suppliedClientId == null
                || !suppliedClientId.equals(candidate.effectiveCallbackClientId())) {
            return current;
        }
        try {
            candidate.requireCallbackConfiguration();
        } catch (IllegalStateException exception) {
            throw new ViewerIntegrationUnavailableException(
                    "Viewer callback is not configured.", exception);
        }
        if (current != null) {
            throw new ViewerIntegrationUnavailableException(
                    "Viewer callback client configuration is ambiguous.",
                    new IllegalStateException("Duplicate viewer callback client ID."));
        }
        return new CallbackCredential(
                provider,
                candidate.getSharedSecret(),
                candidate.getSignatureClockSkewSeconds());
    }

    private void requireProvider(CallbackCredential credential, ViewerLaunchRecord launch) {
        String launchProvider = launch.getViewerProvider();
        if (launchProvider == null || launchProvider.trim().isEmpty()) {
            launchProvider = ViewerProvider.PDF.getCode();
        }
        if (!credential.provider.getCode().equalsIgnoreCase(launchProvider.trim())) {
            throw new ViewerCallbackIdentityException(
                    "Viewer callback provider does not match its launch.");
        }
    }

    private void requireSingleRow(int affectedRows, String operation) {
        if (affectedRows != 1) {
            throw new IllegalStateException("Unable to persist " + operation + ".");
        }
    }

    private static final class CallbackCredential {
        private final ViewerProvider provider;
        private final String sharedSecret;
        private final long clockSkewSeconds;

        private CallbackCredential(ViewerProvider provider,
                                   String sharedSecret,
                                   long clockSkewSeconds) {
            this.provider = provider;
            this.sharedSecret = sharedSecret;
            this.clockSkewSeconds = clockSkewSeconds;
        }
    }
}

package kr.esob.tdms.controller.general.distribution.accountrequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.tdms.controller.general.distribution.accountrequest.DistributionAccountIntegrationProperties.RegisteredClient;

@Service
public class DistributionAccountIntegrationService {
    private static final int MAX_BODY_BYTES = 32 * 1024;
    private static final int MAX_METADATA_BYTES = 4 * 1024;
    private static final byte[] EMPTY_BODY = new byte[0];
    private static final Set<String> ROOT_FIELDS = fields(
        "eventId", "correlationId", "sourceSystemId", "requestType", "occurredAt",
        "representative", "organization", "targetUser", "reason", "metadata");
    private static final Set<String> REPRESENTATIVE_FIELDS = fields("id", "name", "email", "phone");
    private static final Set<String> ORGANIZATION_FIELDS = fields("code", "name", "businessNumber");
    private static final Set<String> TARGET_USER_FIELDS = fields("id", "name", "email", "phone", "position");
    private static final Set<String> SENSITIVE_METADATA_KEY_PARTS = fields(
        "password", "passwd", "pwd", "secret", "token", "credential",
        "apikey", "privatekey");

    private final DistributionAccountIntegrationProperties properties;
    private final DistributionAccountRequestDao dao;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public DistributionAccountIntegrationService(
            DistributionAccountIntegrationProperties properties,
            DistributionAccountRequestDao dao,
            ObjectMapper objectMapper) {
        this(properties, dao, objectMapper, Clock.systemUTC());
    }

    DistributionAccountIntegrationService(
            DistributionAccountIntegrationProperties properties,
            DistributionAccountRequestDao dao,
            ObjectMapper objectMapper,
            Clock clock) {
        this.properties = properties;
        this.dao = dao;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional(rollbackFor = Exception.class)
    public DistributionAccountRequestRecord receive(byte[] body,
            String clientId, String timestamp, String nonce,
            String contentHash, String signature) {
        if (body == null || body.length == 0 || body.length > MAX_BODY_BYTES) {
            throw body != null && body.length > MAX_BODY_BYTES
                ? DistributionAccountRequestException.payloadTooLarge()
                : DistributionAccountRequestException.badRequest(
                    "INVALID_DISTRIBUTION_ACCOUNT_REQUEST", "Request body is required.");
        }
        RegisteredClient client = authenticate("POST",
            DistributionAccountIntegrationProperties.REQUEST_PATH, body,
            clientId, timestamp, nonce, contentHash, signature);
        DistributionAccountRequestRecord request = parse(body, client, contentHash);
        consumeNonce(client.getClientId(), nonce);

        Long requestId = dao.insertRequest(request);
        if (requestId == null) {
            DistributionAccountRequestRecord duplicate = resolveDuplicate(request);
            redactInternalDecisionIdentity(duplicate);
            return duplicate;
        }
        request.setRequestId(requestId);
        requireOne(dao.insertReceivedEvent(request), "record external account request receipt");
        DistributionAccountRequestRecord stored = requireStored(requestId.longValue());
        stored.setDuplicate(Boolean.FALSE);
        redactInternalDecisionIdentity(stored);
        return stored;
    }

    @Transactional(rollbackFor = Exception.class)
    public DistributionAccountRequestRecord status(String eventId,
            String clientId, String timestamp, String nonce,
            String contentHash, String signature) {
        String suppliedEventId = trim(eventId);
        requireCanonicalUuid(suppliedEventId, "eventId", false);
        // The path is signed byte-for-byte as requested. UUID lookup itself is
        // case-insensitive after PostgreSQL casts it to uuid.
        String path = DistributionAccountIntegrationProperties.REQUEST_PATH + "/" + suppliedEventId;
        RegisteredClient client = authenticate("GET", path, EMPTY_BODY,
            clientId, timestamp, nonce, contentHash, signature);
        consumeNonce(client.getClientId(), nonce);
        DistributionAccountRequestRecord request = dao.selectExternalStatus(
            client.getClientId(), client.getSourceSystemId(), suppliedEventId);
        if (request == null) throw DistributionAccountRequestException.notFound();
        request.setEvents(dao.selectEvents(request.getRequestId().longValue()));
        redactInternalDecisionIdentity(request);
        return request;
    }

    private void redactInternalDecisionIdentity(
            DistributionAccountRequestRecord request) {
        request.setDecidedByUserCd(null);
        request.setDecidedByUserId(null);
        request.setDecidedByUserName(null);
        if (request.getEvents() == null) return;
        for (DistributionAccountRequestEvent event : request.getEvents()) {
            if (event != null && "TDMS_USER".equals(event.getActorType())) {
                event.setActorId(null);
                event.setActorName(null);
            }
        }
    }

    private RegisteredClient authenticate(String method, String path, byte[] body,
            String clientId, String timestamp, String nonce,
            String contentHash, String signature) {
        properties.validateLimits();
        RegisteredClient client = properties.requireClient(trim(clientId));
        long epochSeconds = parseEpochSeconds(timestamp);
        long current = Instant.now(clock).getEpochSecond();
        long skew = properties.getSignatureClockSkewSeconds();
        if (epochSeconds < current - skew || epochSeconds > current + skew) {
            throw DistributionAccountRequestException.unauthorized(
                "Distribution integration timestamp is outside the allowed window.");
        }
        requireCanonicalUuid(nonce, "nonce", true);
        if (!DistributionAccountIntegrationCrypto.isLowerHexSha256(contentHash)
                || !DistributionAccountIntegrationCrypto.constantTimeEquals(
                    DistributionAccountIntegrationCrypto.sha256(body), contentHash)) {
            throw DistributionAccountRequestException.unauthorized(
                "Distribution integration content hash is invalid.");
        }
        if (!DistributionAccountIntegrationCrypto.isLowerHexSha256(signature)) {
            throw DistributionAccountRequestException.unauthorized(
                "Distribution integration signature format is invalid.");
        }
        String canonical = method + "\n" + path + "\n" + client.getClientId() + "\n"
            + timestamp + "\n" + nonce + "\n" + contentHash;
        String expected = DistributionAccountIntegrationCrypto.hmacSha256(
            client.getSharedSecret(), canonical);
        if (!DistributionAccountIntegrationCrypto.constantTimeEquals(expected, signature)) {
            throw DistributionAccountRequestException.unauthorized(
                "Distribution integration signature is invalid.");
        }
        return client;
    }

    private DistributionAccountRequestRecord parse(
            byte[] body, RegisteredClient client, String contentHash) {
        try {
            JsonNode root = objectMapper.readTree(body);
            requireObject(root, "request");
            rejectUnknown(root, ROOT_FIELDS, "request");

            DistributionAccountRequestRecord result = new DistributionAccountRequestRecord();
            result.setEventId(required(root, "eventId", 36));
            requireCanonicalUuid(result.getEventId(), "eventId", false);
            result.setEventId(result.getEventId().toLowerCase());
            result.setCorrelationId(requiredPattern(root, "correlationId", 128,
                "[A-Za-z0-9._:-]+"));
            result.setClientId(client.getClientId());
            result.setSourceSystemId(requiredPattern(root, "sourceSystemId", 100,
                "[A-Za-z0-9._:-]+"));
            if (!client.getSourceSystemId().equals(result.getSourceSystemId())) {
                throw DistributionAccountRequestException.unauthorized(
                    "sourceSystemId does not match the authenticated client.");
            }
            result.setRequestType(parseType(required(root, "requestType", 30)));
            result.setOccurredAt(parseOccurredAt(required(root, "occurredAt", 64)));

            JsonNode representative = requiredObject(root, "representative");
            rejectUnknown(representative, REPRESENTATIVE_FIELDS, "representative");
            result.setRepresentativeId(required(representative, "id", 100));
            result.setRepresentativeName(required(representative, "name", 200));
            result.setRepresentativeEmail(email(representative, "email"));
            result.setRepresentativePhone(optional(representative, "phone", 40));

            JsonNode organization = optionalObject(root, "organization");
            if (organization != null) {
                rejectUnknown(organization, ORGANIZATION_FIELDS, "organization");
                result.setOrganizationCode(optional(organization, "code", 100));
                result.setOrganizationName(optional(organization, "name", 200));
                result.setBusinessNumber(optional(organization, "businessNumber", 50));
            }

            JsonNode target = requiredObject(root, "targetUser");
            rejectUnknown(target, TARGET_USER_FIELDS, "targetUser");
            result.setTargetUserId(required(target, "id", 100));
            if (DistributionAccountRequestType.REGISTER_USER.name()
                    .equals(result.getRequestType())) {
                result.setTargetUserName(required(target, "name", 200));
                result.setTargetUserEmail(email(target, "email"));
            } else {
                result.setTargetUserName(optional(target, "name", 200));
                result.setTargetUserEmail(optionalEmail(target, "email"));
            }
            result.setTargetUserPhone(optional(target, "phone", 40));
            result.setTargetUserPosition(optional(target, "position", 100));
            result.setReason(optional(root, "reason", 1000));

            JsonNode metadata = root.get("metadata");
            if (metadata == null || metadata.isNull()) {
                result.setMetadataJson("{}");
            } else {
                requireObject(metadata, "metadata");
                rejectSensitiveMetadataKeys(metadata);
                byte[] serialized = objectMapper.writeValueAsBytes(metadata);
                if (serialized.length > MAX_METADATA_BYTES) {
                    throw DistributionAccountRequestException.badRequest(
                        "DISTRIBUTION_ACCOUNT_METADATA_TOO_LARGE",
                        "metadata must be no larger than 4096 UTF-8 bytes.");
                }
                result.setMetadataJson(new String(serialized, StandardCharsets.UTF_8));
            }
            result.setContentSha256(contentHash);
            result.setStatus(DistributionAccountRequestStatus.PENDING.name());
            return result;
        } catch (DistributionAccountRequestException exception) {
            throw exception;
        } catch (IOException exception) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_REQUEST_JSON", "Request JSON is invalid.");
        }
    }

    private DistributionAccountRequestRecord resolveDuplicate(
            DistributionAccountRequestRecord incoming) {
        DistributionAccountRequestRecord byEvent = dao.selectBySourceEvent(
            incoming.getSourceSystemId(), incoming.getEventId());
        DistributionAccountRequestRecord byCorrelation = dao.selectBySourceCorrelation(
            incoming.getSourceSystemId(), incoming.getCorrelationId());
        DistributionAccountRequestRecord existing = byEvent != null ? byEvent : byCorrelation;
        if (existing != null
                && existing.getEventId().equals(incoming.getEventId())
                && existing.getCorrelationId().equals(incoming.getCorrelationId())
                && existing.getClientId().equals(incoming.getClientId())
                && DistributionAccountIntegrationCrypto.constantTimeEquals(
                    existing.getContentSha256(), incoming.getContentSha256())) {
            existing.setDuplicate(Boolean.TRUE);
            return existing;
        }
        throw DistributionAccountRequestException.conflict(
            "DISTRIBUTION_ACCOUNT_REQUEST_IDEMPOTENCY_CONFLICT",
            "eventId or correlationId was already used with different request content.");
    }

    private void consumeNonce(String clientId, String nonce) {
        dao.deleteExpiredNonces(properties.getNonceRetentionDays());
        if (dao.insertNonce(clientId, nonce) != 1) {
            throw DistributionAccountRequestException.unauthorized(
                "Distribution integration nonce was already used.");
        }
    }

    private DistributionAccountRequestRecord requireStored(long requestId) {
        DistributionAccountRequestRecord stored = dao.selectRequest(requestId);
        if (stored == null) throw new IllegalStateException("Persisted account request is unavailable.");
        return stored;
    }

    private String parseType(String value) {
        try {
            return DistributionAccountRequestType.valueOf(value).name();
        } catch (IllegalArgumentException exception) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_REQUEST_TYPE", "requestType is invalid.");
        }
    }

    private String parseOccurredAt(String value) {
        try {
            Instant parsed = Instant.parse(value);
            if (parsed.isAfter(Instant.now(clock).plusSeconds(
                    properties.getSignatureClockSkewSeconds()))) {
                throw DistributionAccountRequestException.badRequest(
                    "INVALID_DISTRIBUTION_ACCOUNT_OCCURRED_AT", "occurredAt is in the future.");
            }
            return parsed.toString();
        } catch (DistributionAccountRequestException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_OCCURRED_AT", "occurredAt must be an ISO-8601 instant.");
        }
    }

    private long parseEpochSeconds(String value) {
        if (value == null || !value.matches("[0-9]{1,19}")) {
            throw DistributionAccountRequestException.unauthorized(
                "Distribution integration timestamp is invalid.");
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw DistributionAccountRequestException.unauthorized(
                "Distribution integration timestamp is invalid.");
        }
    }

    private void requireCanonicalUuid(String value, String field, boolean authentication) {
        try {
            UUID parsed = UUID.fromString(value);
            if (!parsed.toString().equalsIgnoreCase(value)) throw new IllegalArgumentException();
        } catch (RuntimeException exception) {
            if (authentication) {
                throw DistributionAccountRequestException.unauthorized(field + " is invalid.");
            }
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_" + field.toUpperCase(), field + " is invalid.");
        }
    }

    private JsonNode requiredObject(JsonNode parent, String field) {
        JsonNode value = parent.get(field);
        requireObject(value, field);
        return value;
    }

    private JsonNode optionalObject(JsonNode parent, String field) {
        JsonNode value = parent.get(field);
        if (value == null || value.isNull()) return null;
        requireObject(value, field);
        return value;
    }

    private void requireObject(JsonNode value, String field) {
        if (value == null || !value.isObject()) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_REQUEST", field + " must be a JSON object.");
        }
    }

    private void rejectUnknown(JsonNode value, Set<String> allowed, String field) {
        Iterator<String> names = value.fieldNames();
        while (names.hasNext()) {
            if (!allowed.contains(names.next())) {
                throw DistributionAccountRequestException.badRequest(
                    "UNSUPPORTED_DISTRIBUTION_ACCOUNT_FIELD",
                    field + " contains an unsupported field.");
            }
        }
    }

    private void rejectSensitiveMetadataKeys(JsonNode value) {
        Deque<JsonNode> pending = new ArrayDeque<JsonNode>();
        pending.push(value);
        while (!pending.isEmpty()) {
            JsonNode current = pending.pop();
            if (current.isObject()) {
                Iterator<String> names = current.fieldNames();
                while (names.hasNext()) {
                    String name = names.next();
                    String normalized = normalizeMetadataKey(name);
                    for (String blocked : SENSITIVE_METADATA_KEY_PARTS) {
                        if (normalized.contains(blocked)) {
                            throw DistributionAccountRequestException.badRequest(
                                "DISTRIBUTION_ACCOUNT_METADATA_SENSITIVE_KEY",
                                "metadata must not contain password, token, credential, or secret fields.");
                        }
                    }
                    pending.push(current.get(name));
                }
            } else if (current.isArray()) {
                for (JsonNode child : current) {
                    pending.push(child);
                }
            }
        }
    }

    private String normalizeMetadataKey(String value) {
        String lower = value == null ? "" : value.toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder(lower.length());
        for (int index = 0; index < lower.length(); index++) {
            char character = lower.charAt(index);
            if ((character >= 'a' && character <= 'z')
                    || (character >= '0' && character <= '9')) {
                result.append(character);
            }
        }
        return result.toString();
    }

    private String requiredPattern(JsonNode parent, String field, int maxLength, String pattern) {
        String value = required(parent, field, maxLength);
        if (!value.matches(pattern)) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_FIELD", field + " has an invalid format.");
        }
        return value;
    }

    private String required(JsonNode parent, String field, int maxLength) {
        JsonNode node = parent.get(field);
        if (node == null || !node.isTextual()) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_FIELD", field + " is required.");
        }
        String value = node.textValue() == null ? "" : node.textValue().trim();
        if (value.isEmpty() || value.length() > maxLength) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_FIELD", field + " is invalid.");
        }
        return value;
    }

    private String optional(JsonNode parent, String field, int maxLength) {
        JsonNode node = parent.get(field);
        if (node == null || node.isNull()) return "";
        if (!node.isTextual()) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_FIELD", field + " must be text.");
        }
        String value = node.textValue() == null ? "" : node.textValue().trim();
        if (value.length() > maxLength) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_FIELD", field + " is too long.");
        }
        return value;
    }

    private String email(JsonNode parent, String field) {
        String value = required(parent, field, 254);
        if (!value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_EMAIL", field + " is invalid.");
        }
        return value;
    }

    private String optionalEmail(JsonNode parent, String field) {
        String value = optional(parent, field, 254);
        if (!value.isEmpty() && !value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_EMAIL", field + " is invalid.");
        }
        return value;
    }

    private void requireOne(int affectedRows, String operation) {
        if (affectedRows != 1) throw new IllegalStateException("Unable to " + operation + '.');
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }

    private static Set<String> fields(String... values) {
        return new HashSet<String>(Arrays.asList(values));
    }
}

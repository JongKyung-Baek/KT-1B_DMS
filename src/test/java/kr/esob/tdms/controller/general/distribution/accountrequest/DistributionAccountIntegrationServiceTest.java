package kr.esob.tdms.controller.general.distribution.accountrequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;

class DistributionAccountIntegrationServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-01T03:00:00Z");
    private static final String CLIENT_ID = "distribution-demo";
    private static final String SOURCE_SYSTEM_ID = "DISTRIBUTION-DEMO";
    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private DistributionAccountRequestDao dao;
    private ObjectMapper objectMapper;
    private DistributionAccountIntegrationService service;

    @BeforeEach
    void setUp() {
        DistributionAccountIntegrationProperties properties =
            new DistributionAccountIntegrationProperties();
        properties.setEnabled(true);
        properties.setClientId(CLIENT_ID);
        properties.setSourceSystemId(SOURCE_SYSTEM_ID);
        properties.setSharedSecret(SECRET);
        dao = org.mockito.Mockito.mock(DistributionAccountRequestDao.class);
        objectMapper = new ObjectMapper();
        service = new DistributionAccountIntegrationService(properties, dao, objectMapper,
            Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void authenticatedRequestPersistsImmutableSnapshotsAndReceiptEvent() throws Exception {
        SignedRequest signed = signPost(validBody());
        when(dao.insertNonce(CLIENT_ID, signed.nonce)).thenReturn(1);
        when(dao.insertRequest(any())).thenReturn(Long.valueOf(42));
        when(dao.insertReceivedEvent(any())).thenReturn(1);
        DistributionAccountRequestRecord stored = record(42L, signed);
        stored.setDecidedByUserCd("ADMIN-CD");
        stored.setDecidedByUserId("admin");
        stored.setDecidedByUserName("Administrator");
        when(dao.selectRequest(42L)).thenReturn(stored);

        DistributionAccountRequestRecord result = service.receive(signed.body, CLIENT_ID,
            signed.timestamp, signed.nonce, signed.contentHash, signed.signature);

        assertEquals(42L, result.getRequestId().longValue());
        assertEquals(Boolean.FALSE, result.getDuplicate());
        assertNull(result.getDecidedByUserCd());
        assertNull(result.getDecidedByUserId());
        assertNull(result.getDecidedByUserName());
        ArgumentCaptor<DistributionAccountRequestRecord> saved =
            ArgumentCaptor.forClass(DistributionAccountRequestRecord.class);
        verify(dao).insertRequest(saved.capture());
        assertEquals("REGISTER_USER", saved.getValue().getRequestType());
        assertEquals("rep@example.com", saved.getValue().getRepresentativeEmail());
        assertEquals("new.user@example.com", saved.getValue().getTargetUserEmail());
        assertEquals("Example Aerospace", saved.getValue().getOrganizationName());
        verify(dao).insertReceivedEvent(any());
    }

    @Test
    void exactEventAndCorrelationRetryIsIdempotent() throws Exception {
        SignedRequest signed = signPost(validBody());
        when(dao.insertNonce(CLIENT_ID, signed.nonce)).thenReturn(1);
        when(dao.insertRequest(any())).thenReturn(null);
        DistributionAccountRequestRecord existing = record(7L, signed);
        existing.setStatus("APPROVED");
        existing.setDecidedByUserCd("ADMIN-CD");
        existing.setDecidedByUserId("admin");
        existing.setDecidedByUserName("Administrator");
        when(dao.selectBySourceEvent(SOURCE_SYSTEM_ID, signed.eventId)).thenReturn(existing);
        when(dao.selectBySourceCorrelation(SOURCE_SYSTEM_ID, signed.correlationId)).thenReturn(existing);

        DistributionAccountRequestRecord result = service.receive(signed.body, CLIENT_ID,
            signed.timestamp, signed.nonce, signed.contentHash, signed.signature);

        assertEquals(Boolean.TRUE, result.getDuplicate());
        assertNull(result.getDecidedByUserCd());
        assertNull(result.getDecidedByUserId());
        assertNull(result.getDecidedByUserName());
        verify(dao, never()).insertReceivedEvent(any());
    }

    @Test
    void reusedCorrelationWithDifferentPayloadIsRejected() throws Exception {
        SignedRequest signed = signPost(validBody());
        when(dao.insertNonce(CLIENT_ID, signed.nonce)).thenReturn(1);
        when(dao.insertRequest(any())).thenReturn(null);
        DistributionAccountRequestRecord existing = record(7L, signed);
        existing.setEventId(UUID.randomUUID().toString());
        existing.setContentSha256("f".repeat(64));
        when(dao.selectBySourceCorrelation(SOURCE_SYSTEM_ID, signed.correlationId)).thenReturn(existing);

        DistributionAccountRequestException exception = assertThrows(
            DistributionAccountRequestException.class,
            () -> service.receive(signed.body, CLIENT_ID, signed.timestamp,
                signed.nonce, signed.contentHash, signed.signature));

        assertEquals("DISTRIBUTION_ACCOUNT_REQUEST_IDEMPOTENCY_CONFLICT", exception.getCode());
    }

    @Test
    void badSignatureIsRejectedBeforeDatabaseAccess() throws Exception {
        SignedRequest signed = signPost(validBody());

        assertThrows(DistributionAccountRequestException.class,
            () -> service.receive(signed.body, CLIENT_ID, signed.timestamp,
                signed.nonce, signed.contentHash, "0".repeat(64)));

        verifyNoInteractions(dao);
    }

    @Test
    void payloadSourceMustMatchAuthenticatedClientRegistration() throws Exception {
        Map<String, Object> body = validBody();
        body.put("sourceSystemId", "ANOTHER-SYSTEM");
        SignedRequest signed = signPost(body);

        assertThrows(DistributionAccountRequestException.class,
            () -> service.receive(signed.body, CLIENT_ID, signed.timestamp,
                signed.nonce, signed.contentHash, signed.signature));

        verify(dao, never()).insertRequest(any());
    }

    @Test
    void unlockOnlyRequiresTheTargetUserId() throws Exception {
        Map<String, Object> body = validBody();
        body.put("requestType", "UNLOCK_ACCOUNT");
        Map<String, Object> target = new LinkedHashMap<String, Object>();
        target.put("id", "locked.user");
        body.put("targetUser", target);
        SignedRequest signed = signPost(body);
        when(dao.insertNonce(CLIENT_ID, signed.nonce)).thenReturn(1);
        when(dao.insertRequest(any())).thenReturn(Long.valueOf(43));
        when(dao.insertReceivedEvent(any())).thenReturn(1);
        when(dao.selectRequest(43L)).thenReturn(record(43L, signed));

        service.receive(signed.body, CLIENT_ID, signed.timestamp,
            signed.nonce, signed.contentHash, signed.signature);

        ArgumentCaptor<DistributionAccountRequestRecord> saved =
            ArgumentCaptor.forClass(DistributionAccountRequestRecord.class);
        verify(dao).insertRequest(saved.capture());
        assertEquals("locked.user", saved.getValue().getTargetUserId());
        assertEquals("", saved.getValue().getTargetUserName());
        assertEquals("", saved.getValue().getTargetUserEmail());
    }

    @Test
    void passwordResetOnlyRequiresTheTargetUserId() throws Exception {
        Map<String, Object> body = validBody();
        body.put("requestType", "RESET_PASSWORD");
        Map<String, Object> target = new LinkedHashMap<String, Object>();
        target.put("id", "reset.user");
        body.put("targetUser", target);
        SignedRequest signed = signPost(body);
        when(dao.insertNonce(CLIENT_ID, signed.nonce)).thenReturn(1);
        when(dao.insertRequest(any())).thenReturn(Long.valueOf(44));
        when(dao.insertReceivedEvent(any())).thenReturn(1);
        when(dao.selectRequest(44L)).thenReturn(record(44L, signed));

        service.receive(signed.body, CLIENT_ID, signed.timestamp,
            signed.nonce, signed.contentHash, signed.signature);

        ArgumentCaptor<DistributionAccountRequestRecord> saved =
            ArgumentCaptor.forClass(DistributionAccountRequestRecord.class);
        verify(dao).insertRequest(saved.capture());
        assertEquals("reset.user", saved.getValue().getTargetUserId());
        assertEquals("", saved.getValue().getTargetUserName());
        assertEquals("", saved.getValue().getTargetUserEmail());
    }

    @Test
    void registerUserStillRequiresTargetNameAndEmail() throws Exception {
        for (String requiredField : Arrays.asList("name", "email")) {
            Map<String, Object> body = validBody();
            @SuppressWarnings("unchecked")
            Map<String, Object> target = (Map<String, Object>) body.get("targetUser");
            target.remove(requiredField);
            SignedRequest signed = signPost(body);

            DistributionAccountRequestException exception = assertThrows(
                DistributionAccountRequestException.class,
                () -> service.receive(signed.body, CLIENT_ID, signed.timestamp,
                    signed.nonce, signed.contentHash, signed.signature));

            assertEquals("INVALID_DISTRIBUTION_ACCOUNT_FIELD", exception.getCode());
        }
        verifyNoInteractions(dao);
    }

    @Test
    void optionalResetEmailMustBeValidWhenSupplied() throws Exception {
        Map<String, Object> body = validBody();
        body.put("requestType", "RESET_PASSWORD");
        Map<String, Object> target = new LinkedHashMap<String, Object>();
        target.put("id", "reset.user");
        target.put("email", "not-an-email");
        body.put("targetUser", target);
        SignedRequest signed = signPost(body);

        DistributionAccountRequestException exception = assertThrows(
            DistributionAccountRequestException.class,
            () -> service.receive(signed.body, CLIENT_ID, signed.timestamp,
                signed.nonce, signed.contentHash, signed.signature));

        assertEquals("INVALID_DISTRIBUTION_ACCOUNT_EMAIL", exception.getCode());
        verifyNoInteractions(dao);
    }

    @Test
    void sensitiveMetadataKeysAreRejectedAtEveryDepthAfterNormalization()
            throws Exception {
        for (String sensitiveKey : Arrays.asList(
                "db_password", "Pass-Wd", "p.w.d", "clientSecret",
                "access-token", "credentialValue", "api_key", "private-Key")) {
            Map<String, Object> body = validBody();
            Map<String, Object> sensitive = new LinkedHashMap<String, Object>();
            sensitive.put(sensitiveKey, "must-not-be-stored");
            Map<String, Object> nested = new LinkedHashMap<String, Object>();
            nested.put("items", Arrays.asList(
                Collections.singletonMap("safe", "value"), sensitive));
            body.put("metadata", Collections.singletonMap("profile", nested));
            SignedRequest signed = signPost(body);

            DistributionAccountRequestException exception = assertThrows(
                DistributionAccountRequestException.class,
                () -> service.receive(signed.body, CLIENT_ID, signed.timestamp,
                    signed.nonce, signed.contentHash, signed.signature));

            assertEquals("DISTRIBUTION_ACCOUNT_METADATA_SENSITIVE_KEY",
                exception.getCode());
        }
        verifyNoInteractions(dao);
    }

    @Test
    void signedStatusLookupReturnsDecisionAndHistoryToOriginalClient() throws Exception {
        SignedRequest original = signPost(validBody());
        SignedRequest lookup = signGet(original.eventId);
        when(dao.insertNonce(CLIENT_ID, lookup.nonce)).thenReturn(1);
        DistributionAccountRequestRecord approved = record(9L, original);
        approved.setStatus("APPROVED");
        approved.setDecidedByUserCd("ADMIN-CD");
        approved.setDecidedByUserId("admin");
        approved.setDecidedByUserName("Administrator");
        DistributionAccountRequestEvent received = new DistributionAccountRequestEvent();
        received.setActorType("EXTERNAL_SYSTEM");
        received.setActorId("representative-1");
        received.setActorName("Representative");
        DistributionAccountRequestEvent decision = new DistributionAccountRequestEvent();
        decision.setActorType("TDMS_USER");
        decision.setActorId("admin");
        decision.setActorName("Administrator");
        when(dao.selectExternalStatus(CLIENT_ID, SOURCE_SYSTEM_ID, original.eventId))
            .thenReturn(approved);
        when(dao.selectEvents(9L)).thenReturn(Arrays.asList(received, decision));

        DistributionAccountRequestRecord result = service.status(original.eventId, CLIENT_ID,
            lookup.timestamp, lookup.nonce, lookup.contentHash, lookup.signature);

        assertEquals("APPROVED", result.getStatus());
        assertNull(result.getDecidedByUserCd());
        assertNull(result.getDecidedByUserId());
        assertNull(result.getDecidedByUserName());
        assertEquals("representative-1", result.getEvents().get(0).getActorId());
        assertEquals("Representative", result.getEvents().get(0).getActorName());
        assertNull(result.getEvents().get(1).getActorId());
        assertNull(result.getEvents().get(1).getActorName());
    }

    private Map<String, Object> validBody() {
        Map<String, Object> representative = new LinkedHashMap<String, Object>();
        representative.put("id", "representative-1");
        representative.put("name", "Representative");
        representative.put("email", "rep@example.com");
        representative.put("phone", "+62-21-555-0100");

        Map<String, Object> organization = new LinkedHashMap<String, Object>();
        organization.put("code", "ORG-001");
        organization.put("name", "Example Aerospace");
        organization.put("businessNumber", "ID-12345");

        Map<String, Object> target = new LinkedHashMap<String, Object>();
        target.put("id", "new.user");
        target.put("name", "New User");
        target.put("email", "new.user@example.com");
        target.put("phone", "+62-21-555-0101");
        target.put("position", "Engineer");

        Map<String, Object> metadata = new LinkedHashMap<String, Object>();
        metadata.put("partnerType", "SUPPLIER");

        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("eventId", UUID.randomUUID().toString());
        root.put("correlationId", UUID.randomUUID().toString());
        root.put("sourceSystemId", SOURCE_SYSTEM_ID);
        root.put("requestType", "REGISTER_USER");
        root.put("occurredAt", NOW.minusSeconds(10).toString());
        root.put("representative", representative);
        root.put("organization", organization);
        root.put("targetUser", target);
        root.put("reason", "New project participant");
        root.put("metadata", metadata);
        return root;
    }

    private SignedRequest signPost(Map<String, Object> values) throws Exception {
        SignedRequest result = new SignedRequest();
        result.body = objectMapper.writeValueAsBytes(values);
        result.eventId = (String) values.get("eventId");
        result.correlationId = (String) values.get("correlationId");
        result.timestamp = Long.toString(NOW.getEpochSecond());
        result.nonce = UUID.randomUUID().toString();
        result.contentHash = DistributionAccountIntegrationCrypto.sha256(result.body);
        result.signature = sign("POST", DistributionAccountIntegrationProperties.REQUEST_PATH,
            result.timestamp, result.nonce, result.contentHash);
        return result;
    }

    private SignedRequest signGet(String eventId) {
        SignedRequest result = new SignedRequest();
        result.body = new byte[0];
        result.eventId = eventId;
        result.timestamp = Long.toString(NOW.getEpochSecond());
        result.nonce = UUID.randomUUID().toString();
        result.contentHash = DistributionAccountIntegrationCrypto.sha256(result.body);
        result.signature = sign("GET",
            DistributionAccountIntegrationProperties.REQUEST_PATH + "/" + eventId,
            result.timestamp, result.nonce, result.contentHash);
        return result;
    }

    private String sign(String method, String path, String timestamp,
            String nonce, String contentHash) {
        return DistributionAccountIntegrationCrypto.hmacSha256(SECRET,
            method + "\n" + path + "\n" + CLIENT_ID + "\n" + timestamp
                + "\n" + nonce + "\n" + contentHash);
    }

    private DistributionAccountRequestRecord record(long id, SignedRequest signed) {
        DistributionAccountRequestRecord result = new DistributionAccountRequestRecord();
        result.setRequestId(Long.valueOf(id));
        result.setEventId(signed.eventId);
        result.setCorrelationId(signed.correlationId);
        result.setClientId(CLIENT_ID);
        result.setSourceSystemId(SOURCE_SYSTEM_ID);
        result.setContentSha256(signed.contentHash);
        result.setStatus("PENDING");
        return result;
    }

    private static class SignedRequest {
        byte[] body;
        String eventId;
        String correlationId;
        String timestamp;
        String nonce;
        String contentHash;
        String signature;
    }
}

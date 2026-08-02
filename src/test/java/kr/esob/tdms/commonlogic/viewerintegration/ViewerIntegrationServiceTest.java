package kr.esob.tdms.commonlogic.viewerintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.fasterxml.jackson.databind.ObjectMapper;

class ViewerIntegrationServiceTest {
    private static final Instant NOW = Instant.parse("2026-07-31T12:00:00Z");
    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final String STEP_SECRET = "step-secret-0123456789abcdef0123456789";

    private ViewerIntegrationProperties properties;
    private StepViewerIntegrationProperties stepProperties;
    private ViewerIntegrationClient client;
    private ViewerIntegrationDao dao;
    private ObjectMapper objectMapper;
    private ViewerIntegrationService service;

    @BeforeEach
    void setUp() {
        properties = new ViewerIntegrationProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://demo.esob.kr:7442");
        properties.setClientId("tdms-demo");
        properties.setCallbackClientId("collabview");
        properties.setSharedSecret(SECRET);
        stepProperties = new StepViewerIntegrationProperties();
        stepProperties.setEnabled(true);
        stepProperties.setBaseUrl("http://127.0.0.1:7443");
        stepProperties.setClientId("tdms-step-demo");
        stepProperties.setCallbackClientId("collabview3d");
        stepProperties.setSharedSecret(STEP_SECRET);
        client = org.mockito.Mockito.mock(ViewerIntegrationClient.class);
        dao = org.mockito.Mockito.mock(ViewerIntegrationDao.class);
        objectMapper = new ObjectMapper();
        service = new ViewerIntegrationService(properties, stepProperties, client, dao,
                objectMapper, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void rejectsBadSignatureBeforeAnyDatabaseAccess() throws Exception {
        SignedCallback callback = signedCallback(validEvent());
        assertThrows(ViewerCallbackAuthenticationException.class, () -> service.acceptCallback(
                callback.body, callback.clientId, callback.timestamp, callback.nonce,
                callback.contentHash, "0".repeat(64)));
        verifyNoInteractions(dao);
    }

    @Test
    void persistsExactlyOneHistoryAfterAuthenticatedViewOpened() throws Exception {
        Map<String, Object> values = validEvent();
        SignedCallback callback = signedCallback(values);
        ViewerLaunchRecord launch = matchingLaunch(values);
        when(dao.selectLaunch((String) values.get("correlationId"))).thenReturn(launch);
        when(dao.insertNonce(callback.clientId, callback.nonce)).thenReturn(1);
        when(dao.insertEvent(any(ViewerCallbackEvent.class))).thenReturn(1);
        when(dao.insertViewHistory(any(ViewerLaunchRecord.class), any(ViewerCallbackEvent.class))).thenReturn(1);
        when(dao.markViewed(any(ViewerCallbackEvent.class))).thenReturn(1);

        service.acceptCallback(callback.body, callback.clientId, callback.timestamp, callback.nonce,
                callback.contentHash, callback.signature);

        ArgumentCaptor<ViewerCallbackEvent> event = ArgumentCaptor.forClass(ViewerCallbackEvent.class);
        verify(dao).insertEvent(event.capture());
        assertEquals(callback.contentHash, event.getValue().getContentSha256());
        verify(dao).insertViewHistory(any(ViewerLaunchRecord.class), any(ViewerCallbackEvent.class));
        verify(dao).markViewed(any(ViewerCallbackEvent.class));
    }

    @Test
    void duplicateCorrelationEventDoesNotDuplicateHistory() throws Exception {
        Map<String, Object> values = validEvent();
        SignedCallback callback = signedCallback(values);
        when(dao.selectLaunch((String) values.get("correlationId"))).thenReturn(matchingLaunch(values));
        when(dao.insertNonce(callback.clientId, callback.nonce)).thenReturn(1);
        when(dao.insertEvent(any(ViewerCallbackEvent.class))).thenReturn(0);

        service.acceptCallback(callback.body, callback.clientId, callback.timestamp, callback.nonce,
                callback.contentHash, callback.signature);

        verify(dao, never()).insertViewHistory(any(), any());
        verify(dao, never()).markViewed(any());
    }

    @Test
    void identityMismatchCannotMutateCallbackTables() throws Exception {
        Map<String, Object> values = validEvent();
        SignedCallback callback = signedCallback(values);
        ViewerLaunchRecord launch = matchingLaunch(values);
        launch.setActorUserId("someone-else");
        when(dao.selectLaunch((String) values.get("correlationId"))).thenReturn(launch);

        assertThrows(ViewerCallbackIdentityException.class, () -> service.acceptCallback(
                callback.body, callback.clientId, callback.timestamp, callback.nonce,
                callback.contentHash, callback.signature));

        verify(dao).selectLaunch((String) values.get("correlationId"));
        verifyNoMoreInteractions(dao);
    }

    @Test
    void rejectsCallbackThatPredatesItsLaunchBeyondClockSkew() throws Exception {
        Map<String, Object> values = validEvent();
        values.put("occurredAt", NOW.minusSeconds(301).toString());
        SignedCallback callback = signedCallback(values);
        ViewerLaunchRecord launch = matchingLaunch(values);
        launch.setCreatedAt(NOW.toString());
        when(dao.selectLaunch((String) values.get("correlationId"))).thenReturn(launch);

        assertThrows(ViewerCallbackIdentityException.class, () -> service.acceptCallback(
                callback.body, callback.clientId, callback.timestamp, callback.nonce,
                callback.contentHash, callback.signature));

        verify(dao).selectLaunch((String) values.get("correlationId"));
        verifyNoMoreInteractions(dao);
    }

    @Test
    void cleansExpiredViewerStateBeforeCreatingANewLaunch() {
        ViewerDocumentMetadata metadata = validMetadata();
        ViewerIngestResponse response = new ViewerIngestResponse();
        response.setLaunchToken("token");
        response.setExpiresAt(NOW.plusSeconds(300).toString());
        response.setCorrelationId(metadata.getCorrelationId());
        when(client.ingest(any(), any())).thenReturn(response);
        when(dao.insertLaunch(any(ViewerLaunchRecord.class))).thenReturn(1);

        service.prepareLaunch(java.nio.file.Path.of("sample.pdf"), metadata);

        InOrder order = org.mockito.Mockito.inOrder(dao, client);
        order.verify(dao).deleteExpiredState(30);
        order.verify(client).ingest(any(), any());
        order.verify(dao).insertLaunch(any(ViewerLaunchRecord.class));
    }

    @Test
    void persistsStepProviderAndUsesStepLaunchUri() {
        ViewerDocumentMetadata metadata = validMetadata();
        metadata.setFileName("sample.step");
        ViewerIngestResponse response = new ViewerIngestResponse();
        response.setLaunchToken("step-token");
        response.setExpiresAt(NOW.plusSeconds(300).toString());
        response.setCorrelationId(metadata.getCorrelationId());
        when(client.ingest(any(Path.class), any(ViewerDocumentMetadata.class),
                eq(ViewerProvider.STEP))).thenReturn(response);
        when(dao.insertLaunch(any(ViewerLaunchRecord.class))).thenReturn(1);

        ViewerPreparedLaunch prepared = service.prepareLaunch(
                Path.of("sample.step"), metadata, ViewerProvider.STEP);

        assertEquals(URI.create(
                "http://127.0.0.1:7443/api/integrations/tdms/v1/launch"), prepared.getLaunchUri());
        assertEquals("step-token", prepared.getLaunchToken());
        assertEquals(metadata.getCorrelationId(), prepared.getCorrelationId());
        ArgumentCaptor<ViewerLaunchRecord> launch =
                ArgumentCaptor.forClass(ViewerLaunchRecord.class);
        verify(dao).insertLaunch(launch.capture());
        assertEquals(ViewerProvider.STEP.getCode(), launch.getValue().getViewerProvider());
        assertEquals("sample.step", launch.getValue().getOrgFileNm());
        verify(client).ingest(any(Path.class), eq(metadata), eq(ViewerProvider.STEP));
    }

    @Test
    void acceptsStepCallbackUsingStepClientAndSecretForStepLaunch() throws Exception {
        Map<String, Object> values = validEvent();
        SignedCallback callback = signedCallback(
                values, stepProperties.getCallbackClientId(), STEP_SECRET);
        ViewerLaunchRecord launch = matchingLaunch(values);
        launch.setViewerProvider(ViewerProvider.STEP.getCode());
        launch.setOrgFileNm("sample.step");
        when(dao.selectLaunch((String) values.get("correlationId"))).thenReturn(launch);
        when(dao.insertNonce(callback.clientId, callback.nonce)).thenReturn(1);
        when(dao.insertEvent(any(ViewerCallbackEvent.class))).thenReturn(1);
        when(dao.insertViewHistory(any(ViewerLaunchRecord.class),
                any(ViewerCallbackEvent.class))).thenReturn(1);
        when(dao.markViewed(any(ViewerCallbackEvent.class))).thenReturn(1);

        service.acceptCallback(callback.body, callback.clientId, callback.timestamp, callback.nonce,
                callback.contentHash, callback.signature);

        verify(dao).insertNonce(stepProperties.getCallbackClientId(), callback.nonce);
        verify(dao).insertViewHistory(eq(launch), any(ViewerCallbackEvent.class));
        verify(dao).markViewed(any(ViewerCallbackEvent.class));
    }

    @Test
    void rejectsStepCallbackSignedWithPdfSecretBeforeDatabaseAccess() throws Exception {
        SignedCallback callback = signedCallback(
                validEvent(), stepProperties.getCallbackClientId(), SECRET);

        assertThrows(ViewerCallbackAuthenticationException.class, () -> service.acceptCallback(
                callback.body, callback.clientId, callback.timestamp, callback.nonce,
                callback.contentHash, callback.signature));

        verifyNoInteractions(dao);
    }

    @Test
    void rejectsStepCallbackForPdfLaunchBeforeWritingHistory() throws Exception {
        Map<String, Object> values = validEvent();
        SignedCallback callback = signedCallback(
                values, stepProperties.getCallbackClientId(), STEP_SECRET);
        ViewerLaunchRecord launch = matchingLaunch(values);
        launch.setViewerProvider(ViewerProvider.PDF.getCode());
        when(dao.selectLaunch((String) values.get("correlationId"))).thenReturn(launch);

        assertThrows(ViewerCallbackIdentityException.class, () -> service.acceptCallback(
                callback.body, callback.clientId, callback.timestamp, callback.nonce,
                callback.contentHash, callback.signature));

        verify(dao).selectLaunch((String) values.get("correlationId"));
        verifyNoMoreInteractions(dao);
    }

    private Map<String, Object> validEvent() {
        Map<String, Object> event = new LinkedHashMap<String, Object>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("correlationId", UUID.randomUUID().toString());
        event.put("eventType", "VIEW_OPENED");
        event.put("occurredAt", NOW.toString());
        event.put("objectId", "OBJ-1");
        event.put("fileNo", "1");
        event.put("userId", "admin");
        return event;
    }

    private ViewerLaunchRecord matchingLaunch(Map<String, Object> event) {
        ViewerLaunchRecord launch = new ViewerLaunchRecord();
        launch.setCorrelationId((String) event.get("correlationId"));
        launch.setObjectId((String) event.get("objectId"));
        launch.setFileNo((String) event.get("fileNo"));
        launch.setActorUserId((String) event.get("userId"));
        launch.setActorUserNm("Administrator");
        launch.setOrgFileNm("sample.pdf");
        launch.setCreatedAt(NOW.minusSeconds(60).toString());
        return launch;
    }

    private ViewerDocumentMetadata validMetadata() {
        ViewerDocumentMetadata metadata = new ViewerDocumentMetadata();
        metadata.setCorrelationId(UUID.randomUUID().toString());
        metadata.setObjectType("DOCUMENT");
        metadata.setObjectId("OBJ-1");
        metadata.setAclObjectType("DOCUMENT");
        metadata.setAclObjectId("OBJ-1");
        metadata.setFileNo("1");
        metadata.setFileName("sample.pdf");
        metadata.setUserCd("ADMIN");
        metadata.setUserId("admin");
        metadata.setUserName("Administrator");
        metadata.setAuthority("2");
        return metadata;
    }

    private SignedCallback signedCallback(Map<String, Object> values) throws Exception {
        return signedCallback(values, properties.getCallbackClientId(), SECRET);
    }

    private SignedCallback signedCallback(
            Map<String, Object> values, String callbackClientId, String sharedSecret) throws Exception {
        SignedCallback callback = new SignedCallback();
        callback.body = objectMapper.writeValueAsBytes(values);
        callback.clientId = callbackClientId;
        callback.timestamp = String.valueOf(NOW.getEpochSecond());
        callback.nonce = UUID.randomUUID().toString();
        callback.contentHash = ViewerCrypto.sha256(callback.body);
        String canonical = "POST\n" + ViewerIntegrationProperties.CALLBACK_PATH + "\n"
                + callback.clientId + "\n" + callback.timestamp + "\n" + callback.nonce + "\n"
                + callback.contentHash;
        callback.signature = ViewerCrypto.hmacSha256(sharedSecret, canonical);
        return callback;
    }

    private static class SignedCallback {
        byte[] body;
        String clientId;
        String timestamp;
        String nonce;
        String contentHash;
        String signature;
    }
}

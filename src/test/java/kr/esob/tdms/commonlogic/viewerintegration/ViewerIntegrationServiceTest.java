package kr.esob.tdms.commonlogic.viewerintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    void authenticatedCallbackPersistsEventButNeverWritesHistory()
            throws Exception {
        Map<String, Object> values = validEvent();
        SignedCallback callback = signedCallback(values);
        ViewerLaunchRecord launch = matchingLaunch(values);
        when(dao.selectLaunch((String) values.get("correlationId")))
                .thenReturn(launch);
        when(dao.insertNonce(callback.clientId, callback.nonce)).thenReturn(1);
        when(dao.insertEvent(any(ViewerCallbackEvent.class))).thenReturn(1);
        when(dao.markViewed(any(ViewerCallbackEvent.class))).thenReturn(1);

        service.acceptCallback(callback.body, callback.clientId,
                callback.timestamp, callback.nonce,
                callback.contentHash, callback.signature);

        verify(dao).insertEvent(any(ViewerCallbackEvent.class));
        verify(dao).markViewed(any(ViewerCallbackEvent.class));
        verify(dao, never()).insertLaunchWithHistory(any());
    }

    @Test
    void duplicateCallbackEventDoesNotWriteHistoryOrRemarkLaunch()
            throws Exception {
        Map<String, Object> values = validEvent();
        SignedCallback callback = signedCallback(values);
        when(dao.selectLaunch((String) values.get("correlationId")))
                .thenReturn(matchingLaunch(values));
        when(dao.insertNonce(callback.clientId, callback.nonce)).thenReturn(1);
        when(dao.insertEvent(any(ViewerCallbackEvent.class))).thenReturn(0);

        service.acceptCallback(callback.body, callback.clientId,
                callback.timestamp, callback.nonce,
                callback.contentHash, callback.signature);

        verify(dao, never()).insertLaunchWithHistory(any());
        verify(dao, never()).markViewed(any());
    }

    @Test
    void replayedNonceCannotWriteEventOrHistory() throws Exception {
        Map<String, Object> values = validEvent();
        SignedCallback callback = signedCallback(values);
        when(dao.selectLaunch((String) values.get("correlationId")))
                .thenReturn(matchingLaunch(values));
        when(dao.insertNonce(callback.clientId, callback.nonce)).thenReturn(0);

        assertThrows(ViewerCallbackAuthenticationException.class, () ->
                service.acceptCallback(callback.body, callback.clientId,
                        callback.timestamp, callback.nonce,
                        callback.contentHash, callback.signature));

        verify(dao, never()).insertEvent(any());
        verify(dao, never()).insertLaunchWithHistory(any());
        verify(dao, never()).markViewed(any());
    }

    @Test
    void identityMismatchCannotMutateCallbackTables() throws Exception {
        Map<String, Object> values = validEvent();
        SignedCallback callback = signedCallback(values);
        ViewerLaunchRecord launch = matchingLaunch(values);
        launch.setActorUserId("someone-else");
        when(dao.selectLaunch((String) values.get("correlationId")))
                .thenReturn(launch);

        assertThrows(ViewerCallbackIdentityException.class, () ->
                service.acceptCallback(callback.body, callback.clientId,
                        callback.timestamp, callback.nonce,
                        callback.contentHash, callback.signature));

        verify(dao).selectLaunch((String) values.get("correlationId"));
        verifyNoMoreInteractions(dao);
    }

    @Test
    void rejectsCallbackThatPredatesItsLaunchBeyondClockSkew()
            throws Exception {
        Map<String, Object> values = validEvent();
        values.put("occurredAt", NOW.minusSeconds(301).toString());
        SignedCallback callback = signedCallback(values);
        ViewerLaunchRecord launch = matchingLaunch(values);
        launch.setCreatedAt(NOW.toString());
        when(dao.selectLaunch((String) values.get("correlationId")))
                .thenReturn(launch);

        assertThrows(ViewerCallbackIdentityException.class, () ->
                service.acceptCallback(callback.body, callback.clientId,
                        callback.timestamp, callback.nonce,
                        callback.contentHash, callback.signature));

        verify(dao).selectLaunch((String) values.get("correlationId"));
        verifyNoMoreInteractions(dao);
    }

    @Test
    void successfulPdfLaunchCallsAtomicDaoExactlyOnceAfterIngest() {
        ViewerDocumentMetadata metadata = validMetadata();
        ViewerIngestResponse response = new ViewerIngestResponse();
        response.setLaunchToken("token");
        response.setExpiresAt(NOW.plusSeconds(300).toString());
        response.setCorrelationId(metadata.getCorrelationId());
        when(client.ingest(any(), any())).thenReturn(response);
        when(dao.insertLaunchWithHistory(any(ViewerLaunchRecord.class)))
                .thenReturn(1);

        ViewerPreparedLaunch prepared = service.prepareLaunch(
                Path.of("sample.pdf"), metadata);

        InOrder order = org.mockito.Mockito.inOrder(dao, client);
        order.verify(dao).deleteExpiredState(30);
        order.verify(client).ingest(any(), any());
        ArgumentCaptor<ViewerLaunchRecord> launch =
                ArgumentCaptor.forClass(ViewerLaunchRecord.class);
        order.verify(dao, times(1)).insertLaunchWithHistory(launch.capture());
        assertEquals(ViewerProvider.PDF.getCode(), launch.getValue().getViewerProvider());
        assertEquals("sample.pdf", launch.getValue().getOrgFileNm());
        assertEquals("token", prepared.getLaunchToken());
        assertEquals(metadata.getCorrelationId(), prepared.getCorrelationId());
    }

    @Test
    void successfulStepLaunchCallsAtomicDaoExactlyOnceAndUsesStepProvider() {
        ViewerDocumentMetadata metadata = validMetadata();
        metadata.setFileName("sample.step");
        ViewerIngestResponse response = new ViewerIngestResponse();
        response.setLaunchToken("step-token");
        response.setExpiresAt(NOW.plusSeconds(300).toString());
        response.setCorrelationId(metadata.getCorrelationId());
        when(client.ingest(any(Path.class), any(ViewerDocumentMetadata.class),
                eq(ViewerProvider.STEP))).thenReturn(response);
        when(dao.insertLaunchWithHistory(any(ViewerLaunchRecord.class)))
                .thenReturn(1);

        ViewerPreparedLaunch prepared = service.prepareLaunch(
                Path.of("sample.step"), metadata, ViewerProvider.STEP);

        assertEquals(URI.create(
                "http://127.0.0.1:7443/api/integrations/tdms/v1/launch"), prepared.getLaunchUri());
        assertEquals("step-token", prepared.getLaunchToken());
        assertEquals(metadata.getCorrelationId(), prepared.getCorrelationId());
        ArgumentCaptor<ViewerLaunchRecord> launch =
                ArgumentCaptor.forClass(ViewerLaunchRecord.class);
        verify(dao, times(1)).insertLaunchWithHistory(launch.capture());
        assertEquals(ViewerProvider.STEP.getCode(), launch.getValue().getViewerProvider());
        assertEquals("sample.step", launch.getValue().getOrgFileNm());
        verify(client).ingest(any(Path.class), eq(metadata), eq(ViewerProvider.STEP));
    }

    @Test
    void viewerIngestFailureDoesNotWriteLaunchOrHistory() {
        ViewerDocumentMetadata metadata = validMetadata();
        when(client.ingest(any(), any())).thenThrow(
                new IllegalStateException("viewer unavailable"));

        assertThrows(IllegalStateException.class, () ->
                service.prepareLaunch(Path.of("sample.pdf"), metadata));

        verify(dao, never()).insertLaunchWithHistory(any());
    }

    @Test
    void stepViewerIngestFailureDoesNotWriteLaunchOrHistory() {
        ViewerDocumentMetadata metadata = validMetadata();
        metadata.setFileName("sample.step");
        when(client.ingest(any(Path.class), any(ViewerDocumentMetadata.class),
                eq(ViewerProvider.STEP))).thenThrow(
                        new IllegalStateException("STEP viewer unavailable"));

        assertThrows(IllegalStateException.class, () ->
                service.prepareLaunch(
                        Path.of("sample.step"), metadata, ViewerProvider.STEP));

        verify(dao, never()).insertLaunchWithHistory(any());
    }

    @Test
    void atomicLaunchAndHistoryPersistenceFailureDoesNotReturnViewerLaunch() {
        ViewerDocumentMetadata metadata = validMetadata();
        ViewerIngestResponse response = responseFor(metadata, "token");
        when(client.ingest(any(), any())).thenReturn(response);
        when(dao.insertLaunchWithHistory(any())).thenReturn(0);

        assertThrows(IllegalStateException.class, () ->
                service.prepareLaunch(Path.of("sample.pdf"), metadata));

        verify(dao, times(1)).insertLaunchWithHistory(any());
    }

    @Test
    void repeatedPdfLaunchRequestsWriteOneDistinctHistoryEach() {
        ViewerDocumentMetadata first = validMetadata();
        ViewerDocumentMetadata second = validMetadata();
        assertNotEquals(first.getCorrelationId(), second.getCorrelationId());
        when(client.ingest(any(), any())).thenAnswer(invocation ->
                responseFor(invocation.getArgument(1), "token"));
        when(dao.insertLaunchWithHistory(any())).thenReturn(1);

        service.prepareLaunch(Path.of("first.pdf"), first);
        service.prepareLaunch(Path.of("second.pdf"), second);

        ArgumentCaptor<ViewerLaunchRecord> histories =
                ArgumentCaptor.forClass(ViewerLaunchRecord.class);
        verify(dao, times(2)).insertLaunchWithHistory(histories.capture());
        assertEquals(2, histories.getAllValues().size());
        assertNotEquals(histories.getAllValues().get(0).getCorrelationId(),
                histories.getAllValues().get(1).getCorrelationId());
    }

    @Test
    void authenticatedStepCallbackPersistsEventButNeverWritesHistory()
            throws Exception {
        Map<String, Object> values = validEvent();
        SignedCallback callback = signedCallback(values,
                stepProperties.getCallbackClientId(), STEP_SECRET);
        ViewerLaunchRecord launch = matchingLaunch(values);
        launch.setViewerProvider(ViewerProvider.STEP.getCode());
        launch.setOrgFileNm("sample.step");
        when(dao.selectLaunch((String) values.get("correlationId")))
                .thenReturn(launch);
        when(dao.insertNonce(callback.clientId, callback.nonce)).thenReturn(1);
        when(dao.insertEvent(any(ViewerCallbackEvent.class))).thenReturn(1);
        when(dao.markViewed(any(ViewerCallbackEvent.class))).thenReturn(1);

        service.acceptCallback(callback.body, callback.clientId, callback.timestamp, callback.nonce,
                callback.contentHash, callback.signature);

        verify(dao).insertEvent(any(ViewerCallbackEvent.class));
        verify(dao).markViewed(any(ViewerCallbackEvent.class));
        verify(dao, never()).insertLaunchWithHistory(any());
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
    void rejectsStepCallbackForPdfLaunchBeforeWritingEvent()
            throws Exception {
        Map<String, Object> values = validEvent();
        SignedCallback callback = signedCallback(values,
                stepProperties.getCallbackClientId(), STEP_SECRET);
        ViewerLaunchRecord launch = matchingLaunch(values);
        launch.setViewerProvider(ViewerProvider.PDF.getCode());
        when(dao.selectLaunch((String) values.get("correlationId")))
                .thenReturn(launch);

        assertThrows(ViewerCallbackIdentityException.class, () ->
                service.acceptCallback(callback.body, callback.clientId,
                        callback.timestamp, callback.nonce,
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

    private ViewerLaunchRecord matchingLaunch(Map<String, Object> event) {
        ViewerLaunchRecord launch = new ViewerLaunchRecord();
        launch.setCorrelationId((String) event.get("correlationId"));
        launch.setViewerProvider(ViewerProvider.PDF.getCode());
        launch.setObjectId((String) event.get("objectId"));
        launch.setFileNo((String) event.get("fileNo"));
        launch.setActorUserId((String) event.get("userId"));
        launch.setActorUserNm("Administrator");
        launch.setOrgFileNm("sample.pdf");
        launch.setCreatedAt(NOW.minusSeconds(60).toString());
        return launch;
    }

    private ViewerIngestResponse responseFor(
            ViewerDocumentMetadata metadata, String token) {
        ViewerIngestResponse response = new ViewerIngestResponse();
        response.setLaunchToken(token);
        response.setExpiresAt(NOW.plusSeconds(300).toString());
        response.setCorrelationId(metadata.getCorrelationId());
        return response;
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

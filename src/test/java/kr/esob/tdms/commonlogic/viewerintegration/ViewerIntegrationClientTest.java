package kr.esob.tdms.commonlogic.viewerintegration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class ViewerIntegrationClientTest {
    @TempDir
    Path tempDir;

    @Test
    void productionConstructorCanBeCreatedBySpring() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext()) {
            context.registerBean(ViewerIntegrationProperties.class);
            context.registerBean(StepViewerIntegrationProperties.class);
            context.registerBean(ObjectMapper.class, () -> new ObjectMapper());
            context.register(ViewerIntegrationClient.class);
            context.refresh();

            assertNotNull(context.getBean(ViewerIntegrationClient.class));
        }
    }

    @Test
    void streamsPdfWithSignedMetadataContract() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Instant now = Instant.parse("2026-07-31T12:00:00Z");
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        String correlationId = UUID.randomUUID().toString();
        byte[] pdf = "%PDF-1.4\n%%EOF\n".getBytes(StandardCharsets.US_ASCII);
        Path pdfPath = tempDir.resolve("request.pdf");
        Files.write(pdfPath, pdf);
        AtomicReference<CapturedRequest> captured = new AtomicReference<CapturedRequest>();

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(ViewerIntegrationProperties.INGEST_PATH,
                exchange -> captureAndRespond(exchange, objectMapper, correlationId, captured));
        server.start();
        try {
            ViewerIntegrationProperties properties = configured(
                    "http://127.0.0.1:" + server.getAddress().getPort());
            ViewerIntegrationClient client = new ViewerIntegrationClient(properties, objectMapper, clock);
            ViewerDocumentMetadata metadata = metadata(correlationId);

            ViewerIngestResponse response = client.ingest(pdfPath, metadata);

            assertEquals(correlationId, response.getCorrelationId());
            CapturedRequest request = captured.get();
            assertNotNull(request);
            assertEquals("POST", request.method);
            assertArrayEquals(pdf, request.body);
            assertEquals("application/pdf", request.contentType);
            assertEquals(properties.getClientId(), request.clientId);
            assertEquals(String.valueOf(now.getEpochSecond()), request.timestamp);
            UUID.fromString(request.nonce);
            assertEquals(ViewerCrypto.sha256(pdf), request.contentHash);

            byte[] metadataBytes = Base64.getUrlDecoder().decode(request.encodedMetadata);
            Map<String, Object> signedMetadata = objectMapper.readValue(
                    metadataBytes, new TypeReference<Map<String, Object>>() { });
            assertEquals(correlationId, signedMetadata.get("correlationId"));
            assertEquals("OBJ-1", signedMetadata.get("objectId"));
            assertEquals("admin", signedMetadata.get("userId"));

            String canonical = "POST\n" + ViewerIntegrationProperties.INGEST_PATH + "\n"
                    + request.clientId + "\n" + request.timestamp + "\n" + request.nonce + "\n"
                    + request.contentHash + "\n" + ViewerCrypto.sha256(metadataBytes);
            assertEquals(ViewerCrypto.hmacSha256(properties.getSharedSecret(), canonical), request.signature);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void streamsStepWithStepProviderCredentialsAndSignedMetadataContract() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Instant now = Instant.parse("2026-07-31T12:00:00Z");
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        String correlationId = UUID.randomUUID().toString();
        byte[] step = ("ISO-10303-21;\n"
                + "HEADER;\nFILE_DESCRIPTION(('TDMS STEP test'),'2;1');\nENDSEC;\n"
                + "DATA;\nENDSEC;\nEND-ISO-10303-21;\n")
                .getBytes(StandardCharsets.US_ASCII);
        Path stepPath = tempDir.resolve("request.step");
        Files.write(stepPath, step);
        AtomicReference<CapturedRequest> captured = new AtomicReference<CapturedRequest>();

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(StepViewerIntegrationProperties.INGEST_PATH,
                exchange -> captureAndRespond(exchange, objectMapper, correlationId, captured));
        server.start();
        try {
            ViewerIntegrationProperties pdfProperties = configured("https://pdf-viewer.example.test");
            pdfProperties.setClientId("pdf-client");
            pdfProperties.setSharedSecret("pdf-secret-0123456789abcdef0123456789");
            StepViewerIntegrationProperties stepProperties = configuredStep(
                    "http://127.0.0.1:" + server.getAddress().getPort());
            ViewerIntegrationClient client = new ViewerIntegrationClient(
                    pdfProperties, stepProperties, objectMapper, clock);
            ViewerDocumentMetadata metadata = metadata(correlationId);
            metadata.setFileName("sample.step");

            ViewerIngestResponse response = client.ingest(stepPath, metadata, ViewerProvider.STEP);

            assertEquals(correlationId, response.getCorrelationId());
            CapturedRequest request = captured.get();
            assertNotNull(request);
            assertEquals("POST", request.method);
            assertArrayEquals(step, request.body);
            assertEquals("model/step", request.contentType);
            assertEquals(stepProperties.getClientId(), request.clientId);
            assertEquals(String.valueOf(now.getEpochSecond()), request.timestamp);
            UUID.fromString(request.nonce);
            assertEquals(ViewerCrypto.sha256(step), request.contentHash);

            byte[] metadataBytes = Base64.getUrlDecoder().decode(request.encodedMetadata);
            Map<String, Object> signedMetadata = objectMapper.readValue(
                    metadataBytes, new TypeReference<Map<String, Object>>() { });
            assertEquals(correlationId, signedMetadata.get("correlationId"));
            assertEquals("OBJ-1", signedMetadata.get("objectId"));
            assertEquals("sample.step", signedMetadata.get("fileName"));
            assertEquals("admin", signedMetadata.get("userId"));

            String canonical = "POST\n" + StepViewerIntegrationProperties.INGEST_PATH + "\n"
                    + request.clientId + "\n" + request.timestamp + "\n" + request.nonce + "\n"
                    + request.contentHash + "\n" + ViewerCrypto.sha256(metadataBytes);
            assertEquals(ViewerCrypto.hmacSha256(
                    stepProperties.getSharedSecret(), canonical), request.signature);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void rejectsInvalidStepBeforeSendingItToTheViewer() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Path invalidStep = tempDir.resolve("invalid.stp");
        Files.write(invalidStep, "not-a-step-document".getBytes(StandardCharsets.US_ASCII));
        ViewerIntegrationProperties pdfProperties = configured("https://pdf-viewer.example.test");
        StepViewerIntegrationProperties stepProperties = configuredStep("http://127.0.0.1:65535");
        ViewerIntegrationClient client = new ViewerIntegrationClient(
                pdfProperties, stepProperties, objectMapper,
                Clock.fixed(Instant.parse("2026-07-31T12:00:00Z"), ZoneOffset.UTC));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> client.ingest(
                        invalidStep, metadata(UUID.randomUUID().toString()), ViewerProvider.STEP));

        assertEquals("Only a valid STEP file can be sent to the 3D viewer.", exception.getMessage());
    }

    private void captureAndRespond(HttpExchange exchange,
                                   ObjectMapper objectMapper,
                                   String correlationId,
                                   AtomicReference<CapturedRequest> target) throws java.io.IOException {
        CapturedRequest request = new CapturedRequest();
        request.method = exchange.getRequestMethod();
        request.body = exchange.getRequestBody().readAllBytes();
        request.contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        request.clientId = exchange.getRequestHeaders().getFirst(ViewerIntegrationClient.CLIENT_ID_HEADER);
        request.timestamp = exchange.getRequestHeaders().getFirst(ViewerIntegrationClient.TIMESTAMP_HEADER);
        request.nonce = exchange.getRequestHeaders().getFirst(ViewerIntegrationClient.NONCE_HEADER);
        request.contentHash = exchange.getRequestHeaders().getFirst(ViewerIntegrationClient.CONTENT_HASH_HEADER);
        request.encodedMetadata = exchange.getRequestHeaders().getFirst(ViewerIntegrationClient.METADATA_HEADER);
        request.signature = exchange.getRequestHeaders().getFirst(ViewerIntegrationClient.SIGNATURE_HEADER);
        target.set(request);

        byte[] response = objectMapper.writeValueAsBytes(Map.of(
                "launchToken", "opaque-launch-token",
                "expiresAt", "2026-07-31T12:05:00Z",
                "correlationId", correlationId));
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private ViewerIntegrationProperties configured(String baseUrl) {
        ViewerIntegrationProperties properties = new ViewerIntegrationProperties();
        properties.setEnabled(true);
        properties.setBaseUrl(baseUrl);
        properties.setClientId("tdms-demo");
        properties.setCallbackClientId("collabview");
        properties.setSharedSecret("0123456789abcdef0123456789abcdef");
        return properties;
    }

    private StepViewerIntegrationProperties configuredStep(String baseUrl) {
        StepViewerIntegrationProperties properties = new StepViewerIntegrationProperties();
        properties.setEnabled(true);
        properties.setBaseUrl(baseUrl);
        properties.setClientId("tdms-step-demo");
        properties.setCallbackClientId("collabview3d");
        properties.setSharedSecret("step-secret-0123456789abcdef0123456789");
        return properties;
    }

    private ViewerDocumentMetadata metadata(String correlationId) {
        ViewerDocumentMetadata metadata = new ViewerDocumentMetadata();
        metadata.setCorrelationId(correlationId);
        metadata.setObjectType("SW");
        metadata.setObjectId("OBJ-1");
        metadata.setAclObjectType("SW");
        metadata.setAclObjectId("OBJ-1");
        metadata.setFileNo("1");
        metadata.setFileName("sample.pdf");
        metadata.setUserCd("ADMIN");
        metadata.setUserId("admin");
        metadata.setUserName("Administrator");
        metadata.setAuthority("2");
        metadata.setRevision("A");
        metadata.setRequestNo("REQ-1");
        return metadata;
    }

    private static class CapturedRequest {
        String method;
        byte[] body;
        String contentType;
        String clientId;
        String timestamp;
        String nonce;
        String contentHash;
        String encodedMetadata;
        String signature;
    }
}

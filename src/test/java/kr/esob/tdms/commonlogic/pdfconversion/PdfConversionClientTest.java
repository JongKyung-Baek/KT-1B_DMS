package kr.esob.tdms.commonlogic.pdfconversion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class PdfConversionClientTest {
    private static final byte[] VALID_PDF =
            "%PDF-1.4\n1 0 obj\n<<>>\nendobj\n%%EOF\n"
                    .getBytes(StandardCharsets.US_ASCII);

    @TempDir
    Path tempDir;

    @Test
    void signsTheDocumentedCanonicalRequestAndStreamsThePdfResponse() throws Exception {
        byte[] sourceBytes = "office source bytes".getBytes(StandardCharsets.UTF_8);
        Path source = Files.write(tempDir.resolve("source.docx"), sourceBytes);
        Path target = tempDir.resolve("converted.pdf");
        AtomicReference<CapturedRequest> captured = new AtomicReference<CapturedRequest>();
        String idempotencyKey = "tdms:sha256:" + PdfConversionCrypto.sha256(source);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(PdfConversionProperties.CONVERT_PATH,
                exchange -> captureAndRespond(exchange, idempotencyKey, captured));
        server.start();
        try {
            PdfConversionProperties properties = configured(
                    "http://127.0.0.1:" + server.getAddress().getPort());
            PdfConversionClient client = new PdfConversionClient(properties);

            PdfConversionClientResult result = client.convert(
                    source, "source.docx", PdfConversionCrypto.sha256(source),
                    idempotencyKey, target);

            assertThat(result.isReused()).isTrue();
            assertThat(Files.readAllBytes(target)).isEqualTo(VALID_PDF);
            CapturedRequest request = captured.get();
            assertThat(request).isNotNull();
            assertThat(request.method).isEqualTo("POST");
            assertThat(request.clientId).isEqualTo(properties.getClientId());
            assertThat(request.contentHash).isEqualTo(PdfConversionCrypto.sha256(source));
            assertThat(request.idempotencyKey).isEqualTo(idempotencyKey);
            assertThat(request.contentType).startsWith("multipart/form-data; boundary=");
            assertThat(request.body).contains(sourceBytes);
            UUID.fromString(request.nonce);
            assertThat(Long.parseLong(request.timestamp)).isPositive();

            String canonical = "POST\n" + PdfConversionProperties.CONVERT_PATH + "\n"
                    + request.clientId + "\n" + request.timestamp + "\n"
                    + request.nonce + "\n" + request.contentHash + "\n"
                    + request.idempotencyKey;
            assertThat(request.signature).isEqualTo(PdfConversionCrypto.hmacSha256(
                    properties.getSharedSecret(), canonical));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void rejectsAResponseThatEchoesADifferentIdempotencyKey() throws Exception {
        Path source = Files.write(tempDir.resolve("mismatch.docx"),
                "source".getBytes(StandardCharsets.UTF_8));
        Path target = tempDir.resolve("mismatch.pdf");
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(PdfConversionProperties.CONVERT_PATH, exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.getResponseHeaders().set(
                    PdfConversionClient.IDEMPOTENCY_KEY_HEADER, "different-key");
            exchange.sendResponseHeaders(200, VALID_PDF.length);
            exchange.getResponseBody().write(VALID_PDF);
            exchange.close();
        });
        server.start();
        try {
            PdfConversionClient client = new PdfConversionClient(configured(
                    "http://127.0.0.1:" + server.getAddress().getPort()));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> client.convert(source, "mismatch.docx",
                            PdfConversionCrypto.sha256(source), "expected-key", target));

            assertThat(exception.getMessage()).contains("idempotency key does not match");
            assertThat(Files.exists(target)).isFalse();
        } finally {
            server.stop(0);
        }
    }

    private void captureAndRespond(HttpExchange exchange,
                                   String idempotencyKey,
                                   AtomicReference<CapturedRequest> target) throws java.io.IOException {
        CapturedRequest request = new CapturedRequest();
        request.method = exchange.getRequestMethod();
        request.body = exchange.getRequestBody().readAllBytes();
        request.contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        request.clientId = exchange.getRequestHeaders().getFirst(
                PdfConversionClient.CLIENT_ID_HEADER);
        request.timestamp = exchange.getRequestHeaders().getFirst(
                PdfConversionClient.TIMESTAMP_HEADER);
        request.nonce = exchange.getRequestHeaders().getFirst(
                PdfConversionClient.NONCE_HEADER);
        request.contentHash = exchange.getRequestHeaders().getFirst(
                PdfConversionClient.CONTENT_HASH_HEADER);
        request.idempotencyKey = exchange.getRequestHeaders().getFirst(
                PdfConversionClient.IDEMPOTENCY_KEY_HEADER);
        request.signature = exchange.getRequestHeaders().getFirst(
                PdfConversionClient.SIGNATURE_HEADER);
        target.set(request);

        exchange.getResponseHeaders().set("Content-Type", "application/pdf");
        exchange.getResponseHeaders().set(
                PdfConversionClient.IDEMPOTENCY_KEY_HEADER, idempotencyKey);
        exchange.getResponseHeaders().set(PdfConversionClient.REUSED_HEADER, "true");
        exchange.sendResponseHeaders(200, VALID_PDF.length);
        exchange.getResponseBody().write(VALID_PDF);
        exchange.close();
    }

    private PdfConversionProperties configured(String baseUrl) {
        PdfConversionProperties properties = new PdfConversionProperties();
        properties.setEnabled(true);
        properties.setBaseUrl(baseUrl);
        properties.setClientId("kt1b-tdms");
        properties.setSharedSecret("0123456789abcdef0123456789abcdef");
        properties.setConnectTimeoutMs(3000);
        properties.setReadTimeoutMs(3000);
        return properties;
    }

    private static final class CapturedRequest {
        String method;
        byte[] body;
        String contentType;
        String clientId;
        String timestamp;
        String nonce;
        String contentHash;
        String idempotencyKey;
        String signature;
    }
}

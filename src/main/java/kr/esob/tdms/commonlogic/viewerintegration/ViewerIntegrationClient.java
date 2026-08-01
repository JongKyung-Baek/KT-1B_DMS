package kr.esob.tdms.commonlogic.viewerintegration;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ViewerIntegrationClient {
    static final String CLIENT_ID_HEADER = "X-TDMS-Client-Id";
    static final String TIMESTAMP_HEADER = "X-TDMS-Timestamp";
    static final String NONCE_HEADER = "X-TDMS-Nonce";
    static final String CONTENT_HASH_HEADER = "X-TDMS-Content-SHA256";
    static final String METADATA_HEADER = "X-TDMS-Metadata";
    static final String SIGNATURE_HEADER = "X-TDMS-Signature";

    private final ViewerIntegrationProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Autowired
    public ViewerIntegrationClient(ViewerIntegrationProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, Clock.systemUTC());
    }

    ViewerIntegrationClient(ViewerIntegrationProperties properties, ObjectMapper objectMapper, Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public ViewerIngestResponse ingest(Path pdf, ViewerDocumentMetadata metadata) {
        properties.requireOutboundConfiguration();
        requirePdf(pdf);
        if (metadata == null) {
            throw new IllegalArgumentException("Viewer metadata is required.");
        }
        try {
            byte[] metadataBytes = objectMapper.writeValueAsBytes(metadata.toSignedMap());
            String contentHash = ViewerCrypto.sha256(pdf);
            String metadataHash = ViewerCrypto.sha256(metadataBytes);
            String encodedMetadata = Base64.getUrlEncoder().withoutPadding().encodeToString(metadataBytes);
            String timestamp = String.valueOf(Instant.now(clock).getEpochSecond());
            String nonce = UUID.randomUUID().toString();
            String clientId = properties.getClientId().trim();
            URI endpoint = properties.ingestUri();
            String canonical = "POST\n" + ViewerIntegrationProperties.INGEST_PATH + "\n"
                    + clientId + "\n" + timestamp + "\n" + nonce + "\n"
                    + contentHash + "\n" + metadataHash;
            String signature = ViewerCrypto.hmacSha256(properties.getSharedSecret(), canonical);

            RestTemplate restTemplate = new RestTemplate(requestFactory());
            ViewerIngestResponse response = restTemplate.execute(endpoint, HttpMethod.POST, request -> {
                HttpHeaders headers = request.getHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentLength(Files.size(pdf));
                headers.set(CLIENT_ID_HEADER, clientId);
                headers.set(TIMESTAMP_HEADER, timestamp);
                headers.set(NONCE_HEADER, nonce);
                headers.set(CONTENT_HASH_HEADER, contentHash);
                headers.set(METADATA_HEADER, encodedMetadata);
                headers.set(SIGNATURE_HEADER, signature);
                Files.copy(pdf, request.getBody());
            }, responseData -> objectMapper.readValue(responseData.getBody(), ViewerIngestResponse.class));
            validateResponse(response, metadata.getCorrelationId());
            return response;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to transfer the PDF to the viewer.", exception);
        }
    }

    private SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());
        factory.setBufferRequestBody(false);
        return factory;
    }

    private void requirePdf(Path pdf) {
        if (pdf == null || !Files.isRegularFile(pdf)) {
            throw new IllegalArgumentException("Viewer PDF is unavailable.");
        }
        try {
            byte[] signature = new byte[5];
            try (java.io.InputStream input = Files.newInputStream(pdf)) {
                if (input.read(signature) != signature.length
                        || signature[0] != '%' || signature[1] != 'P'
                        || signature[2] != 'D' || signature[3] != 'F' || signature[4] != '-') {
                    throw new IllegalArgumentException("Only a valid PDF can be sent to the viewer.");
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to inspect the viewer PDF.", exception);
        }
    }

    private void validateResponse(ViewerIngestResponse response, String expectedCorrelationId) {
        if (response == null || isBlank(response.getLaunchToken())
                || isBlank(response.getExpiresAt()) || isBlank(response.getCorrelationId())) {
            throw new IllegalStateException("Viewer ingest response is incomplete.");
        }
        if (!response.getCorrelationId().equals(expectedCorrelationId)) {
            throw new IllegalStateException("Viewer correlation identifier does not match the request.");
        }
        try {
            Instant.parse(response.getExpiresAt());
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Viewer expiry timestamp is invalid.", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

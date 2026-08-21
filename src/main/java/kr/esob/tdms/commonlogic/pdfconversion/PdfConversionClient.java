package kr.esob.tdms.commonlogic.pdfconversion;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class PdfConversionClient {
    static final String CLIENT_ID_HEADER = "X-TDMS-Client-Id";
    static final String TIMESTAMP_HEADER = "X-TDMS-Timestamp";
    static final String NONCE_HEADER = "X-TDMS-Nonce";
    static final String CONTENT_HASH_HEADER = "X-TDMS-Content-SHA256";
    static final String IDEMPOTENCY_KEY_HEADER = "X-TDMS-Idempotency-Key";
    static final String SIGNATURE_HEADER = "X-TDMS-Signature";
    static final String REUSED_HEADER = "X-TDMS-Conversion-Reused";

    private final PdfConversionProperties properties;

    public PdfConversionClient(PdfConversionProperties properties) {
        this.properties = properties;
    }

    public PdfConversionClientResult convert(Path source,
                                             String originalFileName,
                                             String sourceSha256,
                                             String idempotencyKey,
                                             Path targetPdf) {
        properties.requireOutboundConfiguration();
        if (source == null || !Files.isRegularFile(source)) {
            throw new IllegalArgumentException("PDF conversion source is unavailable.");
        }
        String actualHash = PdfConversionCrypto.sha256(source);
        if (!actualHash.equalsIgnoreCase(sourceSha256)) {
            throw new IllegalStateException("PDF conversion source hash changed.");
        }

        HttpURLConnection connection = null;
        try {
            URI endpoint = properties.convertUri();
            String clientId = properties.getClientId().trim();
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            String nonce = UUID.randomUUID().toString();
            String canonical = "POST\n" + PdfConversionProperties.CONVERT_PATH + "\n"
                    + clientId + "\n" + timestamp + "\n" + nonce + "\n"
                    + actualHash + "\n" + idempotencyKey;
            String signature = PdfConversionCrypto.hmacSha256(
                    properties.getSharedSecret(), canonical);
            String boundary = "----TDMS-PDF-" + UUID.randomUUID().toString().replace("-", "");

            connection = (HttpURLConnection) endpoint.toURL().openConnection();
            connection.setConnectTimeout(properties.getConnectTimeoutMs());
            connection.setReadTimeout(properties.getReadTimeoutMs());
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(8192);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty(CLIENT_ID_HEADER, clientId);
            connection.setRequestProperty(TIMESTAMP_HEADER, timestamp);
            connection.setRequestProperty(NONCE_HEADER, nonce);
            connection.setRequestProperty(CONTENT_HASH_HEADER, actualHash);
            connection.setRequestProperty(IDEMPOTENCY_KEY_HEADER, idempotencyKey);
            connection.setRequestProperty(SIGNATURE_HEADER, signature);

            try (OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
                 InputStream input = new BufferedInputStream(Files.newInputStream(source))) {
                writeAscii(raw, "--" + boundary + "\r\n");
                writeAscii(raw, "Content-Disposition: form-data; name=\"file\"; filename=\""
                        + safeFileName(originalFileName) + "\"\r\n");
                writeAscii(raw, "Content-Type: application/octet-stream\r\n\r\n");
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    raw.write(buffer, 0, read);
                }
                writeAscii(raw, "\r\n--" + boundary + "--\r\n");
            }

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                String error = readError(connection);
                throw new IllegalStateException("PDF converter rejected the request (HTTP "
                        + status + "): " + error);
            }
            String responseKey = connection.getHeaderField(IDEMPOTENCY_KEY_HEADER);
            if (responseKey != null && !responseKey.isEmpty() && !idempotencyKey.equals(responseKey)) {
                throw new IllegalStateException("PDF converter idempotency key does not match.");
            }
            Path parent = targetPdf.toAbsolutePath().normalize().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (InputStream input = new BufferedInputStream(connection.getInputStream());
                 OutputStream output = new BufferedOutputStream(Files.newOutputStream(targetPdf))) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }
            PdfConversionFiles.requirePdf(targetPdf);
            return new PdfConversionClientResult(Boolean.parseBoolean(
                    connection.getHeaderField(REUSED_HEADER)));
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("PDF conversion request failed.", exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String safeFileName(String value) {
        String result = value == null ? "source" : value.trim();
        result = result.replace('\\', '_').replace('/', '_').replace('"', '_');
        result = result.replaceAll("[\\r\\n\\p{Cntrl}]", "_");
        return result.isEmpty() ? "source" : result;
    }

    private static void writeAscii(OutputStream output, String value) throws Exception {
        output.write(value.getBytes(StandardCharsets.US_ASCII));
    }

    private static String readError(HttpURLConnection connection) {
        try {
            InputStream stream = connection.getErrorStream();
            if (stream == null) {
                return "no response body";
            }
            byte[] buffer = new byte[4096];
            int read = stream.read(buffer);
            return read <= 0 ? "no response body"
                    : new String(buffer, 0, read, StandardCharsets.UTF_8).trim();
        } catch (Exception ignored) {
            return "unreadable response body";
        }
    }
}

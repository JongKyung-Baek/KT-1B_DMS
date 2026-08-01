package kr.esob.tdms.controller.general.distribution.accountrequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DistributionAccountIntegrationController {
    private static final int MAX_BODY_BYTES = 32 * 1024;
    private final DistributionAccountIntegrationService service;

    public DistributionAccountIntegrationController(DistributionAccountIntegrationService service) {
        this.service = service;
    }

    @PostMapping(value = DistributionAccountIntegrationProperties.REQUEST_PATH,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DistributionAccountRequestRecord> receive(
            HttpServletRequest request,
            @RequestHeader(value = "X-DIST-Client-Id", required = false) String clientId,
            @RequestHeader(value = "X-DIST-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-DIST-Nonce", required = false) String nonce,
            @RequestHeader(value = "X-DIST-Content-SHA256", required = false) String contentHash,
            @RequestHeader(value = "X-DIST-Signature", required = false) String signature) {
        DistributionAccountRequestRecord result = service.receive(readBounded(request),
            clientId, timestamp, nonce, contentHash, signature);
        HttpStatus status = Boolean.TRUE.equals(result.getDuplicate())
            ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(result);
    }

    @GetMapping(value = DistributionAccountIntegrationProperties.REQUEST_PATH + "/{eventId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public DistributionAccountRequestRecord status(
            @PathVariable String eventId,
            @RequestHeader(value = "X-DIST-Client-Id", required = false) String clientId,
            @RequestHeader(value = "X-DIST-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-DIST-Nonce", required = false) String nonce,
            @RequestHeader(value = "X-DIST-Content-SHA256", required = false) String contentHash,
            @RequestHeader(value = "X-DIST-Signature", required = false) String signature) {
        return service.status(eventId, clientId, timestamp, nonce, contentHash, signature);
    }

    @ExceptionHandler(DistributionAccountRequestException.class)
    public ResponseEntity<Map<String, Object>> accountRequestError(
            DistributionAccountRequestException exception) {
        return ResponseEntity.status(exception.getStatus())
            .body(error(exception.getCode(), exception.getMessage()));
    }

    private byte[] readBounded(HttpServletRequest request) {
        long length = request.getContentLengthLong();
        if (length > MAX_BODY_BYTES) throw DistributionAccountRequestException.payloadTooLarge();
        try (InputStream input = request.getInputStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream(
                 length > 0 ? (int) length : 1024)) {
            byte[] buffer = new byte[4096];
            int total = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > MAX_BODY_BYTES) {
                    throw DistributionAccountRequestException.payloadTooLarge();
                }
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        } catch (DistributionAccountRequestException exception) {
            throw exception;
        } catch (IOException exception) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_REQUEST", "Unable to read request body.");
        }
    }

    private Map<String, Object> error(String code, String message) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("success", Boolean.FALSE);
        response.put("code", code);
        response.put("message", message);
        return response;
    }
}

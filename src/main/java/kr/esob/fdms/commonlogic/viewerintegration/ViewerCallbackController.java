package kr.esob.fdms.commonlogic.viewerintegration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ViewerCallbackController {
    private static final int MAX_CALLBACK_BYTES = 16 * 1024;
    private final ViewerIntegrationService service;

    public ViewerCallbackController(ViewerIntegrationService service) {
        this.service = service;
    }

    @PostMapping(value = ViewerIntegrationProperties.CALLBACK_PATH,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receive(
            HttpServletRequest request,
            @RequestHeader(value = "X-CV-Client-Id", required = false) String clientId,
            @RequestHeader(value = "X-CV-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-CV-Nonce", required = false) String nonce,
            @RequestHeader(value = "X-CV-Content-SHA256", required = false) String contentHash,
            @RequestHeader(value = "X-CV-Signature", required = false) String signature) {
        byte[] body = readBounded(request);
        service.acceptCallback(body, clientId, timestamp, nonce, contentHash, signature);
    }

    private byte[] readBounded(HttpServletRequest request) {
        long contentLength = request.getContentLengthLong();
        if (contentLength > MAX_CALLBACK_BYTES) {
            throw new ViewerCallbackPayloadTooLargeException();
        }
        try (InputStream input = request.getInputStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream(
                     contentLength > 0 ? (int) contentLength : 1024)) {
            byte[] buffer = new byte[4096];
            int total = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > MAX_CALLBACK_BYTES) {
                    throw new ViewerCallbackPayloadTooLargeException();
                }
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        } catch (ViewerCallbackPayloadTooLargeException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ViewerCallbackValidationException("Unable to read viewer callback body.", exception);
        }
    }
}

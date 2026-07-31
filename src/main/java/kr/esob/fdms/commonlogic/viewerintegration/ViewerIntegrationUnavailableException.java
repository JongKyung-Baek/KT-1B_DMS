package kr.esob.fdms.commonlogic.viewerintegration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ViewerIntegrationUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ViewerIntegrationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

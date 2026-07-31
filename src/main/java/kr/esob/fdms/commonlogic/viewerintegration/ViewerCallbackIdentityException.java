package kr.esob.fdms.commonlogic.viewerintegration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ViewerCallbackIdentityException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ViewerCallbackIdentityException(String message) {
        super(message);
    }
}

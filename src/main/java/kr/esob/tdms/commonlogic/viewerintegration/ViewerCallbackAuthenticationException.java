package kr.esob.tdms.commonlogic.viewerintegration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ViewerCallbackAuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ViewerCallbackAuthenticationException(String message) {
        super(message);
    }
}

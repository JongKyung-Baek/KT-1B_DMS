package kr.esob.tdms.commonlogic.viewerintegration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ViewerCallbackValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ViewerCallbackValidationException(String message) {
        super(message);
    }

    public ViewerCallbackValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

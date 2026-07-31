package kr.esob.fdms.controller.inside.distribution.workflow;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class DistributionWorkflowException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final String code;

    public DistributionWorkflowException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    static DistributionWorkflowException badRequest(String code, String message) {
        return new DistributionWorkflowException(HttpStatus.BAD_REQUEST, code, message);
    }

    static DistributionWorkflowException forbidden(String code, String message) {
        return new DistributionWorkflowException(HttpStatus.FORBIDDEN, code, message);
    }

    static DistributionWorkflowException notFound() {
        return new DistributionWorkflowException(
            HttpStatus.NOT_FOUND, "DISTRIBUTION_REQUEST_NOT_FOUND", "Distribution request was not found.");
    }

    static DistributionWorkflowException conflict(String code, String message) {
        return new DistributionWorkflowException(HttpStatus.CONFLICT, code, message);
    }
}

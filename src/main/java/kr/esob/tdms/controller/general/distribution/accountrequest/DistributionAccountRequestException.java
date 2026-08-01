package kr.esob.tdms.controller.general.distribution.accountrequest;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class DistributionAccountRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final String code;

    DistributionAccountRequestException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    static DistributionAccountRequestException badRequest(String code, String message) {
        return new DistributionAccountRequestException(HttpStatus.BAD_REQUEST, code, message);
    }

    static DistributionAccountRequestException unauthorized(String message) {
        return new DistributionAccountRequestException(
            HttpStatus.UNAUTHORIZED, "DISTRIBUTION_INTEGRATION_AUTHENTICATION_FAILED", message);
    }

    static DistributionAccountRequestException forbidden(String message) {
        return new DistributionAccountRequestException(
            HttpStatus.FORBIDDEN, "DISTRIBUTION_ACCOUNT_REQUEST_ACCESS_DENIED", message);
    }

    static DistributionAccountRequestException notFound() {
        return new DistributionAccountRequestException(
            HttpStatus.NOT_FOUND, "DISTRIBUTION_ACCOUNT_REQUEST_NOT_FOUND",
            "Distribution-system account request was not found.");
    }

    static DistributionAccountRequestException conflict(String code, String message) {
        return new DistributionAccountRequestException(HttpStatus.CONFLICT, code, message);
    }

    static DistributionAccountRequestException payloadTooLarge() {
        return new DistributionAccountRequestException(
            HttpStatus.PAYLOAD_TOO_LARGE, "DISTRIBUTION_ACCOUNT_REQUEST_TOO_LARGE",
            "Distribution-system account request body is too large.");
    }

    static DistributionAccountRequestException unavailable(String message) {
        return new DistributionAccountRequestException(
            HttpStatus.SERVICE_UNAVAILABLE, "DISTRIBUTION_INTEGRATION_UNAVAILABLE", message);
    }
}

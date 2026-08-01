package kr.esob.tdms.controller.general.organizationmanage.partner;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class PartnerManagementException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final String code;

    PartnerManagementException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    static PartnerManagementException badRequest(String code, String message) {
        return new PartnerManagementException(HttpStatus.BAD_REQUEST, code, message);
    }

    static PartnerManagementException notFound() {
        return new PartnerManagementException(HttpStatus.NOT_FOUND,
            "PARTNER_COMPANY_NOT_FOUND", "The partner company was not found.");
    }

    static PartnerManagementException conflict(String code, String message) {
        return new PartnerManagementException(HttpStatus.CONFLICT, code, message);
    }
}

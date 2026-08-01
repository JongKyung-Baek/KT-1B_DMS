package kr.esob.tdms.controller.general.organizationmanage.partner;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/general/organizationmanage/partner/api")
public class PartnerManagementController {
    private final PartnerManagementService service;
    private final PartnerDirectoryService directoryService;

    public PartnerManagementController(PartnerManagementService service,
            PartnerDirectoryService directoryService) {
        this.service = service;
        this.directoryService = directoryService;
    }

    @GetMapping("/companies")
    public List<PartnerCompany> companies(
            @RequestParam(required = false) String keyword) {
        return service.list(keyword);
    }

    @GetMapping("/companies/{partnerCompanyId}")
    public PartnerCompany company(@PathVariable long partnerCompanyId) {
        return service.detail(partnerCompanyId);
    }

    @PostMapping("/companies")
    public ResponseEntity<PartnerCompany> create(
            @RequestBody(required = false) PartnerCompany company) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(company));
    }

    @PutMapping("/companies/{partnerCompanyId}")
    public PartnerCompany update(@PathVariable long partnerCompanyId,
            @RequestBody(required = false) PartnerCompany company) {
        return service.update(partnerCompanyId, company);
    }

    @DeleteMapping("/companies/{partnerCompanyId}")
    public ResponseEntity<Void> delete(@PathVariable long partnerCompanyId) {
        service.delete(partnerCompanyId);
        return ResponseEntity.noContent().build();
    }

    /** Management-screen preview; workflow code should inject the service. */
    @GetMapping("/recipients")
    public List<PartnerRecipient> recipients(
            @RequestParam(required = false) Long partnerCompanyId) {
        return partnerCompanyId == null
            ? directoryService.listActiveRecipients()
            : directoryService.listActiveRecipients(partnerCompanyId.longValue());
    }

    @ExceptionHandler(PartnerManagementException.class)
    public ResponseEntity<Map<String, Object>> partnerError(PartnerManagementException exception) {
        return ResponseEntity.status(exception.getStatus())
            .body(error(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> invalidJson(HttpMessageNotReadableException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(error("INVALID_PARTNER_REQUEST", "The request format is invalid."));
    }

    private Map<String, Object> error(String code, String message) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("success", false);
        response.put("code", code);
        response.put("message", message);
        return response;
    }
}

package kr.esob.tdms.controller.general.distribution.accountrequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequestMapping("/general/distribution/account-requests/api")
public class DistributionAccountRequestAdminController {
    private final DistributionAccountRequestAdminService service;

    public DistributionAccountRequestAdminController(DistributionAccountRequestAdminService service) {
        this.service = service;
    }

    @GetMapping("/requests")
    public List<DistributionAccountRequestRecord> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String requestType,
            @RequestParam(required = false) String sourceSystemId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        return service.list(status, requestType, sourceSystemId, keyword, limit, offset);
    }

    @GetMapping("/requests/{requestId}")
    public DistributionAccountRequestRecord detail(@PathVariable long requestId) {
        return service.detail(requestId);
    }

    @PostMapping("/requests/{requestId}/approve")
    public DistributionAccountRequestRecord approve(@PathVariable long requestId,
            @RequestBody(required = false) DistributionAccountDecisionRequest request) {
        return service.approve(requestId, request);
    }

    @PostMapping("/requests/{requestId}/reject")
    public DistributionAccountRequestRecord reject(@PathVariable long requestId,
            @RequestBody(required = false) DistributionAccountDecisionRequest request) {
        return service.reject(requestId, request);
    }

    @ExceptionHandler(DistributionAccountRequestException.class)
    public ResponseEntity<Map<String, Object>> accountRequestError(
            DistributionAccountRequestException exception) {
        return ResponseEntity.status(exception.getStatus())
            .body(error(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> denied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(error("DISTRIBUTION_ACCOUNT_REQUEST_ACCESS_DENIED", exception.getMessage()));
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        ServletRequestBindingException.class
    })
    public ResponseEntity<Map<String, Object>> invalidRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(error("INVALID_DISTRIBUTION_ACCOUNT_API_REQUEST",
                "The request format is invalid."));
    }

    private Map<String, Object> error(String code, String message) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("success", Boolean.FALSE);
        response.put("code", code);
        response.put("message", message);
        return response;
    }
}

package kr.esob.fdms.controller.inside.distribution.workflow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
@RequestMapping("/inside/distribution/workflow/api")
public class DistributionWorkflowController {
    private final DistributionWorkflowService service;

    public DistributionWorkflowController(DistributionWorkflowService service) {
        this.service = service;
    }

    @PostMapping("/requests")
    public ResponseEntity<DistributionRequestDetail> create(
            @RequestBody(required = false) DistributionRequestSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/requests/{requestId}")
    public DistributionRequestDetail update(
            @PathVariable long requestId,
            @RequestBody(required = false) DistributionRequestSaveRequest request) {
        return service.update(requestId, request);
    }

    @PostMapping("/requests/{requestId}/submit")
    public DistributionRequestDetail submit(@PathVariable long requestId) {
        return service.submit(requestId);
    }

    @PostMapping("/requests/{requestId}/approve")
    public DistributionRequestDetail approve(
            @PathVariable long requestId,
            @RequestBody(required = false) DistributionDecisionRequest request) {
        return service.approve(requestId, request);
    }

    @PostMapping("/requests/{requestId}/reject")
    public DistributionRequestDetail reject(
            @PathVariable long requestId,
            @RequestBody(required = false) DistributionDecisionRequest request) {
        return service.reject(requestId, request);
    }

    @PostMapping("/requests/{requestId}/cancel")
    public DistributionRequestDetail cancel(@PathVariable long requestId) {
        return service.cancel(requestId);
    }

    @GetMapping("/requests/{requestId}")
    public DistributionRequestDetail detail(@PathVariable long requestId) {
        return service.detail(requestId);
    }

    @GetMapping("/requests")
    public List<DistributionRequestRecord> mine(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        return service.mine(status, limit, offset);
    }

    @GetMapping("/approval-queue")
    public List<DistributionRequestRecord> approvalQueue(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        return service.approvalQueue(limit, offset);
    }

    @GetMapping("/approved")
    public List<DistributionRequestRecord> approved(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        return service.approved(limit, offset);
    }

    @ExceptionHandler(DistributionWorkflowException.class)
    public ResponseEntity<Map<String, Object>> workflowError(DistributionWorkflowException exception) {
        return ResponseEntity.status(exception.getStatus())
            .body(error(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> denied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(error("DISTRIBUTION_ACCESS_DENIED", exception.getMessage()));
    }

    private Map<String, Object> error(String code, String message) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("success", false);
        response.put("code", code);
        response.put("message", message);
        return response;
    }
}

package kr.esob.tdms.controller.general.distribution.accountrequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.commonlogic.securityacl.SecurityAuditWriter;
import kr.esob.tdms.commonlogic.value.Constant;
import kr.esob.tdms.controller.login.UserVO;

@Service
public class DistributionAccountRequestAdminService {
    private static final int MAX_PAGE_SIZE = 100;

    private final DistributionAccountRequestDao dao;
    private final SecurityAclService aclService;
    private final SecurityAuditWriter auditWriter;
    private final ObjectMapper objectMapper;

    public DistributionAccountRequestAdminService(
            DistributionAccountRequestDao dao,
            SecurityAclService aclService,
            SecurityAuditWriter auditWriter,
            ObjectMapper objectMapper) {
        this.dao = dao;
        this.aclService = aclService;
        this.auditWriter = auditWriter;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<DistributionAccountRequestRecord> list(
            String status, String requestType, String sourceSystemId,
            String keyword, Integer limit, Integer offset) {
        requireAdministrator();
        return dao.selectRequests(normalizeStatus(status), normalizeType(requestType),
            optional(sourceSystemId, 100, "sourceSystemId"),
            optional(keyword, 200, "keyword"), normalizeLimit(limit), normalizeOffset(offset));
    }

    @Transactional(readOnly = true)
    public DistributionAccountRequestRecord detail(long requestId) {
        requireAdministrator();
        return loadDetail(requestId);
    }

    @Transactional(rollbackFor = Exception.class)
    public DistributionAccountRequestRecord approve(
            long requestId, DistributionAccountDecisionRequest decision) {
        return decide(requestId, decision, DistributionAccountRequestStatus.APPROVED, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public DistributionAccountRequestRecord reject(
            long requestId, DistributionAccountDecisionRequest decision) {
        return decide(requestId, decision, DistributionAccountRequestStatus.REJECTED, true);
    }

    private DistributionAccountRequestRecord decide(long requestId,
            DistributionAccountDecisionRequest decision,
            DistributionAccountRequestStatus targetStatus,
            boolean commentRequired) {
        UserVO actor = requireAdministrator();
        DistributionAccountRequestRecord current = requireLocked(requestId);
        String comment = decision == null ? "" : trim(decision.getDecisionComment());

        // A browser may retry after losing the first response. The row lock makes
        // that retry deterministic: only the same administrator repeating the
        // same decision and normalized comment receives the existing result.
        // Every changed or opposite decision remains a conflict.
        if (targetStatus.name().equals(current.getStatus())) {
            if (sameDecision(current, actor, comment)) {
                return loadDetail(requestId);
            }
            throw invalidTransition();
        }
        if (!DistributionAccountRequestStatus.PENDING.name().equals(current.getStatus())) {
            throw invalidTransition();
        }
        if (commentRequired && comment.isEmpty()) {
            throw DistributionAccountRequestException.badRequest(
                "DISTRIBUTION_ACCOUNT_REJECTION_COMMENT_REQUIRED",
                "A rejection comment is required.");
        }
        if (comment.length() > 1000) {
            throw DistributionAccountRequestException.badRequest(
                "DISTRIBUTION_ACCOUNT_DECISION_COMMENT_TOO_LONG",
                "Decision comment must be 1000 characters or fewer.");
        }

        requireOne(dao.decide(requestId, DistributionAccountRequestStatus.PENDING.name(),
            targetStatus.name(), comment, actor), "decide distribution-system account request");
        requireOne(dao.insertDecisionEvent(requestId, targetStatus.name(),
            DistributionAccountRequestStatus.PENDING.name(), targetStatus.name(), comment, actor),
            "record distribution-system account request decision");
        auditWriter.writeInCurrentTransaction(actor, "ACCOUNT_REQUEST",
            targetStatus == DistributionAccountRequestStatus.APPROVED
                ? "APPROVE_ACCOUNT_REQUEST" : "REJECT_ACCOUNT_REQUEST",
            "SUCCESS", null, comment,
            "DISTRIBUTION_ACCOUNT_REQUEST", Long.toString(requestId), null,
            current.getCorrelationId(), null, auditDetail(current, targetStatus));
        return loadDetail(requestId);
    }

    private boolean sameDecision(DistributionAccountRequestRecord current,
            UserVO actor, String comment) {
        return trim(current.getDecidedByUserCd()).equals(trim(actor.getUserCd()))
            && trim(current.getDecisionComment()).equals(comment);
    }

    private DistributionAccountRequestException invalidTransition() {
        return DistributionAccountRequestException.conflict(
            "INVALID_DISTRIBUTION_ACCOUNT_STATUS_TRANSITION",
            "Only a pending account request may be decided.");
    }

    private DistributionAccountRequestRecord loadDetail(long requestId) {
        if (requestId <= 0) throw DistributionAccountRequestException.notFound();
        DistributionAccountRequestRecord request = dao.selectRequest(requestId);
        if (request == null) throw DistributionAccountRequestException.notFound();
        request.setEvents(dao.selectEvents(requestId));
        return request;
    }

    private DistributionAccountRequestRecord requireLocked(long requestId) {
        if (requestId <= 0) throw DistributionAccountRequestException.notFound();
        DistributionAccountRequestRecord request = dao.selectRequestForUpdate(requestId);
        if (request == null) throw DistributionAccountRequestException.notFound();
        return request;
    }

    private UserVO requireAdministrator() {
        UserVO actor = aclService.requireCurrentUser();
        if (!Constant.GROUP_CD_ADMIN.equals(actor.getRoleGroup())) {
            throw DistributionAccountRequestException.forbidden(
                "Administrator permission is required.");
        }
        return actor;
    }

    private String normalizeStatus(String value) {
        String normalized = trim(value).toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) return "";
        try {
            return DistributionAccountRequestStatus.valueOf(normalized).name();
        } catch (IllegalArgumentException exception) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_STATUS", "Unknown account request status.");
        }
    }

    private String normalizeType(String value) {
        String normalized = trim(value).toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) return "";
        try {
            return DistributionAccountRequestType.valueOf(normalized).name();
        } catch (IllegalArgumentException exception) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_REQUEST_TYPE", "Unknown account request type.");
        }
    }

    private String optional(String value, int maxLength, String field) {
        String normalized = trim(value);
        if (normalized.length() > maxLength) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_FILTER", field + " is too long.");
        }
        return normalized;
    }

    private int normalizeLimit(Integer value) {
        if (value == null) return 50;
        if (value.intValue() < 1 || value.intValue() > MAX_PAGE_SIZE) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_PAGE_LIMIT", "limit must be between 1 and 100.");
        }
        return value.intValue();
    }

    private int normalizeOffset(Integer value) {
        if (value == null) return 0;
        if (value.intValue() < 0) {
            throw DistributionAccountRequestException.badRequest(
                "INVALID_DISTRIBUTION_ACCOUNT_PAGE_OFFSET", "offset cannot be negative.");
        }
        return value.intValue();
    }

    private String auditDetail(DistributionAccountRequestRecord request,
            DistributionAccountRequestStatus status) {
        Map<String, Object> detail = new LinkedHashMap<String, Object>();
        detail.put("sourceSystemId", request.getSourceSystemId());
        detail.put("requestType", request.getRequestType());
        detail.put("targetUserId", request.getTargetUserId());
        detail.put("decisionStatus", status.name());
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize account-request audit detail.", exception);
        }
    }

    private void requireOne(int affectedRows, String operation) {
        if (affectedRows != 1) throw new IllegalStateException("Unable to " + operation + '.');
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }
}

package kr.esob.fdms.commonlogic.securityacl;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import kr.esob.fdms.controller.login.UserVO;

/**
 * Persists security evidence independently from the business transaction that
 * may subsequently be rolled back by an access-denied exception.
 */
@Service
public class SecurityAuditWriter {
    private final SecurityAclDao dao;

    public SecurityAuditWriter(SecurityAclDao dao) {
        this.dao = dao;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(UserVO actor, String eventType, String actionType, String resultCd,
                      String reasonCd, String message, String objectType, String objectId,
                      String fileNo, String requestNo, String gradeCd, String detailJson) {
        persist(actor, eventType, actionType, resultCd, reasonCd, message, objectType, objectId,
            fileNo, requestNo, gradeCd, detailJson);
    }

    /**
     * Business success evidence must commit or roll back with the business
     * transaction that makes the claimed result true.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void writeInCurrentTransaction(UserVO actor, String eventType, String actionType, String resultCd,
                                          String reasonCd, String message, String objectType, String objectId,
                                          String fileNo, String requestNo, String gradeCd, String detailJson) {
        persist(actor, eventType, actionType, resultCd, reasonCd, message, objectType, objectId,
            fileNo, requestNo, gradeCd, detailJson);
    }

    private void persist(UserVO actor, String eventType, String actionType, String resultCd,
                         String reasonCd, String message, String objectType, String objectId,
                         String fileNo, String requestNo, String gradeCd, String detailJson) {
        AccessAuditEventVO event = new AccessAuditEventVO();
        event.setEventType(limit(eventType, 40));
        event.setActionType(limit(actionType, 30));
        event.setResultCd(limit(resultCd, 20));
        event.setReasonCd(limit(reasonCd, 50));
        event.setResultMessage(limit(message, 1000));
        event.setActorUserCd(limit(actor == null ? null : actor.getUserCd(), 20));
        event.setActorUserId(limit(actor == null ? null : actor.getUserId(), 20));
        event.setActorUserNm(limit(actor == null ? null : actor.getUserNm(), 256));
        event.setObjectType(limit(objectType, 30));
        event.setObjectId(limit(objectId, 60));
        event.setFileNo(limit(fileNo, 60));
        event.setRequestNo(limit(requestNo, 100));
        event.setGradeCd(limit(gradeCd, 30));
        event.setDetailJson(isBlank(detailJson) ? "{}" : detailJson);

        HttpServletRequest request = currentHttpRequest();
        if (request != null) {
            event.setClientIp(limit(resolveClientIp(request), 64));
            event.setSessionId(limit(request.getSession(false) == null
                    ? null : request.getSession(false).getId(), 128));
            String correlationId = trim(request.getHeader("X-Correlation-ID"));
            event.setCorrelationId(limit(isBlank(correlationId)
                    ? UUID.randomUUID().toString() : correlationId, 128));
        } else {
            event.setCorrelationId(UUID.randomUUID().toString());
        }
        if (dao.insertAudit(event) != 1) {
            throw new IllegalStateException("Security audit event was not persisted.");
        }
    }

    private HttpServletRequest currentHttpRequest() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = trim(request.getHeader("X-Forwarded-For"));
        if (!isBlank(forwarded)) {
            int comma = forwarded.indexOf(',');
            return comma < 0 ? forwarded : forwarded.substring(0, comma).trim();
        }
        return request.getRemoteAddr();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

package kr.esob.fdms.commonlogic.securityacl;

import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import kr.esob.fdms.commonlogic.audit.AuditMenuContext;
import kr.esob.fdms.commonlogic.audit.AuditRequestSanitizer;
import kr.esob.fdms.controller.login.UserVO;

/**
 * Persists security evidence independently from the business transaction that
 * may subsequently be rolled back by an access-denied exception.
 */
@Service
public class SecurityAuditWriter {
    public static final String CORRELATION_ID_REQUEST_ATTRIBUTE =
            SecurityAuditWriter.class.getName() + ".correlationId";

    private final SecurityAclDao dao;

    public SecurityAuditWriter(SecurityAclDao dao) {
        this.dao = dao;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(UserVO actor, String eventType, String actionType, String resultCd,
                      String reasonCd, String message, String objectType, String objectId,
                      String fileNo, String requestNo, String gradeCd, String detailJson) {
        persist(actor, eventType, actionType, resolveActionName(actionType), resultCd, reasonCd, message,
                objectType, objectId, fileNo, requestNo, gradeCd, null, null, detailJson, null);
    }

    /**
     * Business success evidence must commit or roll back with the business
     * transaction that makes the claimed result true.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void writeInCurrentTransaction(UserVO actor, String eventType, String actionType, String resultCd,
                                          String reasonCd, String message, String objectType, String objectId,
                                          String fileNo, String requestNo, String gradeCd, String detailJson) {
        persist(actor, eventType, actionType, resolveActionName(actionType), resultCd, reasonCd, message,
                objectType, objectId, fileNo, requestNo, gradeCd, null, null, detailJson, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeMenuAction(UserVO actor, AuditMenuContext menu,
                                String actionType, String actionNm, String resultCd,
                                String reasonCd, String message, Integer httpStatus,
                                Long durationMs, String detailJson) {
        persist(actor, "MENU_ACTION", actionType, resolveActionName(actionType), resultCd, reasonCd, message,
                "MENU", menu == null ? null : menu.getMenuCd(),
                null, null, null, httpStatus, durationMs, detailJson, menu);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAuthentication(UserVO actor, String actionType, String actionNm,
                                    String resultCd, String reasonCd, String message,
                                    String fallbackClientIp, String fallbackSessionId) {
        writeAuthentication(actor, actionType, actionNm, resultCd, reasonCd, message,
                fallbackClientIp, fallbackSessionId, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAuthentication(UserVO actor, String actionType, String actionNm,
                                    String resultCd, String reasonCd, String message,
                                    String fallbackClientIp, String fallbackSessionId,
                                    String targetUserIdentifier) {
        AuditMenuContext authenticationArea =
                new AuditMenuContext("AUTH", "인증 / 계정", "/login/**", 0);
        String target = isBlank(targetUserIdentifier)
                ? actor == null ? null : actor.getUserId()
                : targetUserIdentifier;
        persist(actor, "AUTH", actionType, resolveActionName(actionType), resultCd, reasonCd, message,
                "USER_ACCOUNT", target, null, null, null,
                null, null, "{}", authenticationArea, fallbackClientIp, fallbackSessionId);
    }

    private void persist(UserVO actor, String eventType, String actionType, String actionNm,
                         String resultCd, String reasonCd, String message,
                         String objectType, String objectId, String fileNo,
                         String requestNo, String gradeCd, Integer httpStatus,
                         Long durationMs, String detailJson, AuditMenuContext menu) {
        persist(actor, eventType, actionType, actionNm, resultCd, reasonCd, message,
                objectType, objectId, fileNo, requestNo, gradeCd, httpStatus,
                durationMs, detailJson, menu, null, null);
    }

    private void persist(UserVO actor, String eventType, String actionType, String actionNm,
                         String resultCd, String reasonCd, String message,
                         String objectType, String objectId, String fileNo,
                         String requestNo, String gradeCd, Integer httpStatus,
                         Long durationMs, String detailJson, AuditMenuContext menu,
                         String fallbackClientIp, String fallbackSessionId) {
        AccessAuditEventVO event = new AccessAuditEventVO();
        event.setEventType(limit(eventType, 40));
        event.setActionType(limit(actionType, 30));
        event.setActionNm(limit(actionNm, 256));
        event.setResultCd(limit(resultCd, 20));
        event.setReasonCd(limit(reasonCd, 50));
        event.setResultMessage(limit(message, 1000));
        event.setActorUserCd(limit(actor == null ? null : actor.getUserCd(), 20));
        event.setActorUserId(limit(actor == null ? null : actor.getUserId(), 100));
        event.setActorUserNm(limit(actor == null ? null : actor.getUserNm(), 256));
        event.setObjectType(limit(objectType, 30));
        event.setObjectId(limit(objectId, 60));
        event.setFileNo(limit(fileNo, 60));
        event.setRequestNo(limit(requestNo, 100));
        event.setGradeCd(limit(gradeCd, 30));
        event.setHttpStatus(httpStatus);
        event.setDurationMs(durationMs);
        event.setDetailJson(isBlank(detailJson) ? "{}" : detailJson);
        enrichMenu(event, menu);

        HttpServletRequest request = currentHttpRequest();
        if (request != null) {
            enrichMenu(event, request);
            event.setRequestUri(limit(AuditRequestSanitizer.safeRequestUri(request), 1000));
            event.setHttpMethod(limit(request.getMethod(), 10));
            event.setClientIp(limit(resolveClientIp(request), 64));
            HttpSession session = request.getSession(false);
            event.setSessionId(limit(session == null ? fallbackSessionId : session.getId(), 128));
            event.setCorrelationId(resolveCorrelationId(request));
        } else {
            event.setClientIp(limit(fallbackClientIp, 64));
            event.setSessionId(limit(fallbackSessionId, 128));
            event.setCorrelationId(UUID.randomUUID().toString());
        }
        if (dao.insertAudit(event) != 1) {
            throw new IllegalStateException("Security audit event was not persisted.");
        }
    }

    private void enrichMenu(AccessAuditEventVO event, HttpServletRequest request) {
        if (!isBlank(event.getMenuCd())) {
            return;
        }
        Object context = request.getAttribute(AuditMenuContext.REQUEST_ATTRIBUTE);
        if (!(context instanceof AuditMenuContext)) {
            return;
        }
        enrichMenu(event, (AuditMenuContext) context);
    }

    private void enrichMenu(AccessAuditEventVO event, AuditMenuContext menu) {
        if (menu == null) {
            return;
        }
        event.setMenuCd(limit(menu.getMenuCd(), 64));
        event.setMenuNm(limit(menu.getMenuNm(), 256));
        event.setMenuUrl(limit(menu.getMenuUrl(), 512));
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        Object existing = request.getAttribute(CORRELATION_ID_REQUEST_ATTRIBUTE);
        String correlationId = existing == null ? null : trim(existing.toString());
        if (!isAllowedCorrelationId(correlationId)) {
            correlationId = trim(request.getHeader("X-Correlation-ID"));
        }
        if (!isAllowedCorrelationId(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }
        request.setAttribute(CORRELATION_ID_REQUEST_ATTRIBUTE, correlationId);
        return correlationId;
    }

    private boolean isAllowedCorrelationId(String correlationId) {
        if (isBlank(correlationId) || correlationId.length() > 128) {
            return false;
        }
        if (correlationId.toUpperCase(Locale.ROOT).startsWith("LEGACY-AUDIT-")) {
            return false;
        }
        return correlationId.matches("[A-Za-z0-9._:-]+");
    }

    private String resolveActionName(String actionType) {
        String normalized = trim(actionType).toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
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

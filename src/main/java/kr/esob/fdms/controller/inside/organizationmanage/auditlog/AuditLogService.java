package kr.esob.fdms.controller.inside.organizationmanage.auditlog;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuditLogService implements CommonService {

    private static final long BROWSER_LEAVE_LOGOUT_DELAY_MILLIS = 2000L;

    public static final String SESSION_AUDIT_USER_ID = "auditLogUserId";
    public static final String SESSION_AUDIT_USER_NAME = "auditLogUserName";
    public static final String SESSION_AUDIT_ACCESS_IP = "auditLogAccessIp";
    public static final String SESSION_AUDIT_LOGOUT_RECORDED = "auditLogLogoutRecorded";
    public static final String SESSION_AUDIT_LEAVE_PENDING = "auditLogLeavePending";

    private final ScheduledExecutorService browserLeaveExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "audit-log-browser-leave");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<String, ScheduledFuture<?>> browserLeaveTasks = new ConcurrentHashMap<>();

    @Inject
    AuditLogDao dao;

    @Inject
    RequestUtil requestUtil;

    @SuppressWarnings("rawtypes")
    @Override
    public List selectList(Object param) {
        try {
            return dao.selectList(param);
        } catch (Exception e) {
            log.warn("Audit log list query is not ready yet. Returning empty list.", e);
            return Collections.emptyList();
        }
    }

    @Override
    public int selectListCount(Object obj) {
        try {
            Integer count = dao.selectListCount(obj);
            return count == null ? 0 : count;
        } catch (Exception e) {
            log.warn("Audit log count query is not ready yet. Returning zero.", e);
            return 0;
        }
    }

    public void setSessionAuditInfo(HttpSession session, String userId, String userName, HttpServletRequest request) {
        if (session == null) {
            return;
        }

        cancelPendingBrowserLeaveTask(session);
        session.setAttribute(SESSION_AUDIT_USER_ID, normalizeBlank(userId));
        session.setAttribute(SESSION_AUDIT_USER_NAME, normalizeBlank(userName));
        session.setAttribute(SESSION_AUDIT_ACCESS_IP, normalizeBlank(requestUtil.getClientIp(request)));
        session.setAttribute(SESSION_AUDIT_LOGOUT_RECORDED, Boolean.FALSE);
        session.setAttribute(SESSION_AUDIT_LEAVE_PENDING, Boolean.FALSE);
    }

    public void markPendingLogoutOnLeave(HttpSession session, HttpServletRequest request) {
        if (session == null || hasLogoutRecorded(session)) {
            return;
        }

        String accessIp = normalizeBlank(request == null ? null : requestUtil.getClientIp(request));
        if (accessIp != null) {
            session.setAttribute(SESSION_AUDIT_ACCESS_IP, accessIp);
        }

        session.setAttribute(SESSION_AUDIT_LEAVE_PENDING, Boolean.TRUE);
        schedulePendingBrowserLeaveLogout(session);
    }

    public void clearPendingLogoutOnStay(HttpSession session) {
        if (session == null) {
            return;
        }

        session.setAttribute(SESSION_AUDIT_LEAVE_PENDING, Boolean.FALSE);
        cancelPendingBrowserLeaveTask(session);
    }

    public void clearPendingLogoutTask(HttpSession session) {
        cancelPendingBrowserLeaveTask(session);
    }

    public void insertLogoutAuditLogIfNeeded(HttpSession session, HttpServletRequest request) {
        insertLogoutAuditLogIfNeeded(session, request == null ? null : requestUtil.getClientIp(request));
    }

    public void insertLogoutAuditLogIfNeeded(HttpSession session, String accessIp) {
        if (session == null || hasLogoutRecorded(session)) {
            return;
        }

        String userId = normalizeBlank(toStringValue(session.getAttribute(SESSION_AUDIT_USER_ID)));
        String userName = normalizeBlank(toStringValue(session.getAttribute(SESSION_AUDIT_USER_NAME)));
        String sessionAccessIp = normalizeBlank(toStringValue(session.getAttribute(SESSION_AUDIT_ACCESS_IP)));
        String auditAccessIp = normalizeBlank(accessIp);

        if (auditAccessIp == null) {
            auditAccessIp = sessionAccessIp;
        }

        if (userId == null && userName == null && auditAccessIp == null) {
            return;
        }

        insertAuditLog("logOut", userId, userName, auditAccessIp);
        session.setAttribute(SESSION_AUDIT_LOGOUT_RECORDED, Boolean.TRUE);
        session.setAttribute(SESSION_AUDIT_LEAVE_PENDING, Boolean.FALSE);
        cancelPendingBrowserLeaveTask(session);
    }

    public void insertAuditLog(String actionType, String userId, String userName, HttpServletRequest request) {
        insertAuditLog(actionType, userId, userName, requestUtil.getClientIp(request));
    }

    public void insertAuditLog(String actionType, String userId, String userName, String accessIp) {
        AuditLogListParam param = new AuditLogListParam();
        param.setActionType(actionType);
        param.setUserId(normalizeBlank(userId));
        param.setUserName(normalizeBlank(userName));
        param.setAccessIp(normalizeBlank(accessIp));

        try {
            dao.insertAuditLog(param);
        } catch (Exception e) {
            log.warn("Failed to insert audit log. actionType={}, userId={}, userName={}", actionType, userId, userName, e);
        }
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasLogoutRecorded(HttpSession session) {
        Object recorded = session.getAttribute(SESSION_AUDIT_LOGOUT_RECORDED);
        return recorded instanceof Boolean && (Boolean) recorded;
    }

    private boolean isPendingBrowserLeave(HttpSession session) {
        Object pending = session.getAttribute(SESSION_AUDIT_LEAVE_PENDING);
        return pending instanceof Boolean && (Boolean) pending;
    }

    private void schedulePendingBrowserLeaveLogout(HttpSession session) {
        String sessionId = session.getId();
        cancelPendingBrowserLeaveTask(sessionId);

        ScheduledFuture<?> future = browserLeaveExecutor.schedule(() -> finalizePendingBrowserLeaveLogout(session),
                BROWSER_LEAVE_LOGOUT_DELAY_MILLIS, TimeUnit.MILLISECONDS);
        browserLeaveTasks.put(sessionId, future);
    }

    private void finalizePendingBrowserLeaveLogout(HttpSession session) {
        String sessionId;
        try {
            sessionId = session.getId();
            browserLeaveTasks.remove(sessionId);

            if (!isPendingBrowserLeave(session) || hasLogoutRecorded(session)) {
                return;
            }

            insertLogoutAuditLogIfNeeded(session, (String) null);
            session.invalidate();
        } catch (IllegalStateException e) {
            log.debug("Session already invalidated while finalizing pending browser leave logout.");
        }
    }

    private void cancelPendingBrowserLeaveTask(HttpSession session) {
        try {
            cancelPendingBrowserLeaveTask(session.getId());
        } catch (IllegalStateException e) {
            log.debug("Session already invalidated while canceling pending browser leave logout.");
        }
    }

    private void cancelPendingBrowserLeaveTask(String sessionId) {
        ScheduledFuture<?> future = browserLeaveTasks.remove(sessionId);
        if (future != null) {
            future.cancel(false);
        }
    }

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    @PreDestroy
    public void shutdownBrowserLeaveExecutor() {
        browserLeaveExecutor.shutdownNow();
    }
}

package kr.esob.fdms.controller.inside.organizationmanage.auditlog;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@Component
public class AuditLogSessionListener implements HttpSessionListener {

    @Inject
    AuditLogService auditLogService;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        auditLogService.clearPendingLogoutTask(session);
        auditLogService.insertLogoutAuditLogIfNeeded(session, (String) null);
    }
}

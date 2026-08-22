package kr.esob.tdms.controller.general.organizationmanage.auditlog;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpSessionEvent;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class AuditLogSessionListenerTest {

    @Test
    void destroyedSessionDelegatesToTheIdempotentLogoutAuditWriter() {
        AuditLogService service = mock(AuditLogService.class);
        AuditLogSessionListener listener = new AuditLogSessionListener();
        listener.auditLogService = service;
        MockHttpSession session = new MockHttpSession();

        listener.sessionDestroyed(new HttpSessionEvent(session));

        verify(service).insertLogoutAuditLogIfNeeded(session, (String) null);
    }
}

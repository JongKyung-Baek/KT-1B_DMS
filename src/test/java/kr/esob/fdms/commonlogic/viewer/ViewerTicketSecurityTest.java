package kr.esob.fdms.commonlogic.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import kr.esob.fdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.controller.login.UserVO;

class ViewerTicketSecurityTest {
    private static final String KEY = "0123456789abcdef0123456789abcdef";

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void validTicketIsConsumedExactlyOnce() {
        ViewerTicketDao dao = mock(ViewerTicketDao.class);
        SecurityAclService acl = mock(SecurityAclService.class);
        ViewerTicketService service = new ViewerTicketService(dao, acl);
        UserVO actor = actor();
        MockHttpServletRequest request = requestWithSession();

        when(acl.requireCurrentUser()).thenReturn(actor);
        when(dao.selectValid(KEY, actor.getUserCd(), request.getSession().getId()))
                .thenReturn(ticket(actor, request.getSession().getId()));
        when(dao.markUsed(KEY, actor.getUserCd(), request.getSession().getId())).thenReturn(1);

        assertEquals("DOC-1.pdf", service.resolve(KEY));
        verify(acl).requireAccess(any(FileAccessRequest.class));
        verify(dao).markUsed(KEY, actor.getUserCd(), request.getSession().getId());
    }

    @Test
    void concurrentReplayLosesTheAtomicConsume() {
        ViewerTicketDao dao = mock(ViewerTicketDao.class);
        SecurityAclService acl = mock(SecurityAclService.class);
        ViewerTicketService service = new ViewerTicketService(dao, acl);
        UserVO actor = actor();
        MockHttpServletRequest request = requestWithSession();

        when(acl.requireCurrentUser()).thenReturn(actor);
        when(dao.selectValid(KEY, actor.getUserCd(), request.getSession().getId()))
                .thenReturn(ticket(actor, request.getSession().getId()));
        when(dao.markUsed(KEY, actor.getUserCd(), request.getSession().getId())).thenReturn(0);

        assertThrows(AccessDeniedException.class, () -> service.resolve(KEY));
    }

    private MockHttpServletRequest requestWithSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        return request;
    }

    private UserVO actor() {
        UserVO actor = new UserVO();
        actor.setUserCd("USER-1");
        return actor;
    }

    private ViewerTicketVO ticket(UserVO actor, String sessionId) {
        ViewerTicketVO ticket = new ViewerTicketVO();
        ticket.setDisposableKey(KEY);
        ticket.setObjectType("DOCUMENT");
        ticket.setObjectId("DOC-1");
        ticket.setFileNo("FILE-1");
        ticket.setFileName("DOC-1.pdf");
        ticket.setUserCd(actor.getUserCd());
        ticket.setSessionId(sessionId);
        return ticket;
    }
}

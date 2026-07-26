package kr.esob.fdms.commonlogic.securityacl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import kr.esob.fdms.commonlogic.audit.AuditMenuContext;
import kr.esob.fdms.controller.login.UserVO;

class SecurityAuditWriterRequestContextTest {

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void reusesOneCorrelationIdAndEnrichesEveryEventFromTheCurrentRequest() {
        SecurityAclDao dao = mock(SecurityAclDao.class);
        when(dao.insertAudit(any(AccessAuditEventVO.class))).thenReturn(1);
        SecurityAuditWriter writer = new SecurityAuditWriter(dao);
        UserVO actor = actor();
        String ticket = "0123456789abcdef0123456789abcdef";
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/common/viewer/pdf-cache/" + ticket);
        request.setAttribute(
                HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE,
                "/common/viewer/pdf-cache/{ticketKey:[0-9a-fA-F]{32}}");
        request.setAttribute(AuditMenuContext.REQUEST_ATTRIBUTE,
                new AuditMenuContext("MENU_220", "기술자료관리 > 조회",
                        "/inside/distribution/swRequest/**", 2));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        writer.write(actor, "FILE_ACCESS", "VIEW", "ALLOW",
                null, null, "DOCUMENT", "DOC-1", "1", null, "NORMAL", "{}");
        writer.write(actor, "FILE_ACCESS", "DOWNLOAD_ORIGINAL", "ALLOW",
                null, null, "DOCUMENT", "DOC-1", "1", null, "NORMAL", "{}");

        ArgumentCaptor<AccessAuditEventVO> events =
                ArgumentCaptor.forClass(AccessAuditEventVO.class);
        verify(dao, times(2)).insertAudit(events.capture());
        List<AccessAuditEventVO> values = events.getAllValues();
        assertNotNull(values.get(0).getCorrelationId());
        assertEquals(values.get(0).getCorrelationId(), values.get(1).getCorrelationId());
        assertEquals("MENU_220", values.get(0).getMenuCd());
        assertEquals("기술자료관리 > 조회", values.get(0).getMenuNm());
        assertEquals("/common/viewer/pdf-cache/{ticketKey:[0-9a-fA-F]{32}}",
                values.get(0).getRequestUri());
        assertNotEquals(ticket, values.get(0).getRequestUri());
        assertEquals("VIEW", values.get(0).getActionNm());
        assertEquals("DOWNLOAD_ORIGINAL", values.get(1).getActionNm());
    }

    @Test
    void authenticationTargetsTheUserAndKeepsSessionOnlyInTheSessionColumn() {
        SecurityAclDao dao = mock(SecurityAclDao.class);
        when(dao.insertAudit(any(AccessAuditEventVO.class))).thenReturn(1);
        SecurityAuditWriter writer = new SecurityAuditWriter(dao);

        writer.writeAuthentication(
                actor(), "LOGIN", "로그인", "SUCCESS", null, null,
                "127.0.0.1", "secret-session-id");

        ArgumentCaptor<AccessAuditEventVO> event =
                ArgumentCaptor.forClass(AccessAuditEventVO.class);
        verify(dao).insertAudit(event.capture());
        assertEquals("USER_ACCOUNT", event.getValue().getObjectType());
        assertEquals("admin", event.getValue().getObjectId());
        assertEquals("secret-session-id", event.getValue().getSessionId());
        assertEquals("AUTH", event.getValue().getMenuCd());
        assertEquals("인증 / 계정", event.getValue().getMenuNm());
        assertEquals("/login/**", event.getValue().getMenuUrl());
        assertEquals("LOGIN", event.getValue().getActionNm());
    }

    @Test
    void menuActionsPersistTheStableActionCodeInsteadOfTheLocalizedLabel() {
        SecurityAclDao dao = mock(SecurityAclDao.class);
        when(dao.insertAudit(any(AccessAuditEventVO.class))).thenReturn(1);
        SecurityAuditWriter writer = new SecurityAuditWriter(dao);

        writer.writeMenuAction(
            actor(),
            new AuditMenuContext("MENU_220", "기술자료관리 > 조회", "/inside/data/**", 1),
            "READ",
            "조회",
            "SUCCESS",
            null,
            "HTTP 200",
            Integer.valueOf(200),
            Long.valueOf(10),
            "{}");

        ArgumentCaptor<AccessAuditEventVO> event =
            ArgumentCaptor.forClass(AccessAuditEventVO.class);
        verify(dao).insertAudit(event.capture());
        assertEquals("READ", event.getValue().getActionType());
        assertEquals("READ", event.getValue().getActionNm());
    }

    @Test
    void passwordChangeCanTargetAnotherUserWithoutChangingTheActor() {
        SecurityAclDao dao = mock(SecurityAclDao.class);
        when(dao.insertAudit(any(AccessAuditEventVO.class))).thenReturn(1);
        SecurityAuditWriter writer = new SecurityAuditWriter(dao);

        writer.writeAuthentication(
                actor(), "PASSWORD_CHANGE", "비밀번호 변경", "SUCCESS",
                null, null, "127.0.0.1", "session-id", "TARGET-USER-CD");

        ArgumentCaptor<AccessAuditEventVO> event =
                ArgumentCaptor.forClass(AccessAuditEventVO.class);
        verify(dao).insertAudit(event.capture());
        assertEquals("USER-1", event.getValue().getActorUserCd());
        assertEquals("TARGET-USER-CD", event.getValue().getObjectId());
    }

    @Test
    void rejectsReservedOrMalformedClientCorrelationIds() {
        SecurityAclDao dao = mock(SecurityAclDao.class);
        when(dao.insertAudit(any(AccessAuditEventVO.class))).thenReturn(1);
        SecurityAuditWriter writer = new SecurityAuditWriter(dao);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/inside/menu");
        request.addHeader("X-Correlation-ID", "legacy-audit-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        writer.write(actor(), "FILE_ACCESS", "VIEW", "ALLOW",
                null, null, "DOCUMENT", "DOC-1", "1", null, "NORMAL", "{}");

        ArgumentCaptor<AccessAuditEventVO> event =
                ArgumentCaptor.forClass(AccessAuditEventVO.class);
        verify(dao).insertAudit(event.capture());
        assertFalse(event.getValue().getCorrelationId()
                .toUpperCase().startsWith("LEGACY-AUDIT-"));
        assertEquals(event.getValue().getCorrelationId(),
                request.getAttribute(SecurityAuditWriter.CORRELATION_ID_REQUEST_ATTRIBUTE));

        org.mockito.Mockito.clearInvocations(dao);
        MockHttpServletRequest malformed =
                new MockHttpServletRequest("GET", "/inside/menu");
        malformed.addHeader("X-Correlation-ID", "trace id with spaces");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(malformed));
        writer.write(actor(), "FILE_ACCESS", "VIEW", "ALLOW",
                null, null, "DOCUMENT", "DOC-2", "1", null, "NORMAL", "{}");

        verify(dao).insertAudit(event.capture());
        AccessAuditEventVO malformedEvent = event.getAllValues().get(1);
        assertNotEquals("trace id with spaces", malformedEvent.getCorrelationId());
        assertEquals(malformedEvent.getCorrelationId(),
                malformed.getAttribute(SecurityAuditWriter.CORRELATION_ID_REQUEST_ATTRIBUTE));
    }

    private UserVO actor() {
        UserVO actor = new UserVO();
        actor.setUserCd("USER-1");
        actor.setUserId("admin");
        actor.setUserNm("관리자");
        return actor;
    }
}

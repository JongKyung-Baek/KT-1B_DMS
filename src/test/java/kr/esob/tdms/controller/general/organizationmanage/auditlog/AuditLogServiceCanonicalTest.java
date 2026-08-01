package kr.esob.tdms.controller.general.organizationmanage.auditlog;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import kr.esob.tdms.commonlogic.securityacl.SecurityAuditWriter;
import kr.esob.tdms.util.RequestUtil;

class AuditLogServiceCanonicalTest {

    private AuditLogService service;

    @AfterEach
    void shutdownExecutor() {
        if (service != null) {
            service.shutdownBrowserLeaveExecutor();
        }
    }

    @Test
    void mapsLegacyAuthenticationActionsToCanonicalEventsAndStoresUserCode() {
        service = service();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        when(service.requestUtil.getClientIp(request)).thenReturn("127.0.0.1");

        service.setSessionAuditInfo(
                session, "USER-1", "admin", "관리자", request);
        service.insertAuditLog(
                "logIn", "USER-1", "admin", "관리자", request);

        assertEquals("USER-1",
                session.getAttribute(AuditLogService.SESSION_AUDIT_USER_CD));
        verify(service.securityAuditWriter).writeAuthentication(
                org.mockito.ArgumentMatchers.argThat(actor ->
                        "USER-1".equals(actor.getUserCd())
                                && "admin".equals(actor.getUserId())),
                eq("LOGIN"), eq("로그인"), eq("SUCCESS"),
                isNull(), isNull(), eq("127.0.0.1"), eq(session.getId()), isNull());
    }

    @Test
    void returnsAnExplicitZeroSummaryOnlyWhenTheCanonicalQueryReturnsNull() {
        service = service();
        when(service.dao.selectSummary()).thenReturn(null);

        Map<String, Object> summary = service.selectSummary();

        assertEquals(0, summary.get("totalToday"));
        assertEquals(0, summary.get("successToday"));
        assertEquals(0, summary.get("deniedToday"));
        assertEquals(0, summary.get("failedToday"));
        assertEquals(0, summary.get("activeUsersToday"));
    }

    @Test
    void summaryQueryFailureIsNotDisguisedAsZeroActivity() {
        service = service();
        when(service.dao.selectSummary())
                .thenThrow(new IllegalStateException("ledger unavailable"));

        assertThrows(IllegalStateException.class, () -> service.selectSummary());
    }

    @Test
    void authenticationAuditFailureDoesNotBlockLoginFlow() {
        service = service();
        doThrow(new IllegalStateException("down"))
                .when(service.securityAuditWriter)
                .writeAuthentication(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any());

        assertDoesNotThrow(() ->
                service.insertAuditLog("loginFail", "unknown", null, "127.0.0.1"));
    }

    @Test
    void administratorPasswordChangeUsesTheTargetUserCode() {
        service = service();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("userCd", "TARGET-USER-CD");
        when(service.requestUtil.getClientIp(request)).thenReturn("127.0.0.1");

        service.insertAuditLog(
                "changePassword", "ADMIN-CD", "admin", "관리자", request);

        verify(service.securityAuditWriter).writeAuthentication(
                org.mockito.ArgumentMatchers.any(),
                eq("PASSWORD_CHANGE"), eq("비밀번호 변경"), eq("SUCCESS"),
                isNull(), isNull(), eq("127.0.0.1"), isNull(),
                eq("TARGET-USER-CD"));
    }

    @Test
    void auditQueryFailureIsNotDisguisedAsAnEmptyLedger() {
        service = service();
        when(service.dao.selectList(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new IllegalStateException("ledger unavailable"));

        assertThrows(IllegalStateException.class,
                () -> service.selectList(new AuditLogListParam()));
    }

    private AuditLogService service() {
        AuditLogService result = new AuditLogService();
        result.dao = mock(AuditLogDao.class);
        result.requestUtil = mock(RequestUtil.class);
        result.securityAuditWriter = mock(SecurityAuditWriter.class);
        return result;
    }
}

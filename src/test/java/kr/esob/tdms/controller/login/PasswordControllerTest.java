package kr.esob.tdms.controller.login;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.commonlogic.value.Constant;
import kr.esob.tdms.controller.general.organizationmanage.auditlog.AuditLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class PasswordControllerTest {

    private static final String ACCEPTABLE_PASSWORD = "Abcdefghi1!@";

    @Mock
    private LoginService loginService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private PasswordController controller;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void savesPasswordAndEndsAuthenticatedSession() {
        UserVO principal = authenticatedPrincipal();
        principal.setUserPwd("must-be-cleared");
        when(loginService.changeOwnPassword(principal.getUserCd(), ACCEPTABLE_PASSWORD))
                .thenReturn(true);
        when(request.getSession(false)).thenReturn(session);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResultVO result = controller.changeOwnPassword(
                ACCEPTABLE_PASSWORD, authentication, request);

        assertTrue(result.isSuccess());
        assertNull(principal.getUserPwd());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(loginService).changeOwnPassword(principal.getUserCd(), ACCEPTABLE_PASSWORD);
        verify(auditLogService).insertAuditLog(
                "changePassword", principal.getUserId(), principal.getUserNm(), request);
        verify(session).invalidate();
    }

    @Test
    void rejectsPasswordThatDoesNotMeetSpecialCharacterBoundary() {
        authenticatedPrincipal();

        ResultVO result = controller.changeOwnPassword(
                "Abcdefghi1!", authentication, request);

        assertFalse(result.isSuccess());
        assertTrue("feature.password.error.invalidPolicy".equals(result.getMessage()));
        verify(loginService, never()).changeOwnPassword(
                principalUserCd(), "Abcdefghi1!");
        verify(auditLogService, never()).insertAuditLog(
                "changePassword", "tester", "테스트 사용자", request);
    }

    @Test
    void rejectsTheFixedInitialPasswordWithoutPersistingIt() {
        authenticatedPrincipal();

        ResultVO result = controller.changeOwnPassword(
                Constant.INITIAL_PASSWORD, authentication, request);

        assertFalse(result.isSuccess());
        assertTrue("feature.password.error.invalidPolicy".equals(result.getMessage()));
        verify(loginService, never()).changeOwnPassword(
                principalUserCd(), Constant.INITIAL_PASSWORD);
        verify(auditLogService, never()).insertAuditLog(
                "changePassword", "tester", "테스트 사용자", request);
    }

    @Test
    void doesNotReportSuccessOrEndSessionWhenStorageUpdatesNoUser() {
        UserVO principal = authenticatedPrincipal();
        when(loginService.changeOwnPassword(principal.getUserCd(), ACCEPTABLE_PASSWORD))
                .thenReturn(false);

        ResultVO result = controller.changeOwnPassword(
                ACCEPTABLE_PASSWORD, authentication, request);

        assertFalse(result.isSuccess());
        assertTrue("feature.password.error.save".equals(result.getMessage()));
        verify(auditLogService, never()).insertAuditLog(
                "changePassword", principal.getUserId(), principal.getUserNm(), request);
        verify(request, never()).getSession(false);
        verify(session, never()).invalidate();
    }

    private UserVO authenticatedPrincipal() {
        UserVO principal = new UserVO();
        principal.setUserCd(principalUserCd());
        principal.setUserId("tester");
        principal.setUserNm("테스트 사용자");
        when(authentication.getPrincipal()).thenReturn(principal);
        return principal;
    }

    private String principalUserCd() {
        return "USER_CD";
    }
}

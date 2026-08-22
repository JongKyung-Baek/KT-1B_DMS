package kr.esob.tdms.commonlogic.audit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.tdms.commonlogic.securityacl.SecurityAuditWriter;
import kr.esob.tdms.controller.login.UserVO;

class RequestAuditFilterTest {
    private AuditMenuResolver menuResolver;
    private SecurityAuditWriter auditWriter;
    private RequestAuditFilter filter;
    private UserVO actor;
    private AuditMenuContext menu;

    @BeforeEach
    void setUp() {
        menuResolver = mock(AuditMenuResolver.class);
        auditWriter = mock(SecurityAuditWriter.class);
        filter = new RequestAuditFilter(menuResolver, auditWriter, new ObjectMapper());
        actor = new UserVO();
        actor.setUserCd("USER-1");
        actor.setUserId("admin");
        actor.setUserNm("관리자");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        actor, null, Collections.emptyList()));
        menu = new AuditMenuContext(
                "MENU_220", "기술자료관리 > 조회",
                "/general/distribution/swRequest/**", 2);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void writesOneSuccessfulMenuActionAndCollectsOnlySafeTargets() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/general/distribution/swRequest/selectList");
        request.setParameter("objectId", "DOC-1");
        request.setParameter("password", "must-not-be-audited");
        request.setParameter("token", "must-not-be-audited-either");
        request.setAttribute(
                HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE,
                "/general/distribution/swRequest/selectList");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(menuResolver.resolve(request)).thenReturn(menu);

        filter.doFilter(request, response, (req, res) -> {
        });

        verify(auditWriter).writeMenuAction(
                eq(actor), eq(menu), eq("READ"), eq("조회"), eq("SUCCESS"),
                isNull(), eq("HTTP 200"), eq(200), anyLong(),
                org.mockito.ArgumentMatchers.argThat(detail ->
                        detail.contains("\"objectId\":\"DOC-1\"")
                                && !detail.contains("must-not-be-audited")));
    }

    @Test
    void recordsSecurityDenialThenRethrowsForNormalExceptionHandling() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/general/distribution/swRequest/selectList");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(menuResolver.resolve(request)).thenReturn(menu);
        FilterChain denied = (req, res) -> {
            throw new AccessDeniedException("denied");
        };

        assertThrows(AccessDeniedException.class,
                () -> filter.doFilter(request, response, denied));

        verify(auditWriter).writeMenuAction(
                eq(actor), eq(menu), eq("READ"), eq("조회"), eq("DENY"),
                eq("ACCESS_DENIED"), eq("HTTP 403"), eq(403), anyLong(), any(String.class));
    }

    @Test
    void auditPersistenceFailureNeverChangesTheBusinessResponse() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/general/distribution/swRequest/regist");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(menuResolver.resolve(request)).thenReturn(menu);
        doThrow(new IllegalStateException("database unavailable"))
                .when(auditWriter).writeMenuAction(
                        any(), any(), any(), any(), any(), any(), any(), any(), any(), any());

        assertDoesNotThrow(() -> filter.doFilter(request, response, (req, res) -> {
            ((MockHttpServletResponse) res).setStatus(201);
        }));
    }

    @Test
    void anonymousOrUnresolvedRequestsAreNotAuditedAndLoginStaysManual() throws Exception {
        MockHttpServletRequest unresolved =
                new MockHttpServletRequest("GET", "/unmapped");
        when(menuResolver.resolve(unresolved)).thenReturn(null);
        filter.doFilter(unresolved, new MockHttpServletResponse(), (req, res) -> {
        });
        verify(auditWriter, never()).writeMenuAction(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any());

        MockHttpServletRequest login =
                new MockHttpServletRequest("POST", "/login/loginProcess");
        filter.doFilter(login, new MockHttpServletResponse(), (req, res) -> {
        });
        verify(menuResolver, never()).resolve(login);

    }

    @Test
    void handlerSaveMethodIsReportedAsSave() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/general/system/securityaccess/grades");
        request.setAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE,
                new HandlerMethod(new SaveHandler(), SaveHandler.class.getMethod("saveGrade")));
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(menuResolver.resolve(request)).thenReturn(menu);

        filter.doFilter(request, response, (req, res) -> {
        });

        verify(auditWriter).writeMenuAction(
                eq(actor), eq(menu), eq("SAVE"), eq("저장"), eq("SUCCESS"),
                isNull(), eq("HTTP 200"), eq(200), anyLong(), any(String.class));
    }

    @Test
    void passwordResetIsRecordedAsItsOwnAdministrativeAction() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/general/organizationmanage/insideuser/resetPwd");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(menuResolver.resolve(request)).thenReturn(menu);

        filter.doFilter(request, response, (req, res) -> {
        });

        verify(auditWriter).writeMenuAction(
                eq(actor), eq(menu), eq("PASSWORD_RESET"), eq("비밀번호 초기화"),
                eq("SUCCESS"), isNull(), eq("HTTP 200"), eq(200), anyLong(),
                any(String.class));
    }

    @Test
    void getRegistrationPopupIsReadRatherThanCreate() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/general/distribution/swRequest/regist");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(menuResolver.resolve(request)).thenReturn(menu);

        filter.doFilter(request, response, (req, res) -> {
        });

        verify(auditWriter).writeMenuAction(
                eq(actor), eq(menu), eq("READ"), eq("조회"), eq("SUCCESS"),
                isNull(), eq("HTTP 200"), eq(200), anyLong(), any(String.class));
    }

    @Test
    void explicitBusinessFailureOverridesSuccessfulHttpStatus() throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/general/system/securityaccess/saveGrade");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(menuResolver.resolve(request)).thenReturn(menu);

        filter.doFilter(request, response, (req, res) ->
                req.setAttribute(
                        AuditBusinessResultContext.REQUEST_ATTRIBUTE, Boolean.FALSE));

        verify(auditWriter).writeMenuAction(
                eq(actor), eq(menu), eq("EXECUTE"), eq("실행"), eq("FAILURE"),
                eq("BUSINESS_FAILURE"), eq("HTTP 200"), eq(200),
                anyLong(), any(String.class));
    }

    private static final class SaveHandler {
        public void saveGrade() {
        }
    }
}

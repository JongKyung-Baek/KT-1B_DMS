package kr.esob.tdms.controller.login;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MfaTest {
    @org.junit.jupiter.api.AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @Test void pendingEnrollmentIncludesQrAndManualKeyWithoutCompletingAuthentication() {
        var store = mock(MfaStore.class);
        var users = mock(LoginService.class);
        var success = mock(LoginSuccess.class);
        var controller = new MfaController(store, users, success);
        var request = new MockHttpServletRequest();
        byte[] seed = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
        request.getSession().setAttribute(MfaStart.PENDING,
                new MfaStart.Challenge("alice", "U1", new byte[32], System.currentTimeMillis() + 300000, 0, seed));
        var response = new MockHttpServletResponse();
        var model = new ExtendedModelMap();
        assertEquals("/login/mfa", controller.challenge(request, response, model));
        assertEquals("alice", model.get("account"));
        assertEquals("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", model.get("setupKey"));
        assertTrue(((String) model.get("setupQr")).startsWith("data:image/png;base64,"));
        assertEquals("no-store", response.getHeader("Cache-Control"));
        assertEquals("no-referrer", response.getHeader("Referrer-Policy"));
        var refreshed = new ExtendedModelMap();
        controller.challenge(request, new MockHttpServletResponse(), refreshed);
        assertEquals(model, refreshed);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(store, users, success);
    }

    @Test void enrolledUsersNeverReceiveSetupMaterial() {
        var controller = new MfaController(mock(MfaStore.class), mock(LoginService.class), mock(LoginSuccess.class));
        var request = new MockHttpServletRequest();
        request.getSession().setAttribute(MfaStart.PENDING,
                new MfaStart.Challenge("alice", "U1", new byte[32], System.currentTimeMillis() + 300000, 1, null));
        var model = new ExtendedModelMap();
        assertEquals("/login/mfa", controller.challenge(request, new MockHttpServletResponse(), model));
        assertTrue(model.isEmpty());
    }

    @Test void anonymousAndExpiredChallengesNeverReceiveSetupMaterial() {
        var controller = new MfaController(mock(MfaStore.class), mock(LoginService.class), mock(LoginSuccess.class));
        var request = new MockHttpServletRequest();
        var model = new ExtendedModelMap();
        assertEquals("redirect:/login/loginPage", controller.challenge(request, new MockHttpServletResponse(), model));
        assertNull(request.getSession(false));
        request.getSession().setAttribute(MfaStart.PENDING,
                new MfaStart.Challenge("alice", "U1", new byte[32], 0, 1, new byte[20]));
        assertEquals("redirect:/login/loginPage", controller.challenge(request, new MockHttpServletResponse(), model));
        assertTrue(model.isEmpty());
        assertNull(request.getSession().getAttribute(MfaStart.PENDING));
    }

    @Test void secondFactorIsRequiredBeforeOriginalLoginHandlerRuns() throws Exception {
        var store = mock(MfaStore.class);
        var users = mock(LoginService.class);
        var success = mock(LoginSuccess.class);
        var controller = new MfaController(store, users, success);
        var request = new MockHttpServletRequest();
        var user = new UserVO();
        user.setUserCd("U1");
        user.setUserId("alice");
        user.setUserPwd("stored-password-hash");
        user.setUseYn("Y");
        user.setRoleGroup("RG_TEST");
        when(users.getInUser("alice")).thenReturn(user);
        var pending = new MfaStart.Challenge("alice","U1",MfaStart.stamp(user),System.currentTimeMillis()+300000,0,null);
        request.getSession().setAttribute(MfaStart.PENDING,pending);
        var rejected = new MockHttpServletResponse();
        controller.verify("000000",request,rejected);
        assertEquals("/login/mfa?retry", rejected.getRedirectedUrl());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(success);

        when(store.verify(pending,"123456")).thenReturn(true);
        var response = new MockHttpServletResponse();
        controller.verify("123456",request,response);
        assertNull(request.getSession().getAttribute(MfaStart.PENDING));
        assertSame(user,SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(success).onAuthenticationSuccess(eq(request),eq(response),any());
    }

    @Test void rfc6238Sha1TruncatedToSixDigits() throws Exception {
        byte[] seed = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
        assertEquals("287082", MfaStore.code(seed, 59 / 30));
        assertEquals("081804", MfaStore.code(seed, 1111111109 / 30));
    }

    @Test void passwordFactorHasNoSecurityContext() throws Exception {
        SecurityContextHolder.clearContext();
        MfaPasswordFilter filter = new MfaPasswordFilter();
        filter.setAuthenticationSuccessHandler((req,res,auth) -> res.sendRedirect("/login/mfa"));
        var response = new MockHttpServletResponse();
        filter.successfulAuthentication(new MockHttpServletRequest(), response, new MockFilterChain(),
                new UsernamePasswordAuthenticationToken("user", null, java.util.List.of()));
        assertEquals("/login/mfa", response.getRedirectedUrl());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

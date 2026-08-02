package kr.esob.tdms.commonlogic.interceptor;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import kr.esob.tdms.commonlogic.viewerintegration.ViewerIntegrationProperties;

class CommonCheckInterceptorTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void dedicatedViewerCallbackPrincipalBypassesInteractiveDuplicateLoginCheck()
            throws Exception {
        Authentication callbackAuthentication =
                new UsernamePasswordAuthenticationToken(
                        "step-callback", null,
                        java.util.Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_VIEWER_CALLBACK")));
        SecurityContextHolder.getContext().setAuthentication(
                callbackAuthentication);
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", ViewerIntegrationProperties.CALLBACK_PATH);
        request.setServletPath(ViewerIntegrationProperties.CALLBACK_PATH);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean proceed = new CommonCheckInterceptor().preHandle(
                request, response, new Object());

        assertTrue(proceed);
        assertSame(callbackAuthentication,
                SecurityContextHolder.getContext().getAuthentication());
    }
}

package kr.esob.tdms.commonlogic.viewerintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.FilterChain;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StreamUtils;

class ViewerCallbackAuthenticationFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void unsignedJsonIsRejectedAsJson401WithoutReachingTheApplication()
            throws Exception {
        ViewerIntegrationService service = mock(ViewerIntegrationService.class);
        doThrow(new ViewerCallbackAuthenticationException("missing signature"))
                .when(service).authenticateCallbackRequest(
                        any(), any(), any(), any(), any(), any());
        ViewerCallbackAuthenticationFilter filter =
                new ViewerCallbackAuthenticationFilter(service);
        MockHttpServletRequest request = callbackRequest("{}");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);

        filter.doFilter(request, response,
                (ignoredRequest, ignoredResponse) -> invoked.set(true));

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("{\"error\":\"unauthorized\"}", response.getContentAsString());
        assertFalse(invoked.get());
        assertFalse(response.getContentAsString().contains("login"));
        assertNull(response.getRedirectedUrl());
    }

    @Test
    void validSignatureCreatesDedicatedAuthenticationAndReplaysExactBody()
            throws Exception {
        ViewerIntegrationService service = mock(ViewerIntegrationService.class);
        ViewerCallbackAuthenticationFilter filter =
                new ViewerCallbackAuthenticationFilter(service);
        String json = "{\"eventType\":\"VIEW_OPENED\"}";
        MockHttpServletRequest request = callbackRequest(json);
        request.addHeader("X-CV-Client-Id", "step-callback");
        request.addHeader("X-CV-Timestamp", "1785630000");
        request.addHeader("X-CV-Nonce", "00000000-0000-4000-8000-000000000001");
        request.addHeader("X-CV-Content-SHA256", "a".repeat(64));
        request.addHeader("X-CV-Signature", "b".repeat(64));
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication existingAuthentication =
                new UsernamePasswordAuthenticationToken("admin", null);
        SecurityContext originalContext = SecurityContextHolder.createEmptyContext();
        originalContext.setAuthentication(existingAuthentication);
        SecurityContextHolder.setContext(originalContext);
        AtomicReference<Authentication> authentication = new AtomicReference<>();
        AtomicReference<String> replayedBody = new AtomicReference<>();
        FilterChain chain = (servletRequest, servletResponse) -> {
            authentication.set(SecurityContextHolder.getContext().getAuthentication());
            replayedBody.set(StreamUtils.copyToString(
                    servletRequest.getInputStream(), StandardCharsets.UTF_8));
            ((HttpServletResponse) servletResponse).setStatus(
                    HttpServletResponse.SC_NO_CONTENT);
        };

        filter.doFilter(request, response, chain);

        assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
        assertEquals(json, replayedBody.get());
        assertTrue(authentication.get().getAuthorities().stream().anyMatch(
                authority -> ViewerCallbackAuthenticationFilter.AUTHORITY.equals(
                        authority.getAuthority())));
        assertSame(originalContext, SecurityContextHolder.getContext());
        assertSame(existingAuthentication,
                SecurityContextHolder.getContext().getAuthentication());
        assertNull(request.getSession(false));
        assertNull(response.getHeader("Set-Cookie"));
        verify(service).authenticateCallbackRequest(
                json.getBytes(StandardCharsets.UTF_8),
                "step-callback",
                "1785630000",
                "00000000-0000-4000-8000-000000000001",
                "a".repeat(64),
                "b".repeat(64));
    }

    @Test
    void unavailableProviderConfigurationReturnsJson503() throws Exception {
        ViewerIntegrationService service = mock(ViewerIntegrationService.class);
        doThrow(new ViewerIntegrationUnavailableException(
                "not configured", new IllegalStateException("disabled")))
                .when(service).authenticateCallbackRequest(
                        any(), any(), any(), any(), any(), any());
        ViewerCallbackAuthenticationFilter filter =
                new ViewerCallbackAuthenticationFilter(service);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(callbackRequest("{}"), response,
                (ignoredRequest, ignoredResponse) -> {
                    throw new AssertionError("Filter chain must not be called.");
                });

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("{\"error\":\"service_unavailable\"}",
                response.getContentAsString());
        assertNull(response.getRedirectedUrl());
    }

    @Test
    void unreadableRawBodyReturnsJson400BeforeAuthentication() throws Exception {
        ViewerIntegrationService service = mock(ViewerIntegrationService.class);
        ViewerCallbackAuthenticationFilter filter =
                new ViewerCallbackAuthenticationFilter(service);
        MockHttpServletRequest baseRequest = new MockHttpServletRequest(
                "POST", ViewerIntegrationProperties.CALLBACK_PATH);
        baseRequest.setServletPath(ViewerIntegrationProperties.CALLBACK_PATH);
        baseRequest.setContentType("application/json");
        HttpServletRequest request = new HttpServletRequestWrapper(baseRequest) {
            @Override
            public ServletInputStream getInputStream() throws IOException {
                throw new IOException("unreadable");
            }
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                (ignoredRequest, ignoredResponse) -> {
                    throw new AssertionError("Filter chain must not be called.");
                });

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("{\"error\":\"invalid_request\"}",
                response.getContentAsString());
        verifyNoInteractions(service);
    }

    private MockHttpServletRequest callbackRequest(String body) {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", ViewerIntegrationProperties.CALLBACK_PATH);
        request.setServletPath(ViewerIntegrationProperties.CALLBACK_PATH);
        request.setContentType("application/json");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }
}

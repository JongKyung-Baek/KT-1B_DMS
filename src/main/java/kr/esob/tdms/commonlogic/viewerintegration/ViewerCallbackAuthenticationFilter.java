package kr.esob.tdms.commonlogic.viewerintegration;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ViewerCallbackAuthenticationFilter extends OncePerRequestFilter {
    public static final String AUTHORITY = "ROLE_VIEWER_CALLBACK";
    private static final int MAX_CALLBACK_BYTES = 16 * 1024;

    private final ViewerIntegrationService service;

    public ViewerCallbackAuthenticationFilter(ViewerIntegrationService service) {
        this.service = service;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod())
                || !ViewerIntegrationProperties.CALLBACK_PATH.equals(
                        request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        byte[] body;
        try {
            body = readBounded(request);
            service.authenticateCallbackRequest(
                    body,
                    request.getHeader("X-CV-Client-Id"),
                    request.getHeader("X-CV-Timestamp"),
                    request.getHeader("X-CV-Nonce"),
                    request.getHeader("X-CV-Content-SHA256"),
                    request.getHeader("X-CV-Signature"));
        } catch (ViewerCallbackPayloadTooLargeException exception) {
            writeError(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                    "payload_too_large");
            return;
        } catch (IOException exception) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "invalid_request");
            return;
        } catch (ViewerCallbackAuthenticationException exception) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "unauthorized");
            return;
        } catch (ViewerIntegrationUnavailableException exception) {
            writeError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "service_unavailable");
            return;
        } catch (ViewerCallbackValidationException exception) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "invalid_request");
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "viewer-callback",
                        null,
                        Collections.singletonList(
                                new SimpleGrantedAuthority(AUTHORITY)));
        SecurityContext originalContext = SecurityContextHolder.getContext();
        SecurityContext callbackContext = SecurityContextHolder.createEmptyContext();
        callbackContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(callbackContext);
        try {
            filterChain.doFilter(new CachedBodyRequest(request, body), response);
        } finally {
            SecurityContextHolder.setContext(originalContext);
        }
    }

    private byte[] readBounded(HttpServletRequest request) throws IOException {
        long contentLength = request.getContentLengthLong();
        if (contentLength > MAX_CALLBACK_BYTES) {
            throw new ViewerCallbackPayloadTooLargeException();
        }
        try (ServletInputStream input = request.getInputStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream(
                     contentLength > 0 ? (int) contentLength : 1024)) {
            byte[] buffer = new byte[4096];
            int total = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > MAX_CALLBACK_BYTES) {
                    throw new ViewerCallbackPayloadTooLargeException();
                }
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private void writeError(HttpServletResponse response, int status,
                            String error) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + error + "\"}");
    }

    private static final class CachedBodyRequest
            extends HttpServletRequestWrapper {
        private final byte[] body;

        private CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body.clone();
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream input = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return input.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // Synchronous request processing does not use a listener.
                }

                @Override
                public int read() {
                    return input.read();
                }

                @Override
                public int read(byte[] bytes, int offset, int length) {
                    return input.read(bytes, offset, length);
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(
                    getInputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }
    }
}

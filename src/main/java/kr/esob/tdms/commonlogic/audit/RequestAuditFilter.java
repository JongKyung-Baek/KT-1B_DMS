package kr.esob.tdms.commonlogic.audit;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.tdms.commonlogic.securityacl.SecurityAuditWriter;
import kr.esob.tdms.controller.login.UserVO;
import lombok.extern.slf4j.Slf4j;

/**
 * Adds one canonical MENU_ACTION event for each authenticated, menu-owned
 * request. Authentication endpoints remain covered by AuditLogService.
 */
@Slf4j
@Component
public class RequestAuditFilter extends OncePerRequestFilter {
    private static final Set<String> SAFE_TARGET_PARAMETERS = Collections.unmodifiableSet(
            Set.of("actioncd", "cnserial", "crno", "documentid", "documentno",
                    "drawingno", "dxfno", "fileno", "gradecd", "menucd",
                    "objectid", "objecttype", "partno", "peerreviewno",
                    "requestno", "swno", "usercd"));
    private static final int MAX_TARGET_VALUE_LENGTH = 200;

    private final AuditMenuResolver menuResolver;
    private final SecurityAuditWriter auditWriter;
    private final ObjectMapper objectMapper;

    public RequestAuditFilter(AuditMenuResolver menuResolver,
                              SecurityAuditWriter auditWriter,
                              ObjectMapper objectMapper) {
        this.menuResolver = menuResolver;
        this.auditWriter = auditWriter;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return "/login".equals(uri)
                || uri.startsWith("/login/")
                || "/general/organizationmanage/auditlog/notifyLogoutOnLeave".equals(uri)
                || "/general/organizationmanage/auditlog/clearPendingLogoutOnStay".equals(uri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        UserVO actor = currentActor();
        AuditMenuContext menu = actor == null ? null : menuResolver.resolve(request);
        if (actor == null || menu == null) {
            filterChain.doFilter(request, response);
            return;
        }

        request.setAttribute(AuditMenuContext.REQUEST_ATTRIBUTE, menu);
        long startedAt = System.nanoTime();
        Throwable failure = null;
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            recordRequest(actor, menu, request, response, failure, startedAt);
        }
    }

    private void recordRequest(UserVO actor, AuditMenuContext menu,
                               HttpServletRequest request, HttpServletResponse response,
                               Throwable failure, long startedAt) {
        int status = effectiveStatus(response.getStatus(), failure);
        String resultCd = resolveResult(request, status, failure);
        String reasonCd = resolveReason(request, status, failure);
        Action action = resolveAction(request);
        long durationMs = Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);

        try {
            auditWriter.writeMenuAction(
                    actor,
                    menu,
                    action.type,
                    action.name,
                    resultCd,
                    reasonCd,
                    "HTTP " + status,
                    status,
                    durationMs,
                    buildDetailJson(request, menu, action, status, durationMs));
        } catch (RuntimeException auditFailure) {
            log.warn("Canonical request audit write failed; business response is preserved. "
                            + "menuCd={}, uri={}, method={}, cause={}",
                    menu.getMenuCd(), AuditRequestSanitizer.safeRequestUri(request), request.getMethod(),
                    auditFailure.getClass().getSimpleName());
        }
    }

    private UserVO currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof UserVO)) {
            return null;
        }
        return (UserVO) authentication.getPrincipal();
    }

    private int effectiveStatus(int responseStatus, Throwable failure) {
        if (failure == null) {
            return responseStatus;
        }
        if (isAccessDenied(failure)) {
            return HttpServletResponse.SC_FORBIDDEN;
        }
        if (isAuthenticationFailure(failure)) {
            return HttpServletResponse.SC_UNAUTHORIZED;
        }
        return responseStatus >= 400 ? responseStatus : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    private String resolveResult(HttpServletRequest request, int status, Throwable failure) {
        if (isAccessDenied(failure) || isAuthenticationFailure(failure)
                || status == HttpServletResponse.SC_UNAUTHORIZED
                || status == HttpServletResponse.SC_FORBIDDEN) {
            return "DENY";
        }
        if (failure == null && status < 400 && isExplicitBusinessFailure(request)) {
            return "FAILURE";
        }
        if (failure != null || status >= 400) {
            return "FAILURE";
        }
        return "SUCCESS";
    }

    private String resolveReason(HttpServletRequest request, int status, Throwable failure) {
        if (isAccessDenied(failure)) {
            return "ACCESS_DENIED";
        }
        if (isAuthenticationFailure(failure)) {
            return "AUTHENTICATION_REQUIRED";
        }
        if (failure == null && status < 400 && isExplicitBusinessFailure(request)) {
            return "BUSINESS_FAILURE";
        }
        if (failure != null) {
            return limit(failure.getClass().getSimpleName().toUpperCase(Locale.ROOT), 50);
        }
        return status >= 400 ? "HTTP_" + status : null;
    }

    private boolean isExplicitBusinessFailure(HttpServletRequest request) {
        return Boolean.FALSE.equals(
                request.getAttribute(AuditBusinessResultContext.REQUEST_ATTRIBUTE));
    }

    private boolean isAccessDenied(Throwable failure) {
        return hasCause(failure, org.springframework.security.access.AccessDeniedException.class);
    }

    private boolean isAuthenticationFailure(Throwable failure) {
        return hasCause(failure, AuthenticationException.class);
    }

    private boolean hasCause(Throwable failure, Class<?> type) {
        Throwable current = failure;
        int depth = 0;
        while (current != null && depth++ < 10) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private Action resolveAction(HttpServletRequest request) {
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        String handlerName = handler instanceof HandlerMethod
                ? ((HandlerMethod) handler).getMethod().getName().toLowerCase(Locale.ROOT)
                : "";
        String uri = request.getRequestURI().toLowerCase(Locale.ROOT);
        String source = handlerName + " " + uri;

        if (containsAny(source, "download", "filedown")) {
            return new Action("DOWNLOAD", "다운로드");
        }
        if (containsAny(source, "print")) {
            return new Action("PRINT", "출력");
        }
        if (containsAny(source, "viewer", "/view", "preview")) {
            return new Action("VIEW", "열람");
        }
        if (containsAny(source, "resetpwd", "passwordreset", "resetpassword")) {
            return new Action("PASSWORD_RESET", "비밀번호 초기화");
        }
        if ("GET".equalsIgnoreCase(request.getMethod())
                || "HEAD".equalsIgnoreCase(request.getMethod())) {
            return new Action("READ", "조회");
        }
        if (containsAny(handlerName, "delete", "remove", "destroy")
                || "DELETE".equalsIgnoreCase(request.getMethod())) {
            return new Action("DELETE", "삭제");
        }
        if (containsAny(handlerName, "reject", "deny")) {
            return new Action("REJECT", "반려");
        }
        if (containsAny(handlerName, "approve", "accept")) {
            return new Action("APPROVE", "승인");
        }
        if (containsAny(handlerName, "save")) {
            return new Action("SAVE", "저장");
        }
        if (containsAny(handlerName, "insert", "create", "register", "regist", "upload")
                || containsAny(uri, "/regist", "/register", "/create", "/upload")) {
            return new Action("CREATE", "등록");
        }
        if (containsAny(handlerName, "update", "edit", "change", "modify")
                || "PATCH".equalsIgnoreCase(request.getMethod())
                || "PUT".equalsIgnoreCase(request.getMethod())) {
            return new Action("UPDATE", "수정");
        }
        if (containsAny(handlerName, "select", "search", "list", "find", "get")) {
            return new Action("READ", "조회");
        }
        return new Action("EXECUTE", "실행");
    }

    private String buildDetailJson(HttpServletRequest request, AuditMenuContext menu,
                                   Action action, int status, long durationMs) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("menuCd", menu.getMenuCd());
        detail.put("menuNm", menu.getMenuNm());
        detail.put("menuUrl", menu.getMenuUrl());
        detail.put("requestUri", AuditRequestSanitizer.safeRequestUri(request));
        detail.put("httpMethod", request.getMethod());
        detail.put("httpStatus", status);
        detail.put("durationMs", durationMs);
        detail.put("actionType", action.type);

        Object bestPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (bestPattern != null) {
            detail.put("handlerPattern", limit(bestPattern.toString(), 500));
        }
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            detail.put("handler", limit(method.getBeanType().getName()
                    + "#" + method.getMethod().getName(), 500));
        }

        Map<String, Object> safeTargets = safeTargets(request);
        if (!safeTargets.isEmpty()) {
            detail.put("targets", safeTargets);
        }

        try {
            return objectMapper.writeValueAsString(detail);
        } catch (JsonProcessingException e) {
            log.warn("Request audit detail serialization failed; storing an empty JSON object. cause={}",
                    e.getClass().getSimpleName());
            return "{}";
        }
    }

    private Map<String, Object> safeTargets(HttpServletRequest request) {
        Map<String, Object> targets = new TreeMap<>();
        request.getParameterMap().forEach((name, values) -> {
            if (name == null || !SAFE_TARGET_PARAMETERS.contains(name.toLowerCase(Locale.ROOT))) {
                return;
            }
            if (values == null || values.length == 0) {
                return;
            }
            if (values.length == 1) {
                targets.put(name, limit(values[0], MAX_TARGET_VALUE_LENGTH));
            } else {
                targets.put(name, Arrays.stream(values)
                        .limit(10)
                        .map(value -> limit(value, MAX_TARGET_VALUE_LENGTH))
                        .toArray(String[]::new));
            }
        });

        Object variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (variables instanceof Map<?, ?>) {
            ((Map<?, ?>) variables).forEach((name, value) -> {
                if (name == null || value == null) {
                    return;
                }
                String parameterName = name.toString();
                if (SAFE_TARGET_PARAMETERS.contains(parameterName.toLowerCase(Locale.ROOT))) {
                    targets.putIfAbsent(parameterName,
                            limit(value.toString(), MAX_TARGET_VALUE_LENGTH));
                }
            });
        }
        return targets;
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static final class Action {
        private final String type;
        private final String name;

        private Action(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }
}

package kr.esob.tdms.commonlogic.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.tdms.commonlogic.branding.TdmsBrandResolver;
import kr.esob.tdms.commonlogic.branding.TdmsBrandView;
import kr.esob.tdms.commonlogic.viewerintegration.ViewerIntegrationProperties;
import kr.esob.tdms.controller.general.distribution.accountrequest.DistributionAccountIntegrationProperties;

/**
 * Rejects browser access from known phone and tablet clients before login or
 * application request processing. Device headers are client-controlled and are
 * therefore a best-effort access policy, not a device-attestation boundary.
 */
@Component
public class MobileClientAccessFilter extends OncePerRequestFilter {
    public static final String BLOCK_VIEW =
            "/WEB-INF/views/error/mobileAccessBlocked.jsp";
    public static final String ERROR_CODE = "MOBILE_ACCESS_BLOCKED";

    private static final String CLIENT_HINTS =
            "Sec-CH-UA-Mobile, Sec-CH-UA-Platform";
    private static final String VARY_HEADERS =
            "User-Agent, Sec-CH-UA-Mobile, Sec-CH-UA-Platform";
    private static final List<String> SUPPORTED_LANGUAGES =
            Collections.unmodifiableList(Arrays.asList("ko", "en", "id"));
    private static final String[] MOBILE_USER_AGENT_TOKENS = {
            "android", "iphone", "ipad", "ipod", "mobi", "mobile",
            "tablet", "silk/", "kindle", "iemobile", "windows phone",
            "blackberry", "bb10", "playbook", "opera mini", "opera mobi",
            "webos", "tizen", "harmonyos", "kaios"
    };

    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final TdmsBrandResolver brandResolver;

    public MobileClientAccessFilter(MessageSource messageSource,
                                    ObjectMapper objectMapper,
                                    TdmsBrandResolver brandResolver) {
        this.messageSource = messageSource;
        this.objectMapper = objectMapper;
        this.brandResolver = brandResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = applicationPath(request);
        return path.equals("/favicon.ico")
                || path.equals("/favicon.svg")
                || path.equals("/apple-touch-icon.png")
                || path.equals("/robots.txt")
                || path.startsWith("/resources/")
                || path.startsWith("/vuexy/")
                || path.equals("/actuator/health")
                || path.startsWith("/actuator/health/")
                || isSignedServerIntegration(request, path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        response.setHeader("Accept-CH", CLIENT_HINTS);
        response.addHeader("Vary", VARY_HEADERS);
        if (!isMobileClient(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Locale locale = resolveLocale(request);
        prepareBlockedResponse(response);
        if (expectsJson(request)) {
            writeJson(response, locale);
            return;
        }

        TdmsBrandView brand = brandResolver.resolve(request, locale);
        request.setAttribute("mobileBlockLocale", locale.getLanguage());
        request.setAttribute("mobileBlockBrand", brand.getCompanyName());
        request.setAttribute("mobileBlockSystemName",
                brand.getSystemDescription());
        request.setAttribute("mobileBlockLogoPath", brand.getLogoLightPath());
        request.setAttribute("mobileBlockLogoAlt", brand.getLogoAlt());
        request.setAttribute("mobileBlockWideLogo", brand.isWideLogo());
        request.setAttribute("mobileBlockAlternateBrand",
                brand.isAlternate());
        request.setAttribute("mobileBlockStatusAria",
                message("feature.error.status.aria", locale));
        request.setAttribute("mobileBlockEyebrow",
                message("feature.mobileAccess.eyebrow", locale));
        request.setAttribute("mobileBlockTitle",
                message("feature.mobileAccess.title", locale));
        request.setAttribute("mobileBlockTitleLine1",
                message("feature.mobileAccess.titleLine1", locale));
        request.setAttribute("mobileBlockTitleLine2",
                message("feature.mobileAccess.titleLine2", locale));
        request.setAttribute("mobileBlockMessage",
                message("feature.mobileAccess.message", locale));
        request.setAttribute("mobileBlockHelp",
                message("feature.mobileAccess.help", locale));
        request.setAttribute("mobileBlockNotice",
                message("feature.mobileAccess.notice", locale));
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        request.getRequestDispatcher(BLOCK_VIEW).forward(request, response);
    }

    boolean isMobileClient(HttpServletRequest request) {
        if (isAffirmativeMobileHint(request.getHeader("Sec-CH-UA-Mobile"))) {
            return true;
        }

        String platform = lower(request.getHeader("Sec-CH-UA-Platform"))
                .trim().replace("\"", "");
        if ("android".equals(platform) || "ios".equals(platform)) {
            return true;
        }

        String userAgent = lower(request.getHeader("User-Agent"));
        for (String token : MOBILE_USER_AGENT_TOKENS) {
            if (userAgent.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAffirmativeMobileHint(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().replace("\"", "");
        return "?1".equals(normalized) || "1".equals(normalized);
    }

    private boolean expectsJson(HttpServletRequest request) {
        String path = applicationPath(request).toLowerCase(Locale.ROOT);
        if (path.equals("/api") || path.startsWith("/api/")
                || path.contains("/api/")) {
            return true;
        }
        if ("XMLHttpRequest".equalsIgnoreCase(
                request.getHeader("X-Requested-With"))) {
            return true;
        }
        return containsJsonMediaType(request.getContentType())
                || containsJsonMediaType(request.getHeader("Accept"));
    }

    private boolean containsJsonMediaType(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.contains("application/json")
                || normalized.contains("application/problem+json")
                || normalized.contains("+json");
    }

    private Locale resolveLocale(HttpServletRequest request) {
        if (request.getHeader("Accept-Language") != null) {
            Enumeration<Locale> locales = request.getLocales();
            while (locales.hasMoreElements()) {
                Locale locale = locales.nextElement();
                String language = locale == null
                        ? "" : locale.getLanguage().toLowerCase(Locale.ROOT);
                if (SUPPORTED_LANGUAGES.contains(language)) {
                    return "id".equals(language)
                            ? Locale.forLanguageTag("id-ID")
                            : Locale.forLanguageTag(language);
                }
            }
        }
        return Locale.KOREAN;
    }

    private void prepareBlockedResponse(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "no-store, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("X-Robots-Tag", "noindex, nofollow, noarchive");
    }

    private void writeJson(HttpServletResponse response, Locale locale)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("success", false);
        body.put("code", ERROR_CODE);
        body.put("message", message("feature.mobileAccess.message", locale));
        body.put("help", message("feature.mobileAccess.help", locale));
        objectMapper.writeValue(response.getWriter(), body);
    }

    private String message(String code, Locale locale) {
        return messageSource.getMessage(code, null, code, locale);
    }

    private boolean isSignedServerIntegration(HttpServletRequest request,
                                              String path) {
        if ("POST".equalsIgnoreCase(request.getMethod())
                && (ViewerIntegrationProperties.CALLBACK_PATH.equals(path)
                || DistributionAccountIntegrationProperties.REQUEST_PATH
                        .equals(path))) {
            return true;
        }
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String statusPrefix =
                DistributionAccountIntegrationProperties.REQUEST_PATH + "/";
        if (!path.startsWith(statusPrefix)) {
            return false;
        }
        String requestId = path.substring(statusPrefix.length());
        return !requestId.isEmpty() && requestId.indexOf('/') < 0;
    }

    private String applicationPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()
                && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}

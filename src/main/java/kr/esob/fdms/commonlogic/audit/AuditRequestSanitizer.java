package kr.esob.fdms.commonlogic.audit;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.HandlerMapping;

/**
 * Prevents one-time viewer/download capabilities from becoming durable audit
 * data. Mapped controller patterns are preferred because they retain useful
 * route context without retaining path-variable values.
 */
public final class AuditRequestSanitizer {
    private static final String REDACTED_SEGMENT = "{redacted}";

    private AuditRequestSanitizer() {
    }

    public static String safeRequestUri(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        Object mappedPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (mappedPattern != null && !mappedPattern.toString().trim().isEmpty()) {
            return mappedPattern.toString().trim();
        }
        return maskSecretPathSegments(request.getRequestURI());
    }

    static String maskSecretPathSegments(String requestUri) {
        if (requestUri == null || requestUri.isEmpty()) {
            return requestUri;
        }

        String lowerUri = requestUri.toLowerCase(Locale.ROOT);
        boolean capabilityRoute = lowerUri.contains("download")
                || lowerUri.contains("viewer")
                || lowerUri.contains("pdf-cache")
                || lowerUri.contains("ticket")
                || lowerUri.contains("token")
                || lowerUri.contains("capability");
        String[] segments = requestUri.split("/", -1);
        for (int index = 0; index < segments.length; index++) {
            String previous = index == 0 ? "" : segments[index - 1].toLowerCase(Locale.ROOT);
            if (isSecretLabel(previous)
                    || (capabilityRoute && looksLikeCapability(segments[index]))) {
                segments[index] = REDACTED_SEGMENT;
            }
        }
        return String.join("/", segments);
    }

    private static boolean isSecretLabel(String segment) {
        return segment.contains("ticket")
                || segment.contains("token")
                || segment.contains("capability")
                || segment.endsWith("key")
                || "pdf-cache".equals(segment);
    }

    private static boolean looksLikeCapability(String segment) {
        if (segment == null) {
            return false;
        }
        return segment.matches("(?i)[0-9a-f]{32,}")
                || segment.matches("(?i)[0-9a-f]{8}-[0-9a-f-]{27,}")
                || segment.matches("[A-Za-z0-9_-]{40,}");
    }
}

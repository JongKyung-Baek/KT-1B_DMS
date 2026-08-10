package kr.esob.tdms.commonlogic.menu;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Central policy for URLs stored in {@code DOCS_MENU}.
 *
 * <p>Menu links are application routes, never arbitrary browser destinations.
 * Null and blank values remain valid for structural menu nodes. Non-blank
 * values must be same-origin absolute paths such as {@code /general/history/}
 * or Spring Security patterns such as {@code /general/history/**}.</p>
 */
public final class MenuUrlPolicy {
    private static final String INVALID_MESSAGE =
            "Menu URL must be an internal absolute path.";
    private static final Pattern ENCODED_CONTROL =
            Pattern.compile("%(?:0[0-9a-f]|1[0-9a-f]|7f)");

    private MenuUrlPolicy() {
    }

    public static String normalizeForStorage(String menuUrl) {
        if (menuUrl == null) {
            return null;
        }

        String normalized = menuUrl.trim();
        if (normalized.isEmpty()) {
            return "";
        }

        validateInternalPath(normalized);
        return normalized;
    }

    /**
     * Returns a safe link target, or {@code null} when a legacy database row
     * contains an invalid target. This disables only the link; it does not
     * remove the menu row used by tree and authorization logic.
     */
    public static String safeNavigationUrlOrNull(String menuUrl) {
        try {
            return normalizeForStorage(menuUrl);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static void validateInternalPath(String menuUrl) {
        String lower = menuUrl.toLowerCase(Locale.ROOT);
        if (!menuUrl.startsWith("/")
                || menuUrl.startsWith("//")
                || menuUrl.indexOf('\\') >= 0
                || menuUrl.indexOf('?') >= 0
                || menuUrl.indexOf('#') >= 0
                || lower.contains("%5c")
                || lower.contains("%2f")
                || ENCODED_CONTROL.matcher(lower).find()) {
            throw new IllegalArgumentException(INVALID_MESSAGE);
        }

        for (int index = 0; index < menuUrl.length(); index++) {
            char value = menuUrl.charAt(index);
            if (Character.isISOControl(value)
                    || Character.isWhitespace(value)
                    || Character.getType(value) == Character.FORMAT
                    || Character.getType(value) == Character.LINE_SEPARATOR
                    || Character.getType(value) == Character.PARAGRAPH_SEPARATOR) {
                throw new IllegalArgumentException(INVALID_MESSAGE);
            }
        }

        try {
            URI uri = new URI(menuUrl);
            if (uri.isAbsolute()
                    || uri.getRawAuthority() != null
                    || uri.getRawQuery() != null
                    || uri.getRawFragment() != null
                    || !menuUrl.equals(uri.getRawPath())) {
                throw new IllegalArgumentException(INVALID_MESSAGE);
            }

            String decodedPath = uri.getPath();
            if (decodedPath == null) {
                throw new IllegalArgumentException(INVALID_MESSAGE);
            }
            for (String segment : decodedPath.split("/", -1)) {
                if (".".equals(segment) || "..".equals(segment)) {
                    throw new IllegalArgumentException(INVALID_MESSAGE);
                }
            }
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(INVALID_MESSAGE, exception);
        }
    }
}

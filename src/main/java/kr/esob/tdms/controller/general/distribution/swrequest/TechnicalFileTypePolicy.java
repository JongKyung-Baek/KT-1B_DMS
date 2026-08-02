package kr.esob.tdms.controller.general.distribution.swrequest;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * File-name policy for technical-data registration and original downloads.
 * Browser accept attributes are only a usability aid; server-side callers must
 * use this policy as the source of truth.
 */
public final class TechnicalFileTypePolicy {
    public static final int MAX_FILE_NAME_LENGTH = 255;

    private static final Set<String> ALLOWED_EXTENSIONS = Collections.unmodifiableSet(
            new LinkedHashSet<>(List.of(
                    "pdf",
                    "docx", "xlsx", "pptx",
                    "hwp", "hwpx",
                    "odt", "ods", "odp", "rtf",
                    "txt", "csv", "xml", "json", "md",
                    "stp", "step",
                    "jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff")));

    private TechnicalFileTypePolicy() {
    }

    public static Set<String> allowedExtensions() {
        return ALLOWED_EXTENSIONS;
    }

    public static String acceptAttribute() {
        return ALLOWED_EXTENSIONS.stream()
                .map(extension -> "." + extension)
                .collect(Collectors.joining(","));
    }

    public static String extensionOf(String pathOrName) {
        if (pathOrName == null) {
            return "";
        }
        String normalized = pathOrName.trim().replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        String fileName = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0 || dot == fileName.length() - 1) {
            return "";
        }
        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return extension.matches("[a-z0-9]{1,16}") ? extension : "";
    }

    public static boolean isAllowedFileName(String fileName) {
        if (fileName == null) {
            return false;
        }
        String normalized = fileName.trim();
        if (normalized.isEmpty() || normalized.length() > MAX_FILE_NAME_LENGTH
                || normalized.indexOf('/') >= 0 || normalized.indexOf('\\') >= 0
                || containsControlCharacter(normalized)) {
            return false;
        }
        return ALLOWED_EXTENSIONS.contains(extensionOf(normalized));
    }

    public static boolean isAllowedStoredPath(String path) {
        if (path == null) {
            return false;
        }
        String normalized = path.trim().replace('\\', '/');
        if (normalized.isEmpty() || containsControlCharacter(normalized)) {
            return false;
        }
        int slash = normalized.lastIndexOf('/');
        String fileName = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        return isAllowedFileName(fileName);
    }

    public static boolean hasMatchingAllowedExtension(String originalFileName, String storedPath) {
        if (!isAllowedFileName(originalFileName) || !isAllowedStoredPath(storedPath)) {
            return false;
        }
        return extensionOf(originalFileName).equals(extensionOf(storedPath));
    }

    public static boolean isPdf(String pathOrName) {
        return "pdf".equals(extensionOf(pathOrName));
    }

    public static boolean isStep(String pathOrName) {
        String extension = extensionOf(pathOrName);
        return "stp".equals(extension) || "step".equals(extension);
    }

    public static boolean isViewerPreview(String pathOrName) {
        return isPdf(pathOrName) || isStep(pathOrName);
    }

    public static String[] splitRepositoryPath(String path) {
        if (path == null) {
            return null;
        }
        String normalized = path.trim().replace('\\', '/');
        if (normalized.isEmpty() || normalized.startsWith("/")
                || normalized.matches("^[A-Za-z]:/.*")
                || containsControlCharacter(normalized)) {
            return null;
        }
        int separator = normalized.indexOf('/');
        if (separator <= 0 || separator != normalized.lastIndexOf('/')
                || separator == normalized.length() - 1) {
            return null;
        }
        String folder = normalized.substring(0, separator);
        String fileName = normalized.substring(separator + 1);
        if (!folder.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,127}")
                || folder.contains("..") || !isAllowedFileName(fileName)) {
            return null;
        }
        return new String[] { folder, fileName };
    }

    private static boolean containsControlCharacter(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}

package kr.esob.tdms.controller.general.distribution.swrequest;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * File-name and preview policy for technical-data registration.
 *
 * Registration intentionally accepts every safely identifiable extension. The
 * preview sets below only decide whether a stored original may be sent to a
 * viewer or to the PDF converter; they are not a registration allowlist.
 */
public final class TechnicalFileTypePolicy {
    public static final int MAX_FILE_NAME_LENGTH = 255;

    private static final Set<String> PDF_CONVERSION_EXTENSIONS = Collections.unmodifiableSet(
            new LinkedHashSet<>(List.of(
                    "doc", "docx", "xls", "xlsx", "ppt", "pptx", "hwp", "hwpx", "msg",
                    "png", "jpg", "jpeg", "jpe", "jfif", "gif", "bmp", "dib", "tif", "tiff",
                    "webp", "ico", "cur", "tga", "pcx", "pbm", "pgm", "ppm", "pnm", "pam",
                    "psd", "8pbs", "cal", "cals", "g4", "cg4", "dcx", "pict", "ras", "rle",
                    "sgi", "xbm", "xpm", "xwd", "pcd", "mac", "pntg", "cut", "bitmap", "bm",
                    "tpic", "wd", "iff", "pct", "clp", "img", "brk", "fs", "gl", "ica", "msp",
                    "dxf", "dwg", "hgl", "hpgl", "plt", "svg", "svgz", "eps", "epsf", "epi",
                    "wmf", "wpg", "pal")));

    private static final Set<String> DIRECT_PDF_EXTENSIONS = Set.of("pdf");
    private static final Set<String> DIRECT_STEP_EXTENSIONS = Set.of("stp", "step");

    private TechnicalFileTypePolicy() {
    }

    public static Set<String> allowedExtensions() {
        LinkedHashSet<String> extensions = new LinkedHashSet<>();
        extensions.addAll(DIRECT_PDF_EXTENSIONS);
        extensions.addAll(PDF_CONVERSION_EXTENSIONS);
        extensions.addAll(DIRECT_STEP_EXTENSIONS);
        return Collections.unmodifiableSet(extensions);
    }

    public static Set<String> pdfConversionExtensions() {
        return PDF_CONVERSION_EXTENSIONS;
    }

    public static Set<String> directPdfExtensions() {
        return DIRECT_PDF_EXTENSIONS;
    }

    public static Set<String> directStepExtensions() {
        return DIRECT_STEP_EXTENSIONS;
    }

    public static String extensionCsv(Set<String> extensions) {
        return String.join(",", extensions);
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

    public static boolean isSafeFileName(String fileName) {
        if (fileName == null) {
            return false;
        }
        String normalized = fileName.trim();
        if (normalized.isEmpty() || normalized.length() > MAX_FILE_NAME_LENGTH
                || normalized.indexOf('/') >= 0 || normalized.indexOf('\\') >= 0
                || containsControlCharacter(normalized)) {
            return false;
        }
        return !extensionOf(normalized).isEmpty();
    }

    /**
     * Kept for existing download and popup callers. "Allowed" now means safe
     * to store/download, not supported by the viewer.
     */
    public static boolean isAllowedFileName(String fileName) {
        return isSafeFileName(fileName);
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
        return DIRECT_PDF_EXTENSIONS.contains(extensionOf(pathOrName));
    }

    public static boolean isStep(String pathOrName) {
        return DIRECT_STEP_EXTENSIONS.contains(extensionOf(pathOrName));
    }

    public static boolean requiresPdfConversion(String pathOrName) {
        return PDF_CONVERSION_EXTENSIONS.contains(extensionOf(pathOrName));
    }

    public static boolean isViewerProcessable(String pathOrName) {
        return isViewerPreview(pathOrName) || requiresPdfConversion(pathOrName);
    }

    public static boolean isViewerPreview(String pathOrName) {
        return isPdf(pathOrName) || isStep(pathOrName);
    }

    public static String initialProcessingStatus(String pathOrName) {
        if (isViewerPreview(pathOrName)) {
            return "DONE";
        }
        return requiresPdfConversion(pathOrName) ? "PENDING" : "NOT_VIEWABLE";
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

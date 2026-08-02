package kr.esob.tdms.commonlogic.viewerintegration;

import java.util.Locale;

/** External viewer selected for a prepared technical-data file. */
public enum ViewerProvider {
    PDF("PDF", ".pdf", "application/pdf"),
    STEP("STEP", ".stp", "model/step");

    private final String code;
    private final String temporarySuffix;
    private final String contentType;

    ViewerProvider(String code, String temporarySuffix, String contentType) {
        this.code = code;
        this.temporarySuffix = temporarySuffix;
        this.contentType = contentType;
    }

    public String getCode() {
        return code;
    }

    public String getTemporarySuffix() {
        return temporarySuffix;
    }

    public String getContentType() {
        return contentType;
    }

    public static boolean isStepFileName(String pathOrName) {
        String extension = extensionOf(pathOrName);
        return "stp".equals(extension) || "step".equals(extension);
    }

    private static String extensionOf(String pathOrName) {
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
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}

package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class MobileAccessBlockPresentationContractTest {
    private static final Path VIEW = Path.of(
            "src/main/webapp/WEB-INF/views/error/mobileAccessBlocked.jsp");
    private static final Path CSS = Path.of(
            "src/main/resources/static/css/pages/error-page.css");
    private static final Path KAI_LOGO = Path.of(
            "src/main/resources/static/images/brand/kai-logo.png");
    private static final Path ALTERNATE_LOGO = Path.of(
            "src/main/resources/static/images/brand/esobsoft-logo-blue.png");

    @Test
    void mobileBlockPageIsSessionlessAndUsesCurrentSafeErrorDesign()
            throws Exception {
        String view = Files.readString(VIEW, StandardCharsets.UTF_8);
        String css = Files.readString(CSS, StandardCharsets.UTF_8);
        byte[] logo = Files.readAllBytes(KAI_LOGO);
        byte[] alternateLogo = Files.readAllBytes(ALTERNATE_LOGO);

        assertTrue(view.contains("session=\"false\""));
        assertTrue(view.contains("<body class=\"error-page\""));
        assertTrue(view.contains("class=\"error-card\""));
        assertTrue(view.contains("class=\"error-status-chip\""));
        assertTrue(view.contains("class=\"error-help\""));
        assertTrue(view.contains("HTTP 403"));
        assertTrue(view.contains("/resources/css/pages/error-page.css?v=20260804.1"));
        assertTrue(view.contains("value=\"${mobileBlockLogoPath}\""));
        assertTrue(view.contains("${mobileBlockLogoAlt}"));
        assertTrue(view.contains("${mobileBlockWideLogo}"));
        assertTrue(view.contains("${mobileBlockAlternateBrand}"));
        assertTrue(view.contains("/resources/images/brand/kai-logo.png?v=20260802.1"));
        assertFalse(view.contains("/resources/images/favicon/favicon.svg"));
        assertTrue(view.contains("/WEB-INF/jspf/favicon.jspf"));
        assertTrue(view.contains("<c:out value=\"${mobileBlockTitle}\""));
        assertTrue(view.contains("class=\"mobile-access-title\""));
        assertTrue(view.contains("<c:out value=\"${mobileBlockTitleLine1}\""));
        assertTrue(view.contains("<c:out value=\"${mobileBlockTitleLine2}\""));
        assertTrue(css.matches(
                "(?s).*\\.mobile-access-title\\s+span\\s*\\{[^}]*"
                        + "display\\s*:\\s*block\\s*;[^}]*}.*"));
        assertTrue(logo.length > 8);
        assertArrayEquals(new byte[] {
                (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
        }, Arrays.copyOf(logo, 8));
        assertArrayEquals(new byte[] {
                (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
        }, Arrays.copyOf(alternateLogo, 8));
        assertTrue(view.contains("<c:out value=\"${mobileBlockMessage}\""));
        assertFalse(view.contains("data-error-back"));
        assertFalse(view.contains("error-button"));
        assertFalse(view.contains("${exception}"));
        assertFalse(view.contains("${message}"));
    }
}

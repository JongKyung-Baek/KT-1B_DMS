package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class TdmsBrandThemeContractTest {

    private static final List<String> CURRENT_PAGE_STYLES = Arrays.asList(
            "technical-data-dashboard.css",
            "technical-data-list.css",
            "access-history.css",
            "audit-log.css",
            "organization-management.css",
            "partner-management.css",
            "role-vuexy.css",
            "roleassign-vuexy.css",
            "tree-management.css",
            "distribution-workflow.css",
            "distribution-account-request.css",
            "error-page.css",
            "popup-vuexy-edit-user.css");

    @Test
    void sharedThemeDefinesTheCanonicalKaiBluePalette() throws Exception {
        String theme = read(Paths.get(
                "src/main/resources/static/css/tdms-theme.css"));
        String fontCss = read(Paths.get(
                "src/main/resources/static/css/custom-font.css"));

        assertTrue(theme.contains("--tdms-primary: #034c8c;"));
        assertTrue(theme.contains("--tdms-primary-dark: #023e73;"));
        assertTrue(theme.contains("--tdms-primary-rgb: 3, 76, 140;"));
        assertTrue(theme.contains("--bs-primary-rgb: var(--tdms-primary-rgb);"));
        assertTrue(fontCss.startsWith(
                "@import url(\"./tdms-theme.css?v=20260802.1\");"));
    }

    @Test
    void currentScreensDoNotReintroduceTheLegacyVuexyPrimary() throws Exception {
        Path pageStyles = Paths.get("src/main/resources/static/css/pages");
        for (String fileName : CURRENT_PAGE_STYLES) {
            String source = read(pageStyles.resolve(fileName)).toLowerCase();
            assertFalse(source.contains("#7367f0"), fileName);
            assertFalse(source.contains("#5b50d6"), fileName);
            assertFalse(source.contains("rgba(115, 103, 240"), fileName);
            assertFalse(source.contains("rgba(115,103,240"), fileName);
        }
    }

    @Test
    void faviconUsesTheKaiAssetsAtExpectedSizes() throws Exception {
        Path faviconDirectory = Paths.get(
                "src/main/resources/static/images/favicon");
        BufferedImage favicon = ImageIO.read(
                faviconDirectory.resolve("favicon-32x32.png").toFile());
        BufferedImage touchIcon = ImageIO.read(
                faviconDirectory.resolve("apple-touch-icon.png").toFile());
        String include = read(Paths.get(
                "src/main/webapp/WEB-INF/jspf/favicon.jspf"));

        assertNotNull(favicon);
        assertEquals(32, favicon.getWidth());
        assertEquals(32, favicon.getHeight());
        assertNotNull(touchIcon);
        assertEquals(180, touchIcon.getWidth());
        assertEquals(180, touchIcon.getHeight());
        assertTrue(Files.size(faviconDirectory.resolve("favicon.ico")) > 1024L);
        assertTrue(include.contains("favicon-32x32.png?v=20260802.2"));
        assertFalse(include.contains("favicon.svg"));
    }

    private String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}

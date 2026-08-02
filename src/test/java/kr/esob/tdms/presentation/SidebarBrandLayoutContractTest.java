package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SidebarBrandLayoutContractTest {

    @Test
    void sidebarUsesOfficialKaiLogoOnlyAndLeavesCollapsedDesktopLayout()
            throws IOException {
        String css = Files.readString(Path.of(
                "src", "main", "resources", "static", "css", "custom-font.css"),
                StandardCharsets.UTF_8);
        String sidebar = Files.readString(Path.of(
                "src", "main", "webapp", "left.jsp"), StandardCharsets.UTF_8);
        String mainDecorator = Files.readString(Path.of("src", "main", "webapp",
                "WEB-INF", "decorator", "decoratorMain.jsp"),
                StandardCharsets.UTF_8);
        Path kaiLogo = Path.of("src", "main", "resources", "static",
                "images", "brand", "kai-logo.png");

        assertTrue(sidebar.contains("aria-label=\"KAI dashboard\""));
        assertTrue(sidebar.contains("class=\"app-brand-kai-logo\""));
        assertTrue(sidebar.contains("/resources/images/brand/kai-logo.png"));
        assertTrue(sidebar.contains("width=\"72\" height=\"44\" alt=\"KAI\""));
        assertFalse(sidebar.contains("KT-1B TDMS"));
        assertFalse(sidebar.contains("class=\"app-brand-text"));
        assertTrue(Files.isRegularFile(kaiLogo));
        assertTrue(Files.size(kaiLogo) > 0);
        assertTrue(css.contains("#layout-menu .app-brand-kai-logo"));
        assertTrue(css.contains("object-fit: contain"));
        assertTrue(mainDecorator.contains("custom-font.css?v=20260802.2"));
        assertTrue(css.contains("@media (min-width: 1200px)"));
        assertTrue(css.contains(".layout-menu-collapsed:not(.layout-menu-hover)"));
        assertTrue(css.contains("#layout-menu .app-brand-link"));
        assertTrue(css.contains("display: none"));
    }
}

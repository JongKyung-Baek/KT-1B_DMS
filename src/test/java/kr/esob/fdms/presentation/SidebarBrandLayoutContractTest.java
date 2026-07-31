package kr.esob.fdms.presentation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SidebarBrandLayoutContractTest {

    @Test
    void sidebarBrandNeverWrapsAndLeavesCollapsedDesktopLayout() throws IOException {
        String css = Files.readString(Path.of(
                "src", "main", "resources", "static", "css", "custom-font.css"),
                StandardCharsets.UTF_8);
        String sidebar = Files.readString(Path.of(
                "src", "main", "webapp", "left.jsp"), StandardCharsets.UTF_8);

        assertTrue(sidebar.contains("class=\"app-brand-text demo menu-text fw-bold ms-2\""));
        assertTrue(sidebar.contains("aria-label=\"KT-1B DMS dashboard\""));
        assertTrue(css.contains("#layout-menu .app-brand-text"));
        assertTrue(css.contains("white-space: nowrap"));
        assertTrue(css.contains("@media (min-width: 1200px)"));
        assertTrue(css.contains(".layout-menu-collapsed:not(.layout-menu-hover)"));
        assertTrue(css.contains("#layout-menu .app-brand-link"));
        assertTrue(css.contains("display: none"));
    }
}

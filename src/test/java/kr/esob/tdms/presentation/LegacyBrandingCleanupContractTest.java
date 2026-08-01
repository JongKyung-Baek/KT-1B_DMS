package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class LegacyBrandingCleanupContractTest {

    private static final Pattern RETIRED_BRAND = Pattern.compile(
            "(?i)((?<![a-z])KARI(?![a-z])|"
                    + "hanwha|한화|collabhub|collabview|"
                    + "wowsoft|와우소프트|이솝소프트|demo\\.esob|"
                    + "Esob Document|Esob Co\\.|By Esob|exEsob|"
                    + "Korea Aerospace Industries|한국항공우주연구원|"
                    + "다목적실용위성)");

    private static final Set<String> TEXT_EXTENSIONS = Set.of(
            ".java", ".jsp", ".html", ".htm", ".properties",
            ".css", ".js", ".xml", ".sql", ".md", ".bat",
            ".cmd", ".ps1", ".yaml", ".yml");

    @Test
    void activeRuntimeAndPresentationSourcesContainNoRetiredBrand()
            throws IOException {
        List<Path> roots = List.of(
                Path.of("src", "main", "java"),
                Path.of("src", "main", "webapp"),
                Path.of("src", "main", "resources", "templates"),
                Path.of("docs"),
                Path.of("deployment"));

        for (Path root : roots) {
            try (Stream<Path> paths = Files.walk(root)) {
                paths.filter(Files::isRegularFile)
                        .filter(this::isTextSource)
                        .forEach(this::assertNoRetiredBrand);
            }
        }

        assertNoRetiredBrand(Path.of(
                "src", "main", "resources", "static", "css",
                "style.css"));
        assertNoRetiredBrand(Path.of(
                "src", "main", "resources", "static", "js",
                "common.js"));
        assertNoRetiredBrand(Path.of(".gitignore"));
        assertNoRetiredBrand(Path.of("pom.xml"));
    }

    @Test
    void retiredBrandAssetsAndLegacyModulesAreAbsent() throws IOException {
        assertFalse(Files.exists(Path.of(
                "src", "main", "resources", "templates", "mail",
                "mps8_kari_document_notification_email_template.html")));
        assertFalse(Files.exists(Path.of(
                "src", "main", "webapp", "WEB-INF", "views", "general",
                "pdm", "deployhistory")));
        assertFalse(Files.exists(Path.of(
                "src", "main", "java", "kr", "esob", "tdms",
                "controller", "general", "pdm", "deployhistory")));
        assertFalse(Files.exists(Path.of(
                "src", "main", "resources", "sqlMaps", "oracle", "its",
                "controller", "general", "pdm", "PdmDeployHistory.xml")));
        assertFalse(Files.exists(Path.of(
                "src", "main", "webapp", "p1_mydoc.html")));

        Path messages = Path.of("src", "main", "webapp", "messages");
        try (Stream<Path> paths = Files.walk(messages)) {
            assertFalse(paths.anyMatch(path -> path.getFileName().toString()
                    .endsWith(".mine")));
        }
    }

    @Test
    void retiredBinaryAndGeneratedArtifactsAreAbsent() throws IOException {
        assertFalse(Files.exists(Path.of("E_PDF")));
        assertFalse(Files.exists(Path.of("260701dumpdb3.backup")));
        assertTrue(Files.isRegularFile(Path.of(
                "db", "260701dumpdb3.backup")));
        assertTrue(Files.isRegularFile(Path.of(
                "deployment", "windows-demo", "assets",
                "demo-document.pdf")));

        Path webappRoot = Path.of("src", "main", "webapp");
        assertFalse(Files.exists(webappRoot.resolve("install")));
        assertFalse(Files.exists(webappRoot.resolve("CR_TEMPLATE.xlsx")));
        assertFalse(Files.exists(webappRoot.resolve("ECR_Template.xls")));
        assertFalse(Files.exists(Path.of(
                "src", "main", "resources", "static", "vuexy",
                "assets", "audio", "Water_Lily.mp3")));

        Path generatedExcel = webappRoot.resolve("excel");
        try (Stream<Path> paths = Files.list(generatedExcel)) {
            assertFalse(paths.anyMatch(path -> path.getFileName().toString()
                    .matches("grid_[0-9]+\\.xlsx")));
        }
    }

    @Test
    void loginUsesKaiLogoAndTdmsBrowserTitle() throws IOException {
        String login = Files.readString(Path.of(
                "src", "main", "webapp", "WEB-INF", "views", "login",
                "login.jsp"), StandardCharsets.UTF_8);

        assertTrue(login.contains("<title>TDMS - Login</title>"));
        assertTrue(login.contains("class=\"kai-logo\""));
        assertTrue(login.contains("src=\"data:image/png;base64,"));
    }

    private boolean isTextSource(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return TEXT_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private void assertNoRetiredBrand(Path path) {
        try {
            String text = Files.readString(path, StandardCharsets.UTF_8);
            if (path.getFileName().toString()
                    .equals("35-install-demo-reference-pdfs.sql")) {
                // Preserve the exact user-supplied sample filename without
                // treating it as active application branding.
                text = text.replace(
                        "이솝소프트(주) 회사소개 및 제품소개서(약식)_202303.pdf",
                        "");
            }
            assertFalse(RETIRED_BRAND.matcher(text).find(),
                    "Retired customer/vendor branding remains in " + path);
        } catch (IOException exception) {
            throw new AssertionError("Cannot inspect " + path, exception);
        }
    }
}

package kr.esob.fdms.deployment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class WindowsOfflinePackageContractTest {

    private static final Path OFFLINE_ROOT =
            Path.of("deployment", "windows-offline");

    @Test
    void composeCannotBuildOrPullAndDoesNotPublishPostgres()
            throws IOException {
        String compose = text("runtime", "compose.yaml");

        assertTrue(compose.contains("image: ${KT1B_APP_IMAGE}"));
        assertTrue(compose.contains("image: ${KT1B_DB_IMAGE}"));
        assertTrue(compose.contains("pull_policy: never"));
        assertTrue(compose.contains(
                "\"127.0.0.1:${KT1B_DEMO_PORT:-3508}:3508\""));
        assertFalse(compose.contains("\n    build:"));

        String dbService = compose.substring(
                compose.indexOf("  db:"),
                compose.indexOf("\n  app:"));
        assertFalse(dbService.contains("\n    ports:"));
    }

    @Test
    void installerLoadsAndVerifiesBundledImagesWithoutNetwork()
            throws IOException {
        String installer = text("INSTALL_AND_RUN.BAT");

        assertTrue(installer.contains(
                "images\\kt1b-dms-offline-images.tar"));
        assertTrue(installer.contains("call \"%CD%\\VERIFY_PACKAGE.BAT\""));
        assertTrue(installer.contains("docker load --input"));
        assertTrue(installer.contains("--no-build --pull never"));
        assertTrue(installer.contains("{{.Os}}/{{.Architecture}}"));
        assertTrue(installer.contains("EXPECTED_APP_IMAGE_ID"));
        assertFalse(installer.contains("docker pull"));
        assertFalse(installer.contains("docker build"));
        assertFalse(installer.contains(" up -d --build"));
    }

    @Test
    void connectedBuilderExportsPinnedImagesAndChecksEmbeddedWar()
            throws IOException {
        String builder = text("Build-OfflinePackage.ps1");

        assertTrue(builder.contains("'image', 'save'"));
        assertTrue(builder.contains(
                "'postgres:17.10-bookworm'"));
        assertTrue(builder.contains(
                "'eclipse-temurin:17-jre-jammy'"));
        assertTrue(builder.contains(
                "--entrypoint sha256sum"));
        assertTrue(builder.contains(
                "if ($imageWarHash -ne $warHash)"));
        assertTrue(builder.contains(
                "$appPlatform -ne 'linux/amd64'"));
        assertTrue(builder.contains(
                "database\\kt1b-demo.backup"));
        assertTrue(builder.contains(
                "$zipHashPath = \"$zipPath.sha256\""));
    }

    @Test
    void packageVerifierRejectsMissingChangedAndEscapingFiles()
            throws IOException {
        String verifier = text("runtime", "Verify-Package.ps1");

        assertTrue(verifier.contains("Get-FileHash"));
        assertTrue(verifier.contains("Checksum mismatch"));
        assertTrue(verifier.contains("Package file is missing"));
        assertTrue(verifier.contains(
                "Checksum path escapes the package"));
    }

    @Test
    void activePagesDoNotLoadTheKnownExternalRuntimeResources()
            throws IOException {
        String sessionPage = Files.readString(Path.of(
                "src", "main", "webapp", "WEB-INF", "views", "inside",
                "system", "session", "sessionManagement.jsp"),
                StandardCharsets.UTF_8);
        String collabPage = Files.readString(Path.of(
                "src", "main", "webapp", "CollabviewInstallPage.jsp"),
                StandardCharsets.UTF_8);
        String customFont = Files.readString(Path.of(
                "src", "main", "resources", "static", "css",
                "custom-font.css"), StandardCharsets.UTF_8);

        assertFalse(sessionPage.contains(
                "https://code.jquery.com/jquery-3.6.0.min.js"));
        assertTrue(sessionPage.contains(
                "/resources/js/jquery-3.4.1.min.js"));
        assertFalse(collabPage.contains("demo.esob.kr"));
        assertTrue(collabPage.contains(
                "${pageContext.request.contextPath}/login/loginPage"));
        assertFalse(customFont.contains(
                "/resources/fonts/pretendard/"));
        assertTrue(customFont.contains(
                "url(\"fonts/Pretendard-Regular.woff2\")"));
    }

    private String text(String first, String... more) throws IOException {
        return Files.readString(
                OFFLINE_ROOT.resolve(Path.of(first, more)),
                StandardCharsets.UTF_8);
    }
}

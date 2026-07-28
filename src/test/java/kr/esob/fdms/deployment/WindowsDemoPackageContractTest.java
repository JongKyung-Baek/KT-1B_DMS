package kr.esob.fdms.deployment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class WindowsDemoPackageContractTest {

    private static final Path DEMO_ROOT =
            Path.of("deployment", "windows-demo");

    @Test
    void composeUsesPinnedJavaAndPostgresAndOnlyPublishesLoopbackWebPort()
            throws IOException {
        String dockerfile = text("Dockerfile");
        String compose = text("runtime", "compose.yaml");

        assertTrue(dockerfile.contains("FROM eclipse-temurin:17-jre-jammy"));
        assertTrue(compose.contains("image: postgres:17.10-bookworm"));
        assertTrue(compose.contains(
                "\"127.0.0.1:${KT1B_DEMO_PORT:-3508}:3508\""));
        assertTrue(compose.contains(
                "KT1B_DB_URL: jdbc:postgresql://db:5432/kt1b"));
        assertTrue(compose.contains("KT1B_DB_USERNAME: kt1b_demo"));
        assertTrue(compose.contains("system_config_group='DEMO_CONFIG'"));

        String dbService = compose.substring(
                compose.indexOf("  db:"),
                compose.indexOf("\n  app:"));
        assertFalse(dbService.contains("\n    ports:"),
                "The demo database must not publish a host port.");
    }

    @Test
    void installerChecksDockerBackupLinuxModeAndApplicationReadiness()
            throws IOException {
        String installer = text("INSTALL_AND_RUN.BAT");
        String readme = text("README-FIRST.txt");

        assertTrue(installer.contains("database\\kt1b-demo.backup"));
        assertTrue(installer.contains("docker compose version"));
        assertTrue(installer.contains("{{.OSType}}"));
        assertTrue(installer.contains("INSTALL_AND_RUN.BAT 3510"));
        assertTrue(installer.contains("/login/loginPage"));
        assertTrue(installer.contains("up -d --build --remove-orphans"));
        assertTrue(installer.contains("Demo is already running."));
        assertTrue(installer.contains(
                "exec -T db bash /docker-entrypoint-initdb.d/"
                        + "40-configure-demo-port.sh"));
        assertTrue(readme.contains("아이디: admin"));
        assertTrue(readme.contains("비밀번호: esob!"));
        assertFalse(readme.contains("비밀번호: /esob!"));
    }

    @Test
    void resetCanOnlyRemoveTheNamedDemoDatabaseVolume() throws IOException {
        String reset = text("RESET_DEMO_DATA.BAT");

        assertTrue(reset.contains("set \"DB_VOLUME=kt1b-dms-demo-db-data\""));
        assertTrue(reset.contains("if not \"%CONFIRM%\"==\"RESET\""));
        assertTrue(reset.contains("docker volume rm \"%DB_VOLUME%\""));
        assertFalse(reset.toLowerCase().contains("volume prune"));
        assertFalse(reset.toLowerCase().contains("system prune"));
    }

    @Test
    void databaseInitializersAreLinuxSafeAndSampleResetSkipsRemovedTable()
            throws IOException {
        List<Path> shellScripts = List.of(
                DEMO_ROOT.resolve(Path.of("database", "10-restore-demo.sh")),
                DEMO_ROOT.resolve(Path.of("database", "20-create-app-user.sh")),
                DEMO_ROOT.resolve(Path.of("database", "40-configure-demo-port.sh")));

        for (Path script : shellScripts) {
            byte[] bytes = Files.readAllBytes(script);
            String content = new String(bytes, StandardCharsets.UTF_8);
            assertTrue(content.startsWith("#!/usr/bin/env bash"));
            assertFalse(content.contains("\r"),
                    "Linux init script contains CRLF: " + script);
        }

        String sampleSql = Files.readString(
                Path.of("src", "main", "resources", "sql",
                        "sample_demo_data.sql"),
                StandardCharsets.UTF_8);
        assertFalse(sampleSql.contains("docs_bbs"));
    }

    private String text(String first, String... more) throws IOException {
        Path path = DEMO_ROOT.resolve(Path.of(first, more));
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}

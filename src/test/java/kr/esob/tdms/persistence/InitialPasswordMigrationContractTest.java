package kr.esob.tdms.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class InitialPasswordMigrationContractTest {

    private static final Path MIGRATION = Path.of(
            "src", "main", "resources", "sql", "initial_password_ddl.sql");
    private static final Path MANIFEST = Path.of(
            "src", "main", "resources", "sql", "fresh_database_migration.psql");
    private static final Path DEMO_PORTABILITY = Path.of(
            "deployment", "windows-demo", "database", "30-demo-portability.sql");

    @Test
    void freshMigrationUpsertsTheFixedInitialPassword() throws IOException {
        String migration = read(MIGRATION);
        String manifest = read(MANIFEST);

        assertThat(migration)
                .contains("'BASIC_PASSWORD'")
                .contains("'0000'")
                .contains("ON CONFLICT (system_config_group, system_config_cd)")
                .contains("system_config_value = EXCLUDED.system_config_value");
        assertThat(manifest).contains("\\ir initial_password_ddl.sql");
    }

    @Test
    void demoSanitizationDoesNotEraseTheInitialPassword() throws IOException {
        String portability = read(DEMO_PORTABILITY);

        assertThat(portability)
                .contains("system_config_cd <> 'BASIC_PASSWORD'")
                .contains("'BASIC_PASSWORD'")
                .contains("'0000'");
    }

    private String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}

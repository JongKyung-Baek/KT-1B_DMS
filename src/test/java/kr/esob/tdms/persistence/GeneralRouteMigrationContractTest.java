package kr.esob.tdms.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class GeneralRouteMigrationContractTest {

    private static final Path SQL = Path.of(
            "src", "main", "resources", "sql",
            "general_route_migration_ddl.sql");

    @Test
    void migratesEveryPersistedApplicationRouteWithoutRewritingAuditHistory()
            throws Exception {
        String sql = Files.readString(SQL, StandardCharsets.UTF_8);

        assertTrue(sql.contains("UPDATE docs_menu"));
        assertTrue(sql.contains("UPDATE docs_role_mapping"));
        assertTrue(sql.contains("UPDATE docs_form_info"));
        assertTrue(sql.contains("regexp_replace(menu_url, '^/inside/', '/general/')"));
        assertTrue(sql.contains("regexp_replace(search_url, '^/inside/', '/general/')"));
        assertTrue(sql.contains("menu_url IN ('/inside/**', '/general/**')"));
        assertTrue(sql.contains("system_config_cd = 'ADAP_DOC_FILE_PATH'"));
        assertTrue(!sql.contains("UPDATE docs_access_audit_log"));
    }

    @Test
    void freshMigrationRunsRouteConversionAfterPortalCleanup() throws Exception {
        String manifest = Files.readString(Path.of(
                "src", "main", "resources", "sql",
                "fresh_database_migration.psql"), StandardCharsets.UTF_8);

        assertTrue(manifest.indexOf("internal_only_cleanup_ddl.sql")
                < manifest.indexOf("general_route_migration_ddl.sql"));
        assertTrue(manifest.indexOf("general_route_migration_ddl.sql")
                < manifest.indexOf("sample_demo_data.sql"));
    }
}

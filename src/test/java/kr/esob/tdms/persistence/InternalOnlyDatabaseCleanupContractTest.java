package kr.esob.tdms.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class InternalOnlyDatabaseCleanupContractTest {

    private static final Path CLEANUP = Path.of(
            "src", "main", "resources", "sql",
            "internal_only_cleanup_ddl.sql");

    private static final Pattern RETIRED_BRAND = Pattern.compile(
            "(?i)(\\bKAI\\b|\\bKARI\\b|hanwha|wowsoft|한화|와우소프트)");

    @Test
    void cleanupRemovesExternalMenusAndTheirRoleAssignmentsRecursively()
            throws IOException {
        String sql = read(CLEANUP);

        assertTrue(sql.contains("WITH RECURSIVE removed_menu"));
        assertTrue(sql.contains("WHERE auth_site = 'E'"));
        assertTrue(sql.contains("kt1b_legacy_external_menu"));
        assertTrue(sql.contains("'(^|/)outside/'"));
        assertTrue(sql.contains(
                "'^/(inside|general)/(unregisted|outregisted)(/|$)'"));
        assertTrue(sql.contains(
                "'^/(inside|general)/organizationmanage/(outsideuser|approval)(/|$)'"));
        assertTrue(sql.contains("'MENU_041'"));
        assertTrue(sql.contains("'MENU_124'"));
        assertTrue(sql.contains("'MENU_079'"));
        assertTrue(sql.contains("'MENU_157'"));
        assertTrue(sql.contains("DELETE FROM docs_rel_role_group"));
        assertTrue(sql.contains("DELETE FROM docs_role_mapping"));
        assertTrue(sql.contains(
                "WHERE menu_url IN ('/inside/**', '/general/**')"));
        assertTrue(sql.contains("DELETE FROM docs_menu menu"));
        assertTrue(sql.contains(
                "menu.role_cd = assignment.role_cd"));
        assertTrue(sql.contains(
                "An orphan menu-role assignment remains."));
        assertTrue(sql.contains("DELETE FROM docs_role_group"));
        assertTrue(sql.contains("group_code = 'RG_006'"));
        assertTrue(sql.contains(
                "ALTER TABLE public.docs_menu DROP COLUMN IF EXISTS auth_site"));
        assertTrue(sql.contains(
                "ALTER TABLE public.docs_user DROP COLUMN IF EXISTS auth_site"));
        assertTrue(sql.contains("SET parent_menu_cd = 'ROOT'"));
        assertTrue(sql.contains(
                "'MENU_013', 'MENU_071', 'MENU_214', 'MENU_223',"));
        assertTrue(sql.contains("'MENU_229'"));
        assertTrue(sql.contains(
                "five current navigation roots"));
        assertTrue(sql.contains("toolbarSystemMenu"));
    }

    @Test
    void cleanupNeutralizesCompanyAndMigratesFileApiKeys() throws IOException {
        String cleanup = read(CLEANUP);
        String acl = read(Path.of(
                "src", "main", "resources", "sql",
                "acl_foundation_ddl.sql"));

        assertTrue(cleanup.contains("company_nm = 'KT-1B'"));
        assertTrue(cleanup.contains("company_cd = 'COMP_0000000999'"));
        assertTrue(cleanup.contains("'FILE_DOWNLOAD_URL'"));
        assertTrue(cleanup.contains("'FILE_VIEW_URL'"));
        assertTrue(cleanup.contains(
                "WHERE system_config_cd IN ('KAI_DOWNLOAD', 'KAI_VIEW')"));
        assertTrue(acl.contains(
                "VALUES ('COMP_0000000999', 'KT-1B', 'I'"));
        assertFalse(acl.contains("'KAI'"));
    }

    @Test
    void externalUserRequestSchemaIsRemovedInsteadOfRecreated() throws IOException {
        String cleanup = read(CLEANUP);
        Path obsoleteDdl = Path.of(
                "src", "main", "resources", "sql",
                "docs_user_request_ddl.sql");

        assertTrue(cleanup.contains(
                "DROP TABLE IF EXISTS public.docs_user_request CASCADE"));
        assertTrue(cleanup.contains(
                "DROP TABLE IF EXISTS public.docs_user_request_number CASCADE"));
        assertTrue(cleanup.contains(
                "DROP SEQUENCE IF EXISTS public.docs_external_user_id_sequence CASCADE"));
        assertFalse(Files.exists(obsoleteDdl));
    }

    @Test
    void demoBuilderRunsFreshManifestAndSampleDoesNotPersistPortalMarkers()
            throws IOException {
        String builder = read(Path.of(
                "deployment", "windows-demo", "Build-DemoPackage.ps1"));
        String sample = read(Path.of(
                "src", "main", "resources", "sql", "sample_demo_data.sql"));

        assertTrue(builder.contains("fresh_database_migration.psql"));
        assertTrue(builder.contains("'include_sample_data=true'"));
        assertTrue(sample.contains("company_nm = 'KT-1B'"));
        assertFalse(sample.contains("    auth_site,"));
        assertTrue(sample.contains("table_name IN ('docs_menu', 'docs_user')"));
        assertFalse(sample.contains("docs_user_request,"));
        assertFalse(sample.contains("docs_user_request_number,"));
    }

    @Test
    void retiredHrSynchronizationMapperIsRemoved() {
        assertFalse(Files.exists(Path.of(
                "src", "main", "resources", "sqlMaps", "oracle", "its",
                "controller", "insa", "Insa.xml")));
    }

    @Test
    void sqlAndMapperSourcesContainNoRetiredCustomerBrandOutsideMigration()
            throws IOException {
        Path resources = Path.of("src", "main", "resources");
        try (Stream<Path> paths = Files.walk(resources)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString()
                                .toLowerCase(Locale.ROOT);
                        return name.endsWith(".sql") || name.endsWith(".xml");
                    })
                    .filter(path -> !path.equals(CLEANUP))
                    .forEach(path -> assertNoRetiredBrand(path));
        }

        assertNoRetiredBrand(Path.of(
                "deployment", "windows-demo", "database",
                "30-demo-portability.sql"));
    }

    private void assertNoRetiredBrand(Path path) {
        try {
            assertFalse(RETIRED_BRAND.matcher(read(path)).find(),
                    "Retired customer/vendor brand remains in " + path);
        } catch (IOException exception) {
            throw new AssertionError("Cannot inspect " + path, exception);
        }
    }

    private String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}

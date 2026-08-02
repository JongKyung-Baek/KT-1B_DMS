package kr.esob.tdms.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class DuplicateTechnicalTreeMigrationContractTest {

    private static final Path SQL_DIRECTORY =
            Path.of("src/main/resources/sql");
    private static final Path MIGRATION = SQL_DIRECTORY.resolve(
            "remove_duplicate_technical_tree_tables_ddl.sql");
    private static final Pattern DROP_TABLE = Pattern.compile(
            "(?i)\\bDROP\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?"
                    + "(?:public\\.)?([a-z0-9_]+)\\s*;");

    @Test
    void guardsEmptyDocumentAndFileTablesBeforeDroppingLegacyTrees()
            throws IOException {
        String sql = read(MIGRATION).toLowerCase(Locale.ROOT);

        assertThat(sql)
                .contains("public.docs_product_document")
                .contains("public.docs_product_document_file")
                .contains("public.docs_dxf_document")
                .contains("public.docs_dxf_document_file")
                .contains("select exists (select 1 from %s limit 1)")
                .contains("protected table % is not empty")
                .contains("lock table %s in access exclusive mode");
    }

    @Test
    void requiresExactCanonicalMatchForEveryActiveNonRootRow()
            throws IOException {
        String sql = read(MIGRATION).toLowerCase(Locale.ROOT);

        assertThat(sql)
                .contains("coalesce(legacy.use_yn, 'y') = 'y'")
                .contains("legacy.tree_cd is distinct from 'root'")
                .contains("canonical.tree_cd")
                .contains("is not distinct from legacy.tree_cd")
                .contains("is not distinct from legacy.function_cd")
                .contains("is not distinct from legacy.upper_tree_cd")
                .contains("is not distinct from legacy.tree_nm")
                .contains("is not distinct from legacy.tree_depth")
                .contains("is not distinct from legacy.sort_order")
                .contains("is not distinct from legacy.tree_path")
                .contains("is not distinct from legacy.use_yn")
                .contains("an active non-root row differs");
    }

    @Test
    void permitsOnlyRootDisplayNameDifferenceAndRequiresCanonicalRoot()
            throws IOException {
        String sql = read(MIGRATION).toLowerCase(Locale.ROOT);
        int rootGuard = sql.indexOf("canonical root is missing");
        int rootComparison = sql.indexOf(
                "active root attributes other than tree_nm differ");

        assertThat(sql)
                .contains("from public.docs_sw_tree")
                .contains("where tree_cd = 'root'")
                .contains("legacy.tree_cd is not distinct from 'root'");
        assertThat(rootGuard).isGreaterThanOrEqualTo(0);
        assertThat(rootComparison).isGreaterThan(rootGuard);

        String comparison = sql.substring(
                sql.lastIndexOf("select exists (", rootComparison),
                rootComparison);
        assertThat(comparison)
                .doesNotContain("legacy.tree_nm")
                .contains("legacy.function_cd")
                .contains("legacy.upper_tree_cd")
                .contains("legacy.tree_depth")
                .contains("legacy.sort_order")
                .contains("legacy.tree_path")
                .contains("legacy.use_yn");
    }

    @Test
    void dropsOnlyDuplicateTreeTablesWithoutCascadeAndIsRerunnable()
            throws IOException {
        String sql = read(MIGRATION);
        Matcher matcher = DROP_TABLE.matcher(sql);
        List<String> droppedTables = new java.util.ArrayList<>();
        while (matcher.find()) {
            droppedTables.add(matcher.group(1).toLowerCase(Locale.ROOT));
        }

        assertThat(droppedTables).containsExactly(
                "docs_product_tree", "docs_dxf_tree");
        assertThat(sql).doesNotContainPattern("(?i)\\bCASCADE\\b");
        assertThat(sql)
                .contains("to_regclass('public.docs_product_tree') IS NULL")
                .contains("to_regclass('public.docs_dxf_tree') IS NULL")
                .contains("DROP TABLE IF EXISTS public.docs_product_tree")
                .contains("DROP TABLE IF EXISTS public.docs_dxf_tree")
                .doesNotContain("DROP TABLE IF EXISTS public.docs_sw_tree")
                .doesNotContain("DROP TABLE IF EXISTS public.docs_sw")
                .doesNotContain("DROP TABLE IF EXISTS public.docs_sw_file")
                .doesNotContain(
                        "DROP TABLE IF EXISTS public.docs_product_document")
                .doesNotContain(
                        "DROP TABLE IF EXISTS public.docs_product_document_file")
                .doesNotContain(
                        "DROP TABLE IF EXISTS public.docs_dxf_document")
                .doesNotContain(
                        "DROP TABLE IF EXISTS public.docs_dxf_document_file");
    }

    @Test
    void boundsLockAndStatementWaitsInsideTheMigrationTransaction()
            throws IOException {
        String sql = read(MIGRATION).toLowerCase(Locale.ROOT);

        assertThat(sql.indexOf("begin;"))
                .isLessThan(sql.indexOf("set local lock_timeout = '15s';"));
        assertThat(sql)
                .contains("set local lock_timeout = '15s';")
                .contains("set local statement_timeout = '5min';");
        assertThat(sql.indexOf("set local statement_timeout = '5min';"))
                .isLessThan(sql.indexOf("lock table public.docs_sw_tree"));
    }

    @Test
    void freshManifestRunsMigrationOnceAfterRetiredContentCleanup()
            throws IOException {
        String manifest = read(SQL_DIRECTORY.resolve(
                "fresh_database_migration.psql"));
        String cleanup = read(SQL_DIRECTORY.resolve(
                "remove_unused_content_features_ddl.sql"));
        String include = "\\ir remove_duplicate_technical_tree_tables_ddl.sql";

        assertThat(count(manifest, include)).isEqualTo(1);
        assertThat(manifest.indexOf("remove_unused_content_features_ddl.sql"))
                .isLessThan(manifest.indexOf(include));
        assertThat(cleanup.toLowerCase(Locale.ROOT))
                .doesNotContain("delete from docs_product_tree")
                .doesNotContain("delete from docs_dxf_tree")
                .contains("delete from docs_sw_tree");
    }

    private long count(String value, String token) {
        return Pattern.compile(Pattern.quote(token))
                .matcher(value)
                .results()
                .count();
    }

    private String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}

package kr.esob.tdms.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class FreshDatabaseMigrationContractTest {

    private static final Path SQL_DIRECTORY =
            Path.of("src/main/resources/sql");
    private static final Path MANIFEST =
            SQL_DIRECTORY.resolve("fresh_database_migration.psql");
    private static final Pattern INCLUDE =
            Pattern.compile("(?m)^\\\\ir\\s+(\\S+)\\s*$");

    @Test
    void manifestIncludesEveryFirstPartyDdlExactlyOnce() throws IOException {
        String manifest = Files.readString(MANIFEST, StandardCharsets.UTF_8);
        Matcher matcher = INCLUDE.matcher(manifest);
        List<String> includes = new java.util.ArrayList<>();
        while (matcher.find()) {
            includes.add(matcher.group(1));
        }

        List<String> expectedDdl;
        try (java.util.stream.Stream<Path> files = Files.list(SQL_DIRECTORY)) {
            expectedDdl = files
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith("_ddl.sql"))
                    .sorted()
                    .collect(Collectors.toList());
        }

        assertThat(includes)
                .filteredOn(name -> name.endsWith("_ddl.sql"))
                .containsExactlyInAnyOrderElementsOf(expectedDdl);
        assertThat(includes)
                .doesNotHaveDuplicates()
                .contains("sample_demo_data.sql");
    }

    @Test
    void aclAndPortalCleanupRunBeforeSampleReset() throws IOException {
        String manifest = Files.readString(MANIFEST, StandardCharsets.UTF_8);
        String cleanup = Files.readString(
                SQL_DIRECTORY.resolve("internal_only_cleanup_ddl.sql"),
                StandardCharsets.UTF_8);
        String runner = Files.readString(
                Path.of("db", "Recreate-Kt1bDatabase.ps1"),
                StandardCharsets.UTF_8);

        assertThat(manifest.indexOf("acl_foundation_ddl.sql"))
                .isLessThan(manifest.indexOf("internal_only_cleanup_ddl.sql"));
        assertThat(manifest.indexOf("internal_only_cleanup_ddl.sql"))
                .isLessThan(manifest.indexOf("sample_demo_data.sql"));
        assertThat(manifest)
                .contains("\\set ON_ERROR_STOP on")
                .contains("\\if :include_sample_data");
        assertThat(cleanup)
                .contains("ALTER TABLE public.docs_menu DROP COLUMN IF EXISTS auth_site")
                .contains("ALTER TABLE public.docs_user DROP COLUMN IF EXISTS auth_site")
                .contains("information_schema.columns");
        assertThat(runner)
                .contains("table_name IN ('docs_menu', 'docs_user')")
                .contains("portalSelectorColumns");
    }
}

package kr.esob.tdms.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class PdfConversionMigrationContractTest {
    private static final Path SQL_DIRECTORY = Path.of("src/main/resources/sql");
    private static final Path MIGRATION =
            SQL_DIRECTORY.resolve("pdf_conversion_ddl.sql");

    @Test
    void migrationCreatesAHashIdempotentDurableConversionOutbox()
            throws IOException {
        String sql = Files.readString(MIGRATION, StandardCharsets.UTF_8);

        assertThat(sql)
                .contains("CREATE TABLE IF NOT EXISTS public.docs_pdf_conversion")
                .contains("source_file_name", "source_file_path", "source_size_bytes")
                .contains("source_sha256       char(64) NOT NULL")
                .contains("output_file_name", "output_file_path", "output_size_bytes")
                .contains("output_sha256       char(64)")
                .contains("UNIQUE (object_type, object_id, file_no, source_sha256)")
                .contains("status_cd IN (")
                .contains("'PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED'")
                .contains("'NOT_REQUIRED', 'SUPERSEDED'")
                .contains("attempt_count", "max_attempts", "next_attempt_at")
                .contains("claim_token", "claimed_at", "claim_expires_at")
                .contains("current_yn          boolean NOT NULL DEFAULT TRUE")
                .contains("BEGIN;", "COMMIT;");
    }

    @Test
    void migrationEnforcesOneCurrentSourceAndIndexesReusableResultsAndWork()
            throws IOException {
        String sql = Files.readString(MIGRATION, StandardCharsets.UTF_8);

        assertThat(sql)
                .contains("CREATE UNIQUE INDEX IF NOT EXISTS uq_pdf_conversion_current_file")
                .contains("WHERE current_yn = TRUE")
                .contains("idx_pdf_conversion_reusable_hash")
                .contains("WHERE status_cd IN ('SUCCEEDED', 'NOT_REQUIRED')")
                .contains("idx_pdf_conversion_due")
                .contains("status_cd = 'PENDING'")
                .contains("idx_pdf_conversion_stale_claim")
                .contains("status_cd = 'PROCESSING'")
                .contains("ck_pdf_conversion_claim")
                .contains("ck_pdf_conversion_completed_output");
    }

    @Test
    void migrationRepairsLegacySoftwareSubFileProcessingColumns()
            throws IOException {
        String sql = Files.readString(MIGRATION, StandardCharsets.UTF_8);

        assertThat(sql)
                .contains("ALTER TABLE IF EXISTS public.docs_sw_file")
                .contains("ALTER TABLE IF EXISTS public.docs_sw_sub_file")
                .contains("ADD COLUMN IF NOT EXISTS processing_status varchar(30)")
                .contains("ADD COLUMN IF NOT EXISTS processing_error text")
                .contains("ADD COLUMN IF NOT EXISTS processed_at timestamp with time zone")
                .contains("SET processing_status = 'DONE'")
                .contains("AND NOT EXISTS (")
                .contains("conversion.object_type = 'SW'")
                .contains("conversion.object_type = 'SW_SUB'")
                .contains("ALTER COLUMN processing_status SET DEFAULT 'PENDING'")
                .contains("ALTER COLUMN processing_status SET NOT NULL");
    }

    @Test
    void migrationUpgradesPreviewOutboxAndNormalizesItBeforeAddingConstraints()
            throws IOException {
        String sql = Files.readString(MIGRATION, StandardCharsets.UTF_8);

        assertThat(sql)
                .contains("Upgrade an earlier preview of the outbox in place")
                .contains("ADD COLUMN IF NOT EXISTS claim_token uuid")
                .contains("ADD COLUMN IF NOT EXISTS current_yn boolean")
                .contains("ROW_NUMBER() OVER (")
                .contains("current_yn IS DISTINCT FROM (ranked.source_rank = 1)")
                .contains("status_cd NOT IN (")
                .contains("DO $migration$")
                .contains("conrelid = 'public.docs_pdf_conversion'::regclass")
                .contains("ADD CONSTRAINT uq_pdf_conversion_source_identity")
                .contains("ADD CONSTRAINT ck_pdf_conversion_completed_output")
                .doesNotContain("&gt;", "&lt;");
    }

    @Test
    void freshManifestRunsConversionMigrationBeforeOptionalSampleReset()
            throws IOException {
        String manifest = Files.readString(
                SQL_DIRECTORY.resolve("fresh_database_migration.psql"),
                StandardCharsets.UTF_8);

        assertThat(manifest).containsOnlyOnce("\\ir pdf_conversion_ddl.sql");
        assertThat(manifest.indexOf("viewer_integration_ddl.sql"))
                .isLessThan(manifest.indexOf("pdf_conversion_ddl.sql"));
        assertThat(manifest.indexOf("pdf_conversion_ddl.sql"))
                .isLessThan(manifest.indexOf("sample_demo_data.sql"));
    }
}

package kr.esob.tdms.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ViewerIntegrationMigrationContractTest {

    private static final Path SQL_DIRECTORY = Path.of("src/main/resources/sql");
    private static final Path VIEWER_MIGRATION =
            SQL_DIRECTORY.resolve("viewer_integration_ddl.sql");

    @Test
    void migrationCreatesDurableLaunchEventAndReplayProtectionState()
            throws IOException {
        String sql = Files.readString(VIEWER_MIGRATION, StandardCharsets.UTF_8);

        assertThat(sql)
                .contains("ALTER TABLE public.docs_history")
                .contains("ADD COLUMN IF NOT EXISTS file_no varchar(60)")
                .contains("CREATE TABLE IF NOT EXISTS docs_viewer_launch")
                .contains("CREATE TABLE IF NOT EXISTS docs_viewer_event")
                .contains("ADD COLUMN IF NOT EXISTS source_system_cd")
                .contains("ADD COLUMN IF NOT EXISTS source_event_id")
                .contains("ADD COLUMN IF NOT EXISTS source_correlation_id")
                .contains("uq_docs_history_source_event")
                .contains("CREATE TABLE IF NOT EXISTS docs_viewer_callback_nonce")
                .contains("REFERENCES docs_viewer_launch (correlation_id) ON DELETE CASCADE")
                .contains("UNIQUE (correlation_id, event_type)")
                .contains("idx_viewer_callback_nonce_created")
                .contains("BEGIN;")
                .contains("COMMIT;");
    }

    @Test
    void migrationRetiresLoopbackLegacyConfigAndRejectsDatabaseSecrets()
            throws IOException {
        String sql = Files.readString(VIEWER_MIGRATION, StandardCharsets.UTF_8);

        assertThat(sql)
                .contains("ADAP_PDF_URL", "ADAP_POST_URL")
                .contains("localhost", "127\\.0\\.0\\.1", "\\[::1\\]")
                .contains("ck_docs_system_config_no_viewer_secret")
                .contains("TDMS_VIEWER_SHARED_SECRET")
                .contains("VIEWER_SHARED_SECRET");
    }

    @Test
    void freshDatabaseRunsViewerMigrationBeforeOptionalSampleReset()
            throws IOException {
        String manifest = Files.readString(
                SQL_DIRECTORY.resolve("fresh_database_migration.psql"),
                StandardCharsets.UTF_8);

        assertThat(manifest).containsOnlyOnce("\\ir viewer_integration_ddl.sql");
        assertThat(manifest.indexOf("viewer_integration_ddl.sql"))
                .isLessThan(manifest.indexOf("sample_demo_data.sql"));
    }

    @Test
    void runtimeConfigurationUsesEnvironmentOnlySecretAndFixedContractPaths()
            throws IOException {
        String properties = Files.readString(
                Path.of("src/main/resources/application.properties"),
                StandardCharsets.UTF_8);
        String guide = Files.readString(
                Path.of("docs/viewer-integration.md"),
                StandardCharsets.UTF_8);

        assertThat(properties)
                .contains("tdms.viewer.enabled=${TDMS_VIEWER_ENABLED:false}")
                .contains("tdms.viewer.base-url=${TDMS_VIEWER_BASE_URL:}")
                .contains("tdms.viewer.client-id=${TDMS_VIEWER_CLIENT_ID:}")
                .contains("tdms.viewer.callback-client-id=${TDMS_VIEWER_CALLBACK_CLIENT_ID:}")
                .contains("tdms.viewer.shared-secret=${TDMS_VIEWER_SHARED_SECRET:}")
                .contains("tdms.viewer.work-dir=${TDMS_VIEWER_WORK_DIR:${java.io.tmpdir}/kt1b-viewer}")
                .contains("tdms.viewer.state-retention-days=${TDMS_VIEWER_STATE_RETENTION_DAYS:30}")
                .doesNotContain("tdms.viewer.documents-path=")
                .doesNotContain("tdms.viewer.launch-path=")
                .doesNotContain("ADAP_POST_URL=")
                .doesNotContain("ADAP_PDF_URL=");

        assertThat(guide)
                .contains("POST /api/integrations/tdms/v1/documents")
                .contains("POST /api/integrations/tdms/v1/launch")
                .contains("POST /api/integrations/cv/v1/events")
                .contains("Windows 테스트/데모")
                .contains("AIX 7.3 운영");
    }

    @Test
    void runtimeMapperDeletesExpiredLaunchesSoEventsCascade() throws IOException {
        String mapper = Files.readString(Path.of(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/viewerintegration/ViewerIntegration.xml"),
                StandardCharsets.UTF_8);

        assertThat(mapper)
                .contains("<delete id=\"deleteExpiredState\">")
                .contains("DELETE FROM docs_viewer_launch")
                .contains("CAST(#{value} AS integer) * INTERVAL '1 day'")
                .contains("TO_CHAR(created_at AT TIME ZONE 'UTC'");
    }

    @Test
    void authenticatedViewCallbackPersistsItsExactFileNumberInHistory()
            throws IOException {
        String mapper = Files.readString(Path.of(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/viewerintegration/ViewerIntegration.xml"),
                StandardCharsets.UTF_8);

        assertThat(mapper)
                .contains("<insert id=\"insertViewHistory\">")
                .contains("revision, file_no, user_id, insert_date, user_nm, log_type")
                .contains("#{launch.fileNo}, #{launch.actorUserId}")
                .contains("#{launch.actorUserNm}, 'VIEWING'");
    }
}

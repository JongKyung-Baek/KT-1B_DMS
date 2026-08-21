package kr.esob.tdms.commonlogic.pdfconversion;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class PdfConversionMapperContractTest {
    private static final Path MAPPER = Path.of(
            "src/main/resources/sqlMaps/oracle/its/commonlogic/pdfconversion/PdfConversion.xml");
    private static final Path DAO = Path.of(
            "src/main/java/kr/esob/tdms/commonlogic/pdfconversion/PdfConversionDao.java");

    @Test
    void mapperAndDaoExposeTheCompleteOutboxLifecycle() throws IOException {
        String mapper = read(MAPPER);
        String dao = read(DAO);

        assertThat(mapper)
                .contains("<mapper namespace=\"sql.PdfConversion\">")
                .contains("<select id=\"enqueue\"")
                .contains("<select id=\"selectById\"")
                .contains("<select id=\"selectCurrent\"")
                .contains("<select id=\"selectReusableByHash\"")
                .contains("<select id=\"selectDueIds\"")
                .contains("<select id=\"claim\"")
                .contains("<select id=\"failExpiredExhausted\" resultMap=\"jobMap\">")
                .contains("<update id=\"markSucceeded\">")
                .contains("<update id=\"markRetry\">")
                .contains("<update id=\"markFailed\">")
                .contains("<update id=\"markNotRequired\">")
                .doesNotContain("${");
        assertThat(dao)
                .contains("private static final String PREFIX = \"sql.PdfConversion.\"")
                .contains("PdfConversionJob enqueue(PdfConversionJob job)")
                .contains("List<String> selectDueIds(int limit)")
                .contains("PdfConversionJob claim(Map<String, Object> params)")
                .contains("List<PdfConversionJob> failExpiredExhausted()")
                .contains("int markSucceeded(Map<String, Object> params)")
                .contains("int markRetry(Map<String, Object> params)")
                .contains("int markFailed(Map<String, Object> params)")
                .contains("int markNotRequired(Map<String, Object> params)");
    }

    @Test
    void enqueueIsIdempotentForTheSameSourceAndSupersedesADifferentCurrentSource()
            throws IOException {
        String mapper = read(MAPPER);
        String enqueue = statement(mapper, "<select id=\"enqueue\"", "</select>");

        assertThat(enqueue)
                .contains("WITH logical_file_lock AS MATERIALIZED (")
                .contains("pg_advisory_xact_lock(")
                .contains("hashtextextended(")
                .contains("CONCAT_WS(CHR(31)")
                .contains("FROM logical_file_lock")
                .contains("superseded AS (")
                .contains("current_yn = FALSE")
                .contains("ELSE 'SUPERSEDED'")
                .contains("source_sha256 &lt;&gt; LOWER(#{sourceSha256})")
                .contains("supersede_barrier")
                .contains("ON CONFLICT (object_type, object_id, file_no, source_sha256)")
                .contains("current_yn = TRUE")
                .contains("RETURNING docs_pdf_conversion.*")
                .contains("'SUCCEEDED', 'NOT_REQUIRED'")
                .contains("claim_expires_at &gt; CURRENT_TIMESTAMP");
    }

    @Test
    void enqueueCannotReduceMaxAttemptsBelowAnAttemptCountThatMustBePreserved()
            throws IOException {
        String mapper = read(MAPPER);
        String enqueue = statement(mapper, "<select id=\"enqueue\"", "</select>");

        assertThat(enqueue)
                .contains("GREATEST(COALESCE(NULLIF(#{maxAttempts}, 0), 3), 1)")
                .contains("max_attempts = GREATEST(")
                .contains("THEN docs_pdf_conversion.attempt_count")
                .contains("ELSE 1");
    }

    @Test
    void dueSelectionAndClaimImplementAnAtomicExpiringLease()
            throws IOException {
        String mapper = read(MAPPER);
        String due = statement(mapper, "<select id=\"selectDueIds\"", "</select>");
        String claim = statement(mapper, "<select id=\"claim\"", "</select>");

        assertThat(due)
                .contains("current_yn = TRUE")
                .contains("attempt_count &lt; max_attempts")
                .contains("status_cd = 'PENDING'")
                .contains("status_cd = 'PROCESSING'")
                .contains("claim_expires_at &lt;= CURRENT_TIMESTAMP")
                .contains("LIMIT #{limit}");
        assertThat(claim)
                .contains("WITH claimed AS (")
                .contains("status_cd = 'PROCESSING'")
                .contains("attempt_count = attempt_count + 1")
                .contains("claim_token = CAST(#{claimToken} AS uuid)")
                .contains("GREATEST(CAST(#{leaseSeconds} AS integer), 1)")
                .contains("RETURNING docs_pdf_conversion.*");
    }

    @Test
    void terminalAndRetryUpdatesAreFencedByTheClaimToken()
            throws IOException {
        String mapper = read(MAPPER);

        for (String id : new String[] {"markSucceeded", "markRetry", "markFailed"}) {
            String update = statement(mapper, "<update id=\"" + id + "\">", "</update>");
            assertThat(update)
                    .contains("status_cd = 'PROCESSING'")
                    .contains("claim_token = CAST(#{claimToken} AS uuid)")
                    .contains("updated_at = CURRENT_TIMESTAMP");
        }
        assertThat(statement(mapper, "<update id=\"markSucceeded\">", "</update>"))
                .contains("status_cd = 'SUCCEEDED'")
                .contains("output_sha256 = LOWER(#{outputSha256})")
                .contains("completed_at = CURRENT_TIMESTAMP");
        assertThat(statement(mapper, "<update id=\"markRetry\">", "</update>"))
                .contains("WHEN attempt_count &lt; max_attempts THEN 'PENDING'")
                .contains("ELSE 'FAILED'")
                .contains("retryDelaySeconds");
    }

    @Test
    void expiredFinalAttemptIsClosedInsteadOfRemainingProcessingForever()
            throws IOException {
        String mapper = read(MAPPER);
        String recovery = statement(
                mapper, "<select id=\"failExpiredExhausted\"", "</select>");

        assertThat(recovery)
                .contains("status_cd = 'FAILED'")
                .contains("claim_token = NULL")
                .contains("claimed_at = NULL")
                .contains("claim_expires_at = NULL")
                .contains("completed_at = CURRENT_TIMESTAMP")
                .contains("current_yn = TRUE")
                .contains("status_cd = 'PROCESSING'")
                .contains("claim_expires_at &lt;= CURRENT_TIMESTAMP")
                .contains("attempt_count &gt;= max_attempts")
                .contains("Conversion worker lease expired after all attempts were exhausted.");
    }

    private String statement(String source, String startMarker, String endMarker) {
        int start = source.indexOf(startMarker);
        assertThat(start).isGreaterThanOrEqualTo(0);
        int end = source.indexOf(endMarker, start);
        assertThat(end).isGreaterThan(start);
        return source.substring(start, end + endMarker.length());
    }

    private String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}

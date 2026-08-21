package kr.esob.tdms.commonlogic.pdfconversion;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class PdfConversionProjectionContractTest {

    private static final Path MAPPER = Path.of(
            "src/main/resources/sqlMaps/oracle/its/commonlogic/pdfconversion/"
                    + "PdfConversionProjection.xml");

    @Test
    void directProjectionIsFencedByTheCurrentConversionId() throws Exception {
        String mapper = Files.readString(MAPPER, StandardCharsets.UTF_8);

        assertThat(mapper)
                .contains("current_job.conversion_id = CAST(#{conversionId} AS uuid)")
                .contains("current_job.current_yn = TRUE")
                .contains("current_job.object_type = 'SW'")
                .contains("current_job.object_type = 'SW_SUB'");
    }

    @Test
    void reconciliationUsesOnlyDurableCurrentJobsForMainAndSupportingFiles()
            throws Exception {
        String mapper = Files.readString(MAPPER, StandardCharsets.UTF_8);

        assertThat(mapper)
                .contains("<update id=\"reconcileCurrentSw\">")
                .contains("<update id=\"reconcileCurrentSwSub\">")
                .contains("WHEN 'SUCCEEDED' THEN 'DONE'")
                .contains("WHEN 'FAILED' THEN 'FAIL'");
        assertThat(count(mapper, "current_job.current_yn = TRUE")).isGreaterThanOrEqualTo(4);
    }

    private int count(String source, String needle) {
        int matches = 0;
        int offset = 0;
        while ((offset = source.indexOf(needle, offset)) >= 0) {
            matches++;
            offset += needle.length();
        }
        return matches;
    }
}

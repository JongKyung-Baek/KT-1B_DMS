package kr.esob.tdms.commonlogic.message;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class KoreanMetadataFallbackContractTest {

    private static final String KOREAN_SESSION_CHECK =
            "LEFT(LOWER(COALESCE(#{sessionLang}, '')), 2) = 'ko'";

    @Test
    void koreanMetadataDoesNotFallThroughToEnglishWhenDatabaseRowsAreMissing()
            throws Exception {
        Map<String, String> sourceFallbacks = new LinkedHashMap<>();
        sourceFallbacks.put(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/toolbar/ToolbarInfo.xml",
                "THEN TOOLBAR.BUTTON_LABEL");
        sourceFallbacks.put(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/grid/GridInfo.xml",
                "THEN grid.COLUMN_NM");
        sourceFallbacks.put(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/form/FormInfo.xml",
                "THEN form.COLUMN_NM");
        sourceFallbacks.put(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/excel/Excel.xml",
                "THEN grid.COLUMN_NM");

        for (Map.Entry<String, String> mapper : sourceFallbacks.entrySet()) {
            String xml = read(mapper.getKey());
            assertTrue(xml.contains(KOREAN_SESSION_CHECK),
                    mapper.getKey() + " must recognize Korean locale variants");
            assertTrue(xml.contains(mapper.getValue()),
                    mapper.getKey() + " must prefer its Korean source label");
            assertTrue(xml.contains("LANG_TYPE = 'en'"),
                    mapper.getKey() + " must retain English fallback for other locales");
        }
    }

    @Test
    void repeatableMigrationContainsOrganizationManagementKoreanLabels()
            throws Exception {
        String migration = read(
                "src/main/resources/sql/korean_menu_locale_ddl.sql");

        assertTrue(migration.contains(
                "('ko', 'btn.resetPwd', '비밀번호 초기화')"));
        assertTrue(migration.contains(
                "('ko', 'btn.unlockAccount', '계정 잠금 해제')"));
        assertTrue(migration.contains(
                "('ko', 'btn.create', '사용자 생성')"));
        assertTrue(migration.contains(
                "('ko', 'btn.createDept', '부서 생성')"));
        assertTrue(migration.contains(
                "ON CONFLICT (lang_type, lang_cd) DO UPDATE"));
    }

    private String read(String path) throws Exception {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }
}

package kr.esob.tdms.commonlogic.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class KoreanMenuLocaleContractTest {

    private static final Map<String, String> KOREAN_UI_LABELS =
            koreanUiLabels();

    @Test
    void packagedKoreanBundlesCoverActiveLegacyNavigationKeys()
            throws Exception {
        assertKoreanLabels(Paths.get(
                "src/main/webapp/messages/message.properties"));
        assertKoreanLabels(Paths.get(
                "src/main/webapp/messages/message_ko.properties"));
        assertKoreanLabels(Paths.get(
                "src/main/webapp/messages/message_ko_KR.properties"));
        assertKoreanLabels(Paths.get(
                "src/main/resources/messages/message.properties"));
        assertKoreanLabels(Paths.get(
                "src/main/resources/messages/message_ko.properties"));
    }

    @Test
    void repeatableMigrationSeedsEveryKoreanMenuLabelWithoutTechnicalDataWrites()
            throws Exception {
        String migration = read(Paths.get(
                "src/main/resources/sql/korean_menu_locale_ddl.sql"));

        for (Map.Entry<String, String> label : KOREAN_UI_LABELS.entrySet()) {
            assertTrue(migration.contains("('ko', '" + label.getKey()
                    + "', '" + label.getValue() + "')"),
                    "Missing Korean DOCS_LANG row: " + label.getKey());
        }
        assertTrue(migration.contains(
                "ON CONFLICT (lang_type, lang_cd) DO UPDATE"));
        assertTrue(migration.contains(
                "An active legacy navigation entry still lacks Korean text."));

        String normalized = migration.toLowerCase();
        assertFalse(normalized.contains("docs_sw"));
        assertFalse(normalized.contains("docs_sw_file"));
        assertFalse(normalized.contains("docs_sw_sub_file"));
        assertFalse(normalized.contains("update docs_menu"));

        String manifest = read(Paths.get(
                "src/main/resources/sql/fresh_database_migration.psql"));
        assertEquals(1, occurrences(
                manifest, "\\ir korean_menu_locale_ddl.sql"));
    }

    @Test
    void navigationMapperPrefersSessionKoreanBeforeEnglishFallback()
            throws Exception {
        String mapper = read(Paths.get(
                "src/main/resources/sqlMaps/oracle/its/controller/menu/Menu.xml"));

        assertTrue(mapper.contains(
                "COALESCE(lang.LANG_DESC, fallbackLang.LANG_DESC, menu.MENU_NM)"));
        assertTrue(mapper.contains(
                "lang.LANG_TYPE = #{sessionLang}"));
        assertTrue(mapper.contains(
                "fallbackLang.LANG_TYPE = 'en'"));
    }

    private void assertKoreanLabels(Path path) throws Exception {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        for (Map.Entry<String, String> label : KOREAN_UI_LABELS.entrySet()) {
            assertEquals(label.getValue(), properties.getProperty(label.getKey()),
                    path + " has no Korean label for " + label.getKey());
        }
    }

    private static Map<String, String> koreanUiLabels() {
        Map<String, String> labels = new LinkedHashMap<String, String>();
        labels.put("menu.datamanage", "기술자료관리");
        labels.put("menu.usermanage", "사용자 관리");
        labels.put("menu.deptmanage", "부서 관리");
        labels.put("menu.search", "조회");
        labels.put("menu.register", "등록");
        labels.put("btn.resetPwd", "비밀번호 초기화");
        labels.put("btn.unlockAccount", "계정 잠금 해제");
        labels.put("btn.create", "사용자 생성");
        labels.put("btn.createDept", "부서 생성");
        labels.put("grid.id", "사용자 계정");
        labels.put("grid.name", "사용자 성명");
        labels.put("grid.active", "사용 여부");
        labels.put("grid.deptCode", "부서 코드");
        labels.put("grid.accountLock", "계정 잠금 여부");
        labels.put("grid.incorrectPwd", "비밀번호 오류 횟수");
        labels.put("grid.lastLogin", "최종 로그인");
        labels.put("grid.position", "직급");
        labels.put("grid.role", "사용자 권한");
        return labels;
    }

    private int occurrences(String source, String token) {
        int count = 0;
        int position = 0;
        while ((position = source.indexOf(token, position)) >= 0) {
            count += 1;
            position += token.length();
        }
        return count;
    }

    private String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}

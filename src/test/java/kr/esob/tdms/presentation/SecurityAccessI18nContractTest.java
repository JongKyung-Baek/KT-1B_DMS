package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class SecurityAccessI18nContractTest {

    private static final Path VIEW = Paths.get(
            "src/main/webapp/WEB-INF/views/general/system/securityaccess/securityAccess.jsp");
    private static final Path SCRIPT = Paths.get(
            "src/main/resources/static/js/general/system/securityaccess/securityAccess.js");
    private static final Path KOREAN_MESSAGES = Paths.get(
            "src/main/webapp/messages/feature.properties");
    private static final Path ENGLISH_MESSAGES = Paths.get(
            "src/main/webapp/messages/feature_en.properties");
    private static final Pattern HANGUL = Pattern.compile("[가-힣]");
    private static final Pattern VIEW_KEY = Pattern.compile(
            "code\\s*=\\s*['\"]feature\\.securityAccess\\.[^'\"]+['\"]");
    private static final Pattern SCRIPT_KEY = Pattern.compile(
            "t\\(\\s*'feature\\.securityAccess\\.[^']+'");
    private static final Pattern LOCALIZED_KOREAN_FALLBACK = Pattern.compile(
            "t\\(\\s*'feature\\.securityAccess\\.[^']+'\\s*,\\s*'[^']*[가-힣][^']*'",
            Pattern.DOTALL);

    @Test
    void staticAclLabelsUseSpringFeatureMessages() throws Exception {
        String view = read(VIEW);

        assertTrue(view.contains("<%@ taglib prefix=\"spring\" uri=\"http://www.springframework.org/tags\"%>"));
        assertTrue(view.contains("<html lang=\"<spring:message code='feature.securityAccess.page.language'/>"));
        assertTrue(view.contains("<spring:message code=\"feature.securityAccess.page.title\"/>"));
        assertTrue(view.contains("placeholder=\"<spring:message code='feature.securityAccess"));
        assertTrue(countMatches(VIEW_KEY, view) >= 80);
        assertFalse(HANGUL.matcher(view).find(),
                "ACL JSP에 Spring 메시지로 치환되지 않은 한국어 문구가 남아 있습니다.");
    }

    @Test
    void dynamicAclLabelsUseSdmsI18nWithKoreanFallbacks() throws Exception {
        String script = read(SCRIPT);

        assertTrue(script.contains("window.SdmsI18n.t.apply(window.SdmsI18n"));
        assertTrue(script.contains("feature.securityAccess.common.requestFailedHttp"));
        assertTrue(script.contains("feature.securityAccess.permission.tooltip.globalPermissionMissing"));
        assertTrue(script.contains("feature.securityAccess.file.emptyInType"));
        assertTrue(script.contains("formatMessage(value, args)"));
        assertTrue(countMatches(SCRIPT_KEY, script) >= 50);

        String withoutLocalizedFallbacks =
                LOCALIZED_KOREAN_FALLBACK.matcher(script).replaceAll("");
        assertFalse(HANGUL.matcher(withoutLocalizedFallbacks).find(),
                "ACL JavaScript의 한국어 문구는 feature 키와 fallback을 가진 t(...) 호출 안에 있어야 합니다.");
    }

    @Test
    void aclViewDoesNotReintroduceStaticLocaleOrStaleScriptCacheKey() throws Exception {
        String view = read(VIEW);

        assertFalse(view.contains("<html lang=\"ko\">"));
        assertFalse(view.contains("securityAccess.js?v=20260726.3"));
        assertFalse(view.contains("securityAccess.js?v=20260726.4"));
        assertTrue(view.contains("securityAccess.js?v=20260726.5"));
    }

    @Test
    void gradeCodesDriveLocalizedNamesAcrossGradeUserAndDocumentDisplays() throws Exception {
        String script = read(SCRIPT);

        assertTrue(script.contains("function localizedGradeName(code, fallback)"));
        assertTrue(script.contains("feature.documentGrade.general"));
        assertTrue(script.contains("feature.documentGrade.internal"));
        assertTrue(script.contains("feature.documentGrade.restricted"));
        assertTrue(script.contains("feature.documentGrade.confidential"));
        assertTrue(script.contains("escapeHtml(gradeDisplayName(grade))"));
        assertTrue(script.contains("escapeHtml(localizedGradeName("));
        assertTrue(script.contains("$('#gradeNm').val(gradeName(grade))"),
                "관리자 편집 입력에는 DB 원본 등급명을 유지해야 합니다.");
    }

    @Test
    void gradeLabelsAndStatusChipsHaveKoreanEnglishParity() throws Exception {
        String korean = read(KOREAN_MESSAGES);
        String english = read(ENGLISH_MESSAGES);
        String[] keys = {
                "feature.documentGrade.general",
                "feature.documentGrade.internal",
                "feature.documentGrade.restricted",
                "feature.documentGrade.confidential",
                "feature.securityAccess.common.default",
                "feature.securityAccess.common.active",
                "feature.securityAccess.common.inactive"
        };

        for (String key : keys) {
            assertEquals(1, propertyCount(korean, key), key + " Korean key count");
            assertEquals(1, propertyCount(english, key), key + " English key count");
        }
        assertTrue(english.contains("feature.documentGrade.general=General"));
        assertTrue(english.contains("feature.documentGrade.internal=Internal Use Only"));
        assertTrue(english.contains("feature.documentGrade.restricted=Restricted"));
        assertTrue(english.contains("feature.documentGrade.confidential=Confidential"));
        assertTrue(scriptUsesLocalizedStatusChips(read(SCRIPT)));
    }

    private int countMatches(Pattern pattern, String value) {
        int count = 0;
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            count += 1;
        }
        return count;
    }

    private int propertyCount(String source, String key) {
        Pattern property = Pattern.compile("(?m)^" + Pattern.quote(key) + "=");
        return countMatches(property, source);
    }

    private boolean scriptUsesLocalizedStatusChips(String script) {
        return script.contains("t('feature.securityAccess.common.default', '기본')")
                && script.contains("t('feature.securityAccess.common.active', '사용')")
                && script.contains("t('feature.securityAccess.common.inactive', '중지')");
    }

    private String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}

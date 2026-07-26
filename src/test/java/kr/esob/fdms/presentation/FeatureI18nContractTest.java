package kr.esob.fdms.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class FeatureI18nContractTest {

    private static final Path KOREAN =
            Paths.get("src/main/webapp/messages/feature.properties");
    private static final Path ENGLISH =
            Paths.get("src/main/webapp/messages/feature_en.properties");
    private static final Pattern FEATURE_KEY =
            Pattern.compile("feature(?:\\.[A-Za-z0-9_-]+)+");
    private static final Pattern HANGUL = Pattern.compile("[가-힣]");

    @Test
    void koreanAndEnglishFeatureBundlesHaveTheSameUniqueKeys() throws Exception {
        Map<String, String> korean = load(KOREAN);
        Map<String, String> english = load(ENGLISH);

        assertEquals(korean.keySet(), english.keySet(),
                "한국어/영어 feature 번들의 키 집합이 다릅니다.");
        assertEquals(korean.size(), countPropertyKeys(KOREAN),
                "한국어 feature 번들에 중복 키가 있습니다.");
        assertEquals(english.size(), countPropertyKeys(ENGLISH),
                "영어 feature 번들에 중복 키가 있습니다.");
    }

    @Test
    void everyFeatureKeyReferencedByApplicationCodeExistsInBothBundles()
            throws Exception {
        Set<String> referenced = referencedFeatureKeys();
        Set<String> korean = load(KOREAN).keySet();
        Set<String> english = load(ENGLISH).keySet();

        Set<String> missingKorean = missingReferences(referenced, korean);
        Set<String> missingEnglish = missingReferences(referenced, english);

        assertTrue(missingKorean.isEmpty(),
                "한국어 feature 번들 누락 키: " + missingKorean);
        assertTrue(missingEnglish.isEmpty(),
                "영어 feature 번들 누락 키: " + missingEnglish);
    }

    private Set<String> missingReferences(
            Set<String> referenced, Set<String> available) {
        Set<String> missing = new TreeSet<String>();
        for (String reference : referenced) {
            if (available.contains(reference)) {
                continue;
            }
            boolean dynamicPrefix = false;
            for (String key : available) {
                if (key.startsWith(reference + ".")) {
                    dynamicPrefix = true;
                    break;
                }
            }
            if (!dynamicPrefix) {
                missing.add(reference);
            }
        }
        return missing;
    }

    @Test
    void englishFeatureBundleDoesNotContainKoreanFallbackText() throws Exception {
        List<String> contaminated = new ArrayList<String>();
        for (Map.Entry<String, String> entry : load(ENGLISH).entrySet()) {
            if (HANGUL.matcher(entry.getValue()).find()) {
                contaminated.add(entry.getKey());
            }
        }
        assertTrue(contaminated.isEmpty(),
                "영어 feature 번들에 한국어가 남아 있습니다: " + contaminated);
    }

    @Test
    void layoutAndNavigationUseTheSessionLocale() throws Exception {
        Path decorators = Paths.get("src/main/webapp/WEB-INF/decorator");
        try (Stream<Path> files = Files.list(decorators)) {
            files.filter(path -> path.getFileName().toString().endsWith(".jsp"))
                    .forEach(path -> {
                        try {
                            String source = read(path);
                            assertFalse(source.contains("<html lang=\"kr\""),
                                    path + " has a fixed legacy language tag");
                            if (source.contains("grid.locale-")) {
                                assertTrue(source.contains(
                                                "LocaleUtil.getJqGridLanguage(request)"),
                                        path + " has a fixed jqGrid locale");
                            }
                            if (source.contains("common_grid_paging.js")) {
                                assertTrue(source.contains(
                                                "common_grid_paging.js?v=20260726.1"),
                                        path + " does not invalidate the shared pager cache");
                            }
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    });
        }

        String header = read(Paths.get("src/main/webapp/header.jsp"));
        String login = read(Paths.get(
                "src/main/webapp/WEB-INF/views/login/login.jsp"));
        String menuAdd = read(Paths.get(
                "src/main/webapp/WEB-INF/views/menu/menuAdd.jsp"));
        assertTrue(header.contains("changeUiLanguage('ko')"));
        assertTrue(header.contains("changeUiLanguage('en')"));
        assertTrue(login.contains("changeLoginLanguage('ko')"));
        assertTrue(login.contains("changeLoginLanguage('en')"));
        assertTrue(login.contains("name=\"lang\""));
        assertTrue(login.contains(
                "value=\"<%=LocaleUtil.getCurrentLanguage(request) %>\""));
        assertTrue(menuAdd.contains(
                "common_grid_paging.js?v=20260726.1"));

        String security = read(Paths.get(
                "src/main/java/kr/esob/fdms/config/SecurityConfig.java"));
        assertTrue(security.contains("\"/messages/*.properties\""));
    }

    @Test
    void databaseDrivenRecentMenusAndColumnsHaveKoreanAndEnglishKeys()
            throws Exception {
        String acl = read(Paths.get(
                "src/main/resources/sql/acl_foundation_ddl.sql"));
        String audit = read(Paths.get(
                "src/main/resources/sql/audit_trail_ddl.sql"));

        assertTrue(acl.contains("('MENU_223', 'menu.historyManagement')"));
        assertTrue(acl.contains("('MENU_222', 'menu.securityAccess')"));
        assertTrue(acl.contains("('en', 'menu.viewHistory', 'View History')"));
        assertTrue(acl.contains("('en', 'grid.documentGrade', 'Document Grade')"));
        assertTrue(audit.contains("WHEN 'occurredAt' THEN 'grid.audit.occurredAt'"));
        assertTrue(audit.contains("('en', 'grid.audit.action', 'Action')"));
    }

    private Set<String> referencedFeatureKeys() throws Exception {
        Set<String> result = new TreeSet<String>();
        Path sourceRoot = Paths.get("src/main");
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            files.filter(Files::isRegularFile)
                    .filter(this::isApplicationTextFile)
                    .filter(path -> !path.startsWith(
                            Paths.get("src/main/webapp/messages")))
                    .filter(path -> !path.startsWith(
                            Paths.get("src/main/resources/static/vuexy")))
                    .forEach(path -> {
                        try {
                            Matcher matcher = FEATURE_KEY.matcher(read(path));
                            while (matcher.find()) {
                                result.add(matcher.group());
                            }
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    });
        }
        return result;
    }

    private boolean isApplicationTextFile(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".java")
                || name.endsWith(".jsp")
                || name.endsWith(".js")
                || name.endsWith(".xml");
    }

    private Map<String, String> load(Path path) throws Exception {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        Map<String, String> values = new TreeMap<String, String>();
        for (String name : properties.stringPropertyNames()) {
            values.put(name, properties.getProperty(name));
        }
        return values;
    }

    private int countPropertyKeys(Path path) throws Exception {
        Set<String> keys = new HashSet<String>();
        int count = 0;
        try (BufferedReader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")
                        || trimmed.startsWith("!")) {
                    continue;
                }
                int separator = trimmed.indexOf('=');
                assertTrue(separator > 0,
                        "잘못된 properties 행: " + trimmed);
                count += 1;
                keys.add(trimmed.substring(0, separator).trim());
            }
        }
        assertEquals(count, keys.size(),
                path + " contains duplicate property keys");
        return count;
    }

    private String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}

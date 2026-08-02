package kr.esob.tdms.commonlogic.message;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

class IndonesianBundleContractTest {

    private static final Path FEATURE_BASE = bundle("feature.properties");
    private static final Path FEATURE_ENGLISH = bundle("feature_en.properties");
    private static final Path FEATURE_INDONESIAN = bundle("feature_id.properties");
    private static final Path MESSAGE_BASE = bundle("message.properties");
    private static final Path MESSAGE_ENGLISH = bundle("message_en.properties");
    private static final Path MESSAGE_INDONESIAN = bundle("message_id.properties");
    private static final Path INDONESIAN_DDL = Paths.get(
            "src/main/resources/sql/indonesian_locale_ddl.sql");
    private static final Pattern MESSAGE_ARGUMENT =
            Pattern.compile("\\{\\d+\\}");
    private static final Pattern HTML_TAG =
            Pattern.compile("<[^>]+>");
    private static final Pattern HANGUL_OR_REPLACEMENT =
            Pattern.compile("[가-힣\\uFFFD]");

    @Test
    void featureBundleMatchesKoreanAndEnglishContracts() throws Exception {
        Map<String, String> base = load(FEATURE_BASE);
        Map<String, String> english = load(FEATURE_ENGLISH);
        Map<String, String> indonesian = load(FEATURE_INDONESIAN);

        assertEquals(base.keySet(), indonesian.keySet());
        assertEquals(english.keySet(), indonesian.keySet());
        assertEquals(indonesian.size(), countPropertyKeys(FEATURE_INDONESIAN));
        assertTokensPreserved(english, indonesian, true);
        assertNoKoreanOrBrokenText(indonesian);
    }

    @Test
    void legacyMessageBundleCoversEveryBaseAndEnglishKey() throws Exception {
        Map<String, String> base = load(MESSAGE_BASE);
        Map<String, String> english = load(MESSAGE_ENGLISH);
        Map<String, String> indonesian = load(MESSAGE_INDONESIAN);

        assertTrue(indonesian.keySet().containsAll(base.keySet()),
                "message_id.properties is missing default message keys");
        assertTrue(indonesian.keySet().containsAll(english.keySet()),
                "message_id.properties is missing English message keys");
        assertEquals(indonesian.size(), countPropertyKeys(MESSAGE_INDONESIAN));
        assertTokensPreserved(base, indonesian, false);
        assertTokensPreserved(english, indonesian, false);
        assertNoKoreanOrBrokenText(indonesian);
    }

    @Test
    void databaseMigrationExactlySeedsThePackagedIndonesianMessages()
            throws Exception {
        String ddl = read(INDONESIAN_DDL);
        String marker = "$indonesian$";
        int jsonStart = ddl.indexOf(marker);
        int jsonEnd = ddl.indexOf(marker, jsonStart + marker.length());

        assertTrue(jsonStart >= 0 && jsonEnd > jsonStart,
                "Indonesian migration JSON block is missing");
        String json = ddl.substring(jsonStart + marker.length(), jsonEnd);
        Map<String, String> seeded = new ObjectMapper().readValue(
                json, new TypeReference<Map<String, String>>() { });

        assertEquals(load(MESSAGE_INDONESIAN), new TreeMap<String, String>(seeded),
                "message_id.properties and DOCS_LANG migration differ");
        assertTrue(ddl.contains("ON CONFLICT (lang_type, lang_cd) DO UPDATE"));
        assertTrue(read(Paths.get(
                "src/main/resources/sql/fresh_database_migration.psql"))
                .contains("\\ir indonesian_locale_ddl.sql"));
    }

    @Test
    void jqGridLocaleDoesNotExposeKnownEnglishFallbackLabels()
            throws Exception {
        List<Path> localeFiles = new ArrayList<Path>();
        localeFiles.add(Paths.get(
                "src/main/resources/static/js/i18n/grid.locale-id.js"));
        localeFiles.add(Paths.get(
                "src/main/resources/static/js/jqGrid-master/js/i18n/grid.locale-id.js"));

        String[] englishFallbacks = {
                "Saving...", "First Page", "Last Page", "Next Page",
                "Previous Page", "Records per Page",
                "Toggle Expand Collapse Grid", "Grid::Page Settings",
                "No more records...", "Pull up to load more...",
                "Click to select search operation.", "Reset Search Value",
                "Save row", "Cancel row editing", "Sort Ascending",
                "Sort Descending", "Get items with value that:"
        };
        for (Path localeFile : localeFiles) {
            String source = read(localeFile);
            assertTrue(source.contains("regional") && source.contains("id"),
                    localeFile + " does not register the Indonesian locale");
            assertTrue(source.contains("recordPage"),
                    localeFile + " changed a jqGrid option name");
            for (String fallback : englishFallbacks) {
                assertFalse(source.contains(fallback),
                        localeFile + " still exposes English: " + fallback);
            }
        }
    }

    private void assertTokensPreserved(
            Map<String, String> source,
            Map<String, String> translated,
            boolean compareHtml) {
        for (Map.Entry<String, String> entry : source.entrySet()) {
            String translatedValue = translated.get(entry.getKey());
            assertEquals(tokens(entry.getValue(), MESSAGE_ARGUMENT),
                    tokens(translatedValue, MESSAGE_ARGUMENT),
                    entry.getKey() + " changed its message arguments");
            if (compareHtml) {
                assertEquals(tokens(entry.getValue(), HTML_TAG),
                        tokens(translatedValue, HTML_TAG),
                        entry.getKey() + " changed its HTML structure");
            }
        }
    }

    private List<String> tokens(String value, Pattern pattern) {
        List<String> result = new ArrayList<String>();
        Matcher matcher = pattern.matcher(value == null ? "" : value);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    private void assertNoKoreanOrBrokenText(Map<String, String> bundle) {
        List<String> contaminated = new ArrayList<String>();
        for (Map.Entry<String, String> entry : bundle.entrySet()) {
            if (HANGUL_OR_REPLACEMENT.matcher(entry.getValue()).find()) {
                contaminated.add(entry.getKey());
            }
        }
        assertTrue(contaminated.isEmpty(),
                "Indonesian bundle contains Korean or broken text: "
                        + contaminated);
    }

    private Map<String, String> load(Path path) throws Exception {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        Map<String, String> result = new TreeMap<String, String>();
        for (String name : properties.stringPropertyNames()) {
            result.put(name, properties.getProperty(name));
        }
        return result;
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
                        "Invalid properties line in " + path + ": " + trimmed);
                count += 1;
                keys.add(trimmed.substring(0, separator).trim());
            }
        }
        assertEquals(count, keys.size(), path + " contains duplicate keys");
        return count;
    }

    private static Path bundle(String filename) {
        return Paths.get("src/main/webapp/messages", filename);
    }

    private String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}

package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class DecoratorJavaScriptEscapingContractTest {

    private static final Path DECORATOR_DIRECTORY = Path.of(
            "src", "main", "webapp", "WEB-INF", "decorator");
    private static final Pattern INLINE_SCRIPT = Pattern.compile(
            "<script(?![^>]*\\bsrc\\s*=)[^>]*>(.*?)</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SAFE_JAVASCRIPT_VALUE = Pattern.compile(
            "<spring:escapeBody\\s+htmlEscape=\"false\"\\s+javaScriptEscape=\"true\">"
                    + ".*?</spring:escapeBody>",
            Pattern.DOTALL);
    private static final Pattern DYNAMIC_VALUE = Pattern.compile(
            "\\$\\{[^}\\r\\n]+}|<%=[^%\\r\\n]*%>");

    @Test
    void dynamicValuesInDecoratorInlineScriptsAreContextEncoded() throws IOException {
        List<String> violations = new ArrayList<>();
        int encodedValueCount = 0;

        try (Stream<Path> files = Files.list(DECORATOR_DIRECTORY)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".jsp")).toList()) {
                String jsp = Files.readString(file, StandardCharsets.UTF_8);
                Matcher scriptMatcher = INLINE_SCRIPT.matcher(jsp);
                while (scriptMatcher.find()) {
                    String script = scriptMatcher.group(1);
                    Matcher safeValueMatcher = SAFE_JAVASCRIPT_VALUE.matcher(script);
                    StringBuffer sanitizedScript = new StringBuffer();
                    while (safeValueMatcher.find()) {
                        encodedValueCount++;
                        safeValueMatcher.appendReplacement(sanitizedScript, "__JS_ESCAPED_VALUE__");
                    }
                    safeValueMatcher.appendTail(sanitizedScript);

                    Matcher unsafeValueMatcher = DYNAMIC_VALUE.matcher(sanitizedScript);
                    while (unsafeValueMatcher.find()) {
                        violations.add(file.getFileName() + ": " + unsafeValueMatcher.group());
                    }
                }
            }
        }

        assertTrue(encodedValueCount > 0, "Decorator JavaScript values must use Spring context encoding");
        assertTrue(violations.isEmpty(),
                "Unescaped dynamic values remain in inline JavaScript: " + violations);
    }

    @Test
    void escapeBodyTagsDisableHtmlEscapingAndEnableJavaScriptEscaping() throws IOException {
        try (Stream<Path> files = Files.list(DECORATOR_DIRECTORY)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".jsp")).toList()) {
                String jsp = Files.readString(file, StandardCharsets.UTF_8);
                int openingTags = count(jsp, "<spring:escapeBody");
                int safeOpeningTags = count(jsp,
                        "<spring:escapeBody htmlEscape=\"false\" javaScriptEscape=\"true\">");
                int closingTags = count(jsp, "</spring:escapeBody>");

                assertEquals(openingTags, safeOpeningTags,
                        file.getFileName() + " has an escapeBody tag without the required context");
                assertEquals(openingTags, closingTags,
                        file.getFileName() + " has an unbalanced escapeBody tag");
            }
        }
    }

    private int count(String source, String token) {
        int count = 0;
        int offset = 0;
        while ((offset = source.indexOf(token, offset)) >= 0) {
            count++;
            offset += token.length();
        }
        return count;
    }
}

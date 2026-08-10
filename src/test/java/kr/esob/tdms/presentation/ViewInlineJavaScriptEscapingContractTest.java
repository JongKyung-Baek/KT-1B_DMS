package kr.esob.tdms.presentation;

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

class ViewInlineJavaScriptEscapingContractTest {

    private static final Path VIEW_DIRECTORY = Path.of(
            "src", "main", "webapp", "WEB-INF", "views");
    private static final Path HEADER_JSP = Path.of(
            "src", "main", "webapp", "header.jsp");
    private static final Pattern INLINE_SCRIPT = Pattern.compile(
            "<script(?![^>]*\\bsrc\\s*=)[^>]*>(.*?)</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern JSP_COMMENT = Pattern.compile(
            "<%--.*?--%>", Pattern.DOTALL);
    private static final Pattern SERVER_CONTROL_TAG = Pattern.compile(
            "</?c:(?:forEach|if)\\b[^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SAFE_JAVASCRIPT_VALUE = Pattern.compile(
            "<spring:escapeBody\\s+htmlEscape=\"false\"\\s+javaScriptEscape=\"true\">"
                    + ".*?</spring:escapeBody>",
            Pattern.DOTALL);
    private static final Pattern DYNAMIC_VALUE = Pattern.compile(
            "\\$\\{[^}\\r\\n]+}|<%=[^%\\r\\n]*%>");

    @Test
    void dynamicValuesInViewInlineScriptsAreContextEncoded() throws IOException {
        List<String> violations = new ArrayList<>();
        int encodedValueCount = 0;

        List<Path> jspFiles;
        try (Stream<Path> files = Files.walk(VIEW_DIRECTORY)) {
            jspFiles = new ArrayList<>(files
                    .filter(Files::isRegularFile)
                    .filter(this::isJsp)
                    .toList());
        }
        jspFiles.add(HEADER_JSP);

        for (Path file : jspFiles) {
                String jsp = JSP_COMMENT.matcher(
                        Files.readString(file, StandardCharsets.UTF_8))
                        .replaceAll("");
                Matcher scriptMatcher = INLINE_SCRIPT.matcher(jsp);
                while (scriptMatcher.find()) {
                    String script = scriptMatcher.group(1);
                    Matcher safeValueMatcher = SAFE_JAVASCRIPT_VALUE.matcher(script);
                    StringBuffer sanitizedScript = new StringBuffer();
                    while (safeValueMatcher.find()) {
                        encodedValueCount++;
                        safeValueMatcher.appendReplacement(
                                sanitizedScript, "__JS_ESCAPED_VALUE__");
                    }
                    safeValueMatcher.appendTail(sanitizedScript);

                    String controlsRemoved = SERVER_CONTROL_TAG.matcher(
                            sanitizedScript).replaceAll("__SERVER_CONTROL__");
                    Matcher unsafeValueMatcher = DYNAMIC_VALUE.matcher(
                            controlsRemoved);
                    while (unsafeValueMatcher.find()) {
                        violations.add(file + ": "
                                + unsafeValueMatcher.group());
                    }
                }
        }

        assertTrue(encodedValueCount > 0,
                "View JavaScript values must use Spring context encoding");
        assertTrue(violations.isEmpty(),
                "Unescaped dynamic values remain in inline JavaScript: " + violations);
    }

    private boolean isJsp(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(".jsp") || fileName.endsWith(".jspf");
    }
}

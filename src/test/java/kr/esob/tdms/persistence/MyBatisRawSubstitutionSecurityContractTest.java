package kr.esob.tdms.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class MyBatisRawSubstitutionSecurityContractTest {

    private static final Path MAPPER_ROOT = Path.of(
            "src", "main", "resources", "sqlMaps");
    private static final Pattern RAW_SUBSTITUTION = Pattern.compile(
            "\\$\\{([A-Za-z_][A-Za-z0-9_.]*)}");
    private static final Set<String> ALLOWED_IDENTIFIERS = Set.of(
            "sortColumn", "order", "TABLE_NAME");

    @Test
    void rawSubstitutionIsLimitedToValidatedSqlIdentifiers()
            throws IOException {
        List<String> unexpected = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(MAPPER_ROOT)) {
            paths.filter(path -> path.toString().endsWith(".xml"))
                    .forEach(path -> inspect(path, unexpected));
        }

        assertTrue(unexpected.isEmpty(),
                "Unbound MyBatis values must use #{}: " + unexpected);
    }

    private void inspect(Path path, List<String> unexpected) {
        try {
            Matcher matcher = RAW_SUBSTITUTION.matcher(
                    Files.readString(path, StandardCharsets.UTF_8));
            while (matcher.find()) {
                String variable = matcher.group(1);
                if (!ALLOWED_IDENTIFIERS.contains(variable)) {
                    unexpected.add(path + ":" + variable);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not inspect mapper: " + path,
                    exception);
        }
    }
}

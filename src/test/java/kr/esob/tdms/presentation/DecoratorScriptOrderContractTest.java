package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class DecoratorScriptOrderContractTest {

    private static final Path SIDE = Path.of(
            "src/main/webapp/WEB-INF/decorator/decoratorSide.jsp");
    private static final Path TREE = Path.of(
            "src/main/webapp/WEB-INF/decorator/decoratorTree.jsp");

    @Test
    void systemManagementDecoratorsLoadScriptDependenciesInOrder()
            throws Exception {
        assertDependencyOrder(SIDE);
        assertDependencyOrder(TREE);
    }

    private void assertDependencyOrder(Path decorator) throws Exception {
        String source = Files.readString(decorator, StandardCharsets.UTF_8);
        String jquery = "resources/js/jquery-3.4.1.min.js";
        String jqueryUi = "resources/css/jquery-ui-1.12.1.custom/jquery-ui.js";
        String jqueryUiI18n = "resources/js/i18n/jquery-ui-i18n.min.js";
        String esapi = "resources/js/esapi/esapi.js";
        String commonUtil = "resources/js/common_util.js";

        assertBefore(source, jquery, jqueryUi, decorator);
        assertBefore(source, jqueryUi, jqueryUiI18n, decorator);
        assertBefore(source, jquery, esapi, decorator);
        assertBefore(source, esapi, commonUtil, decorator);
        assertEquals(1, occurrences(source, jqueryUi),
                decorator + " must load jQuery UI exactly once");
        assertEquals(1, occurrences(source, esapi),
                decorator + " must load ESAPI exactly once");
    }

    private void assertBefore(String source, String first, String second,
            Path decorator) {
        int firstIndex = source.indexOf(first);
        int secondIndex = source.indexOf(second);
        assertTrue(firstIndex >= 0, decorator + " is missing " + first);
        assertTrue(secondIndex >= 0, decorator + " is missing " + second);
        assertTrue(firstIndex < secondIndex,
                decorator + " must load " + first + " before " + second);
    }

    private int occurrences(String source, String value) {
        int count = 0;
        int index = 0;
        while ((index = source.indexOf(value, index)) >= 0) {
            count++;
            index += value.length();
        }
        return count;
    }
}

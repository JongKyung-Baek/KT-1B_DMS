package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class TreeManagePresentationContractTest {

    private static final Path VIEW = Paths.get(
            "src/main/webapp/WEB-INF/views/general/system/treemanage/treeManage.jsp");
    private static final Path SCRIPT = Paths.get(
            "src/main/resources/static/js/views/general/system/treemanage/treeManage.js");
    private static final Path STYLE = Paths.get(
            "src/main/resources/static/css/pages/tree-management.css");

    @Test
    void classificationManagementUsesCompactTdmsCardsWithoutHeroOrInlineStyle()
            throws Exception {
        String view = read(VIEW);
        String css = read(STYLE);

        assertTrue(view.contains("tree-management.css?v=20260803.1"));
        assertTrue(view.contains("treeManage.js?v=20260803.1"));
        assertTrue(view.contains("class=\"tm-page-header\""));
        assertTrue(view.contains("class=\"tm-workspace-card\""));
        assertFalse(view.contains("class=\"wrap tm-workspace-card\""));
        assertTrue(view.contains("class=\"tm-context-chip\""));
        assertTrue(view.contains("class=\"tm-count-chip\""));
        assertFalse(view.contains("<style>"));
        assertFalse(view.toLowerCase().contains("hero"));

        assertTrue(styleBlock(css, ".system-manage-page .tm-page-heading {")
                .contains("text-align: left"));
        assertTrue(styleBlock(css, ".system-manage-page .tm-page-heading h1 {")
                .contains("text-align: left"));
        assertTrue(styleBlock(css, ".system-manage-page .tm-workspace-card {")
                .contains("border-radius: 14px"));
        assertTrue(styleBlock(css, ".system-manage-page .tm-tree-column {")
                .contains("border-radius: 12px"));
        assertTrue(styleBlock(css,
                ".system-manage-page button.tm-button.ui-button {")
                .contains("min-height: 36px"));
        assertTrue(styleBlock(css, ".system-manage-page .list-item {")
                .contains("min-height: 54px"));
        assertTrue(styleBlock(css, ".system-manage-page .list-item.active {")
                .contains("background: var(--tm-primary-soft)"));
        assertTrue(css.contains(".system-manage-page .tm-list-priority"));
    }

    @Test
    void existingDomFunctionsAndEndpointsRemainStable() throws Exception {
        String view = read(VIEW);
        String script = read(SCRIPT);

        String[] requiredIds = {
                "treeManageLayout", "leftTreePanel", "treeMainTitle",
                "treeCol1Title", "treeCol2Title", "levelActions",
                "function1List", "function2List", "docTypePanel", "docTypeList"
        };
        for (String id : requiredIds) {
            assertTrue(view.contains("id=\"" + id + "\""), "Missing DOM ID: " + id);
        }

        String[] requiredActions = {
                "addFunction1()", "editFunction1()", "deleteFunction1()",
                "addFunction2()", "editFunction2()", "deleteFunction2()",
                "addDocType()", "editDocType()", "deleteDocType()"
        };
        for (String action : requiredActions) {
            assertTrue(view.contains("onclick=\"" + action + "\""),
                    "Missing action binding: " + action);
        }

        assertTrue(script.contains("'/general/system/treemanage/function1/list'"));
        assertTrue(script.contains("'/general/system/treemanage/function2/list'"));
        assertTrue(script.contains("'/general/system/treemanage/doctype/list'"));
        assertTrue(script.contains("'/general/system/treemanage/node/add'"));
        assertTrue(script.contains("'/general/system/treemanage/node/update'"));
        assertTrue(script.contains("'/general/system/treemanage/node/delete'"));
        assertTrue(script.contains("id=\"treeNodeEditDialog\""));
        assertTrue(script.contains("'treeNodeCodeInput'"));
        assertTrue(script.contains("'treeNodeNameInput'"));
        assertTrue(script.contains("id=\"treeNodePriorityInput\""));
        assertTrue(script.contains("type=\"number\""));
        assertTrue(script.contains("min=\"1\""));
        assertTrue(script.contains("step=\"1\""));
        assertTrue(script.contains("sortOrder: priority"));
        assertTrue(script.contains("nextTreePriority(function1)"));
        assertTrue(script.contains("nextTreePriority(function2)"));
        assertTrue(script.contains("item ? item.sort : null"));
        assertTrue(script.contains("levelItem ? levelItem.sort : null"));
    }

    @Test
    void listsAndDialogUseSafeAccessibleModernMarkup() throws Exception {
        String view = read(VIEW);
        String script = read(SCRIPT);
        String css = read(STYLE);

        assertTrue(view.contains("role=\"listbox\""));
        assertTrue(script.contains("function escapeTreeManageHtml(value)"));
        assertTrue(script.contains("role=\"option\""));
        assertTrue(script.contains("aria-selected=\""));
        assertTrue(script.contains("dialogClass: 'tree-manage-dialog'"));
        assertTrue(script.contains("feature.common.countSuffix"));
        assertTrue(css.contains(".tree-manage-dialog.ui-dialog"));
        assertTrue(css.contains(".tree-node-dialog__row input.ui-widget-content:focus"));
        assertTrue(script.contains("feature.treeManage.field.priority"));
        assertTrue(script.contains("feature.treeManage.validation.priorityPositive"));
    }

    @Test
    void visibleLabelsContinueToUseExistingFeatureMessagesAndFallbacks()
            throws Exception {
        String view = read(VIEW);

        assertTrue(view.contains(
                "code=\"feature.treeManage.title\" text=\"분류/레벨 관리\""));
        assertTrue(view.contains(
                "code=\"feature.treeManage.level.parent\" text=\"상위 Level\""));
        assertTrue(view.contains(
                "code=\"feature.treeManage.level.child\" text=\"하위 Level\""));
        assertTrue(view.contains(
                "code=\"feature.common.button.add\" text=\"추가\""));
        assertTrue(view.contains(
                "code=\"feature.common.button.edit\" text=\"수정\""));
        assertTrue(view.contains(
                "code=\"feature.common.button.delete\" text=\"삭제\""));
        assertTrue(view.contains(
                "code=\"feature.common.countSuffix\" text=\"건\""));
    }

    private String styleBlock(String css, String selector) {
        int start = css.indexOf(selector);
        assertTrue(start >= 0, "CSS selector is missing: " + selector);
        int end = css.indexOf('}', start);
        assertTrue(end > start, "CSS block is not closed: " + selector);
        return css.substring(start, end + 1);
    }

    private String read(Path path) throws Exception {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}

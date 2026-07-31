package kr.esob.fdms.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class GridStyleUnificationContractTest {

    @Test
    void accessViewAndPrintHistoryStartAtUnifiedCardsAndUseCenteredTables() throws Exception {
        String css = read("src/main/resources/static/css/pages/access-history.css");
        String accessPage = read(
                "src/main/webapp/WEB-INF/views/inside/distribution/viewPrintHistory/historyList.jsp");
        String recordPage = read(
                "src/main/webapp/WEB-INF/views/inside/distribution/viewPrintHistory/recordHistory.jsp");

        assertFalse(accessPage.contains("ah-hero"));
        assertFalse(recordPage.contains("ah-hero"));
        assertFalse(css.contains(".ah-hero"));
        assertTrue(css.contains(".ah-log-card__header > div"));
        assertTrue(styleBlock(css, ".ah-table th {").contains("text-align: center"));
        assertTrue(styleBlock(css, ".ah-table td {").contains("text-align: center"));
        assertTrue(styleBlock(css, ".ah-cell {").contains("align-items: center"));
        assertTrue(styleBlock(css, ".rh-detail-chips {").contains("justify-content: center"));
        assertTrue(accessPage.contains("access-history.css?v=20260726.7"));
        assertTrue(recordPage.contains("access-history.css?v=20260731.1"));
    }

    @Test
    void auditLogStartsWithSummaryAndGroupsSearchAndGridInOneCard() throws Exception {
        String page = read(
                "src/main/webapp/WEB-INF/views/inside/organizationmanage/auditlog/auditlogList.jsp");
        String css = read("src/main/resources/static/css/pages/audit-log.css");
        String script = read(
                "src/main/resources/static/js/views/inside/organizationmanage/auditlog/auditlogList.js");

        assertFalse(page.contains("운영 행위를 하나의 흐름으로 확인하세요"));
        assertFalse(page.contains("class=\"audit-log-flow\""));
        assertFalse(page.contains("class=\"audit-log-hero\""));
        assertFalse(css.contains(".audit-log-page .audit-log-hero"));
        assertTrue(page.indexOf("class=\"audit-log-summary\"")
                < page.indexOf("class=\"audit-log-results-card\""));
        assertTrue(page.contains(
                "<section class=\"audit-log-results-card\" aria-label=\"${resultsAria}\">"));
        assertTrue(page.contains("code=\"feature.audit.results.aria\""));
        assertTrue(styleBlock(css, ".audit-log-page section.audit-log-results-card {")
                .contains("padding: 22px"));
        assertTrue(styleBlock(css, ".audit-log-page .distribution-filter-card {")
                .contains("box-shadow: none"));
        assertTrue(styleBlock(css, ".audit-log-page .distribution-grid-card {")
                .contains("box-shadow: none"));
        assertTrue(styleBlock(css,
                ".audit-log-page .formAcceptance .formAcceptanceActions .searchBtn.btn.btn-primary {")
                .contains("min-height: 36px !important"));
        assertTrue(styleBlock(css,
                ".audit-log-page .formAcceptance .formAcceptanceActions .audit-log-reset-btn {")
                .contains("min-height: 36px !important"));
        assertTrue(script.contains("function ensureAuditLogResetButton()"));
        assertTrue(script.contains("id: \"auditLogResetButton\""));
        assertTrue(script.contains(".on(\"click\", resetAuditLogSearch)"));
        assertTrue(script.contains("$resetButtons.slice(1).remove()"));
        assertTrue(styleBlock(css,
                ".audit-log-page .formAcceptance input.form-control:focus,")
                .contains("box-shadow: 0 0 0 3px rgba(115, 103, 240, 0.11) !important"));
        assertTrue(css.contains(
                "#gview_gridInsideAuditLogList .ui-jqgrid-htable th"));
        assertTrue(css.contains("height: 42px !important"));
        assertTrue(styleBlock(css,
                ".audit-log-page #gview_gridInsideAuditLogList .ui-jqgrid-htable th,")
                .contains("font-size: 10px !important"));
        assertTrue(css.contains(
                "#gview_gridInsideAuditLogList .ui-jqgrid-btable tr.jqgrow td"));
        assertTrue(css.contains("height: 56px !important"));
        assertTrue(styleBlock(css,
                ".audit-log-page #gview_gridInsideAuditLogList .ui-jqgrid-btable tr.jqgrow td {")
                .contains("font-size: 11px !important"));
        assertTrue(styleBlock(css, ".audit-log-page small.audit-grid-cell__meta {")
                .contains("font-size: 9px"));
        assertTrue(styleBlock(css,
                ".audit-log-page #gridInsideAuditLogListPager input,")
                .contains("height: 28px !important"));
        assertTrue(styleBlock(css,
                ".audit-log-page #gridInsideAuditLogListPager input,")
                .contains("font-size: 11px !important"));
        assertTrue(css.contains("background: #f1efff !important"));
        assertTrue(page.contains("audit-log.css?v=20260726.5"));
        assertTrue(page.contains("auditlogList.js?v=20260726.2"));
    }

    @Test
    void internalUserAndDepartmentManagementShareOneScopedModernGridStyle() throws Exception {
        String userPage = read(
                "src/main/webapp/WEB-INF/views/inside/organizationmanage/insideuser/insideuserList.jsp");
        String departmentPage = read(
                "src/main/webapp/WEB-INF/views/inside/organizationmanage/insidedept/insidedeptList.jsp");
        String css = read("src/main/resources/static/css/pages/organization-management.css");
        String script = read(
                "src/main/resources/static/js/views/inside/organizationmanage/organization-management.js");

        assertTrue(userPage.contains("organization-management-page"));
        assertTrue(departmentPage.contains("organization-management-page"));
        assertTrue(userPage.contains(
                "<section class=\"organization-management-results-card\" aria-label=\"${resultsAria}\">"));
        assertTrue(departmentPage.contains(
                "<section class=\"organization-management-results-card\" aria-label=\"${resultsAria}\">"));
        assertTrue(userPage.contains("feature.organization.user.resultsAria"));
        assertTrue(departmentPage.contains("feature.organization.department.resultsAria"));
        assertTrue(userPage.contains("organization-management.css?v=20260726.2"));
        assertTrue(departmentPage.contains("organization-management.css?v=20260726.2"));
        assertTrue(userPage.contains("organization-management.js?v=20260726.1"));
        assertTrue(departmentPage.contains("organization-management.js?v=20260726.1"));
        assertTrue(styleBlock(css,
                ".organization-management-page .organization-management-results-card {")
                .contains("padding: 22px"));
        assertTrue(styleBlock(css,
                ".organization-management-page .distribution-filter-card {")
                .contains("box-shadow: none"));
        assertTrue(styleBlock(css,
                ".organization-management-page .distribution-grid-card {")
                .contains("box-shadow: none"));
        assertTrue(styleBlock(css,
                ".organization-management-page .formAcceptance input.form-control,")
                .contains("height: 36px !important"));
        assertTrue(styleBlock(css,
                ".organization-management-page .formAcceptance .formAcceptanceActions.formAcceptanceItem {")
                .contains("gap: 7px"));
        assertTrue(styleBlock(css,
                ".organization-management-page .formAcceptance .formAcceptanceActions .searchBtn.btn.btn-primary,")
                .contains("min-height: 36px !important"));
        assertTrue(script.contains("function enhanceSearchActions()"));
        assertTrue(script.contains("\"class\": \"btn resetBtn organization-management-reset-btn\""));
        assertTrue(script.contains("resetSearchForm($form)"));
        assertTrue(script.contains("window.searchList(window.gridParam)"));
        assertTrue(css.contains("#gview_gridInsideUserList"));
        assertTrue(css.contains("#gview_gridInsideDeptList"));
        assertTrue(css.contains("height: 42px !important"));
        assertTrue(css.contains("height: 56px !important"));
        assertTrue(css.contains("text-align: center !important"));
        assertTrue(styleBlock(css,
                ".organization-management-page #gridInsideUserListPager input,")
                .contains("height: 28px !important"));
        assertTrue(css.contains(
                "#gridInsideDeptListPager > table > tbody > tr > td:nth-child(2)"));
        assertFalse(css.contains(".technical-register-page"));
    }

    @Test
    void technicalDataDetailFileGridsUseTheSameHeaderRowAndSelectionStyle() throws Exception {
        String page = read(
                "src/main/webapp/WEB-INF/views/inside/distribution/swFilePopup.jsp");

        assertTrue(page.contains("var MAIN_GRID_BODY_HEIGHT = 56"));
        assertTrue(page.contains("var SUB_GRID_BODY_HEIGHT = 168"));
        assertTrue(page.contains(".sw-file-popup .ui-jqgrid .ui-jqgrid-htable th"));
        assertTrue(page.contains("height: 42px !important"));
        assertTrue(page.contains(
                ".sw-file-popup .ui-jqgrid .ui-jqgrid-btable tr.jqgrow td"));
        assertTrue(page.contains("height: 56px !important"));
        assertTrue(page.contains("background: #f1efff !important"));
        assertTrue(page.contains(".sw-file-popup .sw-file-link"));
    }

    private String styleBlock(String css, String selector) {
        int start = css.indexOf(selector);
        assertTrue(start >= 0, "CSS selector is missing: " + selector);
        int end = css.indexOf('}', start);
        assertTrue(end > start, "CSS block is not closed: " + selector);
        return css.substring(start, end + 1);
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}

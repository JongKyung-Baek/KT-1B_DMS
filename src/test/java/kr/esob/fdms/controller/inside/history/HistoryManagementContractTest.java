package kr.esob.fdms.controller.inside.history;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Authentication, menu actions and ACL decisions share one canonical audit
 * ledger. Successful viewing and printing remain independent ledgers because
 * an ACL decision is not proof that either action completed.
 */
class HistoryManagementContractTest {

    private static final String LEGACY_ACCESS_CONTROLLER =
            "src/main/java/kr/esob/fdms/controller/inside/distribution/"
                    + "viewprinthistory/ViewPrintHistoryController.java";
    private static final String HISTORY_CONTROLLER =
            "src/main/java/kr/esob/fdms/controller/inside/distribution/"
                    + "viewprinthistory/HistoryManagementController.java";
    private static final String HISTORY_MAPPER =
            "src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/"
                    + "viewPrintHistory/ViewPrintHistory.xml";
    private static final String AUDIT_MAPPER =
            "src/main/resources/sqlMaps/oracle/its/controller/inside/"
                    + "organizationmanage/auditlog/auditLog.xml";
    private static final String AUDIT_JSP =
            "src/main/webapp/WEB-INF/views/inside/organizationmanage/"
                    + "auditlog/auditlogList.jsp";
    private static final String RECORD_JSP =
            "src/main/webapp/WEB-INF/views/inside/distribution/"
                    + "viewPrintHistory/recordHistory.jsp";
    private static final String AUDIT_JS =
            "src/main/resources/static/js/views/inside/organizationmanage/"
                    + "auditlog/auditlogList.js";
    private static final String RECORD_JS =
            "src/main/resources/static/js/views/inside/distribution/"
                    + "viewPrintHistory/record-history.js";
    private static final String HISTORY_CSS =
            "src/main/resources/static/css/pages/access-history.css";
    private static final String AUDIT_CSS =
            "src/main/resources/static/css/pages/audit-log.css";

    @Test
    void accessAndAuditHistoryUseOneCompleteCanonicalLedgerScreen()
            throws Exception {
        String legacyController = read(LEGACY_ACCESS_CONTROLLER);
        String historyController = read(HISTORY_CONTROLLER);
        String mapper = compact(selectBlock(read(AUDIT_MAPPER), "selectList"));
        String jsp = read(AUDIT_JSP);

        assertTrue(legacyController.contains(
                "@RequestMapping(\"/inside/distribution/viewPrintHistory\")"));
        assertTrue(legacyController.contains(
                "redirect:/inside/organizationmanage/auditlog/"));
        assertFalse(legacyController.contains("/accessEvents"));
        assertFalse(legacyController.contains("/selectList"));
        assertFalse(legacyController.contains("/destroyRequest"));
        assertTrue(historyController.contains(
                "redirect:/inside/organizationmanage/auditlog/"));

        assertTrue(mapper.contains("FROM DOCS_ACCESS_AUDIT_LOG AUDITLOG"));
        assertTrue(mapper.contains("<INCLUDE REFID=\"AUDITFILTERS\"/>"));
        assertFalse(mapper.contains("EVENT_TYPE IN ("));
        assertTrue(jsp.contains("gridInsideAuditLogList"));
        assertFalse(jsp.contains("audit-log-summary"));
    }

    @Test
    void legacyAccessUrlOnlyAllowsTheProtectedRootRedirect() throws Exception {
        String security = read("src/main/java/kr/esob/fdms/config/SecurityConfig.java");

        assertTrue(security.contains(
                "\"/inside/distribution/viewPrintHistory/\")"));
        assertTrue(security.contains(".hasAuthority(\"ROLE_MENU_218\")"));
        assertTrue(security.contains(
                ".antMatchers(\"/inside/distribution/viewPrintHistory/**\").denyAll()"));
    }

    @Test
    void viewingHistoryIsServerScopedToPersistedViewingRecords()
            throws Exception {
        String controller = read(HISTORY_CONTROLLER);
        String mapper = compact(selectBlock(read(HISTORY_MAPPER), "selectViewEvents"));

        assertTrue(controller.contains("\"/inside/history\""));
        assertTrue(controller.contains("\"/view/\""));
        assertTrue(controller.contains("\"/view/events\""));
        assertTrue(mapper.contains("FROM DOCS_HISTORY"));
        assertTrue(mapper.contains("LOG_TYPE = 'VIEWING'"));
        assertFalse(mapper.contains("#{LOGTYPE}"));
        assertTrue(mapper.contains("'VIEW' AS ACTION_TYPE"));
        assertFalse(mapper.contains("'SUCCESS' AS STATUS_CD"));
    }

    @Test
    void printingHistoryUsesPersistedPrintJobsAndResourceItems()
            throws Exception {
        String controller = read(HISTORY_CONTROLLER);
        String mapper = compact(selectBlock(read(HISTORY_MAPPER), "selectPrintEvents"));

        assertTrue(controller.contains("\"/print/\""));
        assertTrue(controller.contains("\"/print/events\""));
        assertTrue(mapper.contains("FROM DOCS_PRINT_JOB"));
        assertTrue(mapper.contains("JOIN DOCS_PRINT_JOB_ITEM"));
        assertTrue(mapper.contains("PRINT_JOB_ID"));
        assertTrue(mapper.contains("STATUS_CD"));
        assertFalse(mapper.contains("FILE_ACCESS"));
    }

    @Test
    void viewAndPrintStillShareOneModeConfiguredRecordTemplate()
            throws Exception {
        String controller = read(HISTORY_CONTROLLER);
        String jsp = read(RECORD_JSP);
        String javascript = read(RECORD_JS);

        assertTrue(controller.contains("\"feature.history.view.title\""));
        assertTrue(controller.contains("\"feature.history.print.title\""));
        assertTrue(jsp.contains("${historyMode}"));
        assertTrue(jsp.contains("${historyEndpoint}"));
        assertTrue(jsp.contains("record-history.js"));
        assertTrue(javascript.contains("config.mode"));
        assertTrue(javascript.contains("config.endpoint"));
        assertTrue(javascript.contains("renderRows"));
        assertFalse(javascript.contains("renderSummary"));
        assertFalse(javascript.contains("jqGrid"));
    }

    @Test
    void allHistoryScreensStartWithSearchAndGridWithoutHeaderCards()
            throws Exception {
        String auditJsp = read(AUDIT_JSP);
        String recordJsp = read(RECORD_JSP);
        String auditJavascript = read(AUDIT_JS);
        String recordJavascript = read(RECORD_JS);
        String historyCss = read(HISTORY_CSS);
        String auditCss = read(AUDIT_CSS);

        assertFalse(auditJsp.contains("audit-log-hero"));
        assertFalse(auditJsp.contains("audit-log-summary"));
        assertTrue(auditJsp.contains(
                "<section class=\"audit-log-results-card\" aria-label=\"${resultsAria}\">"));
        assertTrue(recordJsp.contains(
                "<section class=\"ah-log-card\" aria-labelledby=\"recordLogTitle\">"));
        assertNoLegacyGrid(recordJsp);
        assertFalse(auditJavascript.contains("renderSummary"));
        assertFalse(recordJavascript.contains("renderSummary"));

        assertTrue(styleBlock(auditCss, ".audit-log-page {").contains("gap: 0"));
        assertTrue(styleBlock(historyCss, ".access-history-page {").contains("gap: 0"));
        assertFalse(auditCss.contains(".audit-log-summary"));
        assertTrue(styleBlock(auditCss,
                ".audit-log-page .formAcceptance .formAcceptanceActions .searchBtn.btn.btn-primary {")
                .contains("min-height: 36px !important"));
        assertTrue(styleBlock(historyCss, ".ah-button {")
                .contains("min-height: 36px"));
    }

    @Test
    void historyScreensDoNotExposeStatusOrResultFields() throws Exception {
        String auditJsp = read(AUDIT_JSP);
        String recordJsp = read(RECORD_JSP);
        String auditJavascript = read(AUDIT_JS);
        String recordJavascript = read(RECORD_JS);
        String auditDdl = read("src/main/resources/sql/audit_trail_ddl.sql");

        assertFalse(auditJsp.contains("처리 결과"));
        assertFalse(auditJsp.contains("<strong>결과</strong>"));
        assertFalse(auditJavascript.contains("formatAuditResult"));
        assertTrue(auditDdl.contains(
                "lower(column_id) IN ('resultcd', 'result')"));
        assertFalse(recordJsp.contains("처리 상태"));
        assertFalse(recordJsp.contains("<th scope=\"col\">처리 결과</th>"));
        assertFalse(recordJavascript.contains("resultHtml"));
        assertFalse(recordJavascript.contains("row.statusCd"));
    }

    @Test
    void viewingHistoryGridMapsUserMenuActionDocumentFileNumberAndTime()
            throws Exception {
        String jsp = read(RECORD_JSP);
        String javascript = read(RECORD_JS);

        assertTrue(jsp.contains("code=\"feature.history.column.time\""));
        assertTrue(jsp.contains("code=\"feature.history.column.user\""));
        assertTrue(jsp.contains("code=\"feature.history.column.menu\""));
        assertTrue(jsp.contains("code=\"feature.history.column.action\""));
        assertTrue(jsp.contains("code=\"feature.history.column.document\""));
        assertTrue(jsp.contains("code=\"feature.history.column.fileNumber\""));
        assertTrue(javascript.contains("row.occurredAt"));
        assertTrue(javascript.contains("row.actorUserId"));
        assertTrue(javascript.contains("row.menuCd"));
        assertTrue(javascript.contains("row.actionType"));
        assertTrue(javascript.contains("row.drawingNo || row.objectId"));
        assertTrue(javascript.contains("return cellHtml(row.fileNo"));
    }

    private void assertNoLegacyGrid(String jsp) {
        assertFalse(jsp.contains("<custom:listTemplate"));
        assertFalse(jsp.contains("gridViewPrintHistoryList"));
        assertFalse(jsp.contains("gridPrintHistoryList"));
        assertFalse(jsp.contains("jqGrid"));
    }

    private String selectBlock(String mapper, String selectId) {
        String marker = "<select id=\"" + selectId + "\"";
        int start = mapper.indexOf(marker);
        assertTrue(start >= 0, "Mapper statement is missing: " + selectId);
        int end = mapper.indexOf("</select>", start);
        assertTrue(end > start, "Mapper statement is not closed: " + selectId);
        return mapper.substring(start, end + "</select>".length());
    }

    private String styleBlock(String css, String selector) {
        int start = css.indexOf(selector);
        assertTrue(start >= 0, "CSS selector is missing: " + selector);
        int end = css.indexOf('}', start);
        assertTrue(end > start, "CSS block is not closed: " + selector);
        return css.substring(start, end + 1);
    }

    private String compact(String value) {
        return value.replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }

    private String read(String path) throws Exception {
        Path source = Paths.get(path);
        assertTrue(Files.isRegularFile(source), "Expected source file is missing: " + path);
        return new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
    }
}

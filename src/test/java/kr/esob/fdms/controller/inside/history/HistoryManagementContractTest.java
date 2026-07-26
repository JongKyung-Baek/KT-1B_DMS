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
 * Source contracts for the separated access, viewing and printing histories.
 *
 * <p>The three screens intentionally use different ledgers. An ACL decision is
 * not proof that a viewer opened successfully, and a PRINT permission decision
 * is not proof that a print job completed.</p>
 */
class HistoryManagementContractTest {

    private static final String ACCESS_CONTROLLER =
            "src/main/java/kr/esob/fdms/controller/inside/distribution/"
                    + "viewprinthistory/ViewPrintHistoryController.java";
    private static final String HISTORY_CONTROLLER =
            "src/main/java/kr/esob/fdms/controller/inside/distribution/"
                    + "viewprinthistory/"
                    + "HistoryManagementController.java";
    private static final String HISTORY_MAPPER =
            "src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/"
                    + "viewPrintHistory/ViewPrintHistory.xml";
    private static final String ACL_MAPPER =
            "src/main/resources/sqlMaps/oracle/its/commonlogic/securityacl/"
                    + "SecurityAcl.xml";
    private static final String ACCESS_JSP =
            "src/main/webapp/WEB-INF/views/inside/distribution/"
                    + "viewPrintHistory/historyList.jsp";
    private static final String RECORD_JSP =
            "src/main/webapp/WEB-INF/views/inside/distribution/"
                    + "viewPrintHistory/recordHistory.jsp";
    private static final String ACCESS_JS =
            "src/main/resources/static/js/views/inside/distribution/"
                    + "viewPrintHistory/access-history.js";
    private static final String RECORD_JS =
            "src/main/resources/static/js/views/inside/distribution/"
                    + "viewPrintHistory/record-history.js";
    private static final String HISTORY_CSS =
            "src/main/resources/static/css/pages/access-history.css";

    @Test
    void accessHistoryHasItsOwnRouteAndExcludesViewingAndPrintingEvents()
            throws Exception {
        String controller = read(ACCESS_CONTROLLER);
        String mapper = compact(selectBlock(read(ACL_MAPPER), "selectAccessHistory"));
        String jsp = read(ACCESS_JSP);
        String javascript = read(ACCESS_JS);

        assertTrue(controller.contains(
                "@RequestMapping(\"/inside/distribution/viewPrintHistory\")"));
        assertTrue(controller.contains("\"/accessEvents\""));
        assertTrue(javascript.contains(
                "/inside/distribution/viewPrintHistory/accessEvents"));

        assertTrue(mapper.contains(
                "EVENT_TYPE IN ('FILE_ACCESS', 'DOWNLOAD_RESULT', 'ACL_CHANGE')"),
                "접근이력은 PRINT_RESULT를 포함하지 않는 서버측 화이트리스트여야 합니다.");
        assertTrue(mapper.contains("ACTION_TYPE, '') NOT IN ('VIEW', 'PRINT')"),
                "열람·출력 ACL 판정은 각각의 전용 이력 화면으로 분리해야 합니다.");
        assertFalse(mapper.contains("'PRINT_RESULT'"));

        assertTrue(jsp.contains("access-history.js"));
        assertFalse(jsp.contains("record-history.js"));
    }

    @Test
    void viewingHistoryIsServerScopedToSuccessfulLegacyViewingRecords()
            throws Exception {
        String controller = read(HISTORY_CONTROLLER);
        String mapper = compact(selectBlock(read(HISTORY_MAPPER), "selectViewEvents"));

        assertTrue(controller.contains("\"/inside/history\""));
        assertTrue(controller.contains("\"/view/\""));
        assertTrue(controller.contains("\"/view/events\""));
        assertTrue(controller.contains("\"view\""));
        assertTrue(controller.contains("\"feature.history.view.title\""));
        assertTrue(controller.contains(
                "model.addAttribute(\"historyMode\", mode)"));
        assertTrue(controller.contains(
                "\"/inside/history/view/events\""));
        assertTrue(controller.contains(
                "\"inside/distribution/viewPrintHistory/recordHistory\""));

        assertTrue(mapper.contains("FROM DOCS_HISTORY"));
        assertTrue(mapper.contains("LOG_TYPE = 'VIEWING'"),
                "열람이력 범위는 요청 파라미터가 아니라 SQL에서 고정해야 합니다.");
        assertFalse(mapper.contains("#{LOGTYPE}"));
        assertTrue(mapper.contains("INSERT_DATE"));
        assertTrue(mapper.contains("USER_ID"));
        assertTrue(mapper.contains("OBJECT_ID"));
    }

    @Test
    void printingHistoryUsesPersistedPrintJobsAndTheirResourceItems()
            throws Exception {
        String controller = read(HISTORY_CONTROLLER);
        String mapper = compact(selectBlock(read(HISTORY_MAPPER), "selectPrintEvents"));

        assertTrue(controller.contains("\"/print/\""));
        assertTrue(controller.contains("\"/print/events\""));
        assertTrue(controller.contains("\"print\""));
        assertTrue(controller.contains("\"feature.history.print.title\""));
        assertTrue(controller.contains(
                "\"/inside/history/print/events\""));

        assertTrue(mapper.contains("FROM DOCS_PRINT_JOB"));
        assertTrue(mapper.contains("JOIN DOCS_PRINT_JOB_ITEM"),
                "병합 출력도 실제 대상 파일을 식별할 수 있도록 item 원장을 조인해야 합니다.");
        assertTrue(mapper.contains("PRINT_JOB_ID"));
        assertTrue(mapper.contains("STATUS_CD"));
        assertTrue(mapper.contains("ACTOR_USER_CD"));
        assertTrue(mapper.contains("REQUESTED_AT"));
        assertTrue(mapper.contains("COMPLETED_AT"));
        assertFalse(mapper.contains("FILE_ACCESS"),
                "PRINT 접근 허용을 실제 출력 이력으로 계산하면 안 됩니다.");
    }

    @Test
    void recordTemplateAndScriptAreModeConfiguredForIndependentViewAndPrintRoutes()
            throws Exception {
        String controller = read(HISTORY_CONTROLLER);
        String jsp = read(RECORD_JSP);
        String javascript = read(RECORD_JS);

        assertTrue(controller.contains("\"feature.history.view.title\""));
        assertTrue(controller.contains("\"feature.history.print.title\""));
        assertTrue(controller.contains("\"feature.history.view.description\""));
        assertTrue(controller.contains("\"feature.history.print.description\""));
        assertTrue(jsp.contains("${historyMode}"));
        assertTrue(jsp.contains("${historyTitle}"));
        assertTrue(jsp.contains("code=\"feature.locale.code\""));
        assertTrue(jsp.contains("<html lang=\"${pageLocale}\">"));
        assertTrue(jsp.contains("value=\"TECHNICAL_DATA\""));
        assertTrue(jsp.contains("${historyEndpoint}"));
        assertTrue(jsp.contains("window.recordHistoryConfig"));
        assertTrue(jsp.contains("record-history.js"));
        assertFalse(jsp.contains("access-history.js"));

        assertTrue(javascript.contains("config.mode"));
        assertTrue(javascript.contains("config.endpoint"));
        assertTrue(javascript.contains("window.SdmsI18n.t"));
        assertTrue(javascript.contains("feature.history.view.empty"));
        assertTrue(javascript.contains("feature.history.print.empty"));
        assertFalse(javascript.contains("renderSummary"));
        assertTrue(javascript.contains("renderRows"));
        assertFalse(javascript.contains("jqGrid"));
        assertFalse(javascript.contains("setGridParam"));
    }

    @Test
    void allHistoryScreensStartWithOneUnifiedSearchAndGridCard()
            throws Exception {
        String accessJsp = read(ACCESS_JSP);
        String recordJsp = read(RECORD_JSP);
        String accessJavascript = read(ACCESS_JS);
        String recordJavascript = read(RECORD_JS);
        String css = read(HISTORY_CSS);

        assertFalse(accessJsp.contains("ah-hero"));
        assertFalse(recordJsp.contains("ah-hero"));
        assertFalse(accessJsp.contains("ah-chip"));
        assertFalse(recordJsp.contains("ah-chip"));
        assertFalse(css.contains(".ah-hero"));
        assertTrue(accessJsp.contains(
                "<main class=\"distribution-invoice-page access-history-page\" "
                        + "aria-labelledby=\"accessLogTitle\">"));
        assertTrue(recordJsp.contains("aria-labelledby=\"recordLogTitle\">"));
        assertTrue(accessJsp.contains(
                "<section class=\"ah-log-card\" aria-labelledby=\"accessLogTitle\">"));
        assertTrue(recordJsp.contains(
                "<section class=\"ah-log-card\" aria-labelledby=\"recordLogTitle\">"));

        assertNoLegacyGrid(accessJsp);
        assertNoLegacyGrid(recordJsp);
        assertFalse(accessJsp.contains("기존 열람·출력 기록"));
        assertFalse(accessJsp.contains("ah-legacy-card"));
        assertFalse(accessJavascript.contains("renderSummary"));
        assertFalse(recordJavascript.contains("renderSummary"));
        assertFalse(accessJavascript.contains("HistoryUpdatedAt"));
        assertFalse(recordJavascript.contains("HistoryUpdatedAt"));

        assertTrue(styleBlock(css, ".access-history-page {").contains("gap: 0"),
                "상단 카드 제거 후 검색·목록 카드 위에 불필요한 간격이 없어야 합니다.");
        assertFalse(css.contains(".ah-summary"));
        assertFalse(css.contains(".ah-metric"));
    }

    @Test
    void accessViewAndPrintScreensShareIdenticalSearchButtonStyling()
            throws Exception {
        String accessJsp = read(ACCESS_JSP);
        String recordJsp = read(RECORD_JSP);
        String css = read(HISTORY_CSS);

        assertTrue(accessJsp.contains(
                "class=\"ah-button ah-button--ghost\" id=\"accessResetButton\""));
        assertTrue(accessJsp.contains(
                "class=\"ah-button ah-button--primary\" id=\"accessSearchButton\""));
        assertTrue(recordJsp.contains(
                "class=\"ah-button ah-button--ghost\" id=\"recordResetButton\""));
        assertTrue(recordJsp.contains(
                "class=\"ah-button ah-button--primary\" id=\"recordSearchButton\""));

        String actions = styleBlock(css, ".ah-search__actions {");
        assertTrue(actions.contains("gap: 7px"));

        String button = styleBlock(css, ".ah-button {");
        assertTrue(button.contains("min-height: 36px"));
        assertTrue(button.contains("padding: 7px 13px"));

        String primary = styleBlock(css, ".ah-button--primary {");
        assertTrue(primary.contains("background: var(--ah-primary)"));
        String ghost = styleBlock(css, ".ah-button--ghost {");
        assertTrue(ghost.contains("background: #fff"));
    }

    @Test
    void historyScreensDoNotExposeStatusOrResultFields() throws Exception {
        String accessJsp = read(ACCESS_JSP);
        String recordJsp = read(RECORD_JSP);
        String accessJavascript = read(ACCESS_JS);
        String recordJavascript = read(RECORD_JS);

        assertFalse(accessJsp.contains("id=\"accessResultCd\""));
        assertFalse(accessJsp.contains("<span>처리 결과</span>"));
        assertFalse(accessJsp.contains("<th scope=\"col\">결과</th>"));
        assertFalse(accessJavascript.contains("resultHtml"));
        assertFalse(accessJavascript.contains("resultCd:"));

        assertFalse(recordJsp.contains("처리 상태"));
        assertFalse(recordJsp.contains("<th scope=\"col\">처리 결과</th>"));
        assertFalse(recordJavascript.contains("statusLabels"));
        assertFalse(recordJavascript.contains("resultHtml"));
        assertFalse(recordJavascript.contains("row.statusCd"));
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

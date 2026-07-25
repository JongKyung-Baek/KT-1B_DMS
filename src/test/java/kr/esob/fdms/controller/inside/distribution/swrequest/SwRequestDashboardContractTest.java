package kr.esob.fdms.controller.inside.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class SwRequestDashboardContractTest {

	@Test
	void dashboardUsesLatestDocumentsAndPersonalListAclFoundation() throws Exception {
		String mapper = read(
			"src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/swrequest/SwRequest.xml");
		String dashboard = section(
			mapper, "<sql id=\"dashboardCommonCtes\">", "</sql>");
		String documentScope = tail(
			dashboard, "document_scope AS (");

		assertTrue(dashboard.contains("DISTINCT ON (sw.OBJECT_ID)"));
		assertTrue(dashboard.contains("COALESCE(sw.DELETED_YN, 'N') != 'Y'"));
		assertTrue(documentScope.contains("actionPermission.ACTION_CD = 'LIST'"));
		assertTrue(documentScope.contains("objectPermission.OBJECT_TYPE = 'SW'"));
		assertTrue(documentScope.contains("objectPermission.ACTION_CD = 'LIST'"));
		assertTrue(documentScope.contains("actor.user_grade_level &gt;= fileGrade.GRADE_LEVEL"));
		assertTrue(documentScope.contains("securityLabel.FILE_NO = '*'"));
		assertFalse(documentScope.contains("actor.manage_acl_yn"),
			"문서 범위는 관리자 권한으로 개인 ACL을 우회하면 안 됩니다.");

		assertTrue(mapper.contains("<select id=\"selectDashboardSummary\""));
		assertTrue(mapper.contains("<select id=\"selectDashboardGradeDistribution\""));
		assertTrue(mapper.contains("<select id=\"selectDashboardStatusDistribution\""));
		assertTrue(mapper.contains("<select id=\"selectDashboardRecentDocuments\""));
		assertTrue(mapper.contains("<select id=\"selectDashboardRecentActivities\""));
	}

	@Test
	void dashboardExposesCurrentActorsGradeValidityAndActionFlags() throws Exception {
		String mapper = read(
			"src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/swrequest/SwRequest.xml");
		String commonCtes = section(
			mapper, "<sql id=\"dashboardCommonCtes\">", "</sql>");
		String actorContext = section(
			commonCtes, "actor_context AS (", "latest_sw AS (");
		String summary = section(
			mapper, "<select id=\"selectDashboardSummary\"", "</select>");

		assertTrue(actorContext.contains("userGrade.GRADE_CD"));
		assertTrue(actorContext.contains("userGrade.GRADE_NM"));
		assertTrue(actorContext.contains("userGrade.GRADE_LEVEL"));
		assertTrue(actorContext.contains("clearance.VALID_TO"));
		assertTrue(actorContext.contains("ACTION_CD = 'LIST'"));
		assertTrue(actorContext.contains("ACTION_CD = 'VIEW'"));
		assertTrue(actorContext.contains("ACTION_CD = 'DOWNLOAD_ORIGINAL'"));
		assertTrue(actorContext.contains("ACTION_CD = 'PRINT'"));

		assertTrue(summary.contains("AS \"userGradeCd\""));
		assertTrue(summary.contains("AS \"userGradeNm\""));
		assertTrue(summary.contains("AS \"userGradeLevel\""));
		assertTrue(summary.contains("AS \"clearanceValidTo\""));
		assertTrue(summary.contains("AS \"listAllowedYn\""));
		assertTrue(summary.contains("AS \"viewAllowedYn\""));
		assertTrue(summary.contains("AS \"downloadAllowedYn\""));
		assertTrue(summary.contains("AS \"printAllowedYn\""));
	}

	@Test
	void dashboardActivityCountsUsePersistedOutcomeSources() throws Exception {
		String mapper = read(
			"src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/swrequest/SwRequest.xml");
		String summary = section(
			mapper, "<select id=\"selectDashboardSummary\"", "</select>");

		assertTrue(summary.contains("FROM DOCS_HISTORY viewHistory"));
		assertTrue(summary.contains("viewHistory.LOG_TYPE = 'VIEWING'"));
		assertTrue(summary.contains("viewHistory.USER_ID = actor.user_id"));
		assertTrue(summary.contains("auditLog.EVENT_TYPE = 'DOWNLOAD_RESULT'"));
		assertTrue(summary.contains("auditLog.RESULT_CD = 'SUCCESS'"));
		assertTrue(summary.contains("auditLog.ACTOR_USER_CD = actor.user_cd"));
		assertTrue(summary.contains("FROM DOCS_PRINT_JOB printJob"));
		assertTrue(summary.contains("printJob.STATUS_CD = 'SUCCESS'"));
		assertTrue(summary.contains("printJob.ACTOR_USER_CD = actor.user_cd"));
	}

	@Test
	void dashboardRecentActivitiesAreLimitedToTheCurrentActor() throws Exception {
		String mapper = read(
			"src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/swrequest/SwRequest.xml");
		String recentActivity = section(
			mapper, "<select id=\"selectDashboardRecentActivities\"", "</select>");

		assertFalse(recentActivity.contains("actor.manage_acl_yn"),
			"최근 활동은 관리자 권한으로 다른 사용자의 활동까지 노출하면 안 됩니다.");
		assertTrue(recentActivity.contains("activityEvent.actor_user_cd = actor.user_cd"));
		assertTrue(recentActivity.contains("activityEvent.actor_user_id = actor.user_id"));
	}

	@Test
	void dashboardRendersPersonalGradePermissionAndActivityContext() throws Exception {
		String page = read(
			"src/main/webapp/WEB-INF/views/inside/distribution/swDashboard.jsp");
		String css = read(
			"src/main/resources/static/css/pages/technical-data-dashboard.css");
		String pageWithoutWhitespace = page.replaceAll("\\s+", "");

		assertTrue(page.contains("dashboard-summary-grid"));
		assertTrue(page.contains("dashboard-metric-card"));
		assertTrue(page.contains("dashboard-context-chips"));
		assertTrue(page.contains("dashboard-activity-chip"));
		assertTrue(page.contains("문서등급 분포"));
		assertTrue(pageWithoutWhitespace.contains("내인가등급"));
		assertTrue(pageWithoutWhitespace.contains("내권한"));
		assertTrue(pageWithoutWhitespace.contains("내파일활동"));
		assertTrue(pageWithoutWhitespace.contains("내접근가능범위"));
		assertFalse(pageWithoutWhitespace.contains("관리자전체범위"));
		assertFalse(pageWithoutWhitespace.contains("보안등급/인가"));
		assertFalse(pageWithoutWhitespace.contains("보안등급·인가"));
		assertFalse(pageWithoutWhitespace.contains("등급미지정"));
		assertFalse(pageWithoutWhitespace.contains("사용자인가필요"));
		assertTrue(page.contains("document.gradeCd eq 'UNASSIGNED'"));
		assertTrue(page.contains("icon-base ti tabler-"));
		assertFalse(page.contains("class=\"ti ti-"),
			"현재 Iconify 번들은 tabler-* 클래스를 사용해야 합니다.");
		assertTrue(css.contains(".dashboard-summary-grid"));
		assertTrue(css.contains("@media (max-width: 767px)"));
	}

	@Test
	void legacyDashboardIsRemovedAndMainRedirectsServerSide() throws Exception {
		String controller = read(
			"src/main/java/kr/esob/fdms/controller/main/MainController.java");
		Path oldDashboard = Paths.get("src/main/webapp/WEB-INF/views/main/main.jsp");

		assertTrue(controller.contains(
			"return \"redirect:/inside/distribution/swRequest/dashboard\";"));
		assertFalse(Files.exists(oldDashboard));
	}

	@Test
	void technicalDataListHasAnIdempotentDashboardRoute() throws Exception {
		String list = read(
			"src/main/webapp/WEB-INF/views/inside/distribution/swRequestList.jsp");
		String ddl = read("src/main/resources/sql/acl_foundation_ddl.sql");

		assertTrue(list.contains("function openTechnicalDashboard()"));
		assertTrue(ddl.contains("'toolbarSwRequest', 'btnDashboard'"));
		assertTrue(ddl.contains("'openTechnicalDashboard()'"));
		assertTrue(ddl.contains("ON CONFLICT (toolbar_id, button_id) DO UPDATE"));
	}

	private String read(String path) throws Exception {
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}

	private String section(String source, String startMarker, String endMarker) {
		int start = source.indexOf(startMarker);
		assertTrue(start >= 0, "시작 구간을 찾을 수 없습니다: " + startMarker);
		int end = source.indexOf(endMarker, start + startMarker.length());
		assertTrue(end >= 0, "종료 구간을 찾을 수 없습니다: " + endMarker);
		return source.substring(start, end);
	}

	private String tail(String source, String startMarker) {
		int start = source.indexOf(startMarker);
		assertTrue(start >= 0, "시작 구간을 찾을 수 없습니다: " + startMarker);
		return source.substring(start);
	}
}

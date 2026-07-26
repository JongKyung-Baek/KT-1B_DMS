package kr.esob.fdms.commonlogic.audit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class CanonicalAuditContractTest {

    @Test
    void canonicalMapperPersistsTheCompleteRequestAndMenuContext() throws Exception {
        String mapper = read(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/securityacl/SecurityAcl.xml");

        assertTrue(mapper.contains(
                "menu_cd, menu_nm, menu_url, action_nm"));
        assertTrue(mapper.contains(
                "request_uri, http_method, http_status, duration_ms"));
        assertTrue(mapper.contains(
                "#{menuCd}, #{menuNm}, #{menuUrl}, #{actionNm}"));
        assertTrue(mapper.contains(
                "#{requestUri}, #{httpMethod}, #{httpStatus}, #{durationMs}"));
    }

    @Test
    void requestAuditRunsOnlyInsideSecurityAndImmediatelyBeforeAuthorization()
            throws Exception {
        String config = read("src/main/java/kr/esob/fdms/config/SecurityConfig.java");

        assertTrue(config.contains(
                "http.addFilterBefore(requestAuditFilter, FilterSecurityInterceptor.class)"));
        assertTrue(config.contains(
                "FilterRegistrationBean<RequestAuditFilter> requestAuditFilterRegistration"));
        assertTrue(config.contains("registration.setEnabled(false)"));
    }

    @Test
    void loginAndAuditDashboardAreConnectedToCanonicalIdentityAndSummary()
            throws Exception {
        String login = read("src/main/java/kr/esob/fdms/controller/login/LoginSuccess.java");
        String controller = read(
                "src/main/java/kr/esob/fdms/controller/inside/organizationmanage/auditlog/AuditLogController.java");

        assertTrue(login.contains(
                "session, userVo.getUserCd(), userVo.getUserId(), userVo.getUserNm(), request"));
        assertTrue(controller.contains(
                "model.addAttribute(\"dashboardSummary\", service.selectSummary())"));
    }

    @Test
    void securityAccessHistoryUsesExplicitScopeWithoutViewingOrPrintingNoise()
            throws Exception {
        String mapper = read(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/securityacl/SecurityAcl.xml");
        int selectStart = mapper.indexOf("<select id=\"selectAccessHistory\"");
        String selectAudit = mapper.substring(
                selectStart, mapper.indexOf("</select>", selectStart));

        assertTrue(selectAudit.contains(
                "event_type IN ('FILE_ACCESS', 'DOWNLOAD_RESULT', 'ACL_CHANGE')"));
        assertTrue(selectAudit.contains(
                "COALESCE(action_type, '') NOT IN ('VIEW', 'PRINT')"));
        assertTrue(selectAudit.contains("event_type = #{eventType}"));
        assertFalse(selectAudit.contains("'MENU_ACTION'"));
        assertFalse(selectAudit.contains("'AUTH'"));
        assertFalse(selectAudit.contains("'PRINT_RESULT'"));
    }

    @Test
    void auditLogPresentationOmitsTheMeaninglessResultField() throws Exception {
        String page = read(
                "src/main/webapp/WEB-INF/views/inside/organizationmanage/auditlog/auditlogList.jsp");
        String javascript = read(
                "src/main/resources/static/js/views/inside/organizationmanage/auditlog/auditlogList.js");
        String ddl = read("src/main/resources/sql/audit_trail_ddl.sql");
        String presentationMetadata = ddl.substring(
                ddl.indexOf("-- Search form metadata."));

        assertFalse(page.contains("처리 결과"));
        assertFalse(page.contains("<strong>결과</strong>"));
        assertFalse(page.contains("<small>Outcome</small>"));
        assertFalse(javascript.contains("formatAuditResult"));
        assertTrue(ddl.contains(
                "lower(column_id) IN ('resultcd', 'result')"));
        assertFalse(presentationMetadata.contains("'resultCd'"));
        assertFalse(presentationMetadata.contains("'결과'"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}

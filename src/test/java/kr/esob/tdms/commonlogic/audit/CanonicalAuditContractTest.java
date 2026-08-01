package kr.esob.tdms.commonlogic.audit;

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
        String config = read("src/main/java/kr/esob/tdms/config/SecurityConfig.java");

        assertTrue(config.contains(
                "http.addFilterBefore(requestAuditFilter, FilterSecurityInterceptor.class)"));
        assertTrue(config.contains(
                "FilterRegistrationBean<RequestAuditFilter> requestAuditFilterRegistration"));
        assertTrue(config.contains("registration.setEnabled(false)"));
    }

    @Test
    void loginUsesCanonicalIdentityAndCombinedScreenDoesNotLoadSummaryCards()
            throws Exception {
        String login = read("src/main/java/kr/esob/tdms/controller/login/LoginSuccess.java");
        String controller = read(
                "src/main/java/kr/esob/tdms/controller/general/organizationmanage/auditlog/AuditLogController.java");

        assertTrue(login.contains(
                "session, userVo.getUserCd(), userVo.getUserId(), userVo.getUserNm(), request"));
        assertFalse(controller.contains("dashboardSummary"));
    }

    @Test
    void combinedAccessAuditQueryReadsTheCompleteCanonicalLedger()
            throws Exception {
        String mapper = read(
                "src/main/resources/sqlMaps/oracle/its/controller/general/organizationmanage/auditlog/auditLog.xml");
        int selectStart = mapper.indexOf("<select id=\"selectList\"");
        String selectAudit = mapper.substring(
                selectStart, mapper.indexOf("</select>", selectStart));

        assertTrue(selectAudit.contains("FROM docs_access_audit_log auditLog"));
        assertTrue(selectAudit.contains("<include refid=\"auditFilters\"/>"));
        assertFalse(selectAudit.contains("event_type IN ("));
        assertFalse(selectAudit.contains("action_type, '') NOT IN"));
    }

    @Test
    void auditLogPresentationOmitsTheMeaninglessResultField() throws Exception {
        String page = read(
                "src/main/webapp/WEB-INF/views/general/organizationmanage/auditlog/auditlogList.jsp");
        String javascript = read(
                "src/main/resources/static/js/views/general/organizationmanage/auditlog/auditlogList.js");
        String ddl = read("src/main/resources/sql/audit_trail_ddl.sql");
        String presentationMetadata = ddl.substring(
                ddl.indexOf("-- Search form metadata."));

        assertFalse(page.contains("처리 결과"));
        assertFalse(page.contains("<strong>결과</strong>"));
        assertFalse(page.contains("<small>Outcome</small>"));
        assertFalse(page.contains("audit-log-summary"));
        assertFalse(javascript.contains("formatAuditResult"));
        assertTrue(ddl.contains(
                "lower(column_id) IN ('resultcd', 'result')"));
        assertFalse(presentationMetadata.contains("'resultCd'"));
        assertFalse(presentationMetadata.contains("'결과'"));
    }

    @Test
    void combinedHistoryLocalizesPasswordResetActions() throws Exception {
        String javascript = read(
                "src/main/resources/static/js/views/general/organizationmanage/auditlog/auditlogList.js");
        String korean = read("src/main/webapp/messages/feature.properties");
        String english = read("src/main/webapp/messages/feature_en.properties");

        assertTrue(javascript.contains(
                "PASSWORD_RESET: [\"feature.audit.action.passwordReset\""));
        assertTrue(korean.contains(
                "feature.audit.action.passwordReset=비밀번호 초기화"));
        assertTrue(english.contains(
                "feature.audit.action.passwordReset=Password Reset"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}

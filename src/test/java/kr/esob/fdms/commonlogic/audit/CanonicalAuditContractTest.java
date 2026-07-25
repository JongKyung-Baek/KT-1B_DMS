package kr.esob.fdms.commonlogic.audit;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void securityAccessHistoryDoesNotMixInMenuOrAuthenticationNoise()
            throws Exception {
        String mapper = read(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/securityacl/SecurityAcl.xml");
        int selectStart = mapper.indexOf("<select id=\"selectAudit\"");
        String selectAudit = mapper.substring(selectStart, mapper.indexOf("</select>", selectStart));

        assertTrue(selectAudit.contains(
                "event_type NOT IN ('MENU_ACTION', 'AUTH')"));
        assertTrue(selectAudit.contains("event_type = #{eventType}"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}

package kr.esob.tdms.controller.general.system.distributionaccountrequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;

class DistributionAccountRequestPageContractTest {
    private static final Path ROOT = Paths.get(System.getProperty("user.dir"));

    @Test
    void pageRouteRequiresAnAuthenticatedUserAndReturnsTheAdministratorView()
            throws Exception {
        SecurityAclService aclService = mock(SecurityAclService.class);
        DistributionAccountRequestPageController controller =
            new DistributionAccountRequestPageController(aclService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/general/distribution/account-requests/"))
            .andExpect(status().isOk())
            .andExpect(view().name(
                "/general/system/distributionaccountrequest/distributionAccountRequestList"));
        verify(aclService).requireCurrentUser();
        assertEquals(
            "/general/system/distributionaccountrequest/distributionAccountRequestList",
            controller.home());
    }

    @Test
    void menuUsesARealNavigationUrlAndOnlyTheAdministratorRole() throws Exception {
        String ddl = read("src/main/resources/sql/distribution_account_request_menu_ddl.sql");
        String fresh = read("src/main/resources/sql/fresh_database_migration.psql");
        String security = read("src/main/java/kr/esob/tdms/config/SecurityConfig.java");

        assertTrue(ddl.contains("'MENU_231', 'MENU_071', '배포시스템 계정요청'"));
        assertTrue(ddl.contains("'/general/distribution/account-requests/', 92"));
        assertFalse(ddl.contains("'/general/distribution/account-requests/**'"));
        assertTrue(ddl.contains("'RG_001', 'ROLE_MENU_231'"));
        assertTrue(ddl.contains("role_cd = 'ROLE_MENU_231'"));
        assertTrue(ddl.contains("group_cd <> 'RG_001'"));
        assertTrue(fresh.contains("\\ir distribution_account_request_ddl.sql"));
        assertTrue(fresh.contains("\\ir distribution_account_request_menu_ddl.sql"));
        assertTrue(fresh.indexOf("distribution_account_request_ddl.sql")
            < fresh.indexOf("distribution_account_request_menu_ddl.sql"));
        assertTrue(security.contains(
            "DistributionAccountIntegrationProperties.REQUEST_PATH + \"/*\""));
        assertTrue(security.contains(
            ".antMatchers(\"/general/distribution/account-requests/**\")"));
        assertTrue(security.contains(".hasAuthority(\"ROLE_MENU_231\")"));
        assertTrue(security.contains(
            "DistributionAccountIntegrationProperties.REQUEST_PATH + \"/**\""));
    }

    @Test
    void compactBilingualPageUsesTheFinalAdminApiContract() throws Exception {
        String jsp = read("src/main/webapp/WEB-INF/views/general/system/"
            + "distributionaccountrequest/distributionAccountRequestList.jsp");
        String script = read("src/main/resources/static/js/views/general/system/"
            + "distributionaccountrequest/distribution-account-request.js");
        String style = read("src/main/resources/static/css/pages/"
            + "distribution-account-request.css");
        String korean = read("src/main/webapp/messages/feature.properties");
        String english = read("src/main/webapp/messages/feature_en.properties");

        assertTrue(jsp.contains("feature.distributionAccountRequest.page.title"));
        assertTrue(jsp.contains("feature.distributionAccountRequest.column.sourceSystem"));
        assertTrue(jsp.contains("feature.distributionAccountRequest.column.representative"));
        assertTrue(jsp.contains("feature.distributionAccountRequest.column.targetUser"));
        assertTrue(jsp.contains("value=\"REGISTER_USER\""));
        assertTrue(jsp.contains("value=\"UNLOCK_ACCOUNT\""));
        assertTrue(jsp.contains("value=\"RESET_PASSWORD\""));
        assertFalse(jsp.contains("partnerCompany"));
        assertTrue(jsp.contains("자사 또는 타사 기술자료배포시스템"));
        assertTrue(style.contains(".dar-search"));
        assertTrue(style.contains(".dar-table th"));
        assertTrue(style.contains("text-align: center"));
        assertTrue(style.contains(".dar-heading > div"));
        assertTrue(style.contains("text-align: left"));

        assertTrue(script.contains(
            "'/general/distribution/account-requests/api'"));
        assertTrue(script.contains("sourceSystemId: value('accountRequestSourceSystem')"));
        assertTrue(script.contains("requestType: value('accountRequestTypeFilter')"));
        assertTrue(script.contains("status: value('accountRequestStatusFilter')"));
        assertTrue(script.contains("limit: 100"));
        assertFalse(script.contains("limit: 500"));
        assertTrue(script.contains("body: JSON.stringify({decisionComment: decisionComment})"));
        assertTrue(script.contains("DISTRIBUTION_ACCOUNT_REJECTION_COMMENT_REQUIRED"));
        assertTrue(script.contains("INVALID_DISTRIBUTION_ACCOUNT_STATUS_TRANSITION"));
        assertTrue(script.contains("DISTRIBUTION_ACCOUNT_DECISION_COMMENT_TOO_LONG"));
        assertTrue(script.contains("DISTRIBUTION_ACCOUNT_REQUEST_ACCESS_DENIED"));
        assertTrue(script.contains("window.SdmsCsrf.headers(headers)"));
        assertTrue(script.contains("meta[name=\"_csrf\"]"));
        assertTrue(script.contains("meta[name=\"_csrf_header\"]"));
        assertTrue(script.contains("requestHeaders(headers, requestOptions.method)"));
        assertTrue(script.contains("identity(record.targetUserName, record.targetUserId)"));
        assertTrue(script.contains("contact(record.targetUserEmail, record.targetUserPhone)"));
        int successBusyRelease = script.indexOf(
            "setBusy(false);\n            return loadRecords();");
        assertTrue(successBusyRelease >= 0,
            "the decision flow must release busy state before refreshing the list");

        assertTrue(korean.contains(
            "feature.distributionAccountRequest.page.title=배포시스템 계정요청"));
        assertTrue(english.contains(
            "feature.distributionAccountRequest.page.title=Distribution Account Requests"));
        assertTrue(korean.contains(
            "feature.distributionAccountRequest.type.RESET_PASSWORD=비밀번호 초기화"));
        assertTrue(english.contains(
            "feature.distributionAccountRequest.type.RESET_PASSWORD=Reset Password"));
    }

    private String read(String relativePath) throws Exception {
        return new String(Files.readAllBytes(ROOT.resolve(relativePath)),
            StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}

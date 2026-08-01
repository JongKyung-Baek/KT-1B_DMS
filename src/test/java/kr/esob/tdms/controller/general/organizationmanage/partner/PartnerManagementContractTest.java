package kr.esob.tdms.controller.general.organizationmanage.partner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class PartnerManagementContractTest {
    @Test
    void databaseKeepsPartnerContactsSeparateAndEnforcesOneRepresentative() throws Exception {
        String ddl = read("src/main/resources/sql/partner_management_ddl.sql");
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_partner_company"));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_partner_user"));
        assertTrue(ddl.contains("uq_docs_partner_user_representative"));
        assertTrue(ddl.contains("WHERE representative_yn = 'Y' AND use_yn = 'Y' AND del_yn = 'N'"));
        assertTrue(ddl.contains("CREATE CONSTRAINT TRIGGER trg_docs_partner_user_representative"));
        assertTrue(ddl.contains("active_representatives <> 1"));
        String partnerUserTable = ddl.substring(
            ddl.indexOf("CREATE TABLE IF NOT EXISTS docs_partner_user"),
            ddl.indexOf("ALTER SEQUENCE docs_partner_user_id_seq"));
        assertFalse(partnerUserTable.toLowerCase().contains("user_pwd"));
        assertFalse(partnerUserTable.toLowerCase().contains("role_group"));
    }

    @Test
    void freshMigrationCreatesDirectoryBeforeDistributionForeignKeys() throws Exception {
        String fresh = read("src/main/resources/sql/fresh_database_migration.psql");
        int partner = fresh.indexOf("\\ir partner_management_ddl.sql");
        int distribution = fresh.indexOf("\\ir distribution_workflow_ddl.sql");
        assertTrue(partner >= 0);
        assertTrue(distribution > partner);
    }

    @Test
    void menuPermissionAndBilingualScreenAreWired() throws Exception {
        String ddl = read("src/main/resources/sql/partner_management_ddl.sql");
        String security = read("src/main/java/kr/esob/tdms/config/SecurityConfig.java");
        String jsp = read("src/main/webapp/WEB-INF/views/general/organizationmanage/partner/partnerManagement.jsp");
        String script = read("src/main/resources/static/js/views/general/organizationmanage/partner/partner-management.js");
        String koreanBase = read("src/main/webapp/messages/feature.properties");
        String korean = read("src/main/webapp/messages/feature_ko.properties");
        String english = read("src/main/webapp/messages/feature_en.properties");

        assertTrue(ddl.contains("'MENU_230', 'MENU_071', '협력업체 관리'"));
        assertTrue(ddl.contains("'/general/organizationmanage/partner/**', 91"));
        assertTrue(ddl.contains("'ROLE_MENU_230'"));
        assertTrue(security.contains(".hasAuthority(\"ROLE_MENU_230\")"));
        assertTrue(jsp.contains("feature.partner.page.title"));
        assertTrue(jsp.contains("partner-management.css?v=20260801.1"));
        assertTrue(korean.contains("feature.partner.page.title=협력업체 관리"));
        assertTrue(english.contains("feature.partner.page.title=Partner Management"));
        assertTrue(script.contains("function localizedApiError(body, fallback)"));
        assertTrue(script.contains("DUPLICATE_PARTNER_BUSINESS_NO: 'feature.partner.error.duplicateBusinessNo'"));
        assertTrue(koreanBase.contains("feature.partner.error.duplicateBusinessNo=이미 등록된 사업자번호입니다."));
        assertTrue(english.contains("feature.partner.error.duplicateBusinessNo=This business number is already registered."));
    }

    @Test
    void distributionFacingDirectoryHasStableValidationBoundary() throws Exception {
        String service = read("src/main/java/kr/esob/tdms/controller/general/organizationmanage/partner/PartnerDirectoryService.java");
        String mapper = read("src/main/resources/sqlMaps/oracle/its/controller/general/organizationmanage/partner/PartnerManagement.xml");
        assertTrue(service.contains("listActiveRecipients(long partnerCompanyId)"));
        assertTrue(service.contains("requireActiveRecipient(long partnerCompanyId, long partnerUserId)"));
        assertTrue(mapper.contains("company.use_yn = 'Y'"));
        assertTrue(mapper.contains("partner_user.use_yn = 'Y'"));
        assertTrue(mapper.contains("partner_user.representative_yn DESC"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}

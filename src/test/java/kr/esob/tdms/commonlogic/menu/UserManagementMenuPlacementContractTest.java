package kr.esob.tdms.commonlogic.menu;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class UserManagementMenuPlacementContractTest {

    @Test
    void partnerAccountRequestAndSecurityAccessLiveUnderUserManagementRoot()
            throws Exception {
        String partner = read("partner_management_ddl.sql");
        String accountRequest = read("distribution_account_request_menu_ddl.sql");
        String securityAccess = read("acl_foundation_ddl.sql");
        String relocation = read("user_management_menu_relocation_ddl.sql");
        String freshMigration = read("fresh_database_migration.psql");

        assertTrue(partner.contains("'MENU_230', 'MENU_071'"));
        assertTrue(partner.contains("'/general/organizationmanage/partner/**', 91"));
        assertTrue(accountRequest.contains("'MENU_231', 'MENU_071'"));
        assertTrue(accountRequest.contains(
                "'/general/distribution/account-requests/', 92"));
        assertTrue(securityAccess.contains("'MENU_222', 'MENU_071'"));
        assertTrue(securityAccess.contains(
                "'/general/system/securityaccess/', 93"));

        assertFalse(partner.contains("'MENU_230', 'MENU_214'"));
        assertFalse(accountRequest.contains("'MENU_231', 'MENU_214'"));
        assertFalse(securityAccess.contains("'MENU_222', 'MENU_214'"));

        assertTrue(relocation.contains("WHERE menu_cd IN ('MENU_230', 'MENU_231', 'MENU_222')"));
        assertTrue(relocation.contains("WHEN 'MENU_230' THEN 91"));
        assertTrue(relocation.contains("WHEN 'MENU_231' THEN 92"));
        assertTrue(relocation.contains("WHEN 'MENU_222' THEN 93"));
        assertTrue(relocation.contains("User-management child menu relocation is incomplete."));
        assertTrue(freshMigration.contains(
                "\\ir user_management_menu_relocation_ddl.sql"));
    }

    private String read(String fileName) throws Exception {
        return Files.readString(Path.of("src", "main", "resources", "sql", fileName),
                StandardCharsets.UTF_8);
    }
}

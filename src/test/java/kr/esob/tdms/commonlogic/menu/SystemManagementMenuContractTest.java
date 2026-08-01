package kr.esob.tdms.commonlogic.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class SystemManagementMenuContractTest {

    @Test
    void securedWildcardBecomesOneCanonicalNavigationSlash() {
        assertEquals("/general/system/menu/",
                MenuDao.toNavigationUrl("/general/system/menu/**"));
        assertEquals("/general/system/treemanage/",
                MenuDao.toNavigationUrl("/general/system/treemanage/"));
    }

    @Test
    void supportedPermissionMenusAreReparentedUnderActiveSystemManagement()
            throws Exception {
        String ddl = read("src/main/resources/sql/acl_foundation_ddl.sql");

        assertTrue(ddl.contains("menu_nm = '분류/레벨 관리'"));
        assertTrue(ddl.contains("message_cd = ''"));
        assertTrue(ddl.contains("WHERE menu_cd = 'MENU_215'"));
        assertTrue(ddl.contains("menu_nm = '메뉴권한'"));
        assertTrue(ddl.contains("WHERE menu_cd = 'MENU_138'"));
        assertTrue(ddl.contains("menu_nm = '사용자등급'"));
        assertTrue(ddl.contains("WHERE menu_cd = 'MENU_141'"));
        assertTrue(ddl.contains("menu_nm = '메뉴권한배정'"));
        assertTrue(ddl.contains("WHERE menu_cd = 'MENU_160'"));
        assertTrue(ddl.contains("parent_menu_cd = 'MENU_214'"));
    }

    @Test
    void accessAndAuditHistoriesBecomeOneMenuAndKeepBothGroupAudiences()
            throws Exception {
        String ddl = read("src/main/resources/sql/acl_foundation_ddl.sql");

        assertTrue(ddl.contains(
                "'MENU_218', 'MENU_223', '접근·감사이력', 'menu.accessAuditHistory'"));
        assertTrue(ddl.contains(
                "'/general/organizationmanage/auditlog/**', 116, 'leaf'"));
        assertTrue(ddl.contains("WHERE source.role_cd = 'ROLE_MENU_206'"));
        assertTrue(ddl.contains("WHERE source.role_cd = 'ROLE_MENU_218'"));
        assertTrue(ddl.contains("'ROLE_MENU_223'"));
        assertTrue(ddl.contains("WHERE role_cd = 'ROLE_MENU_206'"));
        assertTrue(ddl.contains("WHERE menu_cd = 'MENU_206'"));
        assertTrue(ddl.contains(
                "ON CONFLICT (group_cd, role_cd) DO NOTHING"));
    }

    @Test
    void classificationPageUsesFeatureButtonMessagesWithKoreanFallbacks()
            throws Exception {
        String jsp = read(
                "src/main/webapp/WEB-INF/views/general/system/treemanage/treeManage.jsp");

        assertTrue(jsp.contains("code=\"feature.common.button.add\" text=\"추가\""));
        assertTrue(jsp.contains("code=\"feature.common.button.edit\" text=\"수정\""));
        assertTrue(jsp.contains("code=\"feature.common.button.delete\" text=\"삭제\""));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)),
                StandardCharsets.UTF_8);
    }
}

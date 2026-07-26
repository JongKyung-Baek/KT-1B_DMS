package kr.esob.fdms.commonlogic.menu;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class HistoryMenuContractTest {

    @Test
    void historyMenusUseOneExactRootAndThreeIndependentlyAuthorizedChildren()
            throws Exception {
        String ddl = read("src/main/resources/sql/acl_foundation_ddl.sql");

        assertTrue(ddl.contains(
                "'MENU_223', 'I', '이력관리', '', '1', 'T'"));
        assertTrue(ddl.contains(
                "'/inside/history/', 115, 'root', 'N', 'Y'"));
        assertFalse(ddl.contains(
                "'MENU_223', 'I', '이력관리', '', '1', 'T',"
                        + System.lineSeparator()
                        + "    '/inside/history/**'"));

        assertTrue(ddl.contains(
                "'MENU_206', 'MENU_223', '접근이력', '', '2', 'M'"));
        assertTrue(ddl.contains(
                "'/inside/distribution/viewPrintHistory/**', 116, 'leaf'"));
        assertTrue(ddl.contains("'ROLE_MENU_206', 'I', ''"));

        assertTrue(ddl.contains(
                "'MENU_224', 'MENU_223', '열람이력', '', '2', 'M'"));
        assertTrue(ddl.contains(
                "'/inside/history/view/**', 117, 'leaf'"));
        assertTrue(ddl.contains("'ROLE_MENU_224', 'I', ''"));

        assertTrue(ddl.contains(
                "'MENU_225', 'MENU_223', '출력이력', '', '2', 'M'"));
        assertTrue(ddl.contains(
                "'/inside/history/print/**', 118, 'leaf'"));
        assertTrue(ddl.contains("'ROLE_MENU_225', 'I', ''"));
        assertTrue(ddl.contains(
                "SET menu_nm = '출력 승인/폐기 관리'"));
    }

    @Test
    void newHistoryRolesIdempotentlyInheritEveryAccessHistoryGroup()
            throws Exception {
        String ddl = read("src/main/resources/sql/acl_foundation_ddl.sql");

        assertTrue(ddl.contains(
                "('RG_001', 'ROLE_MENU_206', 'SYSTEM', 'SYSTEM'"));
        assertTrue(ddl.contains("('ROLE_MENU_223')"));
        assertTrue(ddl.contains("('ROLE_MENU_224')"));
        assertTrue(ddl.contains("('ROLE_MENU_225')"));
        assertTrue(ddl.contains(
                "WHERE source.role_cd = 'ROLE_MENU_206'"));
        assertTrue(ddl.contains(
                "ON CONFLICT (group_cd, role_cd) DO NOTHING"));
    }

    @Test
    void securityMenuSourceReturnsOnlyActiveNonDeletedRoutes()
            throws Exception {
        String mapper = read(
                "src/main/resources/sqlMaps/oracle/its/controller/menu/Menu.xml");
        int selectStart = mapper.indexOf("<select id=\"getMenuList\"");
        int selectEnd = mapper.indexOf("</select>", selectStart);
        String getMenuList = mapper.substring(selectStart, selectEnd);

        assertTrue(getMenuList.contains("WHERE USE_YN = 'Y'"));
        assertTrue(getMenuList.contains("AND DEL_YN = 'N'"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)),
                StandardCharsets.UTF_8);
    }
}

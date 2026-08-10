package kr.esob.tdms.commonlogic.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class MenuUrlPolicyTest {
    private static final Pattern MENU_INSERT = Pattern.compile(
            "(?is)INSERT\\s+INTO\\s+docs_menu\\s*\\(.*?;");
    private static final Pattern PATH_LITERAL = Pattern.compile("'(/[^'\\r\\n]*)'");

    @Test
    void preservesStructuralBlanksAndNormalizesInternalPaths() {
        assertNull(MenuUrlPolicy.normalizeForStorage(null));
        assertEquals("", MenuUrlPolicy.normalizeForStorage(""));
        assertEquals("", MenuUrlPolicy.normalizeForStorage("  \t"));
        assertEquals("/general/history/",
                MenuUrlPolicy.normalizeForStorage(" /general/history/ "));
        assertEquals("/general/history/view/**",
                MenuUrlPolicy.normalizeForStorage("/general/history/view/**"));
        assertEquals("/general/system/securityaccess/document-permissions",
                MenuUrlPolicy.normalizeForStorage(
                        "/general/system/securityaccess/document-permissions"));
    }

    @Test
    void rejectsSchemesExternalDestinationsAndParserConfusion() {
        List<String> invalid = List.of(
                "javascript:alert(1)",
                "JaVaScRiPt:alert(1)",
                "data:text/html,payload",
                "https://example.test/general/history/",
                "http://example.test/",
                "//example.test/general/history/",
                "///example.test/general/history/",
                "\\\\example.test\\share",
                "/\\example.test/share",
                "/general\\history/",
                "/general/history/?next=https://example.test",
                "/general/history/#fragment",
                "/general/../admin/",
                "/general/%2e%2e/admin/",
                "/%2f%2fexample.test/",
                "/%5cexample.test/",
                "/general/%0d%0ahistory/",
                "/general/%00history/",
                "/general/\r\nhistory/",
                "/general/ history/",
                "general/history/");

        for (String value : invalid) {
            assertThrows(IllegalArgumentException.class,
                    () -> MenuUrlPolicy.normalizeForStorage(value), value);
        }
    }

    @Test
    void allMenuPathsDeclaredBySqlMigrationsRemainAllowed() throws Exception {
        int checked = 0;
        try (var paths = Files.walk(Path.of("src/main/resources/sql"))) {
            for (Path path : paths.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".sql"))
                    .toList()) {
                String sql = Files.readString(path, StandardCharsets.UTF_8);
                Matcher insert = MENU_INSERT.matcher(sql);
                while (insert.find()) {
                    Matcher literal = PATH_LITERAL.matcher(insert.group());
                    while (literal.find()) {
                        String menuPath = literal.group(1);
                        assertEquals(menuPath,
                                MenuUrlPolicy.normalizeForStorage(menuPath),
                                path + " declares a menu path rejected by the policy");
                        checked++;
                    }
                }
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(checked >= 10,
                "Expected the migration fixtures to declare representative menu paths");
    }

    @Test
    void navigationConversionDisablesOnlyUnsafeLinks() {
        assertEquals("/general/history/view/",
                MenuDao.toNavigationUrl("/general/history/view/**"));
        assertEquals("", MenuDao.toNavigationUrl(""));
        assertNull(MenuDao.toNavigationUrl("javascript:alert(1)"));
        assertNull(MenuDao.toNavigationUrl("//example.test/"));
    }

    @Test
    void legacyInsertUsesTheSamePolicyBeforeAnyDatabaseAccess() {
        MenuDao dao = mock(MenuDao.class);
        MenuService service = new MenuService();
        service.dao = dao;
        MenuVO malicious = new MenuVO();
        malicious.setMenuUrl("data:text/html,payload");

        assertThrows(IllegalArgumentException.class,
                () -> service.insertMenu(malicious));
        verifyNoInteractions(dao);

        MenuVO parent = new MenuVO();
        parent.setMenuCd("MENU_100");
        parent.setRoleCd("ROLE_000100");
        parent.setMenuLevel(1);
        MenuVO valid = new MenuVO();
        valid.setParentMenuCd("MENU_100");
        valid.setMenuType("M");
        valid.setMenuUrl(" /general/history/ ");
        when(dao.getParentMenuInfo(valid)).thenReturn(List.of(parent));

        service.insertMenu(valid);

        assertEquals("/general/history/", valid.getMenuUrl());
        verify(dao).getParentMenuInfo(valid);
        verify(dao).insertMenu(valid);
    }
}

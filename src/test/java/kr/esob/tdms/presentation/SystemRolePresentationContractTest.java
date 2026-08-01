package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class SystemRolePresentationContractTest {

    private static final Path ROLE_VIEW = Paths.get(
            "src/main/webapp/WEB-INF/views/general/system/role/roleSide.jsp");
    private static final Path ROLE_POPUP = Paths.get(
            "src/main/webapp/WEB-INF/views/general/system/role/rolePopup.jsp");
    private static final Path ROLE_SCRIPT = Paths.get(
            "src/main/resources/static/js/views/general/system/role/roleSide.js");
    private static final Path ROLE_POPUP_SCRIPT = Paths.get(
            "src/main/resources/static/js/views/general/system/role/rolePopup.js");
    private static final Path ROLE_STYLE = Paths.get(
            "src/main/resources/static/css/pages/role-vuexy.css");
    private static final Path ROLE_ASSIGN_VIEW = Paths.get(
            "src/main/webapp/WEB-INF/views/general/system/roleassign/roleSide.jsp");
    private static final Path ROLE_ASSIGN_SCRIPT = Paths.get(
            "src/main/resources/static/js/views/general/system/roleassign/roleSide.js");
    private static final Path ROLE_ASSIGN_STYLE = Paths.get(
            "src/main/resources/static/css/pages/roleassign-vuexy.css");
    private static final Path DEFAULT_MESSAGES = Paths.get(
            "src/main/webapp/messages/feature.properties");
    private static final Path KOREAN_MESSAGES = Paths.get(
            "src/main/webapp/messages/feature_ko.properties");
    private static final Path ENGLISH_MESSAGES = Paths.get(
            "src/main/webapp/messages/feature_en.properties");
    private static final Pattern SYSTEM_ROLE_KEY = Pattern.compile(
            "feature\\.system\\.(?:role|roleassign)(?:\\.[A-Za-z0-9_-]+)+");
    private static final Pattern HANGUL = Pattern.compile("[가-힣]");

    @Test
    void userGradeScreenUsesCompactTdmsCardsChipsAndUnifiedGrids()
            throws Exception {
        String view = read(ROLE_VIEW);
        String popup = read(ROLE_POPUP);
        String css = read(ROLE_STYLE);

        assertTrue(view.contains("<main class=\"role-management-page\""));
        assertTrue(view.contains("class=\"system-role-page-heading\""));
        assertTrue(view.contains("class=\"system-role-heading-chip\""));
        assertTrue(view.contains("class=\"role-management-layout\""));
        assertTrue(view.contains("role-management-card role-group-card"));
        assertTrue(view.contains("role-management-card role-assignment-card"));
        assertTrue(view.contains("role-grid-panel"));
        assertTrue(view.contains("class=\"role-search-form\""));
        assertTrue(view.contains("class=\"role-selection-chip\""));
        assertTrue(view.contains("role-vuexy.css?v=20260801.2"));
        assertTrue(css.contains(".role-management-page .ui-jqgrid-htable th"));
        assertTrue(css.contains("text-align: center !important"));
        assertTrue(css.contains("background: #f1efff !important"));
        assertTrue(css.contains(".rolePopup .popupHero"));
        assertTrue(styleBlock(css, ".system-role-page-heading {")
                .contains("text-align: left"));
        assertTrue(styleBlock(css, ".role-management-card {")
                .contains("border-radius: 14px"));
        assertTrue(popup.contains("class=\"popupHero\""));
        assertTrue(popup.contains("class=\"section popupCard"));
    }

    @Test
    void menuPermissionAssignmentUsesTheSameGradeCardAndTreeLanguage()
            throws Exception {
        String view = read(ROLE_ASSIGN_VIEW);
        String script = read(ROLE_ASSIGN_SCRIPT);
        String css = read(ROLE_ASSIGN_STYLE);

        assertTrue(view.contains("<main class=\"roleassign-page\""));
        assertTrue(view.contains("class=\"roleassign-page-heading\""));
        assertTrue(view.contains("class=\"roleassign-heading-chip\""));
        assertTrue(view.contains("class=\"roleassign-layout\""));
        assertTrue(view.contains("roleassign-card roleassign-grade-card"));
        assertTrue(view.contains("roleassign-card roleassign-permission-card"));
        assertTrue(view.contains("class=\"roleassign-guide\""));
        assertTrue(view.contains("roleassign-selection-chip"));
        assertTrue(view.contains("roleassign-count-chip--green"));
        assertTrue(view.contains("roleassign-vuexy.css?v=20260801.2"));
        assertTrue(styleBlock(css, ".roleassign-page-heading {")
                .contains("text-align: left"));
        assertTrue(styleBlock(css, ".roleassign-card {")
                .contains("border-radius: 14px"));
        assertTrue(css.contains(".roleassign-page .tree-checkbox.tree-checkbox-on"));
        assertTrue(script.contains("keydown\", \"#menuTree .tree-checkbox"));
        assertTrue(script.contains("role: \"checkbox\""));
        assertTrue(script.contains("tabindex: \"0\""));
        assertTrue(script.contains("\"aria-checked\": \"false\""));
        assertTrue(script.contains(".attr(\"aria-checked\", \"true\")"));
    }

    @Test
    void legacyIdsFunctionsAndServerEndpointsRemainStable() throws Exception {
        String roleView = read(ROLE_VIEW);
        String roleScript = read(ROLE_SCRIPT);
        String rolePopup = read(ROLE_POPUP);
        String rolePopupScript = read(ROLE_POPUP_SCRIPT);
        String assignView = read(ROLE_ASSIGN_VIEW);
        String assignScript = read(ROLE_ASSIGN_SCRIPT);

        String[] roleIds = {
                "addGroup", "modGroup", "delGroup", "managerCount", "tabs",
                "formRoleDept", "gridRoleDept", "gridRoleDeptPager",
                "formRoleDeptAssign", "gridRoleDeptAssigned",
                "formRoleUser", "gridRoleUser", "gridRoleUserPager",
                "formRoleUserAssign", "gridRoleUserAssigned"
        };
        for (String id : roleIds) {
            assertTrue(roleView.contains("id=\"" + id + "\""),
                    "Missing user-grade DOM ID: " + id);
        }

        String[] roleFunctions = {
                "function addGroup()", "function modGroup()",
                "function delGroup()", "function saveRole()",
                "function addList()", "function delList()"
        };
        for (String function : roleFunctions) {
            assertTrue(roleScript.contains(function),
                    "Missing user-grade function: " + function);
        }
        assertTrue(roleScript.contains("'/general/system/role/getRoleGroupList'"));
        assertTrue(roleScript.contains("'/general/system/role/getAssignedDept'"));
        assertTrue(roleScript.contains("'/general/system/role/getAssignedUser'"));
        assertTrue(roleScript.contains("'/general/system/role/saveRoleGroupMember'"));
        assertTrue(roleScript.contains("\"/general/system/role/roleAddPopup\""));
        assertTrue(roleScript.contains("\"/general/system/role/roleModPopup\""));
        assertTrue(rolePopup.contains("id=\"formPopup\""));
        assertTrue(rolePopup.contains("id=\"groupCd\""));
        assertTrue(rolePopup.contains("id=\"saveFlag\""));
        assertTrue(rolePopup.contains("id=\"groupNm\""));
        assertTrue(rolePopupScript.contains("function savePopup()"));
        assertTrue(rolePopupScript.contains("'/general/system/role/saveRoleGroup'"));

        String[] assignIds = {
                "roleassignPageTitle", "roleassignGradeTitle", "managerCount",
                "roleassignMenuTitle", "selectedRoleName", "selectedMenuCount",
                "menuTree"
        };
        for (String id : assignIds) {
            assertTrue(assignView.contains("id=\"" + id + "\""),
                    "Missing menu-assignment DOM ID: " + id);
        }
        assertTrue(assignScript.contains("function setUserGradeList()"));
        assertTrue(assignScript.contains("function setMenuList(groupCd)"));
        assertTrue(assignScript.contains("function saveRole()"));
        assertTrue(assignScript.contains(
                "\"/general/system/roleassign/getRoleGroupList\""));
        assertTrue(assignScript.contains(
                "\"/general/system/roleassign/getAssignedMenuList\""));
        assertTrue(assignScript.contains(
                "\"/general/system/roleassign/saveAssign\""));
    }

    @Test
    void everyRoleFeatureKeyHasDefaultKoreanAndEnglishTranslations()
            throws Exception {
        Set<String> referenced = new LinkedHashSet<String>();
        collectKeys(referenced, ROLE_VIEW);
        collectKeys(referenced, ROLE_POPUP);
        collectKeys(referenced, ROLE_SCRIPT);
        collectKeys(referenced, ROLE_POPUP_SCRIPT);
        collectKeys(referenced, ROLE_ASSIGN_VIEW);
        collectKeys(referenced, ROLE_ASSIGN_SCRIPT);

        Properties defaults = load(DEFAULT_MESSAGES);
        Properties korean = load(KOREAN_MESSAGES);
        Properties english = load(ENGLISH_MESSAGES);
        assertFalse(referenced.isEmpty());

        for (String key : referenced) {
            assertTrue(defaults.containsKey(key), "Missing default message: " + key);
            assertTrue(korean.containsKey(key), "Missing Korean message: " + key);
            assertTrue(english.containsKey(key), "Missing English message: " + key);
            assertFalse(defaults.getProperty(key).trim().isEmpty(),
                    "Blank default message: " + key);
            assertFalse(korean.getProperty(key).trim().isEmpty(),
                    "Blank Korean message: " + key);
            assertFalse(english.getProperty(key).trim().isEmpty(),
                    "Blank English message: " + key);
            assertFalse(HANGUL.matcher(english.getProperty(key)).find(),
                    "Korean text remains in English message: " + key);
        }
    }

    private void collectKeys(Set<String> keys, Path path) throws Exception {
        Matcher matcher = SYSTEM_ROLE_KEY.matcher(read(path));
        while (matcher.find()) {
            keys.add(matcher.group());
        }
    }

    private Properties load(Path path) throws Exception {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private String styleBlock(String css, String selector) {
        int start = css.indexOf(selector);
        assertTrue(start >= 0, "CSS selector is missing: " + selector);
        int end = css.indexOf('}', start);
        assertTrue(end > start, "CSS block is not closed: " + selector);
        return css.substring(start, end + 1);
    }

    private String read(Path path) throws Exception {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}

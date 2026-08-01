package kr.esob.tdms.controller.general.system.menu;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class MenuPermissionContractTest {

	@Test
	void menuAdministrationUsesOnePortalTreeWithoutPortalSelectors()
			throws Exception {
		String jsp = read(
				"src/main/webapp/WEB-INF/views/general/system/menu/menuList.jsp");
		String script = read(
				"src/main/resources/static/js/views/general/system/menu/menuList-vuexy.js");

		assertTrue(jsp.contains("id=\"menuTree\""));
		assertTrue(jsp.contains("id=\"menuBtnArea\""));
		assertFalse(jsp.contains("outsideMenuTree"));
		assertFalse(jsp.contains("외부메뉴"));
		assertFalse(script.contains("authSite"));
		assertFalse(script.contains("addInsideMenu"));
		assertFalse(script.contains("addOutsideMenu"));
	}

	@Test
	void menuAdministrationUsesCompactTreeAndDetailWorkspace()
			throws Exception {
		String jsp = read(
				"src/main/webapp/WEB-INF/views/general/system/menu/menuList.jsp");
		String script = read(
				"src/main/resources/static/js/views/general/system/menu/menuList-vuexy.js");
		String css = read(
				"src/main/resources/static/css/pages/menu-permission.css");

		assertTrue(jsp.contains("resources/css/pages/menu-permission.css"));
		assertTrue(jsp.contains("class=\"menu-permission-header\""));
		assertTrue(jsp.contains("class=\"menu-permission-workspace\""));
		assertTrue(jsp.contains("id=\"menuTreeSearchForm\""));
		assertTrue(jsp.contains("id=\"menuTreeSearch\""));
		assertTrue(jsp.contains("id=\"menuSelectionEmpty\""));
		assertTrue(jsp.contains("id=\"menuSelectionDetail\""));
		assertTrue(jsp.contains("id=\"selectedMenuRole\""));
		assertFalse(jsp.contains("<style>"));

		assertTrue(script.contains("tree.search(keyword)"));
		assertTrue(script.contains("tree.clear_search()"));
		assertTrue(script.contains("renderMenuSelection(data.node, data.instance)"));
		assertTrue(script.contains("updateMenuTreeSummary()"));
		assertTrue(css.contains(".menu-permission-workspace"));
		assertTrue(css.contains(".menu-permission-detail-panel"));
		assertTrue(css.contains(".menu-permission-button--primary"));
	}

	@Test
	void menuPopupKeepsSaveContractAndUsesTheSharedModernSurface()
			throws Exception {
		String popup = read(
				"src/main/webapp/WEB-INF/views/general/system/menu/menuPopup.jsp");
		String script = read(
				"src/main/resources/static/js/views/general/system/menu/menuPopup.js");
		String css = read(
				"src/main/resources/static/css/pages/menu-permission.css");

		assertTrue(popup.contains("class=\"dialogContent commonRequestPopup menuPopup menu-permission-popup"));
		assertTrue(popup.contains("id=\"formPopup\""));
		assertTrue(popup.contains("id=\"menuCd\""));
		assertTrue(popup.contains("id=\"parentMenuCd\""));
		assertTrue(popup.contains("id=\"saveFlag\""));
		assertTrue(popup.contains("name=\"menuNm\" id=\"menuNm\""));
		assertTrue(popup.contains("name=\"menuUrl\" id=\"menuUrl\""));
		assertTrue(popup.contains("name=\"menuIcon\" id=\"menuIcon\""));
		assertTrue(popup.contains("name=\"useYn\" value=\"Y\""));
		assertTrue(popup.contains("name=\"popupYn\" value=\"Y\""));
		assertTrue(popup.contains("function=\"savePopup()\""));
		assertTrue(popup.contains("function=\"closePopup('popupDialog')\""));

		assertTrue(script.contains("/general/system/menu/saveMenu"));
		assertTrue(script.contains("$('#formPopup').serializeObject()"));
		assertTrue(script.contains("setTree()"));
		assertTrue(css.contains(".menuPopup.menu-permission-popup"));
		assertTrue(css.contains(".menu-permission-popup + .menu-permission-dialog-actions #save"));
		assertFalse(popup.toLowerCase().contains("outside"));
		assertFalse(popup.toLowerCase().contains("inside"));
	}

	@Test
	void menuTreeMutationEndpointsAndDragDropBehaviorRemainStable()
			throws Exception {
		String script = read(
				"src/main/resources/static/js/views/general/system/menu/menuList-vuexy.js");

		assertTrue(script.contains("/general/system/menu/getTreeList"));
		assertTrue(script.contains("/general/system/menu/saveMenuSort"));
		assertTrue(script.contains("/general/system/menu/saveMenu"));
		assertTrue(script.contains("/general/system/menu/menuAddPopup"));
		assertTrue(script.contains("/general/system/menu/menuModPopup"));
		assertTrue(script.contains("dragDrop: true"));
		assertTrue(script.contains("function saveMenu()"));
		assertTrue(script.contains("function addMenu()"));
		assertTrue(script.contains("function modMenu()"));
		assertTrue(script.contains("function delMenu()"));
		assertTrue(script.contains("function setRecursiveNode(node, param)"));
	}

	@Test
	void permissionTreeStartsAtTheSameActiveRootsAsNavigation()
			throws Exception {
		String mapper = read(
				"src/main/resources/sqlMaps/oracle/its/controller/menu/Menu.xml");

		assertTrue(mapper.contains("WITH RECURSIVE ACTIVE_MENU"));
		assertTrue(mapper.contains("MENU.PARENT_MENU_CD = 'ROOT'"));
		assertTrue(mapper.contains("MENU.TREE_TYPE = 'root'"));
		assertTrue(mapper.contains("MENU.USE_YN = 'Y'"));
		assertTrue(mapper.contains(
				"CHILD.PARENT_MENU_CD = PARENT.MENU_CD"));
		assertFalse(mapper.contains("AUTH_SITE"));
	}

	@Test
	void menuAdministrationCanReopenInactiveMenusWithoutExposingThemToRoleAssignment()
			throws Exception {
		String controller = read(
				"src/main/java/kr/esob/tdms/controller/general/system/menu/MenuController.java");
		String service = read(
				"src/main/java/kr/esob/tdms/controller/general/system/menu/MenuService.java");
		String dao = read(
				"src/main/java/kr/esob/tdms/controller/general/system/menu/MenuDao.java");
		String roleAssignController = read(
				"src/main/java/kr/esob/tdms/controller/general/system/roleassign/RoleAssignController.java");
		String mapper = read(
				"src/main/resources/sqlMaps/oracle/its/controller/menu/Menu.xml");

		assertTrue(controller.contains("service.selectAdminTree()"));
		assertTrue(service.contains("dao.selectAdminTree()"));
		assertTrue(dao.contains("selectTree(true)"));
		assertTrue(dao.contains("selectTree(false)"));
		assertTrue(dao.contains("param.put(\"includeInactive\", includeInactive)"));
		assertTrue(roleAssignController.contains("menuService.selectTree()"));
		assertTrue(mapper.contains("<if test=\"includeInactive != true\">"));
	}

	@Test
	void aclMigrationRepairsKnownLegacyOrphans() throws Exception {
		String ddl = read(
				"src/main/resources/sql/acl_foundation_ddl.sql");

		assertTrue(ddl.contains(
				"'MENU_019', 'MENU_074', 'MENU_075', 'MENU_076', 'MENU_077'"));
		assertTrue(ddl.contains(
				"Active internal menu tree contains disconnected nodes."));
	}

	@Test
	void roleAssignmentQueriesUseTheirOwnViewModel() throws Exception {
		String mapper = read(
				"src/main/resources/sqlMaps/oracle/its/controller/general/system/roleassign/RoleAssign.xml");

		assertTrue(mapper.contains(
				"<select id=\"selectRoleGroup\" resultType=\"kr.esob.tdms.controller.general.system.roleassign.RoleGroupVO\">"));
		assertTrue(mapper.contains(
				"<select id=\"selectRoleGroupInfo\" resultType=\"kr.esob.tdms.controller.general.system.roleassign.RoleGroupVO\">"));
		assertTrue(mapper.contains("selectAssignableRoleCodes"));
		assertTrue(mapper.contains("deleteMenuRoleAssignments"));
		assertFalse(mapper.contains("AUTH_SITE"));
	}

	private String read(String path) throws Exception {
		return Files.readString(Path.of(path), StandardCharsets.UTF_8);
	}
}

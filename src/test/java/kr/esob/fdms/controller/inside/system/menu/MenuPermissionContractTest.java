package kr.esob.fdms.controller.inside.system.menu;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class MenuPermissionContractTest {

	@Test
	void menuAdministrationUsesOneInternalTreeWithoutExternalTabs()
			throws Exception {
		String jsp = read(
				"src/main/webapp/WEB-INF/views/inside/system/menu/menuList.jsp");
		String script = read(
				"src/main/resources/static/js/views/inside/system/menu/menuList-vuexy.js");

		assertTrue(jsp.contains("id=\"menuTree\""));
		assertTrue(jsp.contains("id=\"menuBtnArea\""));
		assertFalse(jsp.contains("outsideMenuTree"));
		assertFalse(jsp.contains("외부메뉴"));
		assertFalse(script.contains("authSite: 'E'"));
		assertFalse(script.contains("addOutsideMenu"));
	}

	@Test
	void internalTreeStartsAtRealRootsAndCannotReturnOrphans()
			throws Exception {
		String mapper = read(
				"src/main/resources/sqlMaps/oracle/its/controller/menu/Menu.xml");

		assertTrue(mapper.contains("WITH RECURSIVE INTERNAL_MENU"));
		assertTrue(mapper.contains("MENU.PARENT_MENU_CD IN ('I', 'B')"));
		assertTrue(mapper.contains(
				"CHILD.PARENT_MENU_CD = PARENT.MENU_CD"));
		assertFalse(mapper.contains("WHEN #{auth} = 'E'"));
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
				"src/main/resources/sqlMaps/oracle/its/controller/inside/system/roleassign/RoleAssign.xml");

		assertTrue(mapper.contains(
				"<select id=\"selectRoleGroup\" resultType=\"kr.esob.fdms.controller.inside.system.roleassign.RoleGroupVO\">"));
		assertTrue(mapper.contains(
				"<select id=\"selectRoleGroupInfo\" resultType=\"kr.esob.fdms.controller.inside.system.roleassign.RoleGroupVO\">"));
	}

	private String read(String path) throws Exception {
		return Files.readString(Path.of(path), StandardCharsets.UTF_8);
	}
}

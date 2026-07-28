package kr.esob.fdms.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import kr.esob.fdms.commonlogic.menu.MenuVO;

class SecurityConfigMenuRuleOrderTest {

	@Test
	void specificMenuRulePrecedesBroadInsideFallback() {
		MenuVO broad = menu("MENU_124", "/inside/**", "ROLE_MENU_124");
		MenuVO assignment = menu(
				"MENU_160",
				"/inside/system/roleassign/**",
				"ROLE_MENU_160");
		MenuVO menuAdmin = menu(
				"MENU_138",
				"/inside/system/menu/**",
				"ROLE_MENU_138");

		List<MenuVO> ordered = SecurityConfig.orderMenuRules(
				Arrays.asList(broad, assignment, menuAdmin));

		assertEquals("MENU_160", ordered.get(0).getMenuCd());
		assertEquals("MENU_138", ordered.get(1).getMenuCd());
		assertEquals("MENU_124", ordered.get(2).getMenuCd());
	}

	@Test
	void invalidMenuRuleCannotCreateAnEmptyAuthorityMatcher() {
		MenuVO valid = menu(
				"MENU_138",
				"/inside/system/menu/**",
				"ROLE_MENU_138");
		MenuVO missingRole = menu(
				"MENU_184",
				"/inside/distribution/annotationinfo/annotationPopup",
				"");

		List<MenuVO> ordered = SecurityConfig.orderMenuRules(
				Arrays.asList(missingRole, valid));

		assertEquals(1, ordered.size());
		assertEquals("MENU_138", ordered.get(0).getMenuCd());
	}

	private MenuVO menu(String menuCd, String url, String roleCd) {
		MenuVO menu = new MenuVO();
		menu.setMenuCd(menuCd);
		menu.setMenuUrl(url);
		menu.setRoleCd(roleCd);
		return menu;
	}
}

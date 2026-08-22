package kr.esob.tdms.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import kr.esob.tdms.commonlogic.menu.MenuVO;

class SecurityConfigMenuRuleOrderTest {

	@Test
	void exactMenuRulePrecedesItsWildcardRoute() {
		MenuVO exact = menu(
				"MENU_161",
				"/general/system/roleassign/getMenuList",
				"ROLE_MENU_161");
		MenuVO wildcard = menu(
				"MENU_160",
				"/general/system/roleassign/**",
				"ROLE_MENU_160");
		MenuVO menuAdmin = menu(
				"MENU_138",
				"/general/system/menu/**",
				"ROLE_MENU_138");

		List<MenuVO> ordered = SecurityConfig.orderMenuRules(
				Arrays.asList(wildcard, menuAdmin, exact));

		assertEquals("MENU_161", ordered.get(0).getMenuCd());
		assertEquals("MENU_160", ordered.get(1).getMenuCd());
		assertEquals("MENU_138", ordered.get(2).getMenuCd());
	}

	@Test
	void invalidMenuRuleCannotCreateAnEmptyAuthorityMatcher() {
		MenuVO valid = menu(
				"MENU_138",
				"/general/system/menu/**",
				"ROLE_MENU_138");
		MenuVO missingRole = menu(
				"MENU_184",
				"/general/distribution/annotationinfo/annotationPopup",
				"");

		List<MenuVO> ordered = SecurityConfig.orderMenuRules(
				Arrays.asList(missingRole, valid));

		assertEquals(1, ordered.size());
		assertEquals("MENU_138", ordered.get(0).getMenuCd());
	}

	@Test
	void technicalRegistrationEndpointsUseTheRegistrationRoleBeforeDynamicWildcards()
			throws Exception {
		String source = new String(
				Files.readAllBytes(Paths.get(
						"src/main/java/kr/esob/tdms/config/SecurityConfig.java")),
				StandardCharsets.UTF_8);

		int registrationRule = source.indexOf(
				".antMatchers(TECHNICAL_REGISTRATION_ENDPOINTS)");
		int registrationAuthority = source.indexOf(
				".hasAuthority(\"ROLE_MENU_221\")", registrationRule);
		int dynamicRules = source.indexOf("for (MenuVO menuVo : menuList)");

		assertTrue(registrationRule >= 0);
		assertTrue(registrationAuthority > registrationRule);
		assertTrue(dynamicRules > registrationAuthority);

		assertEquals(Arrays.asList(
				"/general/distribution/swRequest/regist",
				"/general/distribution/swRequest/regist/",
				"/general/distribution/swRequest/swRegisterPopup",
				"/general/distribution/swRequest/swRegisterPopup/",
				"/general/distribution/swRequest/nextSwNo",
				"/general/distribution/swRequest/nextSwNo/",
				"/general/distribution/swRequest/uploadSwRegisFile",
				"/general/distribution/swRequest/uploadSwRegisFile/"),
				Arrays.asList(SecurityConfig.TECHNICAL_REGISTRATION_ENDPOINTS));
		for (String endpoint : SecurityConfig.TECHNICAL_REGISTRATION_ENDPOINTS) {
			MockHttpServletRequest request = new MockHttpServletRequest("POST", endpoint);
			request.setServletPath(endpoint);
			assertTrue(new AntPathRequestMatcher(endpoint).matches(request), endpoint);
		}
	}

	@Test
	void registrationRoleWinsOverTheBroadSwRequestRoleForCanonicalAndSlashRoutes() {
		List<RequestMatcher> registrationMatchers = Arrays.stream(
				SecurityConfig.TECHNICAL_REGISTRATION_ENDPOINTS)
				.map(AntPathRequestMatcher::new)
				.collect(Collectors.toList());
		LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> rules =
				new LinkedHashMap<>();
		rules.put(new OrRequestMatcher(registrationMatchers),
				org.springframework.security.access.SecurityConfig.createList("ROLE_MENU_221"));
		rules.put(new AntPathRequestMatcher("/general/distribution/swRequest/**"),
				org.springframework.security.access.SecurityConfig.createList("ROLE_MENU_220"));
		DefaultFilterInvocationSecurityMetadataSource metadata =
				new DefaultFilterInvocationSecurityMetadataSource(rules);
		AffirmativeBased decisions = new AffirmativeBased(Arrays.asList(
				new RoleVoter(), new AuthenticatedVoter()));
		UsernamePasswordAuthenticationToken registrar =
				new UsernamePasswordAuthenticationToken(
						"registrar", "n/a", Collections.singletonList(
								new SimpleGrantedAuthority("ROLE_MENU_221")));
		UsernamePasswordAuthenticationToken viewer =
				new UsernamePasswordAuthenticationToken(
						"viewer", "n/a", Collections.singletonList(
								new SimpleGrantedAuthority("ROLE_MENU_220")));

		for (String endpoint : SecurityConfig.TECHNICAL_REGISTRATION_ENDPOINTS) {
			MockHttpServletRequest request = new MockHttpServletRequest("POST", endpoint);
			request.setServletPath(endpoint);
			FilterInvocation invocation = new FilterInvocation(
					request, new MockHttpServletResponse(), (req, res) -> { });
			Collection<ConfigAttribute> attributes = metadata.getAttributes(invocation);

			assertEquals("ROLE_MENU_221", attributes.iterator().next().getAttribute());
			assertDoesNotThrow(() -> decisions.decide(registrar, invocation, attributes));
			assertThrows(AccessDeniedException.class,
					() -> decisions.decide(viewer, invocation, attributes));
		}
	}

	private MenuVO menu(String menuCd, String url, String roleCd) {
		MenuVO menu = new MenuVO();
		menu.setMenuCd(menuCd);
		menu.setMenuUrl(url);
		menu.setRoleCd(roleCd);
		return menu;
	}
}

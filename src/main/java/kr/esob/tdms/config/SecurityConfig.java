package kr.esob.tdms.config;

import kr.esob.tdms.commonlogic.audit.RequestAuditFilter;
import kr.esob.tdms.commonlogic.menu.MenuDao;
import kr.esob.tdms.commonlogic.menu.MenuVO;
import kr.esob.tdms.commonlogic.security.MobileClientAccessFilter;
import kr.esob.tdms.commonlogic.viewerintegration.ViewerIntegrationProperties;
import kr.esob.tdms.commonlogic.viewerintegration.ViewerCallbackAuthenticationFilter;
import kr.esob.tdms.controller.general.distribution.accountrequest.DistributionAccountIntegrationProperties;
import kr.esob.tdms.controller.general.organizationmanage.auditlog.AuditLogSessionListener;
import kr.esob.tdms.controller.login.*;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter{
	static final String[] TECHNICAL_REGISTRATION_ENDPOINTS = {
			"/general/distribution/swRequest/regist",
			"/general/distribution/swRequest/regist/",
			"/general/distribution/swRequest/swRegisterPopup",
			"/general/distribution/swRequest/swRegisterPopup/",
			"/general/distribution/swRequest/nextSwNo",
			"/general/distribution/swRequest/nextSwNo/",
			"/general/distribution/swRequest/uploadSwRegisFile",
			"/general/distribution/swRequest/uploadSwRegisFile/"
	};

	private final String ID_PARAMETER = "userId";
	private final String PW_PARAMETER = "userPw";
	@Inject
	MenuDao menuDao;

	@Inject
	CustomAuthenticationProvider authProvider;

	@Inject
	RequestAuditFilter requestAuditFilter;

	@Inject
	ViewerCallbackAuthenticationFilter viewerCallbackAuthenticationFilter;

	@Inject
	MobileClientAccessFilter mobileClientAccessFilter;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authProvider);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		/**
		 * Spring Security 룰에서 제외되는 URL 패턴
		 */
		web.ignoring().antMatchers("/resources/css/**", "/resources/js/**", "/resources/images/**", "/resources/excel/**");
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public AuthenticationSuccessHandler successhandler() {
		LoginSuccess handler = new LoginSuccess();
		return handler;
	}

	@Bean
	public LogoutSuccessHandler logoutSuccessHandler() {
		LogoutSuccess handler = new LogoutSuccess();
		return handler;
	}

	@Bean
	public AuthenticationFailureHandler failurehandler() {
		LoginFailure handler = new LoginFailure();
		handler.setLoginName(ID_PARAMETER);
		handler.setLoginPassword(PW_PARAMETER);
		handler.setFailureUrl("/login/loginPage");
		return handler;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		List<MenuVO> menuList = orderMenuRules(menuDao.getMenuList());
		http.headers().frameOptions().sameOrigin();

		// Authentication bootstrap endpoints are the only application URLs that
		// remain public. Static resources are excluded in configure(WebSecurity).
		http.authorizeRequests()
				.antMatchers(HttpMethod.POST,
						ViewerIntegrationProperties.CALLBACK_PATH)
				.hasAuthority(ViewerCallbackAuthenticationFilter.AUTHORITY)
				.antMatchers(ViewerIntegrationProperties.CALLBACK_PATH).denyAll()
				// Account-request ingress and status lookup are authenticated by
				// the integration HMAC service rather than a browser session.
				.antMatchers(HttpMethod.POST,
						DistributionAccountIntegrationProperties.REQUEST_PATH).permitAll()
				.antMatchers(HttpMethod.GET,
						DistributionAccountIntegrationProperties.REQUEST_PATH + "/*").permitAll()
				.antMatchers(DistributionAccountIntegrationProperties.REQUEST_PATH + "/**").denyAll()
				.antMatchers(HttpMethod.GET,
						"/login/loginPage",
						"/login/duplication",
						"/messages/*.properties").permitAll()
				.antMatchers(HttpMethod.POST, "/login/loginProcess").permitAll()
				.antMatchers("/error").permitAll()
				// Duanzong/PDM exchange is intentionally deferred. Re-enable only
				// after signed, replay-safe POST contracts are implemented.
				.antMatchers("/outside", "/outside/**").denyAll()
				// Native download client receives only a 128-bit, one-time capability.
				.antMatchers(HttpMethod.GET, "/download", "/download/", "/download/*").permitAll()
				// Keep sensitive management routes explicit as defense in depth. The
				// dynamic source is restricted to the active ROOT-connected menu tree.
				.antMatchers("/general/system/securityaccess/**").hasAuthority("ROLE_MENU_222")
				.antMatchers("/general/organizationmanage/insidedept/**")
				.hasAuthority("ROLE_MENU_199")
				// Partner contacts are maintained independently from TDMS login
				// accounts and are available only through their explicit menu role.
				.antMatchers("/general/organizationmanage/partner/**")
				.hasAuthority("ROLE_MENU_230")
				// Only the system administrator menu role can inspect and decide
				// distribution-system account requests.
				.antMatchers("/general/distribution/account-requests/**")
				.hasAuthority("ROLE_MENU_231")
				// Only the retired access-history root remains as a compatibility
				// redirect. No legacy child API is exposed to the combined role.
				.antMatchers(HttpMethod.GET,
						"/general/distribution/viewPrintHistory",
						"/general/distribution/viewPrintHistory/")
				.hasAuthority("ROLE_MENU_218")
				.antMatchers("/general/distribution/viewPrintHistory/**").denyAll()
				// The registration page has its own menu role. Its supporting
				// endpoints live beside, rather than below, /regist and would
				// otherwise inherit one of the duplicated swRequest wildcard roles.
				.antMatchers(TECHNICAL_REGISTRATION_ENDPOINTS)
				.hasAuthority("ROLE_MENU_221")
				// Distribution request pages are menu-authorized. Approval mutations
				// also retain the service-level RG_001 check as defense in depth.
				.antMatchers("/general/distribution/workflow/approval/**")
				.hasAuthority("ROLE_MENU_227")
				.antMatchers(HttpMethod.GET,
						"/general/distribution/workflow/api/approval-queue")
				.hasAuthority("ROLE_MENU_227")
				.antMatchers(HttpMethod.POST,
						"/general/distribution/workflow/api/requests/*/approve",
						"/general/distribution/workflow/api/requests/*/reject")
				.hasAuthority("ROLE_MENU_227")
				// Request ownership and approved-item file ACL are enforced by the
				// workflow service. Keep those APIs available to signed-in users.
				.antMatchers("/general/distribution/workflow/api/**").authenticated();

		// Spring Security uses the first matching pattern. Put longer, more
		// specific menu routes first so parent patterns cannot shadow children.
		for (MenuVO menuVo : menuList) {
			http.authorizeRequests()
					.antMatchers(menuVo.getMenuUrl())
					.hasAuthority(menuVo.getRoleCd());
		}
		http.authorizeRequests().anyRequest().authenticated();


//		http.sessionManagement()
//				.invalidSessionUrl("/sessionExpired") //세션이 만료된 경우 이동할 URL
//				.maximumSessions(1); // 동시에 사용 가능한 최대 세션 수 설정

//		http.sessionManagement()
//				.invalidSessionUrl("/login/sessionTimeout") //세션이 만료된 경우 리다이렉션 할 URL. 이건 팝업창이 아니라, 아예 페이지로 보여줌(미적으로 너무 bad함)
//				.maximumSessions(5) // 동시 로그인 세션 수
//				.maxSessionsPreventsLogin(false) // 다른곳에서 로그인해도 현재 사용자가 계속 사용. False로 할 시 이미 로그인한 사용자의 세션을 종료함.
//				.expiredUrl("/login/duplication") // 중복 로그인 시 리다이렉션 할 URL
//				.sessionRegistry(sessionRegistry());

		http
				.sessionManagement()
				.sessionFixation().migrateSession()
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);



		http
//				.exceptionHandling().accessDeniedPage("/login/loginPage");
				.exceptionHandling().accessDeniedHandler(accessDeniedHandler());
		http.formLogin()
				.loginPage("/login/loginPage")
				.loginProcessingUrl("/login/loginProcess")
				.successHandler(successhandler())
				.failureHandler(failurehandler())
				.usernameParameter(ID_PARAMETER)
				.passwordParameter(PW_PARAMETER);
		http.logout()
			.logoutRequestMatcher(new AntPathRequestMatcher("/login/logout", "POST"))
//			.logoutSuccessUrl("/login/loginPage")
			.logoutSuccessHandler(logoutSuccessHandler())
			.invalidateHttpSession(true)
			.deleteCookies("JSESSIONID");
		// Browser mutations use the session-backed token rendered by the shared
		// JSP fragment. Only HMAC-authenticated server-to-server integration paths
		// are excluded from CSRF because they have no browser session.
		http.csrf().ignoringAntMatchers(
				ViewerIntegrationProperties.CALLBACK_PATH,
				DistributionAccountIntegrationProperties.REQUEST_PATH,
				DistributionAccountIntegrationProperties.REQUEST_PATH + "/**");
		// Apply the device policy before authentication, CSRF and application
		// auditing. Keeping HeaderWriterFilter first preserves the normal Spring
		// Security response headers on a blocked response.
		http.addFilterAfter(
				mobileClientAccessFilter,
				org.springframework.security.web.header.HeaderWriterFilter.class);
		http.addFilterAfter(
				viewerCallbackAuthenticationFilter,
				org.springframework.security.web.session.SessionManagementFilter.class);
		http.addFilterBefore(requestAuditFilter, FilterSecurityInterceptor.class);

	}

	static List<MenuVO> orderMenuRules(List<MenuVO> menuList) {
		if (menuList == null) {
			return Collections.emptyList();
		}

		return menuList.stream()
				.filter(menu -> menu != null
						&& StringUtils.hasText(menu.getMenuUrl())
						&& StringUtils.hasText(menu.getRoleCd()))
				.sorted(Comparator
						.comparingInt((MenuVO menu) ->
								literalPathLength(menu.getMenuUrl()))
						.reversed()
						.thenComparingInt(menu ->
								wildcardCount(menu.getMenuUrl()))
						.thenComparing(MenuVO::getMenuCd,
								Comparator.nullsLast(String::compareTo)))
				.collect(Collectors.toList());
	}

	private static int literalPathLength(String pattern) {
		return pattern.replace("*", "").replace("?", "").length();
	}

	private static int wildcardCount(String pattern) {
		int count = 0;
		for (int i = 0; i < pattern.length(); i++) {
			char value = pattern.charAt(i);
			if (value == '*' || value == '?') {
				count++;
			}
		}
		return count;
	}

	/**
	 * The request audit filter belongs only to the Spring Security chain.
	 * Disabling servlet-container auto registration prevents duplicate events.
	 */
	@Bean
	public FilterRegistrationBean<RequestAuditFilter> requestAuditFilterRegistration(
			RequestAuditFilter filter) {
		FilterRegistrationBean<RequestAuditFilter> registration =
				new FilterRegistrationBean<RequestAuditFilter>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<ViewerCallbackAuthenticationFilter>
	viewerCallbackAuthenticationFilterRegistration(
			ViewerCallbackAuthenticationFilter filter) {
		FilterRegistrationBean<ViewerCallbackAuthenticationFilter> registration =
				new FilterRegistrationBean<ViewerCallbackAuthenticationFilter>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<MobileClientAccessFilter>
	mobileClientAccessFilterRegistration(MobileClientAccessFilter filter) {
		FilterRegistrationBean<MobileClientAccessFilter> registration =
				new FilterRegistrationBean<MobileClientAccessFilter>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }// Register HttpSessionEventPublisher

//	@Bean
//    public static ServletListenerRegistrationBean httpSessionEventPublisher() {
//        return new ServletListenerRegistrationBean(new HttpSessionEventPublisher());
//    }

	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return new CustomAccessDeniedHandler();
	}




    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
    	return new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher());
    }

    @Bean
    public ServletListenerRegistrationBean<AuditLogSessionListener> auditLogSessionListenerRegistration(AuditLogSessionListener listener) {
    	return new ServletListenerRegistrationBean<AuditLogSessionListener>(listener);
    }

//    @Bean
//    public SessionRegistry sessionRegistry() {
//        return new SessionRegistryImpl();
//    }

}




package kr.esob.fdms.config;

import kr.esob.fdms.commonlogic.audit.RequestAuditFilter;
import kr.esob.fdms.commonlogic.menu.MenuDao;
import kr.esob.fdms.commonlogic.menu.MenuVO;
import kr.esob.fdms.controller.inside.organizationmanage.auditlog.AuditLogSessionListener;
import kr.esob.fdms.controller.login.*;
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

	private final String ID_PARAMETER = "userId";
	private final String PW_PARAMETER = "userPw";
	@Inject
	MenuDao menuDao;

	@Inject
	CustomAuthenticationProvider authProvider;

	@Inject
	RequestAuditFilter requestAuditFilter;

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
				.antMatchers("/inside/system/securityaccess/**").hasAuthority("ROLE_MENU_222")
				// MENU_124 (/inside/**) precedes MENU_199 in the legacy
				// database order. Protect this management area explicitly so
				// the broad inside role cannot bypass department authorization.
				.antMatchers("/inside/organizationmanage/insidedept/**")
				.hasAuthority("ROLE_MENU_199");

		// Spring Security uses the first matching pattern. Specific menu routes
		// must therefore precede broad fallbacks such as /inside/**, otherwise a
		// group either bypasses every child ACL or can never use an assigned one.
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
		// All browser mutations use the session-backed token rendered by the
		// shared JSP fragment. External callbacks remain denied until their
		// signed interface contract is implemented.
		http.csrf();
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




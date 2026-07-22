package kr.esob.fdms.config;

import kr.esob.fdms.commonlogic.menu.MenuDao;
import kr.esob.fdms.commonlogic.menu.MenuVO;
import kr.esob.fdms.controller.inside.organizationmanage.auditlog.AuditLogSessionListener;
import kr.esob.fdms.controller.login.*;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationDetailsSource;
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
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
		List<MenuVO> menuList = menuDao.getMenuList();
		http.headers().frameOptions().sameOrigin();
		http.authorizeRequests().antMatchers("/**").permitAll();
		for(MenuVO menuVo : menuList) {
			http.authorizeRequests().antMatchers(menuVo.getMenuUrl()).hasAuthority(menuVo.getRoleCd());
		}


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
				.sessionFixation().none()
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);



		http.authorizeRequests()
				.antMatchers("/login/**").permitAll()
				.antMatchers("/user_slo.jsp").permitAll()
				.antMatchers("/redirect.jsp").permitAll()
//				.antMatchers("/inside/**").hasAuthority("ROLE_C897D8E4")
//				.antMatchers("/outside/**").hasAuthority("ROLE_936D3359")
				//.antMatchers("/main/**").hasAuthority("ROLE_MENU_121")
				.antMatchers("/inside/distribution/oldhistory/**").permitAll()
//				.antMatchers("/inside/authorization/**").permitAll()
				.antMatchers(HttpMethod.GET, "/resources/excel/**").permitAll()
				.antMatchers(HttpMethod.POST, "/resources/excel/**").permitAll()
				.antMatchers("/common/createExcel/createExcel").permitAll()
				.antMatchers(HttpMethod.GET,"/hd_slo/**").permitAll()
				//.antMatchers("/**").permitAll()
				.and()
//				.exceptionHandling().accessDeniedPage("/login/loginPage");
				.exceptionHandling().accessDeniedHandler(accessDeniedHandler());
		http.formLogin()
				.authenticationDetailsSource(authenticationDetailsSource())
				.loginPage("/login/loginPage")
				.loginProcessingUrl("/login/loginProcess")
				.successHandler(successhandler())
				.failureHandler(failurehandler())
				.usernameParameter(ID_PARAMETER)
				.passwordParameter(PW_PARAMETER);
		http.logout()
			.logoutUrl("/login/logout")
//			.logoutSuccessUrl("/login/loginPage")
			.logoutSuccessHandler(logoutSuccessHandler())
			.invalidateHttpSession(true)
			.deleteCookies("JSESSIONID");
//		http.csrf().ignoringAntMatchers("/login/**");
		http.csrf().disable();

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




    private AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> authenticationDetailsSource() {

        return new AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails>() {

            @Override
            public WebAuthenticationDetails buildDetails(HttpServletRequest request) {
                return new CustomWebAuthenticationDetails(request);
            }

        };
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

//	@Bean
//	public CustomAuthenticationFilter authenticationFilter() throws Exception {
//		CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter();
//		customAuthenticationFilter.setAuthenticationManager(this.authenticationManager());
//		customAuthenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login/**", "POST"));
//		return customAuthenticationFilter;
//	}


}




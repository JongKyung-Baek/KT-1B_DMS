package kr.esob.fdms.controller.login;


import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.commonlogic.loginhistory.HistoryService;
import kr.esob.fdms.commonlogic.menu.MenuDao;
import kr.esob.fdms.commonlogic.menu.MenuVO;
import kr.esob.fdms.commonlogic.message.LocaleUtil;
import kr.esob.fdms.commonlogic.message.SupportedLocaleChangeInterceptor;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfigDao;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfigVO;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.commonlogic.value.SessionValue;
import kr.esob.fdms.config.SessionExtendController;
import kr.esob.fdms.controller.inside.organizationmanage.auditlog.AuditLogService;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import kr.esob.fdms.controller.main.MainService;
import kr.esob.fdms.util.RequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static kr.esob.fdms.util.seed.PasswordUtils.verifyPassword;

@Component
public class LoginSuccess implements AuthenticationSuccessHandler {

	@Autowired
	MenuDao menuDao;

	@Inject
	ComboDao comboDao;

	@Inject
	SystemConfigDao systemConfigDao;

	@Inject
	LoginDao dao;

	@Inject
	HistoryService historyService;

	@Inject
	AuditLogService auditLogService;

	@Inject
	RequestUtil requestUtil;

	@Inject
	MainService mainService;

	@Inject
	Provider<SessionValue> provider;

	@Autowired
	DocPdfLinkRequestDao dao_for_pwd;


	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	public LoginSuccess() {

	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {


		String basicPassword = findBasicPassword(dao_for_pwd.selectDbConfig());


		String urlType = "I";
		String mainUrl = "/main";
		UserVO userVo = (UserVO) authentication.getPrincipal();
		HttpSession session = request.getSession();
		SessionValue sessionValue = provider.get();
		String sessionLang = resolveLoginLanguage(request, session);
		Locale sessionLocale = LocaleUtil.getLocale(sessionLang);
		sessionValue.setSessionLang(sessionLang);
		userVo.setSessionLang(sessionLang);
		session.setAttribute(
				SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,
				sessionLocale);

		auditLogService.setSessionAuditInfo(
				session, userVo.getUserCd(), userVo.getUserId(), userVo.getUserNm(), request);
		auditLogService.insertAuditLog(
				"logIn", userVo.getUserCd(), userVo.getUserId(), userVo.getUserNm(), request);

		String userPwd = userVo.getUserPwd();
		// 비밀번호가 '0000'라면 비밀번호 초기화 기능 수행
		boolean mustChangePassword = verifyPassword(userPwd, basicPassword);
		// The stored password hash is only needed during authentication. Do not
		// retain it in the SecurityContext-backed session principal.
		userVo.setUserPwd(null);
		if (mustChangePassword) {
			response.sendRedirect(response.encodeRedirectURL(
					request.getContextPath() + "/login/password"));
			return;
		}

//		userVo.setRoleGroup(roleGroup);
		List<MenuVO> menuTopList = menuDao.getMenuTopList(userVo);
		List<MenuVO> menuSubList = menuDao.getMenuSubList(userVo);
		sessionValue.setMenuTop(menuTopList);
		sessionValue.setMenuSub(menuSubList);
		sessionValue.setUrlType(urlType);
		ComboLang.replaceLanguage(
				sessionLang,
				comboDao.selectComboLang(sessionLang));
		SystemConfig.systemConfig = createSystemConfig();
		String timeoutSecond = resolveSessionTimeoutSecond();
		sessionValue.setTimeoutSecond(timeoutSecond);
		if(userVo.getOneOffMainUrl() != null) {
			mainUrl = userVo.getOneOffMainUrl();
		}
		dao.resetLoginCount(userVo.getUserId());
		dao.updateLastLoginDt(userVo.getUserId(), requestUtil.getClientIp(request));
		historyService.insertHistory(Constant.LOGIN_TYPE_LOGIN, request);

		// **세션 초기화 및 타임아웃 설정**
		int sessionTime = parseSessionTime(timeoutSecond);
		SessionExtendController.sessionTime = sessionTime;
		session.setMaxInactiveInterval(sessionTime);  // 세션 만료 시간 설정 (30분)
		session.setAttribute("sessionStartTime", System.currentTimeMillis());  // 세션 시작 시간 저장
		session.setAttribute("sessionTimeLeft", sessionTime);


		redirectStrategy.sendRedirect(request, response, mainUrl);
		
//		I일때만 실행
		LoginManager.loginUser.put(userVo.getUserId(), request.getRemoteAddr());
	}

	private int parseSessionTime(String timeoutSecond) {
		try {
			int sessionTime = Integer.parseInt(timeoutSecond);
			return sessionTime > 0 ? sessionTime : 600;
		} catch(Exception e) {
			return 600;
		}
	}

	private String resolveSessionTimeoutSecond() {
		try {
			Integer sessionTime = mainService.selectSessionTime();
			if(sessionTime != null && sessionTime > 0) {
				return String.valueOf(sessionTime);
			}
		} catch(Exception e) {
			// fall back to system config below
		}
		return SystemConfig.getSystemConfigValue("TIMEOUT_SECOND");
	}
	private Map<String, String> createSystemConfig(){
		List<SystemConfigVO> systemConfigVoList = systemConfigDao.selectSystemConfig();
		Map<String, String> systemMap = new HashMap<String, String>();
		for(SystemConfigVO vo : systemConfigVoList) {
			systemMap.put(Constant.SYSTEM_CONFIG + "|" + vo.getSystemConfigCd(), vo.getSystemConfigValue());
		}
		return systemMap;
	}

	private String findBasicPassword(List<Map<String, Object>> dbConfig) {
		if (dbConfig == null) {
			return null;
		}
		for (Map<String, Object> config : dbConfig) {
			if (config == null) {
				continue;
			}
			Object configCd = value(config, "SYSTEM_CONFIG_CD", "system_config_cd");
			if (!"BASIC_PASSWORD".equals(configCd)) {
				continue;
			}
			Object configValue = value(config, "SYSTEM_CONFIG_VALUE", "system_config_value");
			return configValue == null ? null : configValue.toString();
		}
		return null;
	}

	private Object value(Map<String, Object> config, String... keys) {
		for (String key : keys) {
			Object value = config.get(key);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	private String resolveBrowserLanguage(Locale locale) {
		return LocaleUtil.resolveSupportedLanguage(locale);
	}

	String resolveLoginLanguage(
			HttpServletRequest request, HttpSession session) {
		String requestedLanguage = LocaleUtil.normalizeSupportedLanguage(
				request.getParameter(
						SupportedLocaleChangeInterceptor.PARAMETER_NAME));
		if (requestedLanguage != null) {
			return requestedLanguage;
		}

		Object selectedLocale = session.getAttribute(
				SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
		if (selectedLocale instanceof Locale) {
			return resolveBrowserLanguage((Locale) selectedLocale);
		}

		return resolveBrowserLanguage(RequestContextUtils.getLocale(request));
	}

}

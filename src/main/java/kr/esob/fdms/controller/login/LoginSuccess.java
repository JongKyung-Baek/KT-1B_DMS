package kr.esob.fdms.controller.login;


import kr.esob.fdms.commonlogic.combo.ComboCdVO;
import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.commonlogic.loginhistory.HistoryService;
import kr.esob.fdms.commonlogic.menu.MenuDao;
import kr.esob.fdms.commonlogic.menu.MenuVO;
import kr.esob.fdms.commonlogic.message.LocaleUtil;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfigDao;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfigVO;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.commonlogic.value.SessionValue;
import kr.esob.fdms.config.Property;
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


		List<Map<String,Object>> dbConfig = dao_for_pwd.selectDbConfig();
		String basicPassword="";

		for(Map<String,Object> config : dbConfig) {
			if(config.get("system_config_cd").equals("BASIC_PASSWORD")) {
				basicPassword = config.get("system_config_value").toString();
			}
		}


		String urlType = request.getParameter("url_type");
		String mainUrl = "/main";
		UserVO userVo = (UserVO) authentication.getPrincipal();
		HttpSession session = request.getSession();

		auditLogService.setSessionAuditInfo(session, userVo.getUserId(), userVo.getUserNm(), request);
		auditLogService.insertAuditLog("logIn", userVo.getUserId(), userVo.getUserNm(), request);

		String userPwd = userVo.getUserPwd();
		// 비밀번호가 '0000'라면 비밀번호 초기화 기능 수행
		if (verifyPassword(userPwd, basicPassword)) {
			// 비밀번호 초기화 페이지로 리다이렉트
			response.sendRedirect("/login/password");
			return; // 리다이렉트 후 메소드 종료
		}

		userVo.setUrl_type(urlType);
//		String roleGroup = mainService.selectRoleGroupByUserCd(userVo);
//		userVo.setRoleGroup(roleGroup);
		SessionValue sessionValue = provider.get();
		String sessionLang = resolveBrowserLanguage(request.getLocale());
		sessionValue.setSessionLang(sessionLang);
		userVo.setSessionLang(sessionLang);
		List<MenuVO> menuTopList = menuDao.getMenuTopList(userVo);
		List<MenuVO> menuSubList = menuDao.getMenuSubList(userVo);
		sessionValue.setMenuTop(menuTopList);
		sessionValue.setMenuSub(menuSubList);
		sessionValue.setUrlType(urlType);
		ComboLang.comboLang = createComboMap();
		SystemConfig.systemConfig = createSystemConfig();
		String timeoutSecond = resolveSessionTimeoutSecond();
		sessionValue.setTimeoutSecond(timeoutSecond);
		Locale locales = LocaleUtil.getLocale(sessionLang);
		if(userVo.getOneOffMainUrl() != null) {
			mainUrl = userVo.getOneOffMainUrl();
		}
		request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locales);
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
		Property property = new Property();
		if(property.getProperty("location").equals("I")) {
			LoginManager.loginUser.put(userVo.getUserId(), request.getRemoteAddr());
		}
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
	private Map<String, String> createComboMap() {
		List<ComboCdVO> comboCdVoList = comboDao.selectComboLang();
		Map<String, String> langMap = new HashMap<String, String>();
		for(ComboCdVO comboCdVo : comboCdVoList) {
			langMap.put(comboCdVo.getComboCd() + "|" + comboCdVo.getValue(), comboCdVo.getLangDesc());
		}
		return langMap;
	}

	private Map<String, String> createSystemConfig(){
		List<SystemConfigVO> systemConfigVoList = systemConfigDao.selectSystemConfig();
		Map<String, String> systemMap = new HashMap<String, String>();
		for(SystemConfigVO vo : systemConfigVoList) {
			systemMap.put(Constant.SYSTEM_CONFIG + "|" + vo.getSystemConfigCd(), vo.getSystemConfigValue());
		}
		return systemMap;
	}

	private String resolveBrowserLanguage(Locale locale) {
		String language = locale == null ? "" : locale.getLanguage();
		if ("en".equalsIgnoreCase(language)) return "en";
		if ("ja".equalsIgnoreCase(language)) return "ja";
		if ("zh".equalsIgnoreCase(language)) return "zh";
		return "ko";
	}

}

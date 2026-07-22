package kr.esob.fdms.controller.login;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;

public class LogoutSuccess implements LogoutSuccessHandler {

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		
		try {	
			UserVO user = (UserVO) authentication.getPrincipal();	
			LoginManager.loginUser.remove(user.getUserId());
		}
		catch(Exception e) {}

		
		if(ComboLang.comboLang != null)ComboLang.comboLang.clear();
		if(SystemConfig.systemConfig != null)SystemConfig.systemConfig.clear();
		response.setStatus(HttpServletResponse.SC_OK);

		response.sendRedirect("/");
	}

}

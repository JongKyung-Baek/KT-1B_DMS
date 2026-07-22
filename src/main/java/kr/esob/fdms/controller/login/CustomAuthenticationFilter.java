package kr.esob.fdms.controller.login;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import kr.esob.fdms.commonlogic.value.Constant;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	@Override
	protected String obtainUsername(HttpServletRequest request) {
		String username = request.getParameter("userId");
		String url_type = request.getParameter("url_type");
		String slo_p_ota = request.getParameter("slo_p_ota");
		String combinedParameter = username + Constant.loginParamDelimeter + url_type + Constant.loginParamDelimeter + slo_p_ota;

		return combinedParameter;
	}

	@Override
	protected String obtainPassword(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return request.getParameter("userPw");
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		String url_type = request.getParameter("url_type");
		String slo_p_ota = request.getParameter("slo_p_ota");
		request.getSession().setAttribute("url_type", url_type);
		request.getSession().setAttribute("slo_p_ota", slo_p_ota);

		return super.attemptAuthentication(request, response);
	}


}

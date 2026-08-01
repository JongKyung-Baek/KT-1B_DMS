package kr.esob.tdms.commonlogic.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import kr.esob.tdms.controller.login.LoginManager;
import kr.esob.tdms.controller.login.UserVO;

public class CommonCheckInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String duplicationPath = request.getContextPath() + "/login/duplication";
		if (duplicationPath.equals(request.getRequestURI())) {
			return true;
		}

		// 중복 로그인 체크
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if(null == auth) {
			return super.preHandle(request, response, handler);
		}

		if(null == auth.getPrincipal() || "anonymousUser".equals(auth.getPrincipal())) {
			return super.preHandle(request, response, handler);
		}

		UserVO userVo = (UserVO) auth.getPrincipal();

		if(LoginManager.checkUsing(userVo.getUserId(), request.getRemoteAddr())) {
			if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
				response.sendError(HttpServletResponse.SC_CONFLICT, "Duplicate login");
			} else {
				response.sendRedirect(duplicationPath);
			}
			return false;
		}

		return super.preHandle(request, response, handler);
//		return super.preHandle(request, response, handler);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
	}

}

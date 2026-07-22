package kr.esob.fdms.commonlogic.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import kr.esob.fdms.controller.login.LoginManager;
import kr.esob.fdms.controller.login.UserVO;

public class CommonCheckInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
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
			//throw new LockedException("msg.alreadyLogin");
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

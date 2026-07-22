package kr.esob.fdms.config;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import kr.esob.fdms.commonlogic.menu.MenuService;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Inject
	MenuService menuService;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		System.out.println("accessDenied");
		System.out.println(request.getHeader("x-requested-with"));
		if((request.getHeader("x-requested-with") != null) && request.getHeader("x-requested-with").equals("XMLHttpRequest")) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}else {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//			request.getRequestDispatcher("/error").forward(request, response);
		}
	}

}

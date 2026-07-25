package kr.esob.fdms.controller.error;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

	@RequestMapping("/error")
	public String handleError(HttpServletRequest request, HttpServletResponse response) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

		if (status == null) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "error/500";
		}

		int statusCode = Integer.parseInt(status.toString());
		response.setStatus(statusCode);

		if (statusCode == HttpStatus.BAD_REQUEST.value()) {
			return "error/400";
		}
		if (statusCode == HttpStatus.FORBIDDEN.value()) {
			return "error/403";
		}
		if (statusCode == HttpStatus.NOT_FOUND.value()) {
			return "error/404";
		}
		if (statusCode == HttpStatus.METHOD_NOT_ALLOWED.value()) {
			return "error/405";
		}
		return "error/500";
	}

}

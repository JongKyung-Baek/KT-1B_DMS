package kr.esob.fdms.controller.login;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/hd_slo")
public class HdSlo {

	@RequestMapping(value="/sso", method = RequestMethod.GET)
	public String home(HttpServletRequest request) {
		System.out.println(request.getParameter("url_type"));
		System.out.println(request.getParameter("slo_p_ota"));
		return "";
	}

}

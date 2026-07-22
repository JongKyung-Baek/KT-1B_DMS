package kr.esob.fdms.controller.login;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {
	/**
	 *
	 */
	private static final long serialVersionUID = 135677692283316090L;
	private String url_type;
	private String slo_p_ota;
	public CustomWebAuthenticationDetails(HttpServletRequest request) {
		super(request);
		this.url_type = request.getParameter("url_type");
		this.slo_p_ota = request.getParameter("slo_p_ota");
	}

}

package kr.esob.fdms.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

@Service
public class RequestUtil {

	public Map<String, String> getRequestParameterToMap(HttpServletRequest request){

		Map<String, String> paramMap = new HashMap<String, String>();

		Enumeration<String> enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String strNextElement = enumeration.nextElement().toString();
			paramMap.put(strNextElement, request.getParameter(strNextElement));
		}

		return paramMap;
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	public String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null) {
			ip = request.getRemoteAddr();
		}

		return ip;
	}
}

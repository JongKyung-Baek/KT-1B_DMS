package kr.esob.fdms.commonlogic.message;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

public class LocaleUtil  {

	/**
	 * 기본 로케일을 리턴한다. 기본은 한글이다.
	 */
	public static Locale getDefaultLocale() {
		return Locale.KOREAN;
	}

	/**
	 * HttpServletRequest 를 받아서 저장되어 있를 locale 값을 리턴한다. 없는 경우는 기본 로케일을 리턴한다.
	 * @methodname : getCurrentLocale
	 * @author     : younjh
	 * @date       : 2018. 7. 11. 오후 2:13:53
	 * @param      :
	 * @return     :
	 * @desc       :
	 */
	public static Locale getCurrentLocale(HttpServletRequest request) {
		Locale locale = null;
		HttpSession session = request.getSession();
		locale = (Locale)session.getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);

		if (locale == null ) {
			locale = getDefaultLocale();
		}
		return locale;
	}

	/**
	 * 현재 언어를 구한다.
	 * @methodname : getCurrentLanguage
	 * @author     : younjh
	 * @date       : 2018. 7. 12. 오후 4:51:06
	 * @param      :
	 * @return     :
	 * @desc       :
	 */
	public static String getCurrentLanguage(HttpServletRequest request) {
		return getCurrentLocale(request).getLanguage();
	}

	/**
	 * 특정 언어이름을 현재 설정된 언어로 ...
	 * @methodname : getDisplayLanguage
	 * @author     : younjh
	 * @date       : 2018. 7. 11. 오후 2:13:57
	 * @param      :
	 * @return     :
	 * @desc       :
	 */
	public static String getDisplayLanguage(HttpServletRequest request, String language) {
		Locale locale = getLocale(language);

		HttpSession session = request.getSession();

		Locale currentLocale = (Locale)session.getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);

		if (currentLocale == null ) {
			currentLocale = getDefaultLocale();
		}

		return locale.getDisplayLanguage(currentLocale);
	}

	public static String getDisplayLanguage(String language) {
		Locale locale = getLocale(language);

		return locale.getDisplayLanguage(locale);
	}

	/**
	 * 언어코드 -> locale을 얻는다.
	 * @methodname : getLocale
	 * @author     : younjh
	 * @date       : 2018. 7. 11. 오후 2:13:30
	 * @param      :
	 * @return     :
	 * @desc       :
	 */
	public static Locale getLocale(String language) {
		Locale locale = null;

		if (language.matches(Locale.KOREA.getLanguage())) {
			locale = Locale.KOREA;
		} else if(language.matches(Locale.JAPAN.getLanguage())){
			locale = Locale.JAPAN;
		} else if(language.matches(Locale.CHINA.getLanguage())){
			locale = Locale.CHINA;
		} else {
			locale = Locale.US;
		}

		return locale;
	}

}

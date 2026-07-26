package kr.esob.fdms.commonlogic.message;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.support.RequestContextUtils;

public class LocaleUtil  {
	private static final Set<String> SUPPORTED_LANGUAGES =
			// Add "id" here when the Indonesian feature bundle and DB
			// translations are released. Until then an id-ID browser safely
			// falls back to the Korean default instead of showing mixed text.
			new HashSet<String>(Arrays.asList("ko", "en", "ja", "zh"));

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
		Locale locale = RequestContextUtils.getLocale(request);
		return getLocale(resolveSupportedLanguage(locale));
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
	 * jqGrid uses the legacy "kr" filename for Korean while all other bundled
	 * locales use their standard language code.
	 */
	public static String getJqGridLanguage(HttpServletRequest request) {
		String language = getCurrentLanguage(request);
		return "ko".equals(language) ? "kr" : language;
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
		return locale.getDisplayLanguage(getCurrentLocale(request));
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
		String normalized = normalizeSupportedLanguage(language);
		if (normalized == null) {
			return getDefaultLocale();
		}
		if ("ko".equals(normalized)) {
			return Locale.KOREA;
		}
		if ("en".equals(normalized)) {
			return Locale.US;
		}
		if ("ja".equals(normalized)) {
			return Locale.JAPAN;
		}
		if ("zh".equals(normalized)) {
			return Locale.CHINA;
		}
		return Locale.forLanguageTag(normalized);
	}

	public static String normalizeSupportedLanguage(String language) {
		if (language == null) {
			return null;
		}
		String normalizedTag = language.trim().replace('_', '-');
		if (!normalizedTag.matches("(?i)^[a-z]{2,3}(-[a-z]{2})?$")) {
			return null;
		}
		String normalized = Locale.forLanguageTag(normalizedTag)
				.getLanguage().toLowerCase(Locale.ROOT);
		return SUPPORTED_LANGUAGES.contains(normalized) ? normalized : null;
	}

	public static String resolveSupportedLanguage(Locale locale) {
		String normalized = normalizeSupportedLanguage(
				locale == null ? null : locale.toLanguageTag());
		return normalized == null ? getDefaultLocale().getLanguage() : normalized;
	}

}

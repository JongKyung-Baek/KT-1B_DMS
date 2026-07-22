package kr.esob.fdms.commonlogic.message;

import java.text.MessageFormat;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * <ul>
 * <li>Created by : younjh
 * <li>Created Date : 2016. 12. 08. 오후 02:33:42
 * </ul>

 * @author younjh
 *
 */
@Component
public class Prop implements MessageSourceAware {
	private MessageSource msg;

	public void setMessageSource (MessageSource msg) {
		this.msg = msg;
	}

	/**
	 * @param key
	 * @param locale
	 * @return
	 */
	public String messages(String key, Locale locale) {
		return this.msg(key, locale);
	}

	public String messagesFormat(String key, Object...objects) {
		return MessageFormat.format(this.msg(key),objects);
	}

	public String messagesFormat(String key, Locale locale, Object...objects) {
		return MessageFormat.format(this.msg(key, locale),objects);
	}

	public String messagesFormat(String key, String language, Object...objects) {
		Locale locale = new Locale(language);

		return MessageFormat.format(this.msg(key, locale),objects);
	}

	/**
	 * @param key
	 * @return
	 */
	public String msg(String key){
		return msg.getMessage(key, null, LocaleContextHolder.getLocale());
	}

	/**
	 * @param key
	 * @param locale
	 * @return
	 */
	public String msg(String key, Locale locale){
		return msg.getMessage(key, null, locale);
	}

	/**
	 *
	 * @methodname : msg
	 * @author     : younjh
	 * @date       : 2018. 11. 21. 오후 1:43:37
	 * @param      :
	 * @return     :
	 * @desc       :
	 */
	public String msg(String key, String language){
		Locale lo = new Locale(language);

		return msg.getMessage(key, null, lo);
	}
}

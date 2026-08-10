package kr.esob.tdms.commonlogic.branding;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import kr.esob.tdms.commonlogic.message.LocaleUtil;

/** Selects a validated brand from the effective request port. */
@Component
public class TdmsBrandResolver {
    public static final String REQUEST_ATTRIBUTE = "tdmsBrand";
    private static final String PRIMARY_SYSTEM_NAME = "KT-1B DMS";
    private static final String PRIMARY_LOGO_PATH =
            "/resources/images/brand/kai-logo.png";

    private final TdmsBrandProperties properties;
    private final MessageSource messageSource;

    public TdmsBrandResolver(TdmsBrandProperties properties,
                             MessageSource messageSource) {
        this.properties = properties;
        this.messageSource = messageSource;
    }

    public TdmsBrandView resolve(HttpServletRequest request) {
        return resolve(request, resolveLocale(request));
    }

    public TdmsBrandView resolve(HttpServletRequest request, Locale locale) {
        String language = LocaleUtil.resolveSupportedLanguage(locale);
        Locale supportedLocale = "id".equals(language)
                ? Locale.forLanguageTag("id-ID")
                : Locale.forLanguageTag(language);
        if (properties.isEnabled()
                && request.getServerPort() == properties.getPort()) {
            String companyName = "ko".equals(supportedLocale.getLanguage())
                    ? properties.getCompanyNameKo()
                    : properties.getCompanyNameEn();
            return new TdmsBrandView(true,
                    properties.getSystemName(),
                    companyName,
                    properties.getSystemName(),
                    properties.getLogoLightPath(),
                    properties.getLogoDarkPath(),
                    properties.getLogoAlt());
        }

        return new TdmsBrandView(false,
                PRIMARY_SYSTEM_NAME,
                message("feature.error.brand", supportedLocale,
                        "KT-1B Technical Data Management"),
                message("feature.error.systemName", supportedLocale,
                        "Technical Data Management System"),
                PRIMARY_LOGO_PATH,
                PRIMARY_LOGO_PATH,
                "KAI");
    }

    private Locale resolveLocale(HttpServletRequest request) {
        String requested = LocaleUtil.normalizeSupportedLanguage(
                languageFromQuery(request.getQueryString()));
        if (requested != null) {
            return LocaleUtil.getLocale(requested);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object selected = session.getAttribute(
                    SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
            if (selected instanceof Locale) {
                return (Locale) selected;
            }
        }
        return request.getLocale() == null
                ? LocaleUtil.getDefaultLocale() : request.getLocale();
    }

    private String languageFromQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        for (String parameter : query.split("&")) {
            int separator = parameter.indexOf('=');
            if (separator > 0
                    && "lang".equals(parameter.substring(0, separator))) {
                return parameter.substring(separator + 1);
            }
        }
        return null;
    }

    private String message(String code, Locale locale, String fallback) {
        return messageSource.getMessage(code, null, fallback, locale);
    }
}

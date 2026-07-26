package kr.esob.fdms.commonlogic.combo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;

import kr.esob.fdms.commonlogic.message.LocaleUtil;

public final class ComboLang {

    private static final ConcurrentMap<String, Map<String, String>>
            COMBO_LANG_BY_LANGUAGE =
                    new ConcurrentHashMap<String, Map<String, String>>();

    private ComboLang() {
    }

    public static void replaceLanguage(
            String language,
            List<ComboCdVO> comboList) {
        Map<String, String> languageMap = new HashMap<String, String>();
        if (comboList != null) {
            for (ComboCdVO combo : comboList) {
                if (combo == null || combo.getComboCd() == null) {
                    continue;
                }
                languageMap.put(
                        combo.getComboCd() + "|" + combo.getValue(),
                        combo.getLangDesc());
            }
        }
        replaceLanguage(language, languageMap);
    }

    public static void replaceLanguage(
            String language,
            Map<String, String> languageMap) {
        Map<String, String> safeCopy = languageMap == null
                ? Collections.<String, String>emptyMap()
                : Collections.unmodifiableMap(
                        new HashMap<String, String>(languageMap));
        COMBO_LANG_BY_LANGUAGE.put(normalizeLanguage(language), safeCopy);
    }

    public static String getComboLang(String comboCd, String value) {
        return getComboLang(currentLanguage(), comboCd, value);
    }

    public static String getComboLang(
            String language,
            String comboCd,
            String value) {
        Map<String, String> languageMap =
                COMBO_LANG_BY_LANGUAGE.get(normalizeLanguage(language));
        if (languageMap == null) {
            return value;
        }
        String localized = languageMap.get(comboCd + "|" + value);
        return localized == null ? value : localized;
    }

    public static boolean containsLanguage(String language) {
        return COMBO_LANG_BY_LANGUAGE.containsKey(
                normalizeLanguage(language));
    }

    public static void clearLanguage(String language) {
        COMBO_LANG_BY_LANGUAGE.remove(normalizeLanguage(language));
    }

    public static void clear() {
        COMBO_LANG_BY_LANGUAGE.clear();
    }

    private static String currentLanguage() {
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();
        Locale locale = localeContext == null ? null : localeContext.getLocale();
        return locale == null
                ? LocaleUtil.getDefaultLocale().getLanguage()
                : LocaleUtil.resolveSupportedLanguage(locale);
    }

    private static String normalizeLanguage(String language) {
        String normalized = LocaleUtil.normalizeSupportedLanguage(language);
        return normalized == null
                ? LocaleUtil.getDefaultLocale().getLanguage()
                : normalized;
    }
}

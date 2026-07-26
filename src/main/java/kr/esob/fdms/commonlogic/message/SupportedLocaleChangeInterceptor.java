package kr.esob.fdms.commonlogic.message;

import java.util.List;

import javax.inject.Provider;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.commonlogic.menu.MenuDao;
import kr.esob.fdms.commonlogic.menu.MenuVO;
import kr.esob.fdms.commonlogic.value.SessionValue;
import kr.esob.fdms.controller.login.UserVO;

/**
 * Keeps Spring messages, DB-backed labels and the authenticated navigation in
 * the same locale. Adding a future language only requires its bundle/DB rows;
 * the interceptor already accepts the normalized language tag.
 */
@Component
public class SupportedLocaleChangeInterceptor extends LocaleChangeInterceptor {

    public static final String PARAMETER_NAME = "lang";

    private final Provider<SessionValue> sessionProvider;
    private final MenuDao menuDao;
    private final ComboDao comboDao;

    public SupportedLocaleChangeInterceptor(
            Provider<SessionValue> sessionProvider,
            MenuDao menuDao,
            ComboDao comboDao) {
        this.sessionProvider = sessionProvider;
        this.menuDao = menuDao;
        this.comboDao = comboDao;
        setParamName(PARAMETER_NAME);
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws ServletException {
        String requestedLanguage = request.getParameter(PARAMETER_NAME);
        if (requestedLanguage == null || requestedLanguage.trim().isEmpty()) {
            return true;
        }

        String language = LocaleUtil.normalizeSupportedLanguage(requestedLanguage);
        if (language == null) {
            return true;
        }

        super.preHandle(request, response, handler);
        LocaleContextHolder.setLocale(LocaleUtil.getLocale(language));
        refreshAuthenticatedLocale(language);
        return true;
    }

    private void refreshAuthenticatedLocale(String language) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof UserVO)) {
            return;
        }

        UserVO user = (UserVO) authentication.getPrincipal();
        SessionValue session = sessionProvider.get();
        session.setSessionLang(language);
        user.setSessionLang(language);
        ComboLang.replaceLanguage(
                language,
                comboDao.selectComboLang(language));

        List<MenuVO> menuTop = menuDao.getMenuTopList(user);
        List<MenuVO> menuSub = menuDao.getMenuSubList(user);
        session.setMenuTop(menuTop);
        session.setMenuSub(menuSub);
    }
}

package kr.esob.tdms.commonlogic.combo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Provider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import kr.esob.tdms.commonlogic.menu.MenuDao;
import kr.esob.tdms.commonlogic.message.SupportedLocaleChangeInterceptor;
import kr.esob.tdms.commonlogic.value.SessionValue;
import kr.esob.tdms.controller.login.LogoutSuccess;
import kr.esob.tdms.controller.login.UserVO;

class ComboLanguageLifecycleTest {

    @BeforeEach
    void setUp() {
        ComboLang.clear();
        SecurityContextHolder.clearContext();
        LocaleContextHolder.resetLocaleContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        LocaleContextHolder.resetLocaleContext();
        ComboLang.clear();
    }

    @Test
    @SuppressWarnings("unchecked")
    void localeChangeRefreshesTheRequestedLanguageComboMap()
            throws Exception {
        SessionValue session = new SessionValue();
        Provider<SessionValue> provider = mock(Provider.class);
        MenuDao menuDao = mock(MenuDao.class);
        ComboDao comboDao = mock(ComboDao.class);
        when(provider.get()).thenReturn(session);
        when(menuDao.getMenuTopList(
                org.mockito.ArgumentMatchers.any(UserVO.class)))
                .thenReturn(Collections.emptyList());
        when(menuDao.getMenuSubList(
                org.mockito.ArgumentMatchers.any(UserVO.class)))
                .thenReturn(Collections.emptyList());
        when(comboDao.selectComboLang("en"))
                .thenReturn(Collections.singletonList(
                        combo("requestStatus", "APPROVED", "Approved")));

        UserVO user = new UserVO();
        user.setUserCd("U1");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user, null, Collections.emptyList()));

        SupportedLocaleChangeInterceptor interceptor =
                new SupportedLocaleChangeInterceptor(
                        provider, menuDao, comboDao);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(
                SupportedLocaleChangeInterceptor.PARAMETER_NAME,
                "en-US");
        request.setAttribute(
                DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE,
                new SessionLocaleResolver());
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));

        assertEquals("en", session.getSessionLang());
        assertEquals("en", user.getSessionLang());
        assertEquals(
                "Approved",
                ComboLang.getComboLang(
                        "en", "requestStatus", "APPROVED"));
        assertEquals(
                "Approved",
                ComboLang.getComboLang(
                        "requestStatus", "APPROVED"));
        verify(comboDao).selectComboLang("en");
        verify(menuDao).getMenuTopList(user);
        verify(menuDao).getMenuSubList(user);
        Locale locale = (Locale) request.getSession().getAttribute(
                SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
        assertEquals("en", locale.getLanguage());
    }

    @Test
    void oneUsersLogoutDoesNotClearSharedLanguageCaches()
            throws Exception {
        ComboLang.replaceLanguage("ko", languageMap("승인"));
        ComboLang.replaceLanguage("en", languageMap("Approved"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        new LogoutSuccess().onLogoutSuccess(
                new MockHttpServletRequest(),
                response,
                null);

        assertEquals("/", response.getRedirectedUrl());
        assertEquals(
                "승인",
                ComboLang.getComboLang(
                        "ko", "requestStatus", "APPROVED"));
        assertEquals(
                "Approved",
                ComboLang.getComboLang(
                        "en", "requestStatus", "APPROVED"));
    }

    private ComboCdVO combo(
            String comboCd,
            String value,
            String description) {
        ComboCdVO combo = new ComboCdVO();
        combo.setComboCd(comboCd);
        combo.setValue(value);
        combo.setLangDesc(description);
        return combo;
    }

    private Map<String, String> languageMap(String description) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("requestStatus|APPROVED", description);
        return map;
    }
}

package kr.esob.tdms.controller.login;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

class LoginSuccessLocaleTest {

    private final LoginSuccess loginSuccess = new LoginSuccess();

    @Test
    void explicitLoginLanguageWins() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("lang", "en");
        request.getSession().setAttribute(
                SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,
                Locale.KOREAN);

        assertEquals("en", loginSuccess.resolveLoginLanguage(
                request, request.getSession()));
    }

    @Test
    void selectedSessionLanguageSurvivesSecurityLoginProcessing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute(
                SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,
                Locale.ENGLISH);

        assertEquals("en", loginSuccess.resolveLoginLanguage(
                request, request.getSession()));
    }

    @Test
    void browserLanguageIsUsedWhenNoSelectionExists() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(Locale.ENGLISH);

        assertEquals("en", loginSuccess.resolveLoginLanguage(
                request, request.getSession()));
    }
}

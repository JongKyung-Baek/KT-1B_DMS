package kr.esob.tdms.commonlogic.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Provider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import kr.esob.tdms.commonlogic.combo.ComboDao;
import kr.esob.tdms.commonlogic.menu.MenuDao;
import kr.esob.tdms.commonlogic.value.SessionValue;
import kr.esob.tdms.config.WebConfig;
import kr.esob.tdms.controller.login.UserVO;

class IndonesianLocaleContractTest {

    @AfterEach
    void clearLocaleAndSecurityContexts() {
        LocaleContextHolder.resetLocaleContext();
        SecurityContextHolder.clearContext();
    }

    @Test
    void languageTagsAndBrowserLocaleNormalizeToIndonesian() {
        assertEquals("id", LocaleUtil.normalizeSupportedLanguage("id"));
        assertEquals("id", LocaleUtil.normalizeSupportedLanguage("id-ID"));
        assertEquals("id", LocaleUtil.normalizeSupportedLanguage("id_ID"));
        assertEquals("id", LocaleUtil.getLocale("id").getLanguage());
        assertEquals("ID", LocaleUtil.getLocale("id").getCountry());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(Locale.forLanguageTag("id-ID"));
        assertEquals("id", LocaleUtil.getCurrentLanguage(request));
        assertEquals("id", LocaleUtil.getJqGridLanguage(request));
    }

    @Test
    void configuredLocaleResolverUsesIndonesianBrowserPreference() {
        LocaleResolver resolver = new WebConfig(
                mock(SupportedLocaleChangeInterceptor.class))
                .localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(Locale.forLanguageTag("id-ID"));

        assertEquals("id", resolver.resolveLocale(request).getLanguage());
    }

    @Test
    void loginAndAuthenticatedNavigationOfferIndonesian() throws Exception {
        String login = read(
                "src/main/webapp/WEB-INF/views/login/login.jsp");
        String header = read("src/main/webapp/header.jsp");

        assertTrue(login.contains("changeLoginLanguage('id')"));
        assertTrue(login.contains("lang=\"id\""));
        assertTrue(login.contains("Bahasa Indonesia"));
        assertTrue(header.contains("changeUiLanguage('id')"));
        assertTrue(header.contains("sessionLang eq 'id'"));
        assertTrue(header.contains("Bahasa Indonesia"));
    }

    @Test
    void javascriptBundleLoaderNormalizesIndonesianRegionTags()
            throws Exception {
        String loader = read(
                "src/main/resources/static/js/i18n/common_i18n.js");

        assertTrue(loader.contains("function normalizeBundleLanguage(lang)"));
        assertTrue(loader.contains("ko|en|id|ja|zh"));
        assertTrue(loader.contains("language:normalizeBundleLanguage(lang)"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void manualLocaleChangePersistsIndonesianForAuthenticatedSession()
            throws Exception {
        SessionValue sessionValue = new SessionValue();
        Provider<SessionValue> provider = mock(Provider.class);
        MenuDao menuDao = mock(MenuDao.class);
        ComboDao comboDao = mock(ComboDao.class);
        when(provider.get()).thenReturn(sessionValue);
        when(menuDao.getMenuTopList(any(UserVO.class)))
                .thenReturn(Collections.emptyList());
        when(menuDao.getMenuSubList(any(UserVO.class)))
                .thenReturn(Collections.emptyList());
        when(comboDao.selectComboLang("id"))
                .thenReturn(Collections.emptyList());

        UserVO user = new UserVO();
        user.setUserCd("U1");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user, null, Collections.emptyList()));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("lang", "id");
        request.setAttribute(
                DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE,
                new SessionLocaleResolver());
        SupportedLocaleChangeInterceptor interceptor =
                new SupportedLocaleChangeInterceptor(
                        provider, menuDao, comboDao);

        assertTrue(interceptor.preHandle(
                request, new MockHttpServletResponse(), new Object()));

        Locale selected = (Locale) request.getSession().getAttribute(
                SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
        assertEquals("id", selected.getLanguage());
        assertEquals("id", sessionValue.getSessionLang());
        assertEquals("id", user.getSessionLang());
        verify(comboDao).selectComboLang("id");
    }

    @Test
    void databaseTranslationsAreCompleteAndHaveSafeFallbacks()
            throws Exception {
        Path bundlePath = Paths.get(
                "src/main/webapp/messages/message_id.properties");
        Properties bundle = new Properties();
        try (Reader reader = Files.newBufferedReader(
                bundlePath, StandardCharsets.UTF_8)) {
            bundle.load(reader);
        }

        String migration = read(
                "src/main/resources/sql/indonesian_locale_ddl.sql");
        Matcher jsonKeys = Pattern.compile(
                "^  \\\"[^\\\"]+\\\":", Pattern.MULTILINE)
                .matcher(migration);
        int migratedBundleKeys = 0;
        while (jsonKeys.find()) {
            migratedBundleKeys += 1;
        }
        assertEquals(bundle.size(), migratedBundleKeys);
        assertTrue(migration.contains("'menu.historyManagement'"));
        assertTrue(migration.contains("'menu.securityAccess'"));
        assertTrue(migration.contains(
                "ON CONFLICT (lang_type, lang_cd) DO UPDATE"));

        String freshMigration = read(
                "src/main/resources/sql/fresh_database_migration.psql");
        assertTrue(freshMigration.contains(
                "\\ir indonesian_locale_ddl.sql"));

        assertEnglishFallback(
                "src/main/resources/sqlMaps/oracle/its/controller/menu/Menu.xml");
        assertEnglishFallback(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/combo/Combo.xml");
        assertEnglishFallback(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/grid/GridInfo.xml");
        assertEnglishFallback(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/form/FormInfo.xml");
        assertEnglishFallback(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/toolbar/ToolbarInfo.xml");
        assertEnglishFallback(
                "src/main/resources/sqlMaps/oracle/its/commonlogic/excel/Excel.xml");
    }

    @Test
    void i18nGuideIsReadableAndDocumentsIndonesianPolicy()
            throws Exception {
        String guide = read("docs/i18n.md");
        assertTrue(guide.contains("# 다국어 운영 가이드"));
        assertTrue(guide.contains("인도네시아어: `id`"));
        assertTrue(guide.contains("패키지 번들과 DB 번역 병합"));
    }

    private void assertEnglishFallback(String path) throws Exception {
        String mapper = read(path);
        assertTrue(mapper.contains("LANG_TYPE = 'en'"),
                path + " must fall back to English when id is absent");
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)),
                StandardCharsets.UTF_8);
    }
}

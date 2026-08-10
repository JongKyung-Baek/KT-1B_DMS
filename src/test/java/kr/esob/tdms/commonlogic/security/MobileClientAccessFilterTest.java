package kr.esob.tdms.commonlogic.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.tdms.commonlogic.branding.TdmsBrandProperties;
import kr.esob.tdms.commonlogic.branding.TdmsBrandResolver;
import kr.esob.tdms.commonlogic.viewerintegration.ViewerIntegrationProperties;
import kr.esob.tdms.controller.general.distribution.accountrequest.DistributionAccountIntegrationProperties;

class MobileClientAccessFilterTest {
    private static final String ANDROID_PHONE =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) "
                    + "AppleWebKit/537.36 Chrome/126.0 Mobile Safari/537.36";
    private static final String ANDROID_TABLET =
            "Mozilla/5.0 (Linux; Android 13; SM-X710) "
                    + "AppleWebKit/537.36 Chrome/126.0 Safari/537.36";
    private static final String DESKTOP_CHROME =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                    + "AppleWebKit/537.36 Chrome/126.0 Safari/537.36";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StaticMessageSource messageSource = messages();
    private final TdmsBrandResolver brandResolver = new TdmsBrandResolver(
            new TdmsBrandProperties(), messageSource);
    private final MobileClientAccessFilter filter =
            new MobileClientAccessFilter(
                    messageSource, objectMapper, brandResolver);

    @Test
    void mobileLoginIsBlockedBeforeAuthenticationWithStandaloneHtml()
            throws Exception {
        MockHttpServletRequest request = request("GET", "/login/loginPage");
        request.addHeader("User-Agent", ANDROID_PHONE);
        request.addHeader("Accept", "text/html,application/xhtml+xml");
        request.addHeader("Accept-Language", "ko-KR,ko;q=0.9");
        request.addPreferredLocale(Locale.KOREA);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);

        filter.doFilter(request, response,
                (ignoredRequest, ignoredResponse) -> invoked.set(true));

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals(MobileClientAccessFilter.BLOCK_VIEW,
                response.getForwardedUrl());
        assertTrue(response.getContentType().startsWith("text/html"));
        assertEquals("모바일 환경에서는 접근할 수 없습니다",
                request.getAttribute("mobileBlockTitle"));
        assertEquals("모바일 환경에서는 접근할 수",
                request.getAttribute("mobileBlockTitleLine1"));
        assertEquals("없습니다",
                request.getAttribute("mobileBlockTitleLine2"));
        assertEquals("ko", request.getAttribute("mobileBlockLocale"));
        assertEquals(messageSource.getMessage("feature.error.systemName",
                        null, Locale.KOREAN),
                request.getAttribute("mobileBlockSystemName"));
        assertEquals(Boolean.FALSE,
                request.getAttribute("mobileBlockAlternateBrand"));
        assertFalse(invoked.get());
        assertNull(request.getSession(false));
        assertNull(response.getHeader("Set-Cookie"));
        assertBlockedHeaders(response);
    }

    @Test
    void alternatePortMobilePageUsesRuntimeBrandWithoutCreatingSession()
            throws Exception {
        TdmsBrandProperties properties = new TdmsBrandProperties();
        properties.setResourceLoader(new DefaultResourceLoader());
        properties.setEnabled(true);
        properties.setPort(443);
        properties.setSystemName("ESOB DMS");
        properties.setCompanyNameKo("이솝소프트(주)");
        properties.setCompanyNameEn("ESOB SOFT LTD.");
        properties.setLogoLightPath(
                "/resources/images/brand/esobsoft-logo-blue.png");
        properties.setLogoDarkPath(
                "/resources/images/brand/esobsoft-logo-white.png");
        properties.setLogoAlt("ESOBSOFT");
        properties.afterPropertiesSet();
        MobileClientAccessFilter alternateFilter =
                new MobileClientAccessFilter(messageSource, objectMapper,
                        new TdmsBrandResolver(properties, messageSource));
        MockHttpServletRequest request = request("GET", "/login/loginPage");
        request.setServerPort(443);
        request.addHeader("User-Agent", ANDROID_PHONE);
        request.addHeader("Accept", "text/html");
        request.addHeader("Accept-Language", "ko-KR,ko;q=0.9");
        request.addPreferredLocale(Locale.KOREA);
        MockHttpServletResponse response = new MockHttpServletResponse();

        alternateFilter.doFilter(request, response,
                (ignoredRequest, ignoredResponse) -> {
                    throw new AssertionError("Blocked request reached chain");
                });

        assertEquals("이솝소프트(주)",
                request.getAttribute("mobileBlockBrand"));
        assertEquals("ESOB DMS",
                request.getAttribute("mobileBlockSystemName"));
        assertEquals("/resources/images/brand/esobsoft-logo-blue.png",
                request.getAttribute("mobileBlockLogoPath"));
        assertEquals("ESOBSOFT",
                request.getAttribute("mobileBlockLogoAlt"));
        assertEquals(Boolean.TRUE,
                request.getAttribute("mobileBlockWideLogo"));
        assertEquals(Boolean.TRUE,
                request.getAttribute("mobileBlockAlternateBrand"));
        assertNull(request.getSession(false));
        assertNull(response.getHeader("Set-Cookie"));
        assertBlockedHeaders(response);
    }

    @Test
    void languageParameterCannotCreateSessionOnBlockedIndonesianRequest()
            throws Exception {
        MockHttpServletRequest request = request("GET", "/login/loginPage");
        request.addHeader("Sec-CH-UA-Mobile", "?1");
        request.addHeader("Accept", "text/html");
        request.addHeader("Accept-Language", "id-ID,id;q=0.9,en;q=0.8");
        request.addPreferredLocale(Locale.forLanguageTag("id-ID"));
        request.addParameter("lang", "id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                (ignoredRequest, ignoredResponse) -> {
                    throw new AssertionError("Blocked request reached the chain");
                });

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals("id", request.getAttribute("mobileBlockLocale"));
        assertEquals("Akses Seluler Tidak Tersedia",
                request.getAttribute("mobileBlockTitle"));
        assertEquals("Akses Seluler",
                request.getAttribute("mobileBlockTitleLine1"));
        assertEquals("Tidak Tersedia",
                request.getAttribute("mobileBlockTitleLine2"));
        assertNull(request.getSession(false));
        assertNull(response.getHeader("Set-Cookie"));
    }

    @Test
    void authenticatedMobilePageIsBlockedByTheSamePolicy() throws Exception {
        MockHttpServletRequest request = request(
                "GET", "/general/distribution/swrequest");
        request.setSession(new MockHttpSession());
        request.addHeader("User-Agent", "Mozilla/5.0 (iPad; CPU OS 17_5) Mobile/15E148");
        request.addHeader("Accept", "text/html");
        request.addHeader("Accept-Language", "en-US,en;q=0.8");
        request.addPreferredLocale(Locale.US);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                (ignoredRequest, ignoredResponse) -> {
                    throw new AssertionError("Authenticated page reached the chain");
                });

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals("Mobile Access Is Not Available",
                request.getAttribute("mobileBlockTitle"));
        assertNull(response.getHeader("Set-Cookie"));
    }

    @Test
    void apiAndXhrRequestsReceiveLocalizedJson403() throws Exception {
        MockHttpServletRequest request = request(
                "POST", "/general/distribution/workflow/api/requests/1/approve");
        request.addHeader("User-Agent", ANDROID_PHONE);
        request.addHeader("Accept", "application/json");
        request.addHeader("Accept-Language", "id-ID,id;q=0.9");
        request.addPreferredLocale(Locale.forLanguageTag("id-ID"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                (ignoredRequest, ignoredResponse) -> {
                    throw new AssertionError("Mobile API reached the chain");
                });

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        Map<String, Object> body = objectMapper.readValue(
                response.getContentAsByteArray(),
                new TypeReference<Map<String, Object>>() { });
        assertEquals(Boolean.FALSE, body.get("success"));
        assertEquals(MobileClientAccessFilter.ERROR_CODE, body.get("code"));
        assertTrue(body.get("message").toString().contains("kebijakan keamanan"));
        assertNull(response.getRedirectedUrl());
        assertNull(request.getSession(false));
        assertBlockedHeaders(response);
    }

    @Test
    void apiDetectionUsesTheApplicationPathBehindAContextRoot()
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/tdms/general/documents/api/list");
        request.setContextPath("/tdms");
        request.setServletPath("/general/documents/api/list");
        request.addHeader("User-Agent", ANDROID_PHONE);
        request.addHeader("Accept", "*/*");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                (ignoredRequest, ignoredResponse) -> {
                    throw new AssertionError("Context-root API reached the chain");
                });

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        Map<String, Object> body = objectMapper.readValue(
                response.getContentAsByteArray(),
                new TypeReference<Map<String, Object>>() { });
        assertEquals(MobileClientAccessFilter.ERROR_CODE, body.get("code"));
    }

    @Test
    void xhrOutsideAnApiNamedPathStillReceivesJson() throws Exception {
        MockHttpServletRequest request = request(
                "POST", "/general/organizationmanage/user/search");
        request.addHeader("User-Agent", ANDROID_PHONE);
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        request.addHeader("Accept", "*/*");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response,
                (ignoredRequest, ignoredResponse) -> {
                    throw new AssertionError("Mobile XHR reached the chain");
                });

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
    }

    @Test
    void clientHintsAndTabletUserAgentsAreConservativelyBlocked()
            throws Exception {
        assertBlocked(requestWithHeader("Sec-CH-UA-Mobile", "\"?1\""));
        assertBlocked(requestWithHeader("Sec-CH-UA-Platform", "\"Android\""));
        assertBlocked(requestWithHeader("Sec-CH-UA-Platform", "\"iOS\""));

        MockHttpServletRequest tablet = request("GET", "/");
        tablet.addHeader("User-Agent", ANDROID_TABLET);
        tablet.addHeader("Sec-CH-UA-Mobile", "?0");
        assertBlocked(tablet);
    }

    @Test
    void desktopClientsIncludingEmptyAndOperationalUserAgentsPassThrough()
            throws Exception {
        assertAllowed(null);
        assertAllowed("");
        assertAllowed(DESKTOP_CHROME);
        assertAllowed("Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5) Safari/605.1.15");
        assertAllowed("Mozilla/5.0 (X11; Linux x86_64) Firefox/128.0");
        assertAllowed("Mozilla/5.0 (X11; CrOS x86_64 15917.71.0) Chrome/124.0");
        assertAllowed("curl/8.7.1");

        MockHttpServletRequest clientHintsDesktop = request(
                "GET", "/login/loginPage");
        clientHintsDesktop.addHeader("User-Agent", DESKTOP_CHROME);
        clientHintsDesktop.addHeader("Sec-CH-UA-Mobile", "?0");
        clientHintsDesktop.addHeader("Sec-CH-UA-Platform", "\"Windows\"");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);
        filter.doFilter(clientHintsDesktop, response,
                (ignoredRequest, ignoredResponse) -> invoked.set(true));
        assertTrue(invoked.get());
    }

    @Test
    void staticHealthAndExactSignedIntegrationRequestsRemainOperational()
            throws Exception {
        assertSkipped("GET", "/resources/css/pages/error-page.css");
        assertSkipped("GET", "/vuexy/assets/vendor.css");
        assertSkipped("GET", "/favicon.ico");
        assertSkipped("GET", "/actuator/health/readiness");
        assertSkipped("POST", ViewerIntegrationProperties.CALLBACK_PATH);
        assertSkipped("POST", DistributionAccountIntegrationProperties.REQUEST_PATH);
        assertSkipped("GET",
                DistributionAccountIntegrationProperties.REQUEST_PATH + "/request-1");

        MockHttpServletRequest wrongMethod = request(
                "PUT", DistributionAccountIntegrationProperties.REQUEST_PATH);
        wrongMethod.addHeader("User-Agent", ANDROID_PHONE);
        assertBlocked(wrongMethod);

        MockHttpServletRequest nestedStatusPath = request(
                "GET", DistributionAccountIntegrationProperties.REQUEST_PATH
                        + "/request-1/extra");
        nestedStatusPath.addHeader("User-Agent", ANDROID_PHONE);
        assertBlocked(nestedStatusPath);

        MockHttpServletRequest directError = request("GET", "/error");
        directError.addHeader("User-Agent", ANDROID_PHONE);
        assertBlocked(directError);
    }

    private void assertAllowed(String userAgent) throws Exception {
        MockHttpServletRequest request = request("GET", "/login/loginPage");
        if (userAgent != null) {
            request.addHeader("User-Agent", userAgent);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);
        filter.doFilter(request, response, (ignoredRequest, servletResponse) -> {
            invoked.set(true);
            ((HttpServletResponse) servletResponse).setStatus(
                    HttpServletResponse.SC_NO_CONTENT);
        });

        assertTrue(invoked.get());
        assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
        assertNull(response.getForwardedUrl());
        assertNull(request.getSession(false));
        assertEquals("Sec-CH-UA-Mobile, Sec-CH-UA-Platform",
                response.getHeader("Accept-CH"));
        assertTrue(response.getHeaders("Vary").stream().anyMatch(
                value -> value.contains("Sec-CH-UA-Mobile")));
    }

    private void assertSkipped(String method, String path) throws Exception {
        MockHttpServletRequest request = request(method, path);
        request.addHeader("User-Agent", ANDROID_PHONE);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);
        filter.doFilter(request, response,
                (ignoredRequest, ignoredResponse) -> invoked.set(true));

        assertTrue(invoked.get(), path + " should bypass only the mobile filter");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertNull(response.getForwardedUrl());
    }

    private void assertBlocked(MockHttpServletRequest request) throws Exception {
        request.addHeader("Accept", "application/json");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response,
                (ignoredRequest, ignoredResponse) -> {
                    throw new AssertionError("Mobile request reached the chain");
                });
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    private MockHttpServletRequest requestWithHeader(String name, String value) {
        MockHttpServletRequest request = request("GET", "/api/documents");
        request.addHeader(name, value);
        request.addHeader("User-Agent", DESKTOP_CHROME);
        return request;
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }

    private void assertBlockedHeaders(MockHttpServletResponse response) {
        assertEquals("no-store, max-age=0", response.getHeader("Cache-Control"));
        assertEquals("no-cache", response.getHeader("Pragma"));
        assertTrue(response.getHeaders("Vary").stream().anyMatch(
                value -> value.contains("User-Agent")
                        && value.contains("Sec-CH-UA-Mobile")
                        && value.contains("Sec-CH-UA-Platform")));
        assertEquals("noindex, nofollow, noarchive",
                response.getHeader("X-Robots-Tag"));
    }

    private StaticMessageSource messages() {
        StaticMessageSource source = new StaticMessageSource();
        addMessages(source, Locale.KOREAN,
                "기술자료관리", "기술자료관리시스템", "HTTP 상태 코드",
                "접속 환경 안내", "모바일 환경에서는 접근할 수 없습니다",
                "모바일 환경에서는 접근할 수", "없습니다",
                "TDMS는 보안 정책에 따라 PC 데스크톱 브라우저에서만 사용할 수 있습니다.",
                "PC 환경에서 다시 접속해 주세요.", "기기 식별 정보로 확인합니다.");
        addMessages(source, Locale.ENGLISH,
                "Technical Data Management", "TECHNICAL DATA MANAGEMENT SYSTEM",
                "HTTP status code", "Access Environment Notice",
                "Mobile Access Is Not Available",
                "Mobile Access Is", "Not Available",
                "Under the TDMS security policy, this service is available only from a desktop PC browser.",
                "Open TDMS again from a desktop PC.",
                "The device type is determined from browser information.");
        addMessages(source, Locale.forLanguageTag("id-ID"),
                "Manajemen Data Teknis", "SISTEM MANAJEMEN DATA TEKNIS",
                "Kode status HTTP", "Pemberitahuan Lingkungan Akses",
                "Akses Seluler Tidak Tersedia",
                "Akses Seluler", "Tidak Tersedia",
                "Sesuai kebijakan keamanan TDMS, layanan ini hanya tersedia melalui peramban PC desktop.",
                "Buka kembali TDMS dari PC desktop.",
                "Jenis perangkat ditentukan dari informasi peramban.");
        return source;
    }

    private void addMessages(StaticMessageSource source, Locale locale,
                             String brand, String systemName, String statusAria,
                             String eyebrow, String title,
                             String titleLine1, String titleLine2, String message,
                             String help, String notice) {
        source.addMessage("feature.error.brand", locale, brand);
        source.addMessage("feature.error.systemName", locale, systemName);
        source.addMessage("feature.error.status.aria", locale, statusAria);
        source.addMessage("feature.mobileAccess.eyebrow", locale, eyebrow);
        source.addMessage("feature.mobileAccess.title", locale, title);
        source.addMessage("feature.mobileAccess.titleLine1", locale, titleLine1);
        source.addMessage("feature.mobileAccess.titleLine2", locale, titleLine2);
        source.addMessage("feature.mobileAccess.message", locale, message);
        source.addMessage("feature.mobileAccess.help", locale, help);
        source.addMessage("feature.mobileAccess.notice", locale, notice);
    }
}

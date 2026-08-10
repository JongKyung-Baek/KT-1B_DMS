package kr.esob.tdms.commonlogic.branding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.ForwardedHeaderFilter;

class TdmsBrandResolverTest {

    @Test
    void effective443UsesAlternateWhile444KeepsPrimaryBrand() {
        TdmsBrandResolver resolver = resolver();

        MockHttpServletRequest alternateRequest = request(443, Locale.KOREA);
        TdmsBrandView alternate = resolver.resolve(alternateRequest);
        assertTrue(alternate.isAlternate());
        assertEquals("ESOB DMS", alternate.getSystemName());
        assertEquals("이솝소프트(주)", alternate.getCompanyName());
        assertEquals("/resources/images/brand/esobsoft-logo-blue.png",
                alternate.getLogoLightPath());
        assertNull(alternateRequest.getSession(false));

        MockHttpServletRequest primaryRequest = request(444, Locale.KOREA);
        TdmsBrandView primary = resolver.resolve(primaryRequest);
        assertFalse(primary.isAlternate());
        assertEquals("KT-1B DMS", primary.getSystemName());
        assertEquals("기술자료관리", primary.getCompanyName());
        assertEquals("/resources/images/brand/kai-logo.png",
                primary.getLogoLightPath());
        assertNull(primaryRequest.getSession(false));
    }

    @Test
    void alternateCompanyUsesEnglishForEnglishAndIndonesianLocales() {
        TdmsBrandResolver resolver = resolver();
        assertEquals("ESOB SOFT LTD.",
                resolver.resolve(request(443, Locale.US)).getCompanyName());
        assertEquals("ESOB SOFT LTD.", resolver.resolve(request(
                443, Locale.forLanguageTag("id-ID"))).getCompanyName());
    }

    @Test
    void forwardedPortIsAppliedBeforeBrandSelection() throws Exception {
        TdmsBrandResolver resolver = resolver();
        ForwardedHeaderFilter forwardedHeaderFilter =
                new ForwardedHeaderFilter();
        MockHttpServletRequest backendRequest = request(3508, Locale.KOREA);
        backendRequest.addHeader("X-Forwarded-Host", "demo.example");
        backendRequest.addHeader("X-Forwarded-Proto", "https");
        backendRequest.addHeader("X-Forwarded-Port", "443");
        AtomicReference<TdmsBrandView> selected = new AtomicReference<>();

        forwardedHeaderFilter.doFilter(backendRequest,
                new MockHttpServletResponse(), (request, response) ->
                        selected.set(resolver.resolve(
                                (HttpServletRequest) request)));

        assertTrue(selected.get().isAlternate());
        assertEquals("ESOB DMS", selected.get().getSystemName());
        assertNull(backendRequest.getSession(false));
    }

    @Test
    void requestFilterSetsBrandBeforeChainForRequestAndErrorDispatch()
            throws Exception {
        TdmsBrandFilter filter = new TdmsBrandFilter(resolver());
        for (DispatcherType dispatcherType
                : new DispatcherType[] {
                        DispatcherType.REQUEST, DispatcherType.ERROR}) {
            MockHttpServletRequest request = request(443, Locale.KOREA);
            request.setDispatcherType(dispatcherType);
            AtomicReference<Object> inChain = new AtomicReference<>();

            filter.doFilter(request, new MockHttpServletResponse(),
                    (servletRequest, servletResponse) -> inChain.set(
                            servletRequest.getAttribute(
                                    TdmsBrandResolver.REQUEST_ATTRIBUTE)));

            assertSame(request.getAttribute(
                    TdmsBrandResolver.REQUEST_ATTRIBUTE), inChain.get());
            assertTrue(((TdmsBrandView) inChain.get()).isAlternate());
            assertNull(request.getSession(false));
        }
    }

    @Test
    void errorRedispatchPreservesBrandSelectedFromForwardedRequest()
            throws Exception {
        TdmsBrandFilter brandFilter = new TdmsBrandFilter(resolver());
        ForwardedHeaderFilter forwardedHeaderFilter =
                new ForwardedHeaderFilter();
        MockHttpServletRequest backendRequest = request(3508, Locale.KOREA);
        backendRequest.addHeader("X-Forwarded-Host", "demo.example");
        backendRequest.addHeader("X-Forwarded-Proto", "https");
        backendRequest.addHeader("X-Forwarded-Port", "443");

        forwardedHeaderFilter.doFilter(backendRequest,
                new MockHttpServletResponse(), (forwardedRequest, response) ->
                        brandFilter.doFilter(forwardedRequest, response,
                                (request, ignoredResponse) -> { }));
        Object selected = backendRequest.getAttribute(
                TdmsBrandResolver.REQUEST_ATTRIBUTE);
        assertTrue(((TdmsBrandView) selected).isAlternate());

        backendRequest.setDispatcherType(DispatcherType.ERROR);
        backendRequest.setServerPort(3508);
        brandFilter.doFilter(backendRequest, new MockHttpServletResponse(),
                (request, response) -> assertSame(selected,
                        request.getAttribute(
                                TdmsBrandResolver.REQUEST_ATTRIBUTE)));
    }

    @Test
    void languageQueryNeverParsesOrConsumesPostBody() {
        byte[] body = "{\"signed\":true}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/api/integrations/cv/v1/events") {
            @Override
            public String getParameter(String name) {
                throw new AssertionError("Brand resolution parsed request parameters");
            }
        };
        request.setServerPort(443);
        request.setQueryString("source=cv&lang=id");
        request.setContentType("application/json");
        request.setContent(body);
        request.addPreferredLocale(Locale.KOREA);

        TdmsBrandView selected = resolver().resolve(request);

        assertTrue(selected.isAlternate());
        assertEquals("ESOB SOFT LTD.", selected.getCompanyName());
        assertArrayEquals(body, request.getContentAsByteArray());
        assertNull(request.getSession(false));
    }

    private MockHttpServletRequest request(int port, Locale locale) {
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/login/loginPage");
        request.setServerPort(port);
        request.addPreferredLocale(locale);
        return request;
    }

    private TdmsBrandResolver resolver() {
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

        StaticMessageSource messages = new StaticMessageSource();
        messages.addMessage("feature.error.brand", Locale.KOREAN,
                "기술자료관리");
        messages.addMessage("feature.error.systemName", Locale.KOREAN,
                "기술자료관리시스템");
        messages.addMessage("feature.error.brand", Locale.ENGLISH,
                "Technical Data Management");
        messages.addMessage("feature.error.systemName", Locale.ENGLISH,
                "Technical Data Management System");
        return new TdmsBrandResolver(properties, messages);
    }
}

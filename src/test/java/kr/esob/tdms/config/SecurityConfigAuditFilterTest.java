package kr.esob.tdms.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import kr.esob.tdms.commonlogic.audit.RequestAuditFilter;
import kr.esob.tdms.commonlogic.menu.MenuDao;
import kr.esob.tdms.commonlogic.security.MobileClientAccessFilter;
import kr.esob.tdms.commonlogic.viewerintegration.ViewerCallbackAuthenticationFilter;
import kr.esob.tdms.controller.login.CustomAuthenticationProvider;

class SecurityConfigAuditFilterTest {

    @Test
    void servletContainerRegistrationIsDisabledForTheSecurityChainFilter() {
        RequestAuditFilter filter = mock(RequestAuditFilter.class);
        SecurityConfig config = new SecurityConfig(
                mock(MenuDao.class),
                mock(CustomAuthenticationProvider.class),
                filter,
                mock(ViewerCallbackAuthenticationFilter.class),
                mock(MobileClientAccessFilter.class));

        FilterRegistrationBean<RequestAuditFilter> registration =
                config.requestAuditFilterRegistration(filter);

        assertFalse(registration.isEnabled());
        assertSame(filter, registration.getFilter());
    }

    @Test
    void callbackAuthenticationFilterIsOnlyRegisteredInTheSecurityChain() {
        ViewerCallbackAuthenticationFilter filter =
                mock(ViewerCallbackAuthenticationFilter.class);
        SecurityConfig config = new SecurityConfig(
                mock(MenuDao.class),
                mock(CustomAuthenticationProvider.class),
                mock(RequestAuditFilter.class),
                filter,
                mock(MobileClientAccessFilter.class));

        FilterRegistrationBean<ViewerCallbackAuthenticationFilter> registration =
                config.viewerCallbackAuthenticationFilterRegistration(filter);

        assertFalse(registration.isEnabled());
        assertSame(filter, registration.getFilter());
    }

    @Test
    void mobileAccessFilterIsOnlyRegisteredInTheSecurityChain() {
        MobileClientAccessFilter filter = mock(MobileClientAccessFilter.class);
        SecurityConfig config = new SecurityConfig(
                mock(MenuDao.class),
                mock(CustomAuthenticationProvider.class),
                mock(RequestAuditFilter.class),
                mock(ViewerCallbackAuthenticationFilter.class),
                filter);

        FilterRegistrationBean<MobileClientAccessFilter> registration =
                config.mobileClientAccessFilterRegistration(filter);

        assertFalse(registration.isEnabled());
        assertSame(filter, registration.getFilter());
    }

    @Test
    void mobilePolicyRunsAfterSecurityHeadersAndBeforeAudit() throws Exception {
        String source = new String(
                Files.readAllBytes(Paths.get(
                        "src/main/java/kr/esob/tdms/config/SecurityConfig.java")),
                StandardCharsets.UTF_8).replace("\r\n", "\n");

        int mobileFilter = source.indexOf(
                "http.addFilterAfter(\n\t\t\t\tmobileClientAccessFilter");
        int auditFilter = source.indexOf(
                "http.addFilterBefore(requestAuditFilter");

        assertTrue(mobileFilter >= 0);
        assertTrue(auditFilter > mobileFilter);
        assertTrue(source.contains(
                "org.springframework.security.web.header.HeaderWriterFilter.class"));
    }

    @Test
    void callbackAuthenticationRunsAfterSessionManagementWithoutCreatingLoginState()
            throws Exception {
        String source = new String(
                Files.readAllBytes(Paths.get(
                        "src/main/java/kr/esob/tdms/config/SecurityConfig.java")),
                StandardCharsets.UTF_8);

        assertTrue(source.contains("http.addFilterAfter("));
        assertTrue(source.contains(
                "org.springframework.security.web.session.SessionManagementFilter.class"));
        assertFalse(source.contains("AnonymousAuthenticationFilter.class"));
    }

    @Test
    void departmentManagementRulePrecedesBroadDatabaseMenuRules()
            throws Exception {
        String source = new String(
                Files.readAllBytes(Paths.get(
                        "src/main/java/kr/esob/tdms/config/SecurityConfig.java")),
                StandardCharsets.UTF_8);

        int departmentRule = source.indexOf(
                ".antMatchers(\"/general/organizationmanage/insidedept/**\")");
        int databaseLoop = source.indexOf("for (MenuVO menuVo : menuList)");

        assertTrue(departmentRule >= 0);
        assertTrue(databaseLoop > departmentRule);
        assertTrue(source.contains(
                ".hasAuthority(\"ROLE_MENU_199\")"));
    }
}

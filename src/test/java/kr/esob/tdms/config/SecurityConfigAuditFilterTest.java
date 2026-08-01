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
import kr.esob.tdms.controller.login.CustomAuthenticationProvider;

class SecurityConfigAuditFilterTest {

    @Test
    void servletContainerRegistrationIsDisabledForTheSecurityChainFilter() {
        RequestAuditFilter filter = mock(RequestAuditFilter.class);
        SecurityConfig config = new SecurityConfig(
                mock(MenuDao.class),
                mock(CustomAuthenticationProvider.class),
                filter);

        FilterRegistrationBean<RequestAuditFilter> registration =
                config.requestAuditFilterRegistration(filter);

        assertFalse(registration.isEnabled());
        assertSame(filter, registration.getFilter());
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

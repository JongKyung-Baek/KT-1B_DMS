package kr.esob.fdms.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import kr.esob.fdms.commonlogic.audit.RequestAuditFilter;
import kr.esob.fdms.commonlogic.menu.MenuDao;
import kr.esob.fdms.controller.login.CustomAuthenticationProvider;

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
}

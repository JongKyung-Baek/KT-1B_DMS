package kr.esob.fdms.commonlogic.securityacl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

class SecurityAclServiceI18nTest {

    @AfterEach
    void clearContexts() {
        LocaleContextHolder.resetLocaleContext();
        SecurityContextHolder.clearContext();
    }

    @Test
    void existingThreeArgumentConstructorKeepsKoreanFallbackMessages() {
        SecurityAclService service = service();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.normalizeObjectType("unknown"));

        assertEquals("지원하지 않는 자료 유형입니다: UNKNOWN", exception.getMessage());
    }

    @Test
    void validationMessageUsesTheCurrentRequestLocale() {
        SecurityAclService service = service();
        StaticMessageSource messages = new StaticMessageSource();
        messages.addMessage(
            "feature.acl.error.unsupportedObjectType",
            Locale.ENGLISH,
            "Unsupported resource type: {0}");
        service.setMessageSource(messages);
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.normalizeObjectType("unknown"));

        assertEquals("Unsupported resource type: UNKNOWN", exception.getMessage());
    }

    @Test
    void accessDeniedMessageUsesTheCurrentRequestLocale() {
        SecurityAclService service = service();
        StaticMessageSource messages = new StaticMessageSource();
        messages.addMessage(
            "feature.acl.error.loginRequired",
            Locale.ENGLISH,
            "Login is required.");
        service.setMessageSource(messages);
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        AccessDeniedException exception = assertThrows(
            AccessDeniedException.class,
            service::requireCurrentUser);

        assertEquals("Login is required.", exception.getMessage());
    }

    private SecurityAclService service() {
        return new SecurityAclService(
            mock(SecurityAclDao.class),
            mock(SecurityAuditWriter.class),
            new ObjectMapper());
    }
}

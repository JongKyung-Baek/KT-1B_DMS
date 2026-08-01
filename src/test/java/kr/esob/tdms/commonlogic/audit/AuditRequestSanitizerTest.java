package kr.esob.tdms.commonlogic.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

class AuditRequestSanitizerTest {

    @Test
    void mappedPatternReplacesTheActualSecretPathValue() {
        String ticket = "0123456789abcdef0123456789abcdef";
        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/common/viewer/pdf-cache/" + ticket);
        request.setAttribute(
                HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE,
                "/common/viewer/pdf-cache/{ticketKey:[0-9a-fA-F]{32}}");

        String safe = AuditRequestSanitizer.safeRequestUri(request);

        assertEquals("/common/viewer/pdf-cache/{ticketKey:[0-9a-fA-F]{32}}", safe);
        assertFalse(safe.contains(ticket));
    }

    @Test
    void fallbackMasksCapabilityWhenHandlerPatternIsUnavailable() {
        String ticket = "abcdefabcdefabcdefabcdefabcdefab";

        String safe = AuditRequestSanitizer.maskSecretPathSegments(
                "/common/updown/download/" + ticket);

        assertEquals("/common/updown/download/{redacted}", safe);
        assertFalse(safe.contains(ticket));
    }
}

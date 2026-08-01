package kr.esob.tdms.commonlogic.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import kr.esob.tdms.commonlogic.result.ResultVO;

class AuditResponseBodyAdviceTest {
    private final AuditResponseBodyAdvice advice = new AuditResponseBodyAdvice();

    @Test
    void recordsOnlyTheExplicitResultVoBoolean() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResultVO body = new ResultVO();
        body.setSuccess(false);
        body.setMessage("sensitive business detail");
        body.setData("sensitive response data");

        advice.recordExplicitResult(request, body);

        assertEquals(Boolean.FALSE,
                request.getAttribute(AuditBusinessResultContext.REQUEST_ATTRIBUTE));
        assertNull(request.getAttribute("message"));
        assertNull(request.getAttribute("data"));
    }

    @Test
    void acceptsOnlyBooleanMapFlagsAndDoesNotModifyTheBody() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Map<String, Object> body = new HashMap<>();
        body.put("success", Boolean.FALSE);
        body.put("message", "not retained");

        advice.recordExplicitResult(request, body);

        assertEquals(Boolean.FALSE,
                request.getAttribute(AuditBusinessResultContext.REQUEST_ATTRIBUTE));
        assertSame(Boolean.FALSE, body.get("success"));

        MockHttpServletRequest nonBoolean = new MockHttpServletRequest();
        advice.recordExplicitResult(nonBoolean, Map.of("success", "false"));
        assertNull(nonBoolean.getAttribute(AuditBusinessResultContext.REQUEST_ATTRIBUTE));
    }
}

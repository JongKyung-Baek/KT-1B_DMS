package kr.esob.tdms.commonlogic.audit;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import kr.esob.tdms.commonlogic.result.ResultVO;

/**
 * Exposes only an explicit business success boolean to the request audit
 * filter. No response data, error message, or arbitrary object property is
 * retained.
 */
@ControllerAdvice
public class AuditResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (request instanceof ServletServerHttpRequest) {
            recordExplicitResult(((ServletServerHttpRequest) request).getServletRequest(), body);
        }
        return body;
    }

    void recordExplicitResult(HttpServletRequest request, Object body) {
        Boolean success = explicitSuccess(body);
        if (success != null) {
            request.setAttribute(AuditBusinessResultContext.REQUEST_ATTRIBUTE, success);
        }
    }

    private Boolean explicitSuccess(Object body) {
        if (body instanceof ResultVO) {
            return ((ResultVO) body).isSuccess();
        }
        if (!(body instanceof Map<?, ?>)) {
            return null;
        }
        Map<?, ?> map = (Map<?, ?>) body;
        Object success = map.get("success");
        if (!(success instanceof Boolean)) {
            success = map.get("isSuccess");
        }
        return success instanceof Boolean ? (Boolean) success : null;
    }
}

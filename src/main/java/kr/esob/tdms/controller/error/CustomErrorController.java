package kr.esob.tdms.controller.error;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class CustomErrorController implements ErrorController {

    private static final ErrorContent BAD_REQUEST = new ErrorContent("feature.error.400");
    private static final ErrorContent FORBIDDEN = new ErrorContent("feature.error.403");
    private static final ErrorContent NOT_FOUND = new ErrorContent("feature.error.404");
    private static final ErrorContent METHOD_NOT_ALLOWED = new ErrorContent("feature.error.405");
    private static final ErrorContent INTERNAL_SERVER_ERROR = new ErrorContent("feature.error.500");
    private static final ErrorContent GENERIC_ERROR = new ErrorContent("feature.error.generic");

    @RequestMapping("/error")
    public String handleError(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {
        int statusCode = resolveStatusCode(
                request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
        ErrorContent content = contentFor(statusCode);

        response.setStatus(statusCode);
        response.setHeader("Cache-Control", "no-store, max-age=0");
        response.setHeader("Pragma", "no-cache");

        model.addAttribute("errorCode", statusCode);
        model.addAttribute("errorTitleCode", content.messageCode("title"));
        model.addAttribute("errorMessageCode", content.messageCode("message"));
        model.addAttribute("errorHelpCode", content.messageCode("help"));

        return "error/error";
    }

    private int resolveStatusCode(Object status) {
        if (status == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        try {
            int statusCode = status instanceof Number
                    ? ((Number) status).intValue()
                    : Integer.parseInt(status.toString());
            if (statusCode >= 400 && statusCode <= 599) {
                return statusCode;
            }
        } catch (NumberFormatException ignored) {
            // Invalid container status values are handled as an internal error.
        }

        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private ErrorContent contentFor(int statusCode) {
        if (statusCode == HttpStatus.BAD_REQUEST.value()) {
            return BAD_REQUEST;
        }
        if (statusCode == HttpStatus.FORBIDDEN.value()) {
            return FORBIDDEN;
        }
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return NOT_FOUND;
        }
        if (statusCode == HttpStatus.METHOD_NOT_ALLOWED.value()) {
            return METHOD_NOT_ALLOWED;
        }
        if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return INTERNAL_SERVER_ERROR;
        }
        return GENERIC_ERROR;
    }

    private static final class ErrorContent {
        private final String codePrefix;

        private ErrorContent(String codePrefix) {
            this.codePrefix = codePrefix;
        }

        private String messageCode(String suffix) {
            return codePrefix + "." + suffix;
        }
    }

}

package kr.esob.tdms.controller.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.RequestDispatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;

class CustomErrorControllerTest {

    private final CustomErrorController controller = new CustomErrorController();

    @ParameterizedTest
    @CsvSource({
            "400, feature.error.400",
            "403, feature.error.403",
            "404, feature.error.404",
            "405, feature.error.405",
            "500, feature.error.500"
    })
    void rendersKnownErrorsWithSafeMessageCodes(int statusCode, String expectedCodePrefix) {
        MockHttpServletRequest request = errorRequest(statusCode);
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION,
                new IllegalStateException("database password and internal stack"));
        request.setAttribute(RequestDispatcher.ERROR_MESSAGE,
                "E:\\private\\internal\\document.pdf");
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI,
                "/internal/secret/path");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.handleError(request, response, model);

        assertEquals("error/error", view);
        assertEquals(statusCode, response.getStatus());
        assertEquals("no-store, max-age=0", response.getHeader("Cache-Control"));
        assertEquals("no-cache", response.getHeader("Pragma"));
        assertEquals(statusCode, model.get("errorCode"));
        assertEquals(expectedCodePrefix + ".title", model.get("errorTitleCode"));
        assertEquals(expectedCodePrefix + ".message", model.get("errorMessageCode"));
        assertEquals(expectedCodePrefix + ".help", model.get("errorHelpCode"));
        assertEquals(
                new HashSet<>(Arrays.asList(
                        "errorCode", "errorTitleCode", "errorMessageCode", "errorHelpCode")),
                model.keySet());
        assertFalse(model.toString().contains("database password"));
        assertFalse(model.toString().contains("private"));
        assertFalse(model.toString().contains("/internal/secret/path"));
    }

    @Test
    void preservesUnknownHttpErrorStatusAndUsesGenericGuidance() {
        MockHttpServletRequest request = errorRequest(429);
        MockHttpServletResponse response = new MockHttpServletResponse();
        ExtendedModelMap model = new ExtendedModelMap();

        controller.handleError(request, response, model);

        assertEquals(429, response.getStatus());
        assertEquals(429, model.get("errorCode"));
        assertEquals("feature.error.generic.title", model.get("errorTitleCode"));
    }

    @Test
    void malformedOrMissingStatusFallsBackToInternalServerError() {
        MockHttpServletRequest malformed = new MockHttpServletRequest();
        malformed.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, "not-a-status");
        MockHttpServletResponse malformedResponse = new MockHttpServletResponse();
        ExtendedModelMap malformedModel = new ExtendedModelMap();

        controller.handleError(malformed, malformedResponse, malformedModel);

        assertEquals(500, malformedResponse.getStatus());
        assertEquals(500, malformedModel.get("errorCode"));

        MockHttpServletResponse missingResponse = new MockHttpServletResponse();
        ExtendedModelMap missingModel = new ExtendedModelMap();
        controller.handleError(
                new MockHttpServletRequest(), missingResponse, missingModel);

        assertEquals(500, missingResponse.getStatus());
        assertEquals(500, missingModel.get("errorCode"));
    }

    @ParameterizedTest
    @CsvSource({"200", "399", "600", "999"})
    void nonErrorOrOutOfRangeStatusFallsBackToInternalServerError(int invalidStatus) {
        MockHttpServletRequest request = errorRequest(invalidStatus);
        MockHttpServletResponse response = new MockHttpServletResponse();
        ExtendedModelMap model = new ExtendedModelMap();

        controller.handleError(request, response, model);

        assertEquals(500, response.getStatus());
        assertEquals(500, model.get("errorCode"));
        assertEquals("feature.error.500.title", model.get("errorTitleCode"));
    }

    private MockHttpServletRequest errorRequest(int statusCode) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, statusCode);
        return request;
    }
}

package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class ErrorPagePresentationContractTest {

    private static final Path ERROR_VIEW =
            Paths.get("src/main/webapp/WEB-INF/views/error/error.jsp");
    private static final Path ERROR_CSS =
            Paths.get("src/main/resources/static/css/pages/error-page.css");
    private static final Path ERROR_SCRIPT =
            Paths.get("src/main/resources/static/js/error-page.js");

    @Test
    void errorPageIsStandaloneAndUsesTheCurrentCardChipAndCtaStyle() throws Exception {
        String view = read(ERROR_VIEW);
        String css = read(ERROR_CSS);

        assertTrue(view.contains("session=\"false\""));
        assertTrue(view.contains("<body class=\"error-page\""));
        assertTrue(view.contains("class=\"error-card\""));
        assertTrue(view.contains("class=\"error-status-chip\""));
        assertTrue(view.contains("class=\"error-button error-button--primary\""));
        assertTrue(view.contains("class=\"error-button error-button--secondary\""));
        assertTrue(view.contains("value=\"${tdmsBrand.logoLightPath}\""));
        assertTrue(view.contains("${tdmsBrand.logoAlt}"));
        assertTrue(view.contains("${tdmsBrand.companyName}"));
        assertTrue(view.contains("${tdmsBrand.systemName}"));
        assertFalse(view.contains("/resources/images/favicon/favicon.svg"));
        assertTrue(view.contains("/WEB-INF/jspf/favicon.jspf"));
        assertTrue(view.contains("/resources/css/pages/error-page.css?v=20260804.1"));
        assertTrue(css.contains("--error-accent: var(--tdms-primary, #034c8c)"));
        assertTrue(css.contains("--error-primary: var(--tdms-primary, #034c8c)"));
        assertTrue(css.contains("url(\"../fonts/Pretendard-Medium.woff2\")"));
        assertTrue(css.contains("border-radius: 24px"));
        assertTrue(css.contains("@media (max-width: 600px)"));
        assertTrue(css.contains("@media (max-width: 360px)"));
        assertTrue(css.contains("min-height: 44px"));
        assertTrue(css.contains("word-break: keep-all"));
        assertTrue(css.contains("overflow-wrap: normal"));
        assertTrue(css.contains(".error-button:focus-visible"));
        assertTrue(css.contains("@media (prefers-reduced-motion: reduce)"));
        assertFalse(view.contains("/resources/css/custom-font.css"));
        assertFalse(view.contains("/vuexy/"));
        assertFalse(view.contains("/WEB-INF/decorator/"));
    }

    @Test
    void dynamicContentIsEscapedAndSensitiveErrorAttributesAreNeverRendered()
            throws Exception {
        String view = read(ERROR_VIEW);
        String controller = read(Paths.get(
                "src/main/java/kr/esob/tdms/controller/error/CustomErrorController.java"));

        assertTrue(view.contains("<c:out value=\"${errorTitle}\" />"));
        assertTrue(view.contains("<c:out value=\"${errorMessage}\" />"));
        assertTrue(view.contains("<c:out value=\"${errorHelp}\" />"));
        assertTrue(view.contains("code=\"${errorTitleCode}\""));
        assertTrue(view.contains("code=\"${errorMessageCode}\""));
        assertTrue(view.contains("code=\"${errorHelpCode}\""));
        assertTrue(view.contains("code=\"feature.locale.code\""));
        assertTrue(view.contains("<html lang=\"${pageLocale}\">"));
        assertFalse(view.contains("${exception}"));
        assertFalse(view.contains("${message}"));
        assertFalse(view.contains("${path}"));
        assertFalse(controller.contains("ERROR_EXCEPTION"));
        assertFalse(controller.contains("ERROR_MESSAGE"));
        assertFalse(controller.contains("ERROR_REQUEST_URI"));
        assertTrue(controller.contains("feature.error.400"));
        assertTrue(controller.contains("feature.error.500"));
        assertFalse(controller.contains("요청 내용을 확인해 주세요"));
        assertTrue(controller.contains("return \"error/error\""));
    }

    @Test
    void navigationFallsBackToAuthenticationAwareRootAndNeverTrustsExternalReferrer()
            throws Exception {
        String view = read(ERROR_VIEW);
        String script = read(ERROR_SCRIPT);

        assertTrue(view.contains("<c:url var=\"homeUrl\" value=\"/\" />"));
        assertTrue(view.contains("data-home-url=\"${homeUrl}\""));
        assertTrue(script.contains("referrer.origin === window.location.origin"));
        assertTrue(script.contains("window.location.assign(homeUrl())"));
        assertFalse(script.contains("window.location = document.referrer"));
    }

    @Test
    void obsoleteStatusSpecificViewsCannotBypassTheSharedSafeTemplate() {
        assertFalse(Files.exists(
                Paths.get("src/main/webapp/WEB-INF/views/error/403.jsp")));
        assertFalse(Files.exists(
                Paths.get("src/main/webapp/WEB-INF/views/error/404.jsp")));
        assertFalse(Files.exists(
                Paths.get("src/main/webapp/WEB-INF/views/error/500.jsp")));
    }

    private String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}

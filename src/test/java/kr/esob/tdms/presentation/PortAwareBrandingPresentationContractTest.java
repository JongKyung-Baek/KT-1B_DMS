package kr.esob.tdms.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class PortAwareBrandingPresentationContractTest {

    @Test
    void activeBrowserTitlesUseTheRequestBrand() throws IOException {
        Path views = Path.of("src/main/webapp/WEB-INF/views");
        try (Stream<Path> paths = Files.walk(views)) {
            paths.filter(path -> path.toString().endsWith(".jsp"))
                    .forEach(path -> {
                        String source = read(path);
                        assertFalse(source.contains("<title>KT-1B DMS</title>"),
                                path.toString());
                        assertFalse(source.contains(" - KT-1B TDMS</title>"),
                                path.toString());
                        assertFalse(source.contains(" - KT-1B DMS</title>"),
                                path.toString());
                    });
        }

        String login = read(Path.of("src/main/webapp/WEB-INF/views/login/login.jsp"));
        String password = read(Path.of(
                "src/main/webapp/WEB-INF/views/login/passwordConfig.jsp"));
        String sidebar = read(Path.of("src/main/webapp/left.jsp"));
        String header = read(Path.of("src/main/webapp/header.jsp"));
        String error = read(Path.of(
                "src/main/webapp/WEB-INF/views/error/error.jsp"));
        String mobile = read(Path.of(
                "src/main/webapp/WEB-INF/views/error/mobileAccessBlocked.jsp"));
        String favicon = read(Path.of(
                "src/main/webapp/WEB-INF/jspf/favicon.jspf"));

        assertTrue(login.contains("tdmsBrand.systemName"));
        assertTrue(login.contains("tdmsBrand.companyName"));
        assertTrue(login.contains("tdmsBrand.logoDarkPath"));
        assertTrue(login.contains("<c:otherwise>TDMS - Login</c:otherwise>"));
        assertTrue(login.contains("feature.login.pageLabel"));
        assertTrue(login.contains("aria-label=\"${loginPageLabel}\""));
        assertTrue(login.contains(">KT-1B DMS</text>"));
        assertTrue(password.contains("tdmsBrand.systemName"));
        assertTrue(sidebar.contains("tdmsBrand.logoLightPath"));
        assertFalse(header.contains("tdmsBrand.systemName"));
        assertTrue(error.contains("tdmsBrand.logoLightPath"));
        assertTrue(mobile.contains("mobileBlockLogoPath"));
        assertTrue(password.contains("<c:otherwise>KT-1B</c:otherwise>"));
        assertTrue(error.contains("feature.error.systemName"));
        assertTrue(header.contains("code='label.tdms'"));
        assertTrue(login.contains("width: min(240px, 100%)"));
        assertTrue(login.contains("font-size: 40px"));
        assertTrue(login.contains("white-space: nowrap"));
        assertTrue(login.contains("width: 210px"));
        assertTrue(login.contains("font-size: 35px"));
        assertTrue(favicon.contains("tdmsBrand.alternate"));
        assertFalse(favicon.contains("tdmsBrand.logoLightPath"));
        assertTrue(favicon.contains("esobsoft-favicon-32x32.png?v=20260804.1"));
        assertTrue(favicon.contains("esobsoft-favicon.ico?v=20260804.1"));
        assertTrue(favicon.contains("esobsoft-apple-touch-icon.png?v=20260804.1"));
        assertTrue(favicon.contains("favicon-32x32.png"));
    }

    @Test
    void alternateLogosArePackagedAtTheirNativeRatio() throws IOException {
        assertImage("esobsoft-logo-blue.png");
        assertImage("esobsoft-logo-white.png");

        String ignore = read(Path.of(".gitignore"));
        assertTrue(ignore.contains(
                "!src/main/resources/static/images/brand/esobsoft-logo-blue.png"));
        assertTrue(ignore.contains(
                "!src/main/resources/static/images/brand/esobsoft-logo-white.png"));
        assertTrue(ignore.contains(
                "!src/main/resources/static/images/brand/esobsoft-favicon-32x32.png"));
        assertTrue(ignore.contains(
                "!src/main/resources/static/images/brand/esobsoft-apple-touch-icon.png"));
    }

    @Test
    void alternateFaviconUsesDedicatedSquareAssets() throws IOException {
        assertImageDimensions("esobsoft-favicon-32x32.png", 32, 32);
        assertImageDimensions("esobsoft-apple-touch-icon.png", 180, 180);
        Path icon = Path.of(
                "src/main/resources/static/images/brand/esobsoft-favicon.ico");
        assertTrue(Files.isRegularFile(icon));
        assertTrue(Files.size(icon) > 1024L);
    }

    @Test
    void filterRegistrationCoversRequestForwardAndErrorBeforeSecurity() {
        String webConfig = read(Path.of(
                "src/main/java/kr/esob/tdms/config/WebConfig.java"));
        String filter = read(Path.of("src/main/java/kr/esob/tdms/commonlogic/branding",
                "TdmsBrandFilter.java"));
        String properties = read(Path.of("src/main/resources/application.properties"));

        assertTrue(webConfig.contains("Ordered.HIGHEST_PRECEDENCE + 20"));
        assertTrue(webConfig.contains("DispatcherType.REQUEST"));
        assertTrue(webConfig.contains("DispatcherType.FORWARD"));
        assertTrue(webConfig.contains("DispatcherType.ERROR"));
        assertTrue(filter.contains("shouldNotFilterErrorDispatch"));
        assertTrue(properties.contains(
                "tdms.brand.alternate.enabled=${TDMS_ALTERNATE_BRAND_ENABLED:false}"));
        assertTrue(properties.contains(
                "tdms.brand.alternate.port=${TDMS_ALTERNATE_BRAND_PORT:443}"));
    }

    private void assertImage(String fileName) throws IOException {
        assertImageDimensions(fileName, 868, 233);
    }

    private void assertImageDimensions(String fileName, int width, int height)
            throws IOException {
        Path path = Path.of("src/main/resources/static/images/brand", fileName);
        assertTrue(Files.isRegularFile(path));
        BufferedImage image = ImageIO.read(path.toFile());
        assertNotNull(image);
        assertEquals(width, image.getWidth());
        assertEquals(height, image.getHeight());
    }

    private String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new AssertionError("Cannot read " + path, exception);
        }
    }
}

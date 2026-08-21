package kr.esob.tdms.commonlogic.pdfconversion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class PdfConversionPropertiesTest {

    @Test
    void buildsTheFixedConversionEndpointFromAnHttpsOrigin() {
        PdfConversionProperties properties = configured();
        properties.setBaseUrl("https://converter.example.test///");

        properties.requireOutboundConfiguration();

        assertThat(properties.convertUri().toString()).isEqualTo(
                "https://converter.example.test" + PdfConversionProperties.CONVERT_PATH);
    }

    @Test
    void allowsHttpOnlyForALocalDevelopmentConverter() {
        PdfConversionProperties local = configured();
        local.setBaseUrl("http://127.0.0.1:9001");
        local.requireOutboundConfiguration();

        PdfConversionProperties remoteHttp = configured();
        remoteHttp.setBaseUrl("http://converter.example.test");

        assertThrows(IllegalStateException.class, remoteHttp::requireOutboundConfiguration);
    }

    @Test
    void rejectsUnsafeAuthenticationAndEndpointConfiguration() {
        PdfConversionProperties shortSecret = configured();
        shortSecret.setSharedSecret("too-short");
        assertThat(assertThrows(IllegalStateException.class,
                shortSecret::requireOutboundConfiguration).getMessage())
                .contains("at least 32");

        PdfConversionProperties unsafeClient = configured();
        unsafeClient.setClientId("client with spaces");
        assertThat(assertThrows(IllegalStateException.class,
                unsafeClient::requireOutboundConfiguration).getMessage())
                .contains("client ID");

        PdfConversionProperties endpointWithPath = configured();
        endpointWithPath.setBaseUrl("https://converter.example.test/unexpected");
        assertThat(assertThrows(IllegalStateException.class,
                endpointWithPath::requireOutboundConfiguration).getMessage())
                .contains("HTTPS origin");

        PdfConversionProperties endpointWithCredentials = configured();
        endpointWithCredentials.setBaseUrl("https://user:password@converter.example.test");
        assertThrows(IllegalStateException.class,
                endpointWithCredentials::requireOutboundConfiguration);
    }

    @Test
    void rejectsDisabledIncompleteAndNonPositiveTimeoutConfiguration() {
        PdfConversionProperties disabled = configured();
        disabled.setEnabled(false);
        assertThat(assertThrows(IllegalStateException.class,
                disabled::requireOutboundConfiguration).getMessage())
                .contains("disabled");

        PdfConversionProperties incomplete = new PdfConversionProperties();
        incomplete.setEnabled(true);
        assertThat(assertThrows(IllegalStateException.class,
                incomplete::requireOutboundConfiguration).getMessage())
                .contains("incomplete");

        PdfConversionProperties timeout = configured();
        timeout.setReadTimeoutMs(0);
        assertThat(assertThrows(IllegalStateException.class,
                timeout::requireOutboundConfiguration).getMessage())
                .contains("timeouts");
    }

    @Test
    void windowsLauncherLoadsEveryPdfConversionRuntimeOverride() throws IOException {
        String application = Files.readString(
                Path.of("src/main/resources/application.properties"),
                StandardCharsets.UTF_8);
        String launcher = Files.readString(Path.of("start-server.bat"),
                StandardCharsets.UTF_8);

        for (String environmentName : new String[] {
                "TDMS_PDF_CONVERSION_ENABLED",
                "TDMS_PDF_CONVERSION_BASE_URL",
                "TDMS_PDF_CONVERSION_CLIENT_ID",
                "TDMS_PDF_CONVERSION_SHARED_SECRET",
                "TDMS_PDF_CONVERSION_WORK_DIR",
                "TDMS_PDF_CONVERSION_OUTPUT_FOLDER",
                "TDMS_PDF_CONVERSION_CONNECT_TIMEOUT_MS",
                "TDMS_PDF_CONVERSION_READ_TIMEOUT_MS",
                "TDMS_PDF_CONVERSION_POLL_INTERVAL_MS",
                "TDMS_PDF_CONVERSION_BATCH_SIZE",
                "TDMS_PDF_CONVERSION_WORKER_THREADS",
                "TDMS_PDF_CONVERSION_MAX_ATTEMPTS",
                "TDMS_PDF_CONVERSION_RETRY_DELAY_SECONDS",
                "TDMS_PDF_CONVERSION_STALE_MINUTES"
        }) {
            assertThat(application).contains("${" + environmentName + ":");
            assertThat(launcher).contains("==\"" + environmentName + "\"")
                    .contains("set \"" + environmentName + "=%%B\"");
        }
    }

    private PdfConversionProperties configured() {
        PdfConversionProperties properties = new PdfConversionProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://converter.example.test");
        properties.setClientId("kt1b-tdms");
        properties.setSharedSecret("0123456789abcdef0123456789abcdef");
        return properties;
    }
}

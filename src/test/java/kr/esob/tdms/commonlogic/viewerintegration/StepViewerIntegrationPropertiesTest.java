package kr.esob.tdms.commonlogic.viewerintegration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

class StepViewerIntegrationPropertiesTest {
    @Test
    void bindsUnderIndependentStepViewerPrefix() {
        ConfigurationProperties annotation =
                StepViewerIntegrationProperties.class.getAnnotation(ConfigurationProperties.class);

        assertEquals("tdms.step-viewer", annotation.prefix());
    }

    @Test
    void acceptsTheSharedViewerContractWithStepCredentials() {
        StepViewerIntegrationProperties properties = configured();

        assertDoesNotThrow(properties::requireOutboundConfiguration);
        assertDoesNotThrow(properties::requireCallbackConfiguration);
        assertEquals("https://step.example.test/api/integrations/tdms/v1/documents",
                properties.ingestUri().toString());
        assertEquals("https://step.example.test/api/integrations/tdms/v1/launch",
                properties.launchUri().toString());
    }

    @Test
    void validationNamesTheStepProviderEnvironmentVariables() {
        StepViewerIntegrationProperties properties = configured();
        properties.setEnabled(false);
        IllegalStateException disabled = assertThrows(
                IllegalStateException.class, properties::requireOutboundConfiguration);
        assertTrue(disabled.getMessage().contains("TDMS_STEP_VIEWER_ENABLED"));

        properties = configured();
        properties.setBaseUrl("");
        IllegalStateException baseUrl = assertThrows(
                IllegalStateException.class, properties::requireOutboundConfiguration);
        assertTrue(baseUrl.getMessage().contains("TDMS_STEP_VIEWER_BASE_URL"));

        properties = configured();
        properties.setSharedSecret("too-short");
        StepViewerIntegrationProperties shortSecretProperties = properties;
        IllegalStateException sharedSecret = assertThrows(
                IllegalStateException.class, shortSecretProperties::requireOutboundConfiguration);
        assertTrue(sharedSecret.getMessage().contains("TDMS_STEP_VIEWER_SHARED_SECRET"));

        properties = configured();
        properties.setCallbackClientId("");
        StepViewerIntegrationProperties missingCallbackProperties = properties;
        IllegalStateException callbackClient = assertThrows(
                IllegalStateException.class, missingCallbackProperties::requireCallbackConfiguration);
        assertTrue(callbackClient.getMessage().contains("TDMS_STEP_VIEWER_CALLBACK_CLIENT_ID"));

        properties = configured();
        properties.setWorkDir("relative/path");
        StepViewerIntegrationProperties relativeWorkDirProperties = properties;
        IllegalStateException workDirectory = assertThrows(
                IllegalStateException.class, relativeWorkDirProperties::workDirectory);
        assertTrue(workDirectory.getMessage().contains("TDMS_STEP_VIEWER_WORK_DIR"));

        properties = configured();
        properties.setConnectTimeoutMs(0);
        StepViewerIntegrationProperties invalidTimeoutProperties = properties;
        IllegalStateException timeouts = assertThrows(
                IllegalStateException.class, invalidTimeoutProperties::requireOutboundConfiguration);
        assertTrue(timeouts.getMessage().contains("TDMS_STEP_VIEWER_CONNECT_TIMEOUT_MS"));
        assertTrue(timeouts.getMessage().contains("TDMS_STEP_VIEWER_READ_TIMEOUT_MS"));
    }

    private StepViewerIntegrationProperties configured() {
        StepViewerIntegrationProperties properties = new StepViewerIntegrationProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://step.example.test/");
        properties.setClientId("tdms-step");
        properties.setCallbackClientId("3d-cv");
        properties.setSharedSecret("0123456789abcdef0123456789abcdef");
        properties.setWorkDir(Path.of(System.getProperty("java.io.tmpdir"), "kt1b-step-viewer")
                .toAbsolutePath().toString());
        return properties;
    }
}

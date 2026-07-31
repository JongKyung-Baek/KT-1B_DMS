package kr.esob.fdms.commonlogic.viewerintegration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ViewerIntegrationPropertiesTest {
    @Test
    void acceptsHttpsOriginAndStrongSecret() {
        ViewerIntegrationProperties properties = configured();
        assertDoesNotThrow(properties::requireOutboundConfiguration);
        assertDoesNotThrow(properties::requireCallbackConfiguration);
    }

    @Test
    void rejectsBaseUrlPathBecauseItWouldBreakCanonicalSignature() {
        ViewerIntegrationProperties properties = configured();
        properties.setBaseUrl("https://demo.esob.kr:7442/viewer");
        assertThrows(IllegalStateException.class, properties::ingestUri);
    }

    @Test
    void rejectsSharedSecretShorterThanThirtyTwoUtf8Bytes() {
        ViewerIntegrationProperties properties = configured();
        properties.setSharedSecret("too-short");
        assertThrows(IllegalStateException.class, properties::requireOutboundConfiguration);
        assertThrows(IllegalStateException.class, properties::requireCallbackConfiguration);
    }

    @Test
    void requiresDedicatedCallbackClientId() {
        ViewerIntegrationProperties properties = configured();
        properties.setCallbackClientId("");
        assertThrows(IllegalStateException.class, properties::requireCallbackConfiguration);
    }

    @Test
    void rejectsUnsafeStateRetentionWindow() {
        ViewerIntegrationProperties properties = configured();
        properties.setStateRetentionDays(0);
        assertThrows(IllegalStateException.class, properties::requireOutboundConfiguration);
        assertThrows(IllegalStateException.class, properties::requireCallbackConfiguration);

        properties.setStateRetentionDays(3651);
        assertThrows(IllegalStateException.class, properties::requireOutboundConfiguration);
    }

    private ViewerIntegrationProperties configured() {
        ViewerIntegrationProperties properties = new ViewerIntegrationProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://demo.esob.kr:7442/");
        properties.setClientId("tdms-demo");
        properties.setCallbackClientId("collabview");
        properties.setSharedSecret("0123456789abcdef0123456789abcdef");
        return properties;
    }
}

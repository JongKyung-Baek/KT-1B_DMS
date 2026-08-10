package kr.esob.tdms.commonlogic.branding;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class TdmsBrandPropertiesTest {

    @Test
    void disabledAlternateBrandNeedsNoRuntimeValues() {
        assertDoesNotThrow(new TdmsBrandProperties()::afterPropertiesSet);
    }

    @Test
    void validRuntimeBrandAcceptsRequiredNamesAndPackagedPngs() {
        assertDoesNotThrow(this::configured);
    }

    @Test
    void enabledBrandRejectsInvalidPortUnsafeTextAndExternalAsset() {
        TdmsBrandProperties invalidPort = configuredWithoutValidation();
        invalidPort.setPort(0);
        assertThrows(IllegalStateException.class,
                invalidPort::afterPropertiesSet);

        TdmsBrandProperties unsafeName = configuredWithoutValidation();
        unsafeName.setSystemName("<script>alert(1)</script>");
        assertThrows(IllegalStateException.class,
                unsafeName::afterPropertiesSet);

        TdmsBrandProperties paddedName = configuredWithoutValidation();
        paddedName.setSystemName(" ESOB DMS");
        assertThrows(IllegalStateException.class,
                paddedName::afterPropertiesSet);

        TdmsBrandProperties externalAsset = configuredWithoutValidation();
        externalAsset.setLogoLightPath("https://example.invalid/logo.png");
        assertThrows(IllegalStateException.class,
                externalAsset::afterPropertiesSet);
    }

    @Test
    void enabledBrandRejectsMissingPackagedAsset() {
        TdmsBrandProperties missing = configuredWithoutValidation();
        missing.setLogoLightPath(
                "/resources/images/brand/missing-brand-logo.png");
        assertThrows(IllegalStateException.class, missing::afterPropertiesSet);
    }

    private TdmsBrandProperties configured() {
        TdmsBrandProperties properties = configuredWithoutValidation();
        properties.afterPropertiesSet();
        return properties;
    }

    private TdmsBrandProperties configuredWithoutValidation() {
        TdmsBrandProperties properties = new TdmsBrandProperties();
        properties.setResourceLoader(new DefaultResourceLoader());
        properties.setEnabled(true);
        properties.setPort(443);
        properties.setSystemName("ESOB DMS");
        properties.setCompanyNameKo("이솝소프트(주)");
        properties.setCompanyNameEn("ESOB SOFT LTD.");
        properties.setLogoLightPath(
                "/resources/images/brand/esobsoft-logo-blue.png");
        properties.setLogoDarkPath(
                "/resources/images/brand/esobsoft-logo-white.png");
        properties.setLogoAlt("ESOBSOFT");
        return properties;
    }
}

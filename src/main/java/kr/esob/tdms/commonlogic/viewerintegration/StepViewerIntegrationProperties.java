package kr.esob.tdms.commonlogic.viewerintegration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** STEP viewer provider settings, isolated from the existing PDF provider. */
@Component
@ConfigurationProperties(prefix = "tdms.step-viewer")
public class StepViewerIntegrationProperties extends AbstractViewerIntegrationProperties {
    public static final String INGEST_PATH = AbstractViewerIntegrationProperties.INGEST_PATH;
    public static final String LAUNCH_PATH = AbstractViewerIntegrationProperties.LAUNCH_PATH;
    public static final String CALLBACK_PATH = AbstractViewerIntegrationProperties.CALLBACK_PATH;

    public StepViewerIntegrationProperties() {
        super("TDMS_STEP_VIEWER");
    }
}

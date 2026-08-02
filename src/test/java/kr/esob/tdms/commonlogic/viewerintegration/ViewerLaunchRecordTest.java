package kr.esob.tdms.commonlogic.viewerintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ViewerLaunchRecordTest {
    @Test
    void legacyFactoryLabelsExistingLaunchesAsPdf() {
        ViewerLaunchRecord record = ViewerLaunchRecord.from(metadata(), "2026-08-02T12:05:00Z");

        assertEquals("PDF", record.getViewerProvider());
    }

    @Test
    void providerAwareFactoryPersistsNormalizedStepProvider() {
        ViewerLaunchRecord record = ViewerLaunchRecord.from(
                metadata(), "2026-08-02T12:05:00Z", "step");

        assertEquals("STEP", record.getViewerProvider());
    }

    @Test
    void providerAwareFactoryRejectsUnknownRoutes() {
        assertThrows(IllegalArgumentException.class, () -> ViewerLaunchRecord.from(
                metadata(), "2026-08-02T12:05:00Z", "other"));
    }

    private ViewerDocumentMetadata metadata() {
        ViewerDocumentMetadata metadata = new ViewerDocumentMetadata();
        metadata.setCorrelationId("00000000-0000-0000-0000-000000000001");
        metadata.setObjectType("SW");
        metadata.setObjectId("MODEL-1");
        metadata.setAclObjectType("SW_SUB");
        metadata.setAclObjectId("PARENT-1");
        metadata.setFileNo("201");
        metadata.setFileName("model.stp");
        metadata.setUserCd("ADMIN");
        metadata.setUserId("admin");
        metadata.setUserName("Administrator");
        metadata.setAuthority("2");
        return metadata;
    }
}

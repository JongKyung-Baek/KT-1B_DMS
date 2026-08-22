package kr.esob.tdms.controller.general.distribution.doc_pdf_link_request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;

import kr.esob.tdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.commonlogic.fileapi.FileApiClient;
import kr.esob.tdms.commonlogic.pdfconversion.PdfConversionJob;
import kr.esob.tdms.commonlogic.pdfconversion.PdfConversionQueueService;
import kr.esob.tdms.commonlogic.viewerintegration.ViewerDocumentMetadata;
import kr.esob.tdms.commonlogic.viewerintegration.ViewerIntegrationService;
import kr.esob.tdms.commonlogic.viewerintegration.ViewerPreparedLaunch;
import kr.esob.tdms.commonlogic.viewerintegration.ViewerProvider;
import kr.esob.tdms.controller.login.UserVO;

class DocPdfLinkRequestControllerViewerLaunchTest {
    @TempDir
    Path tempDir;

    @Test
    void subFileUsesParentAclAndDeletesCorrelationPdfAfterTokenLaunch() throws Exception {
        Path sourcePdf = tempDir.resolve("source.pdf");
        Files.write(sourcePdf, "%PDF-1.4\n%%EOF\n".getBytes(StandardCharsets.US_ASCII));

        DocPdfLinkRequestController controller = new DocPdfLinkRequestController();
        controller.dao = org.mockito.Mockito.mock(DocPdfLinkRequestDao.class);
        controller.securityAclService = org.mockito.Mockito.mock(SecurityAclService.class);
        controller.viewerIntegrationService = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        UserVO actor = actor();
        when(authentication.getPrincipal()).thenReturn(actor);
        when(controller.dao.selectSwFile(any())).thenReturn(sourcePdf.toString());
        when(controller.dao.selectSubFileParent("SW", "SUB-1", "2")).thenReturn("PARENT-1");
        when(controller.dao.selectSwNo(any())).thenReturn("SW-001");
        when(controller.dao.selectFileNmSW(any())).thenReturn("source.pdf");
        when(controller.dao.selectRevisionSW(any())).thenReturn("A");

        when(controller.viewerIntegrationService.createRequestPdf(anyString())).thenAnswer(invocation ->
                Files.createTempFile(tempDir, invocation.getArgument(0) + "-", ".pdf"));

        AtomicReference<Path> transferredPdf = new AtomicReference<Path>();
        AtomicReference<ViewerDocumentMetadata> transferredMetadata =
                new AtomicReference<ViewerDocumentMetadata>();
        when(controller.viewerIntegrationService.prepareLaunch(
                any(Path.class), any(ViewerDocumentMetadata.class))).thenAnswer(invocation -> {
            Path path = invocation.getArgument(0);
            ViewerDocumentMetadata metadata = invocation.getArgument(1);
            assertTrue(Files.isRegularFile(path));
            assertNotEquals("SUB-1.pdf", path.getFileName().toString());
            transferredPdf.set(path);
            transferredMetadata.set(metadata);
            return new ViewerPreparedLaunch(
                    URI.create("https://demo.esob.kr:7442/api/integrations/tdms/v1/launch"),
                    "opaque-token", metadata.getCorrelationId());
        });
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.selectItem2(
                "SUB-1", "SW", "REQ-1", "2", authentication, model);

        assertEquals("/general/distribution/redirectPost", view);
        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) model.get("params");
        assertEquals(2, params.size());
        assertEquals("opaque-token", params.get("launchToken"));
        assertEquals("https://demo.esob.kr:7442/api/integrations/tdms/v1/launch", params.get("url"));

        ViewerDocumentMetadata metadata = transferredMetadata.get();
        assertEquals("SUB-1", metadata.getObjectId());
        assertEquals("SW_SUB", metadata.getAclObjectType());
        assertEquals("PARENT-1", metadata.getAclObjectId());
        assertEquals("2", metadata.getFileNo());
        assertFalse(Files.exists(transferredPdf.get()));
        assertTrue(Files.exists(sourcePdf));

        ArgumentCaptor<FileAccessRequest> acl = ArgumentCaptor.forClass(FileAccessRequest.class);
        verify(controller.securityAclService).requireAccess(acl.capture());
        assertEquals(SecurityAclService.VIEW, acl.getValue().getActionCd());
        assertEquals("SW_SUB", acl.getValue().getObjectType());
        assertEquals("PARENT-1", acl.getValue().getObjectId());
        assertEquals("2", acl.getValue().getFileNo());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> sourceLookup = ArgumentCaptor.forClass(Map.class);
        verify(controller.dao).selectSwFile(sourceLookup.capture());
        assertEquals("SUB-1", sourceLookup.getValue().get("OBJECT_ID"));
        assertEquals("2", sourceLookup.getValue().get("FILE_NO"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> metadataLookup = ArgumentCaptor.forClass(Map.class);
        verify(controller.dao).selectFileNmSW(metadataLookup.capture());
        assertEquals("SUB-1", metadataLookup.getValue().get("OBJECT_ID"));
        assertEquals("2", metadataLookup.getValue().get("FILE_NO"));
    }

    @Test
    void mainFileKeepsRequestedFileNumberForSourceMetadataAndAcl() throws Exception {
        Path sourcePdf = tempDir.resolve("main-1.pdf");
        Files.write(sourcePdf, "%PDF-1.4\n%%EOF\n".getBytes(StandardCharsets.US_ASCII));

        DocPdfLinkRequestController controller = new DocPdfLinkRequestController();
        controller.dao = org.mockito.Mockito.mock(DocPdfLinkRequestDao.class);
        controller.securityAclService = org.mockito.Mockito.mock(SecurityAclService.class);
        controller.viewerIntegrationService = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(actor());
        when(controller.dao.selectProduction(any())).thenReturn(sourcePdf.toString());
        when(controller.dao.selectSubFileParent("PRODUCT_DOCUMENT", "MAIN-1", "1")).thenReturn(null);
        when(controller.dao.selectDrawingNoCP(any())).thenReturn("PD-001");
        when(controller.dao.selectFileNmCP(any())).thenReturn("main-1.pdf");
        when(controller.dao.selectRevisionCP(any())).thenReturn("B");
        when(controller.viewerIntegrationService.createRequestPdf(anyString())).thenAnswer(invocation ->
                Files.createTempFile(tempDir, invocation.getArgument(0) + "-", ".pdf"));

        AtomicReference<ViewerDocumentMetadata> transferredMetadata = new AtomicReference<>();
        when(controller.viewerIntegrationService.prepareLaunch(
                any(Path.class), any(ViewerDocumentMetadata.class))).thenAnswer(invocation -> {
            ViewerDocumentMetadata metadata = invocation.getArgument(1);
            transferredMetadata.set(metadata);
            return new ViewerPreparedLaunch(
                    URI.create("https://demo.esob.kr:7442/api/integrations/tdms/v1/launch"),
                    "opaque-token", metadata.getCorrelationId());
        });

        String view = controller.selectItem2(
                "MAIN-1", "PRODUCT_DOCUMENT", "REQ-1", "1",
                authentication, new ExtendedModelMap());

        assertEquals("/general/distribution/redirectPost", view);
        ViewerDocumentMetadata metadata = transferredMetadata.get();
        assertEquals("MAIN-1", metadata.getObjectId());
        assertEquals("PRODUCT_DOCUMENT", metadata.getAclObjectType());
        assertEquals("MAIN-1", metadata.getAclObjectId());
        assertEquals("1", metadata.getFileNo());
        assertEquals("main-1.pdf", metadata.getFileName());
        assertEquals("B", metadata.getRevision());

        ArgumentCaptor<FileAccessRequest> acl = ArgumentCaptor.forClass(FileAccessRequest.class);
        verify(controller.securityAclService).requireAccess(acl.capture());
        assertEquals("PRODUCT_DOCUMENT", acl.getValue().getObjectType());
        assertEquals("MAIN-1", acl.getValue().getObjectId());
        assertEquals("1", acl.getValue().getFileNo());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> sourceLookup = ArgumentCaptor.forClass(Map.class);
        verify(controller.dao).selectProduction(sourceLookup.capture());
        assertEquals("MAIN-1", sourceLookup.getValue().get("OBJECT_ID"));
        assertEquals("1", sourceLookup.getValue().get("FILE_NO"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> metadataLookup = ArgumentCaptor.forClass(Map.class);
        verify(controller.dao).selectFileNmCP(metadataLookup.capture());
        assertEquals("MAIN-1", metadataLookup.getValue().get("OBJECT_ID"));
        assertEquals("1", metadataLookup.getValue().get("FILE_NO"));
    }

    @Test
    void localRelativeSwPdfUsesLocalCopyPathInsteadOfFileApi() throws Exception {
        Path relativeSource = Path.of("deployment/windows-demo/assets/demo-document.pdf");
        assertTrue(Files.isRegularFile(relativeSource));

        DocPdfLinkRequestController controller = new DocPdfLinkRequestController();
        controller.dao = org.mockito.Mockito.mock(DocPdfLinkRequestDao.class);
        controller.securityAclService = org.mockito.Mockito.mock(SecurityAclService.class);
        controller.viewerIntegrationService = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(actor());
        when(controller.dao.selectSwFile(any())).thenReturn(relativeSource.toString());
        when(controller.dao.selectSubFileParent("SW", "SW-MAIN-1", "1")).thenReturn(null);
        when(controller.dao.selectSwNo(any())).thenReturn("SW-001");
        when(controller.dao.selectFileNmSW(any())).thenReturn("demo-document.pdf");
        when(controller.dao.selectRevisionSW(any())).thenReturn("A");
        when(controller.viewerIntegrationService.createRequestPdf(anyString())).thenAnswer(invocation ->
                Files.createTempFile(tempDir, invocation.getArgument(0) + "-", ".pdf"));
        when(controller.viewerIntegrationService.prepareLaunch(any(), any())).thenAnswer(invocation -> {
            Path transferred = invocation.getArgument(0);
            assertTrue(Files.size(transferred) > 4);
            byte[] prefix = Files.readAllBytes(transferred);
            assertEquals("%PDF", new String(prefix, 0, 4, StandardCharsets.US_ASCII));
            ViewerDocumentMetadata metadata = invocation.getArgument(1);
            return new ViewerPreparedLaunch(
                    URI.create("https://demo.esob.kr:7442/api/integrations/tdms/v1/launch"),
                    "opaque-token", metadata.getCorrelationId());
        });

        String view = controller.selectItem2(
                "SW-MAIN-1", "SW", "REQ-1", "1", authentication, new ExtendedModelMap());

        assertEquals("/general/distribution/redirectPost", view);
    }

    @Test
    void localSwStepUsesIndependentThreeDimensionalViewerProvider() throws Exception {
        Path sourceStep = tempDir.resolve("assembly.step");
        byte[] stepBytes = ("ISO-10303-21;\nHEADER;\nFILE_DESCRIPTION(('demo'),'2;1');\n"
                + "ENDSEC;\nDATA;\nENDSEC;\nEND-ISO-10303-21;\n")
                .getBytes(StandardCharsets.US_ASCII);
        Files.write(sourceStep, stepBytes);

        DocPdfLinkRequestController controller = new DocPdfLinkRequestController();
        controller.dao = org.mockito.Mockito.mock(DocPdfLinkRequestDao.class);
        controller.securityAclService = org.mockito.Mockito.mock(SecurityAclService.class);
        controller.viewerIntegrationService = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(actor());
        when(controller.dao.selectSwFile(any())).thenReturn(sourceStep.toString());
        when(controller.dao.selectSubFileParent("SW", "STEP-1", "1")).thenReturn(null);
        when(controller.dao.selectSwNo(any())).thenReturn("TD-STEP-001");
        when(controller.dao.selectFileNmSW(any())).thenReturn("assembly.step");
        when(controller.dao.selectRevisionSW(any())).thenReturn("A");
        when(controller.viewerIntegrationService.createRequestDocument(
                anyString(), eq(ViewerProvider.STEP))).thenAnswer(invocation ->
                Files.createTempFile(tempDir, invocation.getArgument(0) + "-", ".step"));

        AtomicReference<Path> transferredStep = new AtomicReference<>();
        when(controller.viewerIntegrationService.prepareLaunch(
                any(Path.class), any(ViewerDocumentMetadata.class), eq(ViewerProvider.STEP)))
                .thenAnswer(invocation -> {
                    Path transferred = invocation.getArgument(0);
                    assertEquals(new String(stepBytes, StandardCharsets.US_ASCII),
                            Files.readString(transferred, StandardCharsets.US_ASCII));
                    transferredStep.set(transferred);
                    ViewerDocumentMetadata metadata = invocation.getArgument(1);
                    assertEquals("assembly.step", metadata.getFileName());
                    return new ViewerPreparedLaunch(
                            URI.create("http://127.0.0.1:7443/api/integrations/tdms/v1/launch"),
                            "step-launch-token", metadata.getCorrelationId());
                });

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.selectItem2(
                "STEP-1", "SW", "REQ-STEP-1", "1", authentication, model);

        assertEquals("/general/distribution/redirectPost", view);
        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) model.get("params");
        assertEquals("step-launch-token", params.get("launchToken"));
        assertEquals("http://127.0.0.1:7443/api/integrations/tdms/v1/launch", params.get("url"));
        assertFalse(Files.exists(transferredStep.get()));
        verify(controller.viewerIntegrationService, never()).createRequestPdf(anyString());
    }

    @Test
    void convertedSwSubUsesCompletedRepositoryPdfAndKeepsOriginalMetadata() throws Exception {
        DocPdfLinkRequestController controller = new DocPdfLinkRequestController();
        controller.dao = org.mockito.Mockito.mock(DocPdfLinkRequestDao.class);
        controller.securityAclService = org.mockito.Mockito.mock(SecurityAclService.class);
        controller.viewerIntegrationService = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        controller.pdfConversionQueueService = org.mockito.Mockito.mock(PdfConversionQueueService.class);
        controller.fileApiClient = org.mockito.Mockito.mock(FileApiClient.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(actor());
        when(controller.dao.selectSwFile(any())).thenReturn("SW/source-manual.docx");
        when(controller.dao.selectSubFileParent("SW", "SW-SUB-1", "2"))
                .thenReturn("SW-PARENT-1");
        when(controller.dao.selectFileNmSW(any())).thenReturn("source-manual.docx");
        when(controller.dao.selectSwNo(any())).thenReturn("TD-SW-001");
        when(controller.dao.selectRevisionSW(any())).thenReturn("C");

        PdfConversionJob completed = conversionJob(
                "SUCCEEDED", "CONVERTED_PDF/0123456789abcdef.pdf");
        when(controller.pdfConversionQueueService.findCurrent(
                "SW_SUB", "SW-SUB-1", "2")).thenReturn(completed);
        org.mockito.Mockito.doAnswer(invocation -> {
            Path target = invocation.getArgument(2);
            Files.write(target, "%PDF-1.4\n%%EOF\n".getBytes(StandardCharsets.US_ASCII));
            return null;
        }).when(controller.fileApiClient).downloadTo(
                eq("0123456789abcdef.pdf"), eq("CONVERTED_PDF"), any(Path.class));
        when(controller.viewerIntegrationService.createRequestPdf(anyString())).thenAnswer(invocation ->
                Files.createTempFile(tempDir, invocation.getArgument(0) + "-", ".pdf"));

        AtomicReference<ViewerDocumentMetadata> transferredMetadata = new AtomicReference<>();
        when(controller.viewerIntegrationService.prepareLaunch(any(), any())).thenAnswer(invocation -> {
            Path transferred = invocation.getArgument(0);
            assertTrue(Files.isRegularFile(transferred));
            assertTrue(Files.readString(transferred, StandardCharsets.US_ASCII).startsWith("%PDF-"));
            ViewerDocumentMetadata metadata = invocation.getArgument(1);
            transferredMetadata.set(metadata);
            return new ViewerPreparedLaunch(
                    URI.create("https://demo.esob.kr:7442/api/integrations/tdms/v1/launch"),
                    "converted-token", metadata.getCorrelationId());
        });

        String view = controller.selectItem2(
                "SW-SUB-1", "SW", "REQ-SW-1", "2",
                authentication, new ExtendedModelMap());

        assertEquals("/general/distribution/redirectPost", view);
        ViewerDocumentMetadata metadata = transferredMetadata.get();
        assertEquals("SW-SUB-1", metadata.getObjectId());
        assertEquals("SW_SUB", metadata.getAclObjectType());
        assertEquals("SW-PARENT-1", metadata.getAclObjectId());
        assertEquals("2", metadata.getFileNo());
        assertEquals("source-manual.docx", metadata.getFileName());
        verify(controller.pdfConversionQueueService).findCurrent(
                "SW_SUB", "SW-SUB-1", "2");
        verify(controller.pdfConversionQueueService, never()).enqueueStored(
                anyString(), anyString(), anyString(), anyString(), anyString());
        verify(controller.fileApiClient).downloadTo(
                eq("0123456789abcdef.pdf"), eq("CONVERTED_PDF"), any(Path.class));
    }

    @Test
    void pendingSwConversionIsNotReenqueuedAndFailsClosedBeforeViewerPreparation() {
        DocPdfLinkRequestController controller = new DocPdfLinkRequestController();
        controller.dao = org.mockito.Mockito.mock(DocPdfLinkRequestDao.class);
        controller.securityAclService = org.mockito.Mockito.mock(SecurityAclService.class);
        controller.viewerIntegrationService = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        controller.pdfConversionQueueService = org.mockito.Mockito.mock(PdfConversionQueueService.class);
        controller.fileApiClient = org.mockito.Mockito.mock(FileApiClient.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(actor());
        when(controller.dao.selectSwFile(any())).thenReturn("SW/source-manual.docx");
        when(controller.dao.selectSubFileParent("SW", "SW-MAIN-2", "1")).thenReturn(null);
        when(controller.dao.selectFileNmSW(any())).thenReturn("source-manual.docx");
        when(controller.pdfConversionQueueService.findCurrent(
                "SW", "SW-MAIN-2", "1")).thenReturn(conversionJob("PENDING", null));
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.selectItem2(
                "SW-MAIN-2", "SW", "REQ-SW-2", "1", authentication, model);

        assertEquals("/general/distribution/docConvertFail", view);
        assertEquals("Y", model.get("convertFailRestricted"));
        assertEquals("PENDING", model.get("conversionStatus"));
        assertEquals("N", model.get("conversionFailed"));
        verify(controller.pdfConversionQueueService, never()).enqueueStored(
                anyString(), anyString(), anyString(), anyString(), anyString());
        verifyNoInteractions(controller.viewerIntegrationService);
        verifyNoInteractions(controller.fileApiClient);
    }

    @Test
    void unsupportedSwViewerExtensionStopsBeforeQueueAndViewerPreparation() {
        DocPdfLinkRequestController controller = new DocPdfLinkRequestController();
        controller.dao = org.mockito.Mockito.mock(DocPdfLinkRequestDao.class);
        controller.securityAclService = org.mockito.Mockito.mock(SecurityAclService.class);
        controller.viewerIntegrationService = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        controller.pdfConversionQueueService = org.mockito.Mockito.mock(PdfConversionQueueService.class);
        controller.fileApiClient = org.mockito.Mockito.mock(FileApiClient.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(actor());
        when(controller.dao.selectSwFile(any())).thenReturn("SW/source-archive.zip");
        when(controller.dao.selectSubFileParent("SW", "SW-MAIN-UNSUPPORTED", "1")).thenReturn(null);
        when(controller.dao.selectFileNmSW(any())).thenReturn("source-archive.zip");
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.selectItem2(
                "SW-MAIN-UNSUPPORTED", "SW", "REQ-SW-UNSUPPORTED", "1",
                authentication, model);

        assertEquals("/general/distribution/docConvertFail", view);
        assertEquals("Y", model.get("convertFailRestricted"));
        assertEquals("UNSUPPORTED_VIEWER", model.get("conversionStatus"));
        assertEquals("N", model.get("conversionFailed"));
        verifyNoInteractions(controller.pdfConversionQueueService);
        verifyNoInteractions(controller.viewerIntegrationService);
        verifyNoInteractions(controller.fileApiClient);
    }

    @Test
    void unavailableCompletedSwPdfDoesNotFallBackToLegacyOnDemandConversion() throws Exception {
        DocPdfLinkRequestController controller = new DocPdfLinkRequestController();
        controller.dao = org.mockito.Mockito.mock(DocPdfLinkRequestDao.class);
        controller.securityAclService = org.mockito.Mockito.mock(SecurityAclService.class);
        controller.viewerIntegrationService = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        controller.pdfConversionQueueService = org.mockito.Mockito.mock(PdfConversionQueueService.class);
        controller.fileApiClient = org.mockito.Mockito.mock(FileApiClient.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(actor());
        when(controller.dao.selectSwFile(any())).thenReturn("SW/source-image.png");
        when(controller.dao.selectSubFileParent("SW", "SW-MAIN-3", "1")).thenReturn(null);
        when(controller.dao.selectFileNmSW(any())).thenReturn("source-image.png");
        when(controller.pdfConversionQueueService.findCurrent(
                "SW", "SW-MAIN-3", "1")).thenReturn(
                        conversionJob("SUCCEEDED", "CONVERTED_PDF/missing.pdf"));
        when(controller.viewerIntegrationService.createRequestPdf(anyString())).thenAnswer(invocation ->
                Files.createTempFile(tempDir, invocation.getArgument(0) + "-", ".pdf"));
        org.mockito.Mockito.doThrow(new IllegalStateException("repository unavailable"))
                .when(controller.fileApiClient).downloadTo(
                        eq("missing.pdf"), eq("CONVERTED_PDF"), any(Path.class));
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.selectItem2(
                "SW-MAIN-3", "SW", "REQ-SW-3", "1", authentication, model);

        assertEquals("/general/distribution/docConvertFail", view);
        assertEquals("FAILED", model.get("conversionStatus"));
        verify(controller.viewerIntegrationService, never())
                .prepareLaunch(any(Path.class), any(ViewerDocumentMetadata.class));
        verify(controller.pdfConversionQueueService, never()).enqueueStored(
                anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void unresolvedExactFileNumberStopsBeforeAclAndViewerTransfer() {
        DocPdfLinkRequestController controller = new DocPdfLinkRequestController();
        controller.dao = org.mockito.Mockito.mock(DocPdfLinkRequestDao.class);
        controller.securityAclService = org.mockito.Mockito.mock(SecurityAclService.class);
        controller.viewerIntegrationService = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(actor());
        when(controller.dao.selectDxf(any())).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> controller.selectItem2(
                "DXF-1", "DXF", "REQ-1", "9", authentication, new ExtendedModelMap()));

        verifyNoInteractions(controller.securityAclService);
        verifyNoInteractions(controller.viewerIntegrationService);
    }

    @Test
    void deniedAclStopsBeforeViewerRequestOrLaunch() {
        DocPdfLinkRequestController controller = new DocPdfLinkRequestController();
        controller.dao = org.mockito.Mockito.mock(DocPdfLinkRequestDao.class);
        controller.securityAclService = org.mockito.Mockito.mock(SecurityAclService.class);
        controller.viewerIntegrationService = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(actor());
        when(controller.dao.selectSwFile(any()))
                .thenReturn(tempDir.resolve("acl-denied.pdf").toString());
        when(controller.securityAclService.requireAccess(any(FileAccessRequest.class)))
                .thenThrow(new AccessDeniedException("denied"));

        assertThrows(AccessDeniedException.class, () -> controller.selectItem2(
                "SW-DENIED", "SW", "REQ-1", "1",
                authentication, new ExtendedModelMap()));

        verifyNoInteractions(controller.viewerIntegrationService);
    }

    @Test
    void missingPhysicalFileDoesNotPrepareOrReturnViewerLaunch() throws Exception {
        Path missingPdf = tempDir.resolve("missing.pdf");
        assertFalse(Files.exists(missingPdf));

        DocPdfLinkRequestController controller = new DocPdfLinkRequestController();
        controller.dao = org.mockito.Mockito.mock(DocPdfLinkRequestDao.class);
        controller.securityAclService = org.mockito.Mockito.mock(SecurityAclService.class);
        controller.viewerIntegrationService = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(actor());
        when(controller.dao.selectSwFile(any())).thenReturn(missingPdf.toString());
        when(controller.viewerIntegrationService.createRequestPdf(anyString())).thenAnswer(invocation ->
                Files.createTempFile(tempDir, invocation.getArgument(0) + "-", ".pdf"));
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.selectItem2(
                "SW-MISSING", "SW", "REQ-1", "1", authentication, model);

        assertEquals("/general/distribution/docConvertFail", view);
        assertEquals("Y", model.get("convertFailRestricted"));
        verify(controller.viewerIntegrationService, never())
                .prepareLaunch(any(Path.class), any(ViewerDocumentMetadata.class));
        verify(controller.viewerIntegrationService, never()).prepareLaunch(
                any(Path.class), any(ViewerDocumentMetadata.class), any(ViewerProvider.class));
    }

    @Test
    void blankOrNonPositiveFileNumberIsRejectedBeforeAnyResourceLookup() {
        DocPdfLinkRequestController controller = new DocPdfLinkRequestController();
        controller.dao = org.mockito.Mockito.mock(DocPdfLinkRequestDao.class);
        controller.securityAclService = org.mockito.Mockito.mock(SecurityAclService.class);
        controller.viewerIntegrationService = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(actor());

        assertThrows(IllegalArgumentException.class, () -> controller.selectItem2(
                "SW-1", "SW", "REQ-1", "", authentication, new ExtendedModelMap()));
        assertThrows(IllegalArgumentException.class, () -> controller.selectItem2(
                "SW-1", "SW", "REQ-1", "0", authentication, new ExtendedModelMap()));

        verifyNoInteractions(controller.dao);
        verifyNoInteractions(controller.securityAclService);
        verifyNoInteractions(controller.viewerIntegrationService);
    }

    @Test
    void peerReviewLegacyBlankFileNumberIsExplicitlyNormalizedToOne() throws Exception {
        Path sourcePdf = tempDir.resolve("peer-review.pdf");
        Files.write(sourcePdf, "%PDF-1.4\n%%EOF\n".getBytes(StandardCharsets.US_ASCII));

        DocPdfLinkRequestController controller = new DocPdfLinkRequestController();
        controller.dao = org.mockito.Mockito.mock(DocPdfLinkRequestDao.class);
        controller.securityAclService = org.mockito.Mockito.mock(SecurityAclService.class);
        controller.viewerIntegrationService = org.mockito.Mockito.mock(ViewerIntegrationService.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(actor());
        when(controller.dao.selectPeerReview(any())).thenReturn(sourcePdf.toString());
        when(controller.dao.selectPeerReviewNo(any())).thenReturn("PR-001");
        when(controller.dao.selectFileNmPeerReview(any())).thenReturn("peer-review.pdf");
        when(controller.viewerIntegrationService.createRequestPdf(anyString())).thenAnswer(invocation ->
                Files.createTempFile(tempDir, invocation.getArgument(0) + "-", ".pdf"));

        AtomicReference<ViewerDocumentMetadata> captured = new AtomicReference<>();
        when(controller.viewerIntegrationService.prepareLaunch(any(), any())).thenAnswer(invocation -> {
            ViewerDocumentMetadata metadata = invocation.getArgument(1);
            captured.set(metadata);
            return new ViewerPreparedLaunch(
                    URI.create("https://demo.esob.kr:7442/api/integrations/tdms/v1/launch"),
                    "opaque-token", metadata.getCorrelationId());
        });

        controller.selectItem2(
                "PR-1", "PEER_REVIEW", "REQ-1", "", authentication, new ExtendedModelMap());

        assertEquals("1", captured.get().getFileNo());
        ArgumentCaptor<FileAccessRequest> acl = ArgumentCaptor.forClass(FileAccessRequest.class);
        verify(controller.securityAclService).requireAccess(acl.capture());
        assertEquals("1", acl.getValue().getFileNo());
    }

    private UserVO actor() {
        UserVO user = new UserVO();
        user.setUserCd("ADMIN");
        user.setUserId("admin");
        user.setUserNm("Administrator");
        return user;
    }

    private PdfConversionJob conversionJob(String status, String outputFilePath) {
        PdfConversionJob job = new PdfConversionJob();
        job.setStatus(status);
        job.setOutputFilePath(outputFilePath);
        return job;
    }
}

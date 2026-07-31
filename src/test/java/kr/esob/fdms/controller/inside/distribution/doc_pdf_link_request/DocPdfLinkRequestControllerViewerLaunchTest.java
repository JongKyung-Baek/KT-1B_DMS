package kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

import kr.esob.fdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.commonlogic.viewerintegration.ViewerDocumentMetadata;
import kr.esob.fdms.commonlogic.viewerintegration.ViewerIntegrationService;
import kr.esob.fdms.commonlogic.viewerintegration.ViewerPreparedLaunch;
import kr.esob.fdms.controller.login.UserVO;

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

        assertEquals("/inside/distribution/redirectPost", view);
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

        assertEquals("/inside/distribution/redirectPost", view);
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

        assertEquals("/inside/distribution/redirectPost", view);
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
}

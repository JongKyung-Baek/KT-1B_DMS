package kr.esob.tdms.controller.general.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class SwRegistrationFileTypeContractTest {

    @Test
    void registrationUsesActualMultipartNamesAndPreservesValidatedExtensions() throws Exception {
        String service = read("src/main/java/kr/esob/tdms/controller/general/distribution/swrequest/SwRequestService.java");
        String method = section(service, "public ResultVO saveSwRegisterFileX2", "private void sendRegistrationMail");

        assertTrue(method.contains("sanitizeFileName(file.getOriginalFilename())"));
        assertFalse(method.contains("sanitizeFileName(request.getParameter(\"orgFileNm\"))"));
        assertTrue(method.contains("findInvalidSubFileName(subFiles)"));
        assertTrue(method.indexOf("findInvalidSubFileName(subFiles)")
                < method.indexOf("uploadMultipartToFileApi(file"));
        assertTrue(method.contains("objectId + \".\" + extension"));

        String subFiles = section(service, "private void saveSwSubFiles", "private SwRegisterPopupParam getFileSavedPath");
        assertTrue(subFiles.contains("TechnicalFileTypePolicy.isSafeFileName(originalName)"));
        assertTrue(subFiles.contains("subObjectId + \".\" + extension"));
        assertFalse(subFiles.contains("subObjectId + \".pdf\""));
        assertTrue(subFiles.contains("enqueuePreviewIfSupported("));
        assertTrue(subFiles.contains("TechnicalFileTypePolicy.isViewerProcessable(originalFileName)"));
        assertTrue(subFiles.contains("TechnicalFileTypePolicy.initialProcessingStatus(fileName)"));
    }

    @Test
    void registrationUiShowsAllFilesAndUsesServerPreviewPolicy() throws Exception {
        String page = read("src/main/webapp/WEB-INF/views/general/distribution/swRegisterPage.jsp");
        String popup = read("src/main/webapp/WEB-INF/views/general/distribution/swRegisterPopup.jsp");
        String controller = read("src/main/java/kr/esob/tdms/controller/general/distribution/swrequest/SwRequestController.java");

        assertTrue(controller.contains("technicalDirectPdfExtensions"));
        assertTrue(controller.contains("technicalDirectStepExtensions"));
        assertTrue(controller.contains("technicalPdfConversionExtensions"));
        assertFalse(page.contains("accept=\""));
        assertFalse(popup.contains("accept=\""));
        assertTrue(page.contains("swTechnicalFileTypePolicy.js"));
        assertTrue(popup.contains("swTechnicalFileTypePolicy.js"));
        assertTrue(page.contains("appendSwTechnicalFileTypeBadge($item, file)"));
        assertTrue(popup.contains("appendSwTechnicalFileTypeBadge($item, file)"));
        assertTrue(page.contains("id=\"swMainFileTypeStatus\""));
        assertTrue(popup.contains("id=\"swMainFileTypeStatus\""));
        assertTrue(page.contains("response.message || swRegisterMessage"));
        assertTrue(popup.contains("response.message || g_msg('msg.registerComplete')"));
        assertFalse(page.contains("feature.techRegister.required.mainPdf"));
        assertFalse(page.contains("feature.techRegister.upload.requiredPdf"));
    }

    @Test
    void downloadsRemainAclProtectedAndPreviewLinksIncludePdfAndStepFormats() throws Exception {
        String controller = read("src/main/java/kr/esob/tdms/controller/general/distribution/swrequest/SwRequestController.java");
        String popup = read("src/main/webapp/WEB-INF/views/general/distribution/swFilePopup.jsp");

        assertTrue(controller.contains("requireDownloadAccess(fileInfo"));
        assertTrue(controller.contains("TechnicalFileTypePolicy.hasMatchingAllowedExtension"));
        assertTrue(controller.contains("&& isPdfFilePath(filePath) && !isFileApiPath(filePath)"));
        assertTrue(controller.contains("Cache-Control\", \"no-store, private"));
        assertTrue(controller.contains("X-Content-Type-Options\", \"nosniff"));
        assertFalse(controller.contains("!isPdfFilePath(filePath) && !isAdminRole"));

        assertTrue(popup.contains("function isSwPdfFile"));
        assertTrue(popup.contains("function isSwStepFile"));
        assertTrue(popup.contains("function getSwFileProcessingStatus"));
        assertTrue(popup.contains("function isSwFilePreviewBlocked"));
        assertTrue(popup.contains("function isSwFileConversionDone"));
        assertTrue(popup.contains("function isSwViewerPreviewFile"));
        assertTrue(popup.contains("/\\.(?:stp|step)$/i"));
        assertTrue(popup.contains("status === \"PENDING\""));
        assertTrue(popup.contains("status === \"PROCESSING\""));
        assertTrue(popup.contains("status === \"FAIL\""));
        assertTrue(popup.contains("|| isSwFileConversionDone(rowdata)"));
        assertTrue(popup.contains("!isSwViewerPreviewFile(rowdata || {}, name)"));
        assertTrue(popup.contains("feature.techDetail.file.previewUnavailable"));
        assertTrue(popup.contains("initSwFileGrid(\"gridSwMainFile\", mainFileRows)"));
        assertTrue(popup.contains("initSwFileGrid(\"gridSwSubFile\", subFileRows)"));
    }

    @Test
    void unsupportedViewerFilesRemainTerminalAndDistributableWithoutAnOutboxStatus() throws Exception {
        String swMapper = read(
                "src/main/resources/sqlMaps/oracle/its/controller/general/distribution/swrequest/SwRequest.xml");
        String workflowMapper = read(
                "src/main/resources/sqlMaps/oracle/its/controller/general/distribution/workflow/DistributionWorkflow.xml");
        String conversionDdl = read("src/main/resources/sql/pdf_conversion_ddl.sql");

        assertTrue(swMapper.contains("IN ('DONE', 'NOT_REQUIRED', 'NOT_VIEWABLE')"));
        assertTrue(workflowMapper.contains("IN ('DONE', 'NOT_VIEWABLE')"));
        assertFalse(conversionDdl.contains("'UNSUPPORTED_VIEWER'"));
        assertFalse(conversionDdl.contains("'NOT_VIEWABLE'"));
    }

    @Test
    void stepPreviewUsesTheStepProviderWithoutEnteringThePdfOnlyPath() throws Exception {
        String viewer = read("src/main/java/kr/esob/tdms/controller/general/distribution/doc_pdf_link_request/DocPdfLinkRequestController.java");

        assertTrue(viewer.contains("!TechnicalFileTypePolicy.isViewerPreview(orgFileNm)"));
        assertTrue(viewer.contains("!TechnicalFileTypePolicy.isViewerProcessable(swSourceFileName)"));
        assertTrue(viewer.contains("conversionUnavailable(\"UNSUPPORTED_VIEWER\", model)"));
        assertTrue(viewer.contains("ViewerProvider viewerProvider = TechnicalFileTypePolicy.isStep(viewerSourcePath)"));
        assertTrue(viewer.contains("? ViewerProvider.STEP : ViewerProvider.PDF"));
        assertTrue(viewer.contains("viewerIntegrationService.createRequestDocument(correlationId, viewerProvider)"));
        assertTrue(viewer.contains("prepareSwViewerSource("));
        assertTrue(viewer.contains("viewerSourcePath, requestDocument, viewerProvider"));
        assertTrue(viewer.contains("viewerIntegrationService.prepareLaunch("));
        assertTrue(viewer.contains("requestDocument, metadata, viewerProvider"));
        assertTrue(viewer.contains("splitFileApiPath(filePathNm, viewerProvider)"));
        assertFalse(viewer.contains("Only PDF technical-data files are available"));
    }

    @Test
    void koreanAndEnglishMessagesDescribeDataNumbersAndSupportedFiles() throws Exception {
        String korean = read("src/main/webapp/messages/feature.properties");
        String english = read("src/main/webapp/messages/feature_en.properties");
        String indonesian = read("src/main/webapp/messages/feature_id.properties");

        assertTrue(korean.contains("feature.techRegister.field.transmittalNo=자료번호"));
        assertTrue(english.contains("feature.techRegister.field.transmittalNo=Data No."));
        assertTrue(korean.contains("feature.techRegister.validation.invalidFileName="));
        assertTrue(english.contains("feature.techRegister.validation.invalidFileName="));
        assertTrue(korean.contains("feature.techRegister.fileType.unsupportedViewer="));
        assertTrue(english.contains("feature.techRegister.fileType.unsupportedViewer="));
        assertTrue(indonesian.contains("feature.techRegister.fileType.unsupportedViewer="));
        for (String key : new String[] {
                "feature.techRegister.fileType.directPdf=",
                "feature.techRegister.fileType.directStep=",
                "feature.techRegister.fileType.pdfConversion=",
                "feature.techRegister.fileType.invalidFileName=",
                "feature.techRegister.validation.invalidSupportingFileName=",
                "feature.pdfConversion.unsupported.title=",
                "feature.pdfConversion.unsupported.description="
        }) {
            assertTrue(korean.contains(key), "Korean missing: " + key);
            assertTrue(english.contains(key), "English missing: " + key);
            assertTrue(indonesian.contains(key), "Indonesian missing: " + key);
        }
        assertFalse(korean.contains("feature.techRegister.validation.pdfOnly="));
        assertFalse(english.contains("feature.techRegister.validation.pdfOnly="));
    }

    private String read(String path) throws Exception {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }

    private String section(String source, String startMarker, String endMarker) {
        int start = source.indexOf(startMarker);
        int end = source.indexOf(endMarker, start + startMarker.length());
        assertTrue(start >= 0, "Start marker not found: " + startMarker);
        assertTrue(end > start, "End marker not found: " + endMarker);
        return source.substring(start, end);
    }
}

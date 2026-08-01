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
        assertTrue(method.contains("findUnsupportedSubFileName(subFiles)"));
        assertTrue(method.indexOf("findUnsupportedSubFileName(subFiles)")
                < method.indexOf("uploadMultipartToFileApi(file"));
        assertTrue(method.contains("objectId + \".\" + extension"));

        String subFiles = section(service, "private void saveSwSubFiles", "private SwRegisterPopupParam getFileSavedPath");
        assertTrue(subFiles.contains("TechnicalFileTypePolicy.isAllowedFileName(originalName)"));
        assertTrue(subFiles.contains("subObjectId + \".\" + extension"));
        assertFalse(subFiles.contains("subObjectId + \".pdf\""));
    }

    @Test
    void registrationUiUsesServerAllowlistAndReportsServerResult() throws Exception {
        String page = read("src/main/webapp/WEB-INF/views/general/distribution/swRegisterPage.jsp");
        String popup = read("src/main/webapp/WEB-INF/views/general/distribution/swRegisterPopup.jsp");
        String controller = read("src/main/java/kr/esob/tdms/controller/general/distribution/swrequest/SwRequestController.java");

        assertTrue(controller.contains("allowedTechnicalFileExtensions"));
        assertTrue(controller.contains("TechnicalFileTypePolicy.acceptAttribute()"));
        assertTrue(page.contains("accept=\"${allowedTechnicalFileExtensions}\""));
        assertTrue(popup.contains("accept=\"${allowedTechnicalFileExtensions}\""));
        assertTrue(page.contains("response.message || swRegisterMessage"));
        assertTrue(popup.contains("response.message || g_msg('msg.registerComplete')"));
        assertFalse(page.contains("accept=\".pdf,application/pdf\""));
        assertFalse(page.contains("feature.techRegister.required.mainPdf"));
        assertFalse(page.contains("feature.techRegister.upload.requiredPdf"));
    }

    @Test
    void nonPdfDownloadsRemainAclProtectedAndNeverUseThePdfViewerOrWatermark() throws Exception {
        String controller = read("src/main/java/kr/esob/tdms/controller/general/distribution/swrequest/SwRequestController.java");
        String viewer = read("src/main/java/kr/esob/tdms/controller/general/distribution/doc_pdf_link_request/DocPdfLinkRequestController.java");
        String popup = read("src/main/webapp/WEB-INF/views/general/distribution/swFilePopup.jsp");

        assertTrue(controller.contains("requireDownloadAccess(fileInfo"));
        assertTrue(controller.contains("TechnicalFileTypePolicy.hasMatchingAllowedExtension"));
        assertTrue(controller.contains("&& isPdfFilePath(filePath) && !isFileApiPath(filePath)"));
        assertTrue(controller.contains("Cache-Control\", \"no-store, private"));
        assertTrue(controller.contains("X-Content-Type-Options\", \"nosniff"));
        assertFalse(controller.contains("!isPdfFilePath(filePath) && !isAdminRole"));

        assertTrue(viewer.contains("\"SW\".equals(baseAclObjectType) && !TechnicalFileTypePolicy.isPdf(orgFileNm)"));
        assertTrue(popup.contains("function isSwPdfFile"));
        assertTrue(popup.contains("feature.techDetail.file.previewUnavailable"));
    }

    @Test
    void koreanAndEnglishMessagesDescribeDataNumbersAndSupportedFiles() throws Exception {
        String korean = read("src/main/webapp/messages/feature.properties");
        String english = read("src/main/webapp/messages/feature_en.properties");

        assertTrue(korean.contains("feature.techRegister.field.transmittalNo=자료번호"));
        assertTrue(english.contains("feature.techRegister.field.transmittalNo=Data No."));
        assertTrue(korean.contains("feature.techRegister.validation.unsupportedFileType="));
        assertTrue(english.contains("feature.techRegister.validation.unsupportedFileType="));
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

package kr.esob.tdms.controller.general.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TechnicalFileTypePolicyTest {

    @Test
    void allowsSupportedDocumentImageTextAndStepFormats() {
        for (String name : new String[] {
                "manual.PDF", "설계.docx", "표.xlsx", "발표.pptx", "문서.hwp",
                "문서.hwpx", "report.odt", "notes.txt", "data.csv", "image.png",
                "assembly.STP", "engine.step"
        }) {
            assertTrue(TechnicalFileTypePolicy.isAllowedFileName(name), name);
        }
    }

    @Test
    void rejectsExecutableActiveContentArchivesAndUnsupportedThreeDimensionalFormats() {
        for (String name : new String[] {
                "run.exe", "library.dll", "install.msi", "script.bat", "script.cmd",
                "script.ps1", "page.html", "vector.svg", "macro.xlsm", "bundle.zip",
                "model.dwg", "no-extension"
        }) {
            assertFalse(TechnicalFileTypePolicy.isAllowedFileName(name), name);
        }
    }

    @Test
    void validatesOriginalAndStoredExtensionsTogether() {
        assertTrue(TechnicalFileTypePolicy.hasMatchingAllowedExtension(
                "검토 자료.DOCX", "UPLOAD/abc123.docx"));
        assertTrue(TechnicalFileTypePolicy.hasMatchingAllowedExtension(
                "assembly.STP", "UPLOAD/abc123.stp"));
        assertFalse(TechnicalFileTypePolicy.hasMatchingAllowedExtension(
                "검토 자료.docx", "UPLOAD/abc123.pdf"));
        assertFalse(TechnicalFileTypePolicy.hasMatchingAllowedExtension(
                "../검토 자료.pdf", "UPLOAD/abc123.pdf"));
        assertEquals("docx", TechnicalFileTypePolicy.extensionOf("C:\\temp\\review.DOCX"));
    }

    @Test
    void parsesOnlySafeSingleFolderRepositoryPaths() {
        assertArrayEquals(new String[] { "UPLOAD", "abc123.xlsx" },
                TechnicalFileTypePolicy.splitRepositoryPath("UPLOAD/abc123.xlsx"));
        assertNull(TechnicalFileTypePolicy.splitRepositoryPath("C:\\files\\abc123.xlsx"));
        assertNull(TechnicalFileTypePolicy.splitRepositoryPath("UPLOAD/nested/abc123.xlsx"));
        assertNull(TechnicalFileTypePolicy.splitRepositoryPath("../UPLOAD/abc123.xlsx"));
        assertNull(TechnicalFileTypePolicy.splitRepositoryPath("UPLOAD/abc123.exe"));
    }

    @Test
    void exposesBrowserAcceptListFromTheServerPolicy() {
        String accept = TechnicalFileTypePolicy.acceptAttribute();
        assertTrue(accept.contains(".pdf"));
        assertTrue(accept.contains(".docx"));
        assertTrue(accept.contains(".hwp"));
        assertTrue(accept.contains(".png"));
        assertTrue(accept.contains(".stp"));
        assertTrue(accept.contains(".step"));
        assertFalse(accept.contains(".exe"));
    }

    @Test
    void identifiesPdfAndStepViewerPreviewFormats() {
        assertTrue(TechnicalFileTypePolicy.isStep("assembly.STP"));
        assertTrue(TechnicalFileTypePolicy.isStep("engine.step"));
        assertFalse(TechnicalFileTypePolicy.isStep("drawing.pdf"));

        assertTrue(TechnicalFileTypePolicy.isViewerPreview("drawing.PDF"));
        assertTrue(TechnicalFileTypePolicy.isViewerPreview("assembly.stp"));
        assertTrue(TechnicalFileTypePolicy.isViewerPreview("engine.STEP"));
        assertFalse(TechnicalFileTypePolicy.isViewerPreview("notes.txt"));
    }
}

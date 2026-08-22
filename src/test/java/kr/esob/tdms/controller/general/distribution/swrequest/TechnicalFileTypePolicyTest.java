package kr.esob.tdms.controller.general.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class TechnicalFileTypePolicyTest {

    @Test
    void allowsEverySafelyIdentifiableExtensionForOriginalRegistration() {
        for (String name : new String[] {
                "manual.PDF", "설계.docx", "표.xlsx", "발표.pptx", "문서.hwp",
                "문서.hwpx", "report.odt", "notes.txt", "data.csv", "image.png",
                "assembly.STP", "engine.step", "model.dwg", "archive.zip", "run.exe"
        }) {
            assertTrue(TechnicalFileTypePolicy.isSafeFileName(name), name);
        }
    }

    @Test
    void rejectsUnsafeOrUnidentifiableFileNames() {
        for (String name : new String[] {
                "no-extension", ".hidden", "../manual.pdf", "folder/manual.pdf",
                "bad.exe\n.pdf", "report.확장자", "report.abcdefghijklmnopq"
        }) {
            assertFalse(TechnicalFileTypePolicy.isSafeFileName(name), name);
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
        assertArrayEquals(new String[] { "UPLOAD", "abc123.exe" },
                TechnicalFileTypePolicy.splitRepositoryPath("UPLOAD/abc123.exe"));
    }

    @Test
    void exposesTheExactConverterGateSeparatelyFromRegistration() {
        Set<String> expectedConverterGate = Set.of(
                "doc", "docx", "xls", "xlsx", "ppt", "pptx", "hwp", "hwpx", "msg", "pdf",
                "png", "jpg", "jpeg", "jpe", "jfif", "gif", "bmp", "dib", "tif", "tiff",
                "webp", "ico", "cur", "tga", "pcx", "pbm", "pgm", "ppm", "pnm", "pam",
                "psd", "8pbs", "cal", "cals", "g4", "cg4", "dcx", "pict", "ras", "rle",
                "sgi", "xbm", "xpm", "xwd", "pcd", "mac", "pntg", "cut", "bitmap", "bm",
                "tpic", "wd", "iff", "pct", "clp", "img", "brk", "fs", "gl", "ica", "msp",
                "dxf", "dwg", "hgl", "hpgl", "plt", "svg", "svgz", "eps", "epsf", "epi",
                "wmf", "wpg", "pal");
        Set<String> actualConverterGate = new HashSet<>(
                TechnicalFileTypePolicy.pdfConversionExtensions());
        actualConverterGate.addAll(TechnicalFileTypePolicy.directPdfExtensions());

        assertEquals(expectedConverterGate, actualConverterGate);
        assertEquals(74, actualConverterGate.size());
        assertEquals(73, TechnicalFileTypePolicy.pdfConversionExtensions().size());
        assertEquals(76, TechnicalFileTypePolicy.allowedExtensions().size());
        for (String extension : new String[] {
                "doc", "docx", "xls", "xlsx", "ppt", "pptx", "hwp", "hwpx", "msg",
                "dwg", "dxf", "hgl", "hpgl", "plt", "svg", "svgz", "eps", "wmf", "pal"
        }) {
            assertTrue(TechnicalFileTypePolicy.requiresPdfConversion("sample." + extension), extension);
        }
        for (String extension : new String[] {
                "pdf", "stp", "step", "odt", "ods", "odp", "rtf", "txt", "csv",
                "xml", "json", "md", "exe", "zip"
        }) {
            assertFalse(TechnicalFileTypePolicy.requiresPdfConversion("sample." + extension), extension);
        }
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

        assertTrue(TechnicalFileTypePolicy.isViewerProcessable("drawing.dwg"));
        assertFalse(TechnicalFileTypePolicy.isViewerProcessable("notes.txt"));
        assertFalse(TechnicalFileTypePolicy.isViewerProcessable("archive.zip"));
        assertEquals("DONE", TechnicalFileTypePolicy.initialProcessingStatus("drawing.pdf"));
        assertEquals("PENDING", TechnicalFileTypePolicy.initialProcessingStatus("drawing.dwg"));
        assertEquals("NOT_VIEWABLE", TechnicalFileTypePolicy.initialProcessingStatus("archive.zip"));
    }
}

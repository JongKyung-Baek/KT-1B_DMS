package kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class DocPdfLinkRequestFileNoMapperContractTest {
    private static final Path MAPPER = Paths.get(
            "src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/"
                    + "docpdftestrequest/docPdfTestRequest.xml");

    @Test
    void everyMainAndSubSourceAndFileNameLookupRequiresTheRequestedFileNumber() throws Exception {
        String xml = readMapper();
        String[] selectors = {
                "selectFilePathNmDoc", "selectFilePathNmDrawing",
                "selectSwFile", "selectProduction", "selectDxf",
                "selectFileNmDoc", "selectFileNmDrawing", "selectFileNmSW",
                "selectFileNmCP", "selectFileNmDXF"
        };

        for (String selector : selectors) {
            String sql = selectBody(xml, selector);
            assertTrue(count(sql, "FILE_NO::varchar = #{FILE_NO}") >= 2,
                    selector + " must constrain both its main and sub-file branch by FILE_NO");
        }
    }

    @Test
    void everyMainAndSubNumberAndRevisionLookupIsBoundToTheRequestedFileNumber() throws Exception {
        String xml = readMapper();
        String[] selectors = {
                "selectDrawingNoDrawing", "selectDocumentNoDoc",
                "selectDrawingNoCP", "selectDrawingNoDXF",
                "selectRevisionDrawing", "selectRevisionSW",
                "selectRevisionCP", "selectRevisionDXF", "selectSwNo"
        };

        for (String selector : selectors) {
            String sql = selectBody(xml, selector);
            assertTrue(count(sql, "FILE_NO::varchar = #{FILE_NO}") >= 2,
                    selector + " must bind main and sub metadata to FILE_NO");
        }
    }

    @Test
    void subFileParentLookupRequiresTheSameFileNumberForEverySupportedType() throws Exception {
        String sql = selectBody(readMapper(), "selectSubFileParent");
        assertEquals(5, count(sql, "FILE_NO::varchar = #{fileNo}"));
    }

    @Test
    void everySubFileBranchRejectsInactiveFiles() throws Exception {
        String xml = readMapper();
        String[] selectors = {
                "selectFilePathNmDoc", "selectFilePathNmDrawing",
                "selectSwFile", "selectProduction", "selectDxf",
                "selectFileNmDoc", "selectFileNmDrawing", "selectFileNmSW",
                "selectFileNmCP", "selectFileNmDXF",
                "selectDrawingNoDrawing", "selectDocumentNoDoc", "selectSwNo",
                "selectDrawingNoCP", "selectDrawingNoDXF",
                "selectRevisionDrawing", "selectRevisionSW",
                "selectRevisionCP", "selectRevisionDXF"
        };
        for (String selector : selectors) {
            assertTrue(selectBody(xml, selector).contains("COALESCE(s.USE_YN, 'Y') = 'Y'"),
                    selector + " must reject inactive sub-files");
        }
    }

    private String readMapper() throws Exception {
        return new String(Files.readAllBytes(MAPPER), StandardCharsets.UTF_8);
    }

    private String selectBody(String xml, String id) {
        Pattern pattern = Pattern.compile(
                "<select\\s+id=\\\"" + Pattern.quote(id)
                        + "\\\"[^>]*>(.*?)</select>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(xml);
        String body = null;
        while (matcher.find()) {
            body = matcher.group(1);
        }
        assertTrue(body != null, "Missing mapper select: " + id);
        return body;
    }

    private int count(String source, String token) {
        int matches = 0;
        int offset = 0;
        while ((offset = source.indexOf(token, offset)) >= 0) {
            matches++;
            offset += token.length();
        }
        return matches;
    }
}

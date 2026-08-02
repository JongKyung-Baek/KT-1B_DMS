package kr.esob.tdms.controller.general.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class SwRequestFileExtensionSummaryContractTest {

    @Test
    void listAggregatesOnlyViewableMainAndActiveSupplementaryFileExtensions() throws Exception {
        String mapper = read(
            "src/main/resources/sqlMaps/oracle/its/controller/general/distribution/swrequest/SwRequest.xml");
        String activeMapper = mapper.substring(mapper.lastIndexOf("<select id=\"selectList\""));
        String list = section(activeMapper, "<select id=\"selectList\"", "<select id=\"selectListCount\"");

        assertTrue(list.contains("fileTypes.file_extensions AS fileExtensions"));
        assertTrue(list.contains("FROM DOCS_SW_FILE mainFile"));
        assertTrue(list.contains("FROM DOCS_SW_SUB_FILE subFile"));
        assertTrue(list.contains("COALESCE(subFile.USE_YN, 'Y') = 'Y'"));
        assertTrue(list.contains("SELECT DISTINCT"));
        assertTrue(list.contains("STRING_AGG(extension.file_extension"));
        assertTrue(list.contains("extensionActor.USER_CD = #{aclUserCd}"));
        assertTrue(list.contains("extensionActionPermission.ACTION_CD = 'VIEW'"));
        assertTrue(list.contains("extensionObjectPermission.ACTION_CD = 'VIEW'"));
        assertTrue(list.contains("extensionLabel.OBJECT_TYPE = accessibleFile.object_type"));
        assertTrue(list.contains("accessibleFile.object_type = 'SW_SUB'"));
        assertTrue(list.contains("extensionUserGrade.GRADE_LEVEL &gt;= extensionFileGrade.GRADE_LEVEL"));
    }

    @Test
    void listExposesLocalizedFileExtensionChips() throws Exception {
        String page = read(
            "src/main/webapp/WEB-INF/views/general/distribution/swRequestList.jsp");
        String css = read(
            "src/main/resources/static/css/pages/technical-data-list.css");
        String ddl = read("src/main/resources/sql/acl_foundation_ddl.sql");

        assertTrue(page.contains("function formatFileExtensions(cellValue)"));
        assertTrue(page.contains("escapeSwGridHtml(extension)"));
        assertTrue(page.contains("technical-data-list.css?v=20260802.2"));
        assertTrue(css.contains("#gridSwRequestList .file-extension-list"));
        assertTrue(css.contains("#gridSwRequestList .file-extension-badge"));
        assertTrue(ddl.contains("'gridSwRequestList', 'fileExtensions', '파일 확장자', 54, 170"));
        assertTrue(ddl.contains("'formatFileExtensions'"));
        assertTrue(ddl.contains("('ko', 'grid.fileExtensions', '파일 확장자')"));
        assertTrue(ddl.contains("('en', 'grid.fileExtensions', 'File Extensions')"));
    }

    @Test
    void responseModelContainsTheAggregatedExtensionField() throws Exception {
        String model = read(
            "src/main/java/kr/esob/tdms/controller/general/distribution/swrequest/SwRequestVO.java");

        assertTrue(model.contains("private String fileExtensions;"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private String section(String source, String startMarker, String endMarker) {
        int start = source.indexOf(startMarker);
        assertTrue(start >= 0, "Start marker not found: " + startMarker);
        int end = source.indexOf(endMarker, start + startMarker.length());
        assertTrue(end >= 0, "End marker not found: " + endMarker);
        return source.substring(start, end);
    }
}

package kr.esob.fdms.controller.inside.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class SwRequestSecurityGradePresentationContractTest {

    @Test
    void listReturnsTheEffectiveAclGradeWithoutDefaultingMissingLabels() throws Exception {
        String mapper = read(
            "src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/swrequest/SwRequest.xml");
        int selectListStart = mapper.lastIndexOf(
            "<select id=\"selectList\" resultType=\"kr.esob.fdms.controller.inside.distribution.swrequest.SwRequestVO\">");
        String selectList = mapper.substring(
            selectListStart,
            mapper.indexOf("<select id=\"selectListCount\"", selectListStart));

        assertTrue(selectList.contains("securityGrade.grade_cd AS gradeCd"));
        assertTrue(selectList.contains("securityGrade.grade_nm AS gradeNm"));
        assertTrue(selectList.contains("securityGrade.grade_level AS gradeLevel"));
        assertTrue(selectList.contains("label.object_type = 'SW'"));
        assertTrue(selectList.contains(
            "label.file_no IN (CAST(info.fileNo AS TEXT), '*')"));
        assertTrue(selectList.contains("grade.grade_level DESC NULLS LAST"));
        assertNotNull(SwRequestVO.class.getDeclaredField("gradeCd"));
        assertNotNull(SwRequestVO.class.getDeclaredField("gradeNm"));
        assertNotNull(SwRequestVO.class.getDeclaredField("gradeLevel"));
    }

    @Test
    void subFilesExposeTheStrictestDirectOrInheritedGrade() throws Exception {
        String mapper = read(
            "src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/swrequest/SwRequest.xml");
        String subFiles = mapper.substring(
            mapper.indexOf("<select id=\"selectSubFileInfo\""),
            mapper.indexOf("<select id=\"selectSwFileDownloadInfo\""));

        assertTrue(subFiles.contains("label.object_type = 'SW_SUB'"));
        assertTrue(subFiles.contains(
            "label.object_type = 'SW' AND label.file_no = '*'"));
        assertTrue(subFiles.contains("grade.grade_level DESC NULLS LAST"));
        assertTrue(subFiles.contains("securityGrade.grade_nm AS \"gradeNm\""));
    }

    @Test
    void listAndFilePopupRenderVisibleGradeBadgesAndUnassignedState() throws Exception {
        String ddl = read("src/main/resources/sql/acl_foundation_ddl.sql");
        String list = read(
            "src/main/webapp/WEB-INF/views/inside/distribution/swRequestList.jsp");
        String popup = read(
            "src/main/webapp/WEB-INF/views/inside/distribution/swFilePopup.jsp");

        assertTrue(ddl.contains(
            "'gridSwRequestList', 'gradeNm', '문서등급', 52, 90"));
        assertTrue(ddl.contains("formatter = 'formatDocumentGrade'"));
        assertTrue(ddl.contains("WHERE NOT EXISTS"));
        assertTrue(list.contains("function formatDocumentGrade"));
        assertTrue(list.contains("function localizeDocumentGradeName"));
        assertTrue(list.contains("feature.documentGrade.unassigned"));
        assertTrue(popup.contains("id=\"swPopupDocumentGrade\""));
        assertTrue(popup.contains("feature.techDetail.grid.grade"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)),
            StandardCharsets.UTF_8);
    }
}

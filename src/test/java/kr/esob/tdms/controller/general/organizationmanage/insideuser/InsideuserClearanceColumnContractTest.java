package kr.esob.tdms.controller.general.organizationmanage.insideuser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class InsideuserClearanceColumnContractTest {

    private static final String USER_MAPPER =
            "src/main/resources/sqlMaps/oracle/its/controller/general/organizationmanage/insideuser/Insideuser.xml";
    private static final String USER_PAGE =
            "src/main/webapp/WEB-INF/views/general/organizationmanage/insideuser/insideuserList.jsp";
    private static final String ACL_DDL = "src/main/resources/sql/acl_foundation_ddl.sql";

    @Test
    void userListExposesValidityAwareClearanceMetadata() throws Exception {
        String mapper = activeMapper(read(USER_MAPPER)).replaceAll("\\s+", " ");

        assertTrue(mapper.contains("securityGrade.GRADE_CD AS clearanceGradeCd"));
        assertTrue(mapper.contains("securityGrade.GRADE_NM AS clearanceGradeNm"));
        assertTrue(mapper.contains("securityGrade.GRADE_LEVEL AS clearanceGradeLevel"));
        assertTrue(mapper.contains("END AS clearanceStatus"));
        assertTrue(mapper.contains("LEFT JOIN DOCS_USER_SECURITY_CLEARANCE userClearance"));
        assertTrue(mapper.contains("LEFT JOIN DOCS_SECURITY_GRADE securityGrade"));
        assertTrue(mapper.contains("WHEN userClearance.USER_CD IS NULL THEN 'UNASSIGNED'"));
        assertTrue(mapper.contains("WHEN COALESCE(securityGrade.USE_YN, 'N') != 'Y' THEN 'INACTIVE_GRADE'"));
        assertTrue(mapper.contains("WHEN userClearance.VALID_FROM &gt; CURRENT_TIMESTAMP THEN 'SCHEDULED'"));
        assertTrue(mapper.contains("userClearance.VALID_TO &lt;= CURRENT_TIMESTAMP THEN 'EXPIRED'"));
    }

    @Test
    void userListVoCarriesClearancePresentationFields() {
        Map<String, Class<?>> fields;
        try (Stream<java.lang.reflect.Field> declared =
                Stream.of(InsideuserListVO.class.getDeclaredFields())) {
            fields = declared.collect(Collectors.toMap(
                    java.lang.reflect.Field::getName,
                    java.lang.reflect.Field::getType));
        }

        assertEquals(String.class, fields.get("clearanceGradeCd"));
        assertEquals(String.class, fields.get("clearanceGradeNm"));
        assertEquals(Integer.class, fields.get("clearanceGradeLevel"));
        assertEquals(String.class, fields.get("clearanceValidFrom"));
        assertEquals(String.class, fields.get("clearanceValidTo"));
        assertEquals(String.class, fields.get("clearanceStatus"));
    }

    @Test
    void userGridRendersALocalizedAndEscapedClearanceBadge() throws Exception {
        String page = read(USER_PAGE);

        assertTrue(page.contains("function formatUserClearance(cellValue, options, rowdata)"));
        assertTrue(page.contains("function escapeOrganizationHtml(value)"));
        assertTrue(page.contains("document-grade-badge document-grade-badge--"));
        assertTrue(page.contains("feature.organization.user.clearance.unassigned"));
        assertTrue(page.contains("feature.organization.user.clearance.expired"));
        assertTrue(page.contains("feature.organization.user.clearance.scheduled"));
        assertTrue(page.contains("feature.organization.user.clearance.inactiveGrade"));
        assertTrue(page.contains("feature.organization.user.clearance.validUntil"));
    }

    @Test
    void migrationAndLanguageBundlesPublishTheCurrentClearanceColumn() throws Exception {
        String ddl = read(ACL_DDL);
        String koreanMessages = read("src/main/webapp/messages/message_ko.properties");
        String englishMessages = read("src/main/webapp/messages/message_en.properties");
        String koreanFeatures = read("src/main/webapp/messages/feature.properties");
        String englishFeatures = read("src/main/webapp/messages/feature_en.properties");

        assertTrue(ddl.contains("'gridInsideUserList'"));
        assertTrue(ddl.contains("'clearanceGradeNm',    '현재 인가등급'"));
        assertTrue(ddl.contains("'formatUserClearance'"));
        assertTrue(ddl.contains("'clearanceStatus'"));
        assertTrue(ddl.contains("'clearanceValidFrom'"));
        assertTrue(ddl.contains("'clearanceValidTo'"));
        assertTrue(ddl.contains("('ko', 'grid.currentClearance', '현재 인가등급')"));
        assertTrue(ddl.contains("('en', 'grid.currentClearance', 'Current Clearance')"));
        assertTrue(koreanMessages.contains("grid.currentClearance=현재 인가등급"));
        assertTrue(englishMessages.contains("grid.currentClearance=Current Clearance"));
        assertTrue(koreanFeatures.contains("feature.organization.user.clearance.unassigned=미인가"));
        assertTrue(englishFeatures.contains("feature.organization.user.clearance.unassigned=No Clearance"));
    }

    private String activeMapper(String mapper) {
        int activeNamespace = mapper.lastIndexOf("<mapper namespace=\"sql.OrganizationmanageInsideuser\">");
        assertTrue(activeNamespace >= 0, "active Insideuser mapper was not found");
        return mapper.substring(activeNamespace);
    }

    private String read(String path) throws Exception {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }
}

package kr.esob.tdms.commonlogic.securityacl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class DocumentUserPermissionMapperContractTest {

    @Test
    void ddlDefinesFailClosedDocumentUserPermissionsAndOneTimeBackfill()
            throws Exception {
        String ddl = read("src/main/resources/sql/acl_foundation_ddl.sql");

        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_object_user_permission"));
        assertTrue(ddl.contains(
            "PRIMARY KEY (object_type, object_id, user_cd, action_cd)"));
        assertTrue(ddl.contains(
            "action_cd IN ('LIST', 'DETAIL', 'VIEW', 'DOWNLOAD_ORIGINAL', 'PRINT')"));
        assertTrue(ddl.contains("'20260724_DOCUMENT_USER_ACL'"));
        assertTrue(ddl.contains("문서별 사용자 ACL 초기 이관"));
    }

    @Test
    void mapperRequiresAnExplicitDocumentPermissionForEveryFileAction()
            throws Exception {
        String mapper = read(
            "src/main/resources/sqlMaps/oracle/its/commonlogic/securityacl/SecurityAcl.xml");

        assertTrue(mapper.contains("<select id=\"selectFileUserPermissions\""));
        assertTrue(mapper.contains("<select id=\"selectDocumentUserPermissionStates\""));
        assertTrue(mapper.contains("<delete id=\"deleteDocumentUserPermissions\""));
        assertTrue(mapper.contains("AND user_cd = #{userCd}"));
        assertTrue(mapper.contains("<insert id=\"upsertDocumentUserPermission\""));
        assertTrue(mapper.contains("LEFT JOIN docs_object_user_permission document_permission"));
        assertTrue(mapper.contains("DOCUMENT_PERMISSION_NOT_GRANTED"));
        assertTrue(mapper.contains(
            "document_permission.allow_yn IS DISTINCT FROM 'Y' THEN FALSE"));
    }

    @Test
    void permissionMatrixHasNoFiveHundredUserBlindSpotAndIncludesGrantHolders()
            throws Exception {
        String mapper = read(
            "src/main/resources/sqlMaps/oracle/its/commonlogic/securityacl/SecurityAcl.xml");
        String matrix = mapper.substring(
            mapper.indexOf("<select id=\"selectFileUserPermissions\""),
            mapper.indexOf("<select id=\"selectDocumentUserPermissionStates\""));

        assertFalse(matrix.contains("LIMIT 500"));
        assertTrue(matrix.contains("existing_permission.allow_yn = 'Y'"));
        assertTrue(matrix.contains("account_active_yn"));
    }

    @Test
    void subFilesInheritOnlyTheParentWildcardAndUseTheStrictestGrade()
            throws Exception {
        String mapper = read(
            "src/main/resources/sqlMaps/oracle/its/commonlogic/securityacl/SecurityAcl.xml");
        String ddl = read("src/main/resources/sql/acl_foundation_ddl.sql");

        assertTrue(mapper.contains("AND l.file_no = '*'"));
        assertTrue(mapper.contains("grade_level DESC NULLS LAST"));
        assertTrue(mapper.contains("#{permissionObjectType} != #{objectType}"));
        assertTrue(ddl.contains("untouched rollout copies"));
        assertTrue(ddl.contains("label_reason = 'Existing sub-file ACL migration'"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)),
            StandardCharsets.UTF_8);
    }
}

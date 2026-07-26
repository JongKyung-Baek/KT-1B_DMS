package kr.esob.fdms.controller.inside.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class SwRequestListAclContractTest {

	@Test
	void listAndCountApplyTheSameFailClosedDocumentAcl() throws Exception {
		String mapper = read(
			"src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/swrequest/SwRequest.xml");
		String acl = section(mapper, "<sql id=\"accessibleSwListAcl\">", "</sql>");
		String activeMapper = mapper.substring(mapper.lastIndexOf("<select id=\"selectList\""));
		String list = section(activeMapper, "<select id=\"selectList\"", "<select id=\"selectListCount\"");
		String count = section(activeMapper, "<select id=\"selectListCount\"", "<insert id=\"insertSwRegisterInfo\"");

		assertTrue(acl.contains("actor.USER_CD = #{aclUserCd}"));
		assertTrue(acl.contains("actor.USE_YN = 'Y'"));
		assertTrue(acl.contains("actor.DEL_YN = 'N'"));
		assertTrue(acl.contains("COALESCE(actor.LOCK_YN, 'N') != 'Y'"));
		assertTrue(acl.contains("clearance.VALID_FROM &lt;= CURRENT_TIMESTAMP"));
		assertTrue(acl.contains("clearance.VALID_TO &gt; CURRENT_TIMESTAMP"));
		assertTrue(acl.contains("userGrade.GRADE_LEVEL &gt;="));
		assertTrue(acl.contains("documentLabel.OBJECT_TYPE = 'SW'"));
		assertTrue(acl.contains("documentLabel.OBJECT_ID = info.OBJECT_ID"));
		assertTrue(acl.contains("documentLabel.FILE_NO = '*'"));
		assertTrue(acl.contains("actionPermission.ACTION_CD = 'LIST'"));
		assertTrue(acl.contains("actionPermission.ALLOW_YN = 'Y'"));
		assertTrue(acl.contains("objectPermission.OBJECT_TYPE = 'SW'"));
		assertTrue(acl.contains("objectPermission.OBJECT_ID = info.OBJECT_ID"));
		assertTrue(acl.contains("objectPermission.ACTION_CD = 'LIST'"));
		assertTrue(acl.contains("objectPermission.ALLOW_YN = 'Y'"));

		assertTrue(list.contains("<include refid=\"accessibleSwListAcl\"/>"));
		assertTrue(count.contains("<include refid=\"accessibleSwListAcl\"/>"));
	}

	@Test
	void webAndExcelPathsBindAclIdentityFromTheAuthenticatedPrincipal() throws Exception {
		String controller = read(
			"src/main/java/kr/esob/fdms/controller/inside/distribution/swrequest/SwRequestController.java");
		String excelService = read(
			"src/main/java/kr/esob/fdms/commonlogic/excel/CreateExcelService.java");

		String listEndpoint = section(
			controller, "@RequestMapping(\"/selectList\")", "@RequestMapping(\"/selectTree\")");
		String excelEndpoint = section(
			excelService, "public CreateExcelVO createExcel(HttpServletRequest request)",
			"private XSSFCellStyle setBackgroundColor");

		assertTrue(listEndpoint.contains("securityAclService.requireCurrentUser()"));
		assertTrue(listEndpoint.contains("param.setAclUserCd(currentUser.getUserCd())"));
		assertTrue(excelEndpoint.contains(
			"paramMap.put(\"aclUserCd\", user.getUserCd())"));
		assertTrue(excelEndpoint.indexOf("paramMap.put(\"aclUserCd\", user.getUserCd())")
			< excelEndpoint.indexOf("checkExcelCount(paramMap)"));
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

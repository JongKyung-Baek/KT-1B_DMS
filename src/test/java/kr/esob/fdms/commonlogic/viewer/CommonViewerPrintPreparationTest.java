package kr.esob.fdms.commonlogic.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import kr.esob.fdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import kr.esob.fdms.controller.login.UserVO;
import net.sf.json.JSONObject;

class CommonViewerPrintPreparationTest {

	@Test
	void transferMustReportSuccessAndAConcreteFileName() {
		JSONObject successful = new JSONObject();
		successful.put("result", true);
		successful.put("fileNm", " prepared-file.plt ");

		assertEquals("prepared-file.plt",
				CommonViewerService.requireSuccessfulTransfer(successful));

		JSONObject failed = new JSONObject();
		failed.put("result", false);
		failed.put("fileNm", "prepared-file.plt");
		assertThrows(IllegalStateException.class,
				() -> CommonViewerService.requireSuccessfulTransfer(failed));

		JSONObject missingFile = new JSONObject();
		missingFile.put("result", true);
		missingFile.put("fileNm", " ");
		assertThrows(IllegalStateException.class,
				() -> CommonViewerService.requireSuccessfulTransfer(missingFile));
		assertThrows(IllegalStateException.class,
				() -> CommonViewerService.requireSuccessfulTransfer(null));
	}

	@Test
	void singlePrintFailsClosedUntilAuthenticatedResultCallbackIntegrationExists() throws Exception {
		Method method = CommonViewerService.class.getMethod(
				"getPrintInfo", CommonViewerParam.class);
		assertNotNull(method);

		String source = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(
				"src/main/java/kr/esob/fdms/commonlogic/viewer/CommonViewerService.java")),
				java.nio.charset.StandardCharsets.UTF_8);
		int methodStart = source.indexOf("public CommonViewerVO getPrintInfo");
		int methodEnd = source.indexOf("private CommonViewerVO prepareLegacyPrintInfo", methodStart);
		String body = source.substring(methodStart, methodEnd);

		assertTrue(body.contains("bindActorAndRequire(param, SecurityAclService.PRINT)"));
		assertTrue(body.contains("result.setSuccess(false)"));
		assertTrue(body.contains("result.setPrintJobId(null)"));
		assertTrue(body.contains("result.setFilePath(null)"));
		assertTrue(body.contains("PRINT_CALLBACK_REQUIRED_REASON"));
		assertFalse(body.contains("printAuditService.start(param)"));
		assertFalse(body.contains("result.setSuccess(true)"));
	}

	@Test
	void mergedPrintFailsClosedAfterExactPerItemAclWithoutPathOrAuditJob() throws Exception {
		CommonViewerService service = new CommonViewerService();
		SecurityAclService aclService = mock(SecurityAclService.class);
		PrintAuditService auditService = mock(PrintAuditService.class);
		DocPdfLinkRequestDao pdfDao = mock(DocPdfLinkRequestDao.class);
		UserVO actor = new UserVO();
		actor.setUserCd("USER-1");
		when(aclService.requireCurrentUser()).thenReturn(actor);
		ReflectionTestUtils.setField(service, "securityAclService", aclService);
		ReflectionTestUtils.setField(service, "printAuditService", auditService);
		ReflectionTestUtils.setField(service, "pdao", pdfDao);

		CommonViewerParam document = new CommonViewerParam();
		document.setObjectId("DOC-1");
		document.setObjectType("DOC");
		document.setWatermarkType("GENERAL");
		document.setRequestNo("REQ-1");
		document.setRequestType("DISTRIBUTION");
		document.setFileNo("FILE-1");
		CommonViewerParam drawing = new CommonViewerParam();
		drawing.setObjectId("DRAW-1");
		drawing.setObjectType("DRAWING");
		drawing.setWatermarkType("PROTECT");
		drawing.setRequestNo("REQ-2");
		drawing.setRequestType("DISTRIBUTION");
		drawing.setFileNo("FILE-2");

		CommonViewerParam parent = new CommonViewerParam();
		parent.setObjectId("DOC-1_DOC_GENERAL__DRAW-1_DRAWING_PROTECT");
		parent.setList(Arrays.asList(document, drawing));

		CommonViewerVO response = service.getMergePrintInfo(parent);

		assertFalse(response.isSuccess());
		assertEquals(CommonViewerService.MERGE_PRINT_SECURITY_REASON,
				response.getFailType());
		assertNull(response.getFilePath());
		assertNull(response.getPrintJobId());
		assertNull(response.getWatermarkInfo());

		ArgumentCaptor<FileAccessRequest> accessCaptor =
				ArgumentCaptor.forClass(FileAccessRequest.class);
		verify(aclService, times(2)).requireAccess(accessCaptor.capture());
		List<FileAccessRequest> accessRequests = accessCaptor.getAllValues();
		assertEquals("DOC-1", accessRequests.get(0).getObjectId());
		assertEquals("FILE-1", accessRequests.get(0).getFileNo());
		assertEquals("REQ-1", accessRequests.get(0).getRequestNo());
		assertEquals("DRAW-1", accessRequests.get(1).getObjectId());
		assertEquals("FILE-2", accessRequests.get(1).getFileNo());
		assertEquals("REQ-2", accessRequests.get(1).getRequestNo());
		verifyNoInteractions(auditService, pdfDao);
	}

	@Test
	void mergedPrintSourceContainsNoStaticMergeReleaseOrAuditStart() throws Exception {
		String source = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(
				"src/main/java/kr/esob/fdms/commonlogic/viewer/CommonViewerService.java")),
				java.nio.charset.StandardCharsets.UTF_8);
		int methodStart = source.indexOf("public CommonViewerVO getMergePrintInfo");
		int methodEnd = source.indexOf("private int requestPathConvertApi", methodStart);
		String body = source.substring(methodStart, methodEnd);

		int aclLoop = body.indexOf("for (CommonViewerParam printItem : printItems)");
		int rejection = body.indexOf("rejectUnsafeMergePrint(printItems)", aclLoop);
		assertTrue(aclLoop >= 0);
		assertTrue(aclLoop < rejection);
		assertFalse(body.contains("/out/mergefile"));
		assertFalse(body.contains("setFilePath(strMergedirUrl)"));
		assertFalse(body.contains("printAuditService.start(printItems)"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void mergedPrintBuildsOneCanonicalItemSetAndRejectsRepresentationMismatch() throws Exception {
		CommonViewerParam parent = new CommonViewerParam();
		parent.setObjectId("DOC-1_DOC_GENERAL");
		parent.setRequestType("DISTRIBUTION");

		CommonViewerParam listed = new CommonViewerParam();
		listed.setObjectId("DOC-1");
		listed.setObjectType("DOC");
		listed.setWatermarkType("GENERAL");
		listed.setRequestNo("REQ-1");
		listed.setRequestType("DISTRIBUTION");
		listed.setFileNo("FILE-1");
		parent.setList(Collections.singletonList(listed));

		Method builder = CommonViewerService.class.getDeclaredMethod(
				"buildMergePrintItems", CommonViewerParam.class);
		builder.setAccessible(true);
		List<CommonViewerParam> canonical = (List<CommonViewerParam>) builder.invoke(
				new CommonViewerService(), parent);

		assertEquals(1, canonical.size());
		assertEquals("DOC-1", canonical.get(0).getObjectId());
		assertEquals("FILE-1", canonical.get(0).getFileNo());

		listed.setObjectId("OTHER-DOC");
		assertThrows(InvocationTargetException.class,
				() -> builder.invoke(new CommonViewerService(), parent));

	}

	private int count(String value, String token) {
		int result = 0;
		int offset = 0;
		while ((offset = value.indexOf(token, offset)) >= 0) {
			result++;
			offset += token.length();
		}
		return result;
	}
}

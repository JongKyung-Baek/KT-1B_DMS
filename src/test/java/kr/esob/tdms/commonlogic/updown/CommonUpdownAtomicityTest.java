package kr.esob.tdms.commonlogic.updown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.tdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.controller.login.UserVO;
import net.sf.json.JSONObject;

class CommonUpdownAtomicityTest {

	@Test
	void transferMustReportSuccessAndAConcreteFileName() {
		JSONObject successful = new JSONObject();
		successful.put("result", "true");
		successful.put("fileNm", " ticket-name ");
		assertEquals("ticket-name",
				CommonUpdownService.requireSuccessfulTransfer(successful));

		JSONObject failed = new JSONObject();
		failed.put("result", false);
		failed.put("fileNm", "ticket-name");
		assertThrows(IllegalStateException.class,
				() -> CommonUpdownService.requireSuccessfulTransfer(failed));

		JSONObject missingFile = new JSONObject();
		missingFile.put("result", true);
		assertThrows(IllegalStateException.class,
				() -> CommonUpdownService.requireSuccessfulTransfer(missingFile));
	}

	@Test
	void restBackedMetadataCannotBeReturnedWithoutDurablePreparedAudit() {
		CommonUpdownDao dao = mock(CommonUpdownDao.class);
		SecurityAclService acl = mock(SecurityAclService.class);
		CommonUpdownService service = new CommonUpdownService();
		ReflectionTestUtils.setField(service, "dao", dao);
		ReflectionTestUtils.setField(service, "securityAclService", acl);

		UserVO actor = new UserVO();
		actor.setUserCd("ACTOR");
		when(acl.requireCurrentUser()).thenReturn(actor);
		when(acl.normalizeObjectType("DOCUMENT")).thenReturn("DOCUMENT");

		CommonUpdownParam item = new CommonUpdownParam();
		item.setRequestNo("REQ-1");
		item.setObjectId("OBJ-1");
		item.setFileNo("1");
		CommonUpdownParam request = new CommonUpdownParam();
		request.setReqType("DISTRIBUTION");
		request.setList(Collections.singletonList(item));

		CommonUpdownFileVO resource = new CommonUpdownFileVO();
		resource.setRequestNo("REQ-1");
		resource.setObjectId("OBJ-1");
		resource.setDocSeq("OBJ-1");
		resource.setFileNo("1");
		resource.setObjectType("DOCUMENT");
		when(dao.selectDownloadResource("REQ-1", "OBJ-1", "1")).thenReturn(resource);
		when(dao.countDownloadBusinessAccess("REQ-1", "OBJ-1", "1", "ACTOR"))
				.thenReturn(1);

		List<?> result = service.selectList(request);

		assertEquals(1, result.size());
		verify(acl).requireAccess(any(FileAccessRequest.class));
		verify(acl).recordDownloadResult(actor, "PREPARED", null,
				"DOCUMENT", "OBJ-1", "1", "REQ-1",
				"REST-backed download metadata prepared.");
		verify(dao, never()).plusDownloadCount(
				any(String.class), any(String.class), any(String.class));
		verify(dao, never()).addToDownHistory(
				any(String.class), any(String.class), any(Integer.class),
				any(), any(), any(), any(), any());
	}

	@Test
	void disabledLegacyTransferCannotReturnBeforeFailureAudit() throws Exception {
		Transactional transactional = CommonUpdownService.class
				.getMethod("selectList", Object.class)
				.getAnnotation(Transactional.class);
		assertNotNull(transactional);
		assertEquals(Exception.class, transactional.rollbackFor()[0]);

		String source = read(
				"src/main/java/kr/esob/tdms/commonlogic/updown/CommonUpdownService.java");
		int call = source.indexOf("prepareLegacyDownload(param");
		int responseAdd = source.indexOf("response.add(resource);", call);
		assertTrue(call >= 0 && responseAdd > call);

		int method = source.indexOf("private void prepareLegacyDownload(");
		int failureAudit = source.indexOf(
				"securityAclService.recordDownloadResult(actor, \"FAIL\"", method);
		int disabled = source.indexOf("throw new UnsupportedOperationException(",
				failureAudit);
		assertTrue(method >= 0 && failureAudit > method && disabled > failureAudit);
		assertFalse(source.substring(method).contains("dao.addToDownHistory("));
	}

	@Test
	void downloadCounterSqlIsAtomicAndRaceSafe() throws Exception {

		String mapper = read(
				"src/main/resources/sqlMaps/oracle/its/commonlogic/updown/CommonUpdown.xml");
		int counterUpdate = mapper.indexOf("<update id=\"plusDownloadCount\"");
		int counterEnd = mapper.indexOf("</update>", counterUpdate);
		String counterSql = mapper.substring(counterUpdate, counterEnd);
		assertTrue(counterSql.contains("REQUEST_NO = #{requestNo}"));
		assertTrue(counterSql.contains("OBJECT_ID = #{objectId}"));
		assertTrue(counterSql.contains("FILE_NO::text = #{fileNo}"));
		assertTrue(counterSql.contains("COALESCE(DOWNLOAD_COUNT, 0) &lt; 3"));
	}

	private String read(String path) throws Exception {
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}
}

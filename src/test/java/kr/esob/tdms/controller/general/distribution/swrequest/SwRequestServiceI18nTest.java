package kr.esob.tdms.controller.general.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import kr.esob.tdms.commonlogic.message.Prop;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.general.distribution.approvaldetail.DistributionApprovalDetailDao;
import kr.esob.tdms.controller.login.UserVO;

class SwRequestServiceI18nTest {

	@AfterEach
	void resetLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	void mainFileValidationUsesActualMultipartNameAndRequestLocale() throws Exception {
		SwRequestService service = serviceWithMessages(mock(SwRequestDao.class));
		MockMultipartHttpServletRequest request = registrationRequest("malware.exe");
		request.setParameter("orgFileNm", "spoofed.pdf");

		LocaleContextHolder.setLocale(Locale.KOREAN);
		assertEquals("허용되지 않는 주파일 형식입니다: malware.exe",
				service.saveSwRegisterFileX2(request).getMessage());

		LocaleContextHolder.setLocale(Locale.ENGLISH);
		assertEquals("The main file type is not allowed: malware.exe",
				service.saveSwRegisterFileX2(request).getMessage());
	}

	@Test
	void supportingFileValidationPreservesFileNameInLocalizedMessage() {
		SwRequestService service = serviceWithMessages(mock(SwRequestDao.class));
		List<MultipartFile> files = Arrays.asList(
				new MockMultipartFile("subFiles", "run.cmd", "application/octet-stream", new byte[] { 1 }));

		LocaleContextHolder.setLocale(Locale.ENGLISH);
		IllegalArgumentException error = assertThrows(
				IllegalArgumentException.class,
				() -> ReflectionTestUtils.invokeMethod(service, "saveSwSubFiles", files, "DOC-1", "TYPE"));

		assertEquals("The supporting file type is not allowed: run.cmd", error.getMessage());
	}

	@Test
	void withdrawalAndApprovalErrorsUseCurrentRequestLocale() {
		SwRequestDao dao = mock(SwRequestDao.class);
		when(dao.selectSwApprovalInfo("DOC-1")).thenReturn(document("deletedYn", "Y"));
		SwRequestService service = serviceWithMessages(dao);
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		ResultVO withdrawal = service.validateDeleteSW("", new UserVO());
		ResultVO approval = service.validateApproveSW("DOC-1", new UserVO());

		assertEquals("Select an item to withdraw.", withdrawal.getMessage());
		assertEquals("A deleted document cannot be approved.", approval.getMessage());
	}

	@Test
	void approvalStatusKeepsApproverDetailAndLocalizesOnlyDisplayState() {
		SwRequestDao dao = mock(SwRequestDao.class);
		Map<String, Object> info = new HashMap<>();
		info.put("approver", "alice");
		info.put("status", "승인진행중");
		when(dao.selectSwApprovalInfo("DOC-1")).thenReturn(info);
		SwRequestService service = serviceWithMessages(dao);

		LocaleContextHolder.setLocale(Locale.KOREAN);
		assertEquals("alice : 미승인", service.getApprovalStatusMessage("DOC-1"));

		LocaleContextHolder.setLocale(Locale.ENGLISH);
		assertEquals("alice : Not Approved", service.getApprovalStatusMessage("DOC-1"));
	}

	@Test
	void blankApprovalCommentStoresLocalizedDefaultAndReturnsLocalizedResult() {
		SwRequestDao dao = mock(SwRequestDao.class);
		Map<String, Object> info = new HashMap<>();
		info.put("approver", "alice");
		info.put("status", "승인완료");
		when(dao.selectSwApprovalInfo("DOC-1")).thenReturn(info);
		when(dao.upsertApprovalComment(anyMap())).thenReturn(1);
		SwRequestService service = serviceWithMessages(dao);
		UserVO user = new UserVO();
		user.setUserCd("alice");
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		ResultVO result = service.saveApprovalComment("DOC-1", "", user);

		ArgumentCaptor<Map<String, Object>> paramCaptor = ArgumentCaptor.forClass(Map.class);
		org.mockito.Mockito.verify(dao).upsertApprovalComment(paramCaptor.capture());
		assertEquals("Approved.", paramCaptor.getValue().get("comment"));
		assertEquals("Comment saved.", result.getMessage());
	}

	private MockMultipartHttpServletRequest registrationRequest(String originalFileName) {
		MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
		request.addFile(new MockMultipartFile(
				"file",
				originalFileName,
				"application/octet-stream",
				new byte[] { 1, 2, 3 }));
		request.setParameter("orgFileNm", originalFileName);
		request.setParameter("fileName", "drawing");
		request.setParameter("swTypeCd", "");
		request.setParameter("reviewerUser", "");
		return request;
	}

	private Map<String, Object> document(String key, Object value) {
		Map<String, Object> document = new HashMap<>();
		document.put(key, value);
		return document;
	}

	private SwRequestService serviceWithMessages(SwRequestDao dao) {
		StaticMessageSource messages = new StaticMessageSource();
		addMessages(messages, Locale.KOREAN, koreanMessages());
		addMessages(messages, Locale.ENGLISH, englishMessages());

		Prop prop = new Prop();
		prop.setMessageSource(messages);

		SwRequestService service = new SwRequestService();
		ReflectionTestUtils.setField(service, "dao", dao);
		ReflectionTestUtils.setField(service, "approvalDetailDao", mock(DistributionApprovalDetailDao.class));
		ReflectionTestUtils.setField(service, "prop", prop);
		return service;
	}

	private void addMessages(StaticMessageSource source, Locale locale, Map<String, String> messages) {
		for (Map.Entry<String, String> message : messages.entrySet()) {
			source.addMessage(message.getKey(), locale, message.getValue());
		}
	}

	private Map<String, String> koreanMessages() {
		Map<String, String> messages = new HashMap<>();
		messages.put("feature.techRegister.validation.unsupportedFileType", "허용되지 않는 주파일 형식입니다: {0}");
		messages.put("feature.techRegister.validation.unsupportedSupportingFileType", "허용되지 않는 보조파일 형식입니다: {0}");
		messages.put("feature.techList.withdraw.selectionRequired", "철회할 대상을 선택하세요.");
		messages.put("feature.techList.approval.deletedDocument", "삭제된 문서는 승인할 수 없습니다.");
		messages.put("feature.techList.approval.status.notApproved", "미승인");
		messages.put("feature.techList.approval.comment.default", "승인하였습니다.");
		messages.put("feature.techList.approval.comment.saved", "코멘트가 저장되었습니다.");
		return messages;
	}

	private Map<String, String> englishMessages() {
		Map<String, String> messages = new HashMap<>();
		messages.put("feature.techRegister.validation.unsupportedFileType", "The main file type is not allowed: {0}");
		messages.put(
				"feature.techRegister.validation.unsupportedSupportingFileType",
				"The supporting file type is not allowed: {0}");
		messages.put("feature.techList.withdraw.selectionRequired", "Select an item to withdraw.");
		messages.put(
				"feature.techList.approval.deletedDocument",
				"A deleted document cannot be approved.");
		messages.put("feature.techList.approval.status.notApproved", "Not Approved");
		messages.put("feature.techList.approval.comment.default", "Approved.");
		messages.put("feature.techList.approval.comment.saved", "Comment saved.");
		return messages;
	}
}

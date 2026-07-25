package kr.esob.fdms.controller.outside.cr.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.value.CrStatusCdInfo;
import kr.esob.fdms.controller.login.UserVO;

class RequestApprovalAuthorizationTest {

	private RequestDao dao;
	private RequestService service;
	private UserVO actor;

	@BeforeEach
	void setUp() {
		dao = mock(RequestDao.class);
		service = new RequestService();
		ReflectionTestUtils.setField(service, "dao", dao);

		actor = new UserVO();
		actor.setUserCd("CURRENT_VENDOR_APPROVER");
		actor.setCompanyCd("VENDOR-COMPANY");
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(
						actor, null, Collections.emptyList()));
	}

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void missingLockedTargetFailsClosedAndClearsClientControlledState() {
		OutsideCrParam request = request(" CR-OTHER ");
		request.setSessionUser(maliciousActor());
		request.setActualUserCd("CLIENT_ACTOR");
		request.setRequestNo("CLIENT_REQUEST");
		request.setCurrentProcessSeqNo(99);
		request.setFilePathNmList(Arrays.asList("CLIENT_FILE"));
		when(dao.selectApprovalTargetForUpdate(request)).thenReturn(null);

		assertThrows(AccessDeniedException.class, () -> service.approve(request));

		assertEquals("CR-OTHER", request.getCrNo());
		assertEquals(actor, request.getSessionUser());
		assertEquals(actor.getUserCd(), request.getActualUserCd());
		assertNull(request.getRequestNo());
		assertNull(request.getFilePathNmList());
		assertEquals(0, request.getCurrentProcessSeqNo());
		verify(dao).selectApprovalTargetForUpdate(request);
		verify(dao, never()).updateRequestDetail(request);
	}

	@Test
	void approvalUsesLockedRequestAndServerLifecycleValues() {
		OutsideCrParam request = request("CR-1");
		request.setSessionUser(maliciousActor());
		request.setRequestNo("CLIENT_REQUEST");
		request.setActionCd("CLIENT_ACTION");
		request.setReqStatusCd("CLIENT_STATUS");
		request.setCurrentProcessSeqNo(99);

		when(dao.selectApprovalTargetForUpdate(request)).thenReturn("REQ-LOCKED");
		when(dao.updateRequestDetail(request)).thenReturn(1);
		when(dao.updateRequest(request)).thenReturn(1);
		when(dao.updateCr(request)).thenReturn(1);

		ResultVO result = service.approve(request);

		assertTrue(result.isSuccess());
		assertEquals(actor, request.getSessionUser());
		assertEquals(actor.getUserCd(), request.getActualUserCd());
		assertEquals("REQ-LOCKED", request.getRequestNo());
		assertEquals("APPROVAL", request.getActionCd());
		assertEquals("REQUEST", request.getReqStatusCd());
		assertEquals("WAITING", request.getApprovalStatusCd());
		assertEquals(3, request.getCurrentProcessSeqNo());
		assertEquals(CrStatusCdInfo.VENDOR_APPROVAL, request.getStatusCd());
		assertNull(request.getRejectDesc());

		InOrder order = inOrder(dao);
		order.verify(dao).selectApprovalTargetForUpdate(request);
		order.verify(dao).updateRequestDetail(request);
		order.verify(dao).updateRequest(request);
		order.verify(dao).updateCr(request);
	}

	@Test
	void rejectionKeepsProcessTwoAndStopsOnUnexpectedAffectedRows() {
		OutsideCrParam request = request("CR-1");
		request.setRejectDesc("business reason");
		request.setCurrentProcessSeqNo(99);

		when(dao.selectApprovalTargetForUpdate(request)).thenReturn("REQ-LOCKED");
		when(dao.updateRequestDetail(request)).thenReturn(1);
		when(dao.updateRequest(request)).thenReturn(0);

		assertThrows(IllegalStateException.class, () -> service.approvalReject(request));

		assertEquals(actor, request.getSessionUser());
		assertEquals("REJECT", request.getActionCd());
		assertEquals("REJECT", request.getReqStatusCd());
		assertEquals("WAITING", request.getApprovalStatusCd());
		assertEquals(2, request.getCurrentProcessSeqNo());
		assertEquals(CrStatusCdInfo.VENDOR_REJECT, request.getStatusCd());
		verify(dao, never()).updateCr(request);
	}

	@Test
	void listScopeAlsoOverwritesClientSessionUser() {
		RequestListParam request = new RequestListParam();
		request.setSessionUser(maliciousActor());
		when(dao.selectList(request)).thenReturn(Collections.emptyList());

		service.selectList(request);

		assertEquals(actor, request.getSessionUser());
		verify(dao).selectList(request);
	}

	@Test
	void transactionsPostRoutesAndMapperLockExactWaitingActor() throws Exception {
		Transactional approvalTransaction = RequestService.class
				.getMethod("approve", OutsideCrParam.class)
				.getAnnotation(Transactional.class);
		assertNotNull(approvalTransaction);
		assertEquals(Exception.class, approvalTransaction.rollbackFor()[0]);

		Transactional rejectTransaction = RequestService.class
				.getMethod("approvalReject", OutsideCrParam.class)
				.getAnnotation(Transactional.class);
		assertNotNull(rejectTransaction);
		assertEquals(Exception.class, rejectTransaction.rollbackFor()[0]);

		assertNotNull(RequestController.class
				.getMethod("approve", OutsideCrParam.class)
				.getAnnotation(PostMapping.class));
		assertNotNull(RequestController.class
				.getMethod("approvalReject", OutsideCrParam.class)
				.getAnnotation(PostMapping.class));

		assertNotNull(OutsideCrParam.class.getDeclaredField("requestNo")
				.getAnnotation(JsonIgnore.class));
		assertNotNull(OutsideCrParam.class.getDeclaredField("currentProcessSeqNo")
				.getAnnotation(JsonIgnore.class));
		assertNotNull(OutsideCrParam.class.getDeclaredField("filePathNmList")
				.getAnnotation(JsonIgnore.class));

		String mapper = read(
				"src/main/resources/sqlMaps/oracle/its/controller/outside/cr/request/CrRequest.xml");
		int securedSectionStart = mapper.indexOf(
				"<select id=\"selectApprovalTargetForUpdate\"");
		assertTrue(securedSectionStart >= 0);
		String securedSection = mapper.substring(securedSectionStart);

		assertTrue(securedSection.contains("FOR UPDATE OF cr, req, currDetail"));
		assertTrue(securedSection.contains("cr.STATUS_CD = 10"));
		assertTrue(securedSection.contains("req.REQUEST_TYPE = 'CR'"));
		assertTrue(securedSection.contains("req.STATUS_CD = 'WAITING'"));
		assertTrue(securedSection.contains("req.CURRENT_PROCESS_SEQ_NO = 2"));
		assertTrue(securedSection.contains(
				"currDetail.PROCESS_SEQ = req.CURRENT_PROCESS_SEQ_NO"));
		assertTrue(securedSection.contains(
				"currDetail.APPROVAL_STATUS_CD = 'WAITING'"));
		assertTrue(securedSection.contains(
				"currDetail.ACTUAL_USER_CD = actor.USER_CD"));
		assertTrue(securedSection.contains("currDetail.ACTION_CD = 'REQUEST'"));
		assertTrue(securedSection.contains(
				"actor.COMPANY_CD = req.DEPLOY_COMPANY_CD"));
		assertTrue(securedSection.contains(
				"actor.COMPANY_CD = vendorUser.COMPANY_CD"));
		assertFalse(securedSection.contains("#{objectNo}"));
		assertFalse(securedSection.contains("#{deployUserCd}"));
		assertFalse(securedSection.contains("<foreach"));
	}

	private OutsideCrParam request(String crNo) {
		OutsideCrParam request = new OutsideCrParam();
		request.setCrNo(crNo);
		return request;
	}

	private UserVO maliciousActor() {
		UserVO malicious = new UserVO();
		malicious.setUserCd("CLIENT_SUPPLIED");
		malicious.setCompanyCd("OTHER-COMPANY");
		return malicious;
	}

	private String read(String path) throws Exception {
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}
}

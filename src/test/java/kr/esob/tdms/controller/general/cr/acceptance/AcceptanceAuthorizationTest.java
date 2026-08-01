package kr.esob.tdms.controller.general.cr.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import java.util.Collections;

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

import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.commonlogic.value.CrStatusCdInfo;
import kr.esob.tdms.controller.general.cr.CrParam;
import kr.esob.tdms.controller.login.UserVO;

class AcceptanceAuthorizationTest {

	private AcceptanceDao dao;
	private AcceptanceService service;
	private UserVO actor;

	@BeforeEach
	void setUp() {
		dao = mock(AcceptanceDao.class);
		service = new AcceptanceService();
		ReflectionTestUtils.setField(service, "dao", dao);

		actor = new UserVO();
		actor.setUserCd("CURRENT_ACCEPTOR");
		actor.setTeamLeaderUid("ORIGINAL_TEAM_LEADER");
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(
						actor, null, Collections.emptyList()));
	}

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void missingLockedTargetFailsClosedAndOverwritesClientActor() {
		CrParam request = request(" CR-OTHER ");
		request.setSessionUser(maliciousActor());
		request.setActualUserCd("CLIENT_ACTOR");
		request.setCurrentProcessSeqNo(99);
		request.setApprovalUser("NEXT_APPROVER");
		when(dao.selectAcceptanceTargetForUpdate(request)).thenReturn(null);

		assertThrows(AccessDeniedException.class, () -> service.approvalRequest(request));

		assertEquals("CR-OTHER", request.getCrNo());
		assertEquals(actor, request.getSessionUser());
		assertEquals(actor.getUserCd(), request.getActualUserCd());
		verify(dao).selectAcceptanceTargetForUpdate(request);
		verify(dao, never()).updateAcceptance(request);
	}

	@Test
	void approvalUsesServerActorAndStageAndRequiresEveryMutationRow() {
		CrParam request = request("CR-1");
		request.setSessionUser(maliciousActor());
		request.setActualUserCd("CLIENT_ACTOR");
		request.setActionCd("CLIENT_ACTION");
		request.setCurrentProcessSeqNo(99);
		request.setApprovalUser("NEXT_APPROVER");

		when(dao.selectAcceptanceTargetForUpdate(request)).thenReturn("REQ-1");
		when(dao.updateAcceptance(request)).thenReturn(1);
		when(dao.updateApproval(request)).thenReturn(1);
		when(dao.updateRequest(request)).thenReturn(1);
		when(dao.updateCr(request)).thenReturn(1);

		ResultVO result = service.approvalRequest(request);

		assertTrue(result.isSuccess());
		assertEquals(actor, request.getSessionUser());
		assertEquals(actor.getUserCd(), request.getActualUserCd());
		assertEquals("ACCEPT", request.getActionCd());
		assertEquals("ACCEPT", request.getApprovalStatusCd());
		assertEquals("TL", request.getApprovalGradeCd());
		assertEquals(4, request.getCurrentProcessSeqNo());
		assertEquals(CrStatusCdInfo.PURCHASER_ACCEPT, request.getStatusCd());

		InOrder order = inOrder(dao);
		order.verify(dao).selectAcceptanceTargetForUpdate(request);
		order.verify(dao).updateAcceptance(request);
		order.verify(dao).updateApproval(request);
		order.verify(dao).updateRequest(request);
		order.verify(dao).updateCr(request);
	}

	@Test
	void rejectionKeepsServerCurrentStageAndStopsOnUnexpectedRowCount() {
		CrParam request = request("CR-1");
		request.setSessionUser(maliciousActor());
		request.setCurrentProcessSeqNo(99);

		when(dao.selectAcceptanceTargetForUpdate(request)).thenReturn("REQ-1");
		when(dao.updateAcceptance(request)).thenReturn(1);
		when(dao.updateRequest(request)).thenReturn(0);

		assertThrows(IllegalStateException.class, () -> service.acceptanceReject(request));

		assertEquals(actor, request.getSessionUser());
		assertEquals("REJECT", request.getActionCd());
		assertEquals(3, request.getCurrentProcessSeqNo());
		assertEquals(CrStatusCdInfo.PURCHASER_REJECT, request.getStatusCd());
		verify(dao, never()).updateApproval(request);
		verify(dao, never()).updateCr(request);
	}

	@Test
	void transactionPostRoutesAndSqlLockCurrentPendingAcceptor() throws Exception {
		Transactional approvalTransaction = AcceptanceService.class
				.getMethod("approvalRequest", CrParam.class)
				.getAnnotation(Transactional.class);
		assertNotNull(approvalTransaction);
		assertEquals(Exception.class, approvalTransaction.rollbackFor()[0]);

		Transactional rejectTransaction = AcceptanceService.class
				.getMethod("acceptanceReject", CrParam.class)
				.getAnnotation(Transactional.class);
		assertNotNull(rejectTransaction);
		assertEquals(Exception.class, rejectTransaction.rollbackFor()[0]);

		assertNotNull(AcceptanceController.class
				.getMethod("approvalRequest", CrParam.class)
				.getAnnotation(PostMapping.class));
		assertNotNull(AcceptanceController.class
				.getMethod("acceptanceReject", CrParam.class)
				.getAnnotation(PostMapping.class));

		String mapper = read(
				"src/main/resources/sqlMaps/oracle/its/controller/general/cr/acceptance/Acceptance.xml");
		assertTrue(mapper.contains("selectAcceptanceTargetForUpdate"));
		assertTrue(mapper.contains("FOR UPDATE OF cr, req, currDetail"));
		assertTrue(mapper.contains("cr.STATUS_CD = 20"));
		assertTrue(mapper.contains("req.REQUEST_TYPE = 'CR'"));
		assertTrue(mapper.contains("req.STATUS_CD = 'REQUEST'"));
		assertTrue(mapper.contains("req.CURRENT_PROCESS_SEQ_NO = 3"));
		assertTrue(mapper.contains(
				"currDetail.PROCESS_SEQ = req.CURRENT_PROCESS_SEQ_NO"));
		assertTrue(mapper.contains(
				"currDetail.APPROVAL_STATUS_CD = 'ACCEPT'"));
		assertTrue(mapper.contains(
				"currDetail.ACTUAL_USER_CD = #{sessionUser.userCd}"));
		assertTrue(mapper.contains("currDetail.ACTION_CD = 'REQUEST'"));
	}

	private CrParam request(String crNo) {
		CrParam request = new CrParam();
		request.setCrNo(crNo);
		return request;
	}

	private UserVO maliciousActor() {
		UserVO malicious = new UserVO();
		malicious.setUserCd("CLIENT_SUPPLIED");
		return malicious;
	}

	private String read(String path) throws Exception {
		return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}
}

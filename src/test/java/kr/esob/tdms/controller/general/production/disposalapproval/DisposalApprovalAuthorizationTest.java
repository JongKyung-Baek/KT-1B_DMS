package kr.esob.tdms.controller.general.production.disposalapproval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.controller.login.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

class DisposalApprovalAuthorizationTest {

	private DisposalApprovalDao dao;
	private SecurityAclService securityAclService;
	private DisposalApprovalService service;
	private UserVO actor;

	@BeforeEach
	void setUp() {
		dao = mock(DisposalApprovalDao.class);
		securityAclService = mock(SecurityAclService.class);
		service = new DisposalApprovalService();
		ReflectionTestUtils.setField(service, "dao", dao);
		ReflectionTestUtils.setField(service, "securityAclService", securityAclService);

		actor = new UserVO();
		actor.setUserCd("CURRENT_APPROVER");
		when(securityAclService.requireCurrentUser()).thenReturn(actor);
	}

	@Test
	void missingLockedTargetFailsClosedAndOverwritesClientActor() {
		DisposalApprovalPopupParam request = request(" DESTROY-OTHER ", "R");
		request.setSessionUser(maliciousActor());
		when(dao.selectApprovalTargetForUpdate(request)).thenReturn(null);

		assertThrows(AccessDeniedException.class, () -> service.destroyApproval(request));

		assertEquals(actor, request.getSessionUser());
		assertEquals("DESTROY-OTHER", request.getDestroyRequestNo());
		verify(dao).selectApprovalTargetForUpdate(request);
		verify(dao, never()).selectDestroyList(request);
		verify(dao, never()).updateDestroyRequestInfo(request);
		verify(dao, never()).updateDestroyRequestDetail(request);
	}

	@Test
	void approvalUsesLockedMetadataAndRequiresEveryMutationRow() {
		DisposalApprovalPopupParam request = request("DESTROY-1", "A");
		request.setApprovalStatusCd("CLIENT_STATUS");
		request.setApprovalGradeCd("CLIENT_GRADE");
		DisposalApprovalPopupParam lockedTarget = lockedTarget();
		when(dao.selectApprovalTargetForUpdate(request)).thenReturn(lockedTarget);
		when(dao.selectDestroyList(request)).thenReturn(Collections.singletonList(item()));
		when(dao.updateDestroyCount(any(DisposalApprovalPopupParam.class))).thenReturn(1);
		when(dao.deleteProductStatus(any(DisposalApprovalPopupParam.class))).thenReturn(1);
		when(dao.updateDestroyRequestInfo(request)).thenReturn(1);
		when(dao.updateDestroyRequestDetail(request)).thenReturn(1);

		ResultVO result = service.destroyApproval(request);

		assertTrue(result.isSuccess());
		assertEquals(actor, request.getSessionUser());
		assertEquals("APPROVAL", request.getApprovalStatusCd());
		assertEquals("TL", request.getApprovalGradeCd());
		assertEquals("APPROVAL", request.getStatusCd());
		assertEquals("APPROVAL", request.getActionCd());

		ArgumentCaptor<DisposalApprovalPopupParam> itemCaptor =
				ArgumentCaptor.forClass(DisposalApprovalPopupParam.class);
		verify(dao).updateDestroyCount(itemCaptor.capture());
		DisposalApprovalPopupParam serverItem = itemCaptor.getValue();
		assertEquals(actor, serverItem.getSessionUser());
		assertEquals("DESTROY-1", serverItem.getDestroyRequestNo());
		assertEquals("APPROVAL", serverItem.getApprovalStatusCd());
		assertEquals("TL", serverItem.getApprovalGradeCd());

		InOrder order = inOrder(securityAclService, dao);
		order.verify(securityAclService).requireCurrentUser();
		order.verify(dao).selectApprovalTargetForUpdate(request);
		order.verify(dao).selectDestroyList(request);
		order.verify(dao).updateDestroyCount(any(DisposalApprovalPopupParam.class));
		order.verify(dao).deleteProductStatus(any(DisposalApprovalPopupParam.class));
		order.verify(dao).updateDestroyRequestInfo(request);
		order.verify(dao).updateDestroyRequestDetail(request);
	}

	@Test
	void unexpectedChildAffectedRowsStopsBeforeApprovalStateChange() {
		DisposalApprovalPopupParam request = request("DESTROY-1", "A");
		when(dao.selectApprovalTargetForUpdate(request)).thenReturn(lockedTarget());
		when(dao.selectDestroyList(request)).thenReturn(Collections.singletonList(item()));
		when(dao.updateDestroyCount(any(DisposalApprovalPopupParam.class))).thenReturn(0);

		assertThrows(IllegalStateException.class, () -> service.destroyApproval(request));

		verify(dao, never()).deleteProductStatus(any(DisposalApprovalPopupParam.class));
		verify(dao, never()).updateDestroyRequestInfo(request);
		verify(dao, never()).updateDestroyRequestDetail(request);
	}

	@Test
	void unexpectedRequestAffectedRowsStopsBeforeCurrentDetailChange() {
		DisposalApprovalPopupParam request = request("DESTROY-1", "A");
		when(dao.selectApprovalTargetForUpdate(request)).thenReturn(lockedTarget());
		when(dao.selectDestroyList(request)).thenReturn(Collections.singletonList(item()));
		when(dao.updateDestroyCount(any(DisposalApprovalPopupParam.class))).thenReturn(1);
		when(dao.deleteProductStatus(any(DisposalApprovalPopupParam.class))).thenReturn(1);
		when(dao.updateDestroyRequestInfo(request)).thenReturn(0);

		assertThrows(IllegalStateException.class, () -> service.destroyApproval(request));

		verify(dao, never()).updateDestroyRequestDetail(request);
	}

	@Test
	void rejectionRequiresOneProductStatusRequestAndCurrentDetailRow() {
		DisposalApprovalPopupParam request = request("DESTROY-1", "R");
		when(dao.selectApprovalTargetForUpdate(request)).thenReturn(lockedTarget());
		when(dao.selectDestroyList(request)).thenReturn(Collections.singletonList(item()));
		when(dao.updateDisposalReject(any(DisposalApprovalPopupParam.class))).thenReturn(1);
		when(dao.updateDestroyRequestInfo(request)).thenReturn(1);
		when(dao.updateDestroyRequestDetail(request)).thenReturn(1);

		ResultVO result = service.destroyApproval(request);

		assertTrue(result.isSuccess());
		assertEquals("REJECT", request.getStatusCd());
		assertEquals("REJECT", request.getActionCd());
		verify(dao).updateDisposalReject(any(DisposalApprovalPopupParam.class));
		verify(dao).updateDestroyRequestInfo(request);
		verify(dao).updateDestroyRequestDetail(request);
	}

	@Test
	void transactionPostRouteAndSqlEnforceCurrentPendingActor() throws Exception {
		Transactional transactional = DisposalApprovalService.class
				.getMethod("destroyApproval", DisposalApprovalPopupParam.class)
				.getAnnotation(Transactional.class);
		assertNotNull(transactional);
		assertEquals(Exception.class, transactional.rollbackFor()[0]);

		PostMapping postMapping = DisposalApprovalController.class
				.getMethod("destroyApproval", DisposalApprovalPopupParam.class, Model.class)
				.getAnnotation(PostMapping.class);
		assertNotNull(postMapping);

		String mapper = read(
				"src/main/resources/sqlMaps/oracle/its/controller/general/production/disposalapproval/DisposalApproval.xml");
		assertTrue(mapper.contains("selectApprovalTargetForUpdate"));
		assertTrue(mapper.contains("FOR UPDATE OF destReq, currDetail"));
		assertTrue(mapper.contains("destReq.REQUEST_TYPE = 'PRODUCT'"));
		assertTrue(mapper.contains("destReq.STATUS_CD = 'REQUEST'"));
		assertTrue(mapper.contains("currDetail.PROCESS_SEQ = destReq.CURRENT_PROCESS_SEQ_NO"));
		assertTrue(mapper.contains("currDetail.ACTUAL_USER_CD = #{sessionUser.userCd}"));
		assertTrue(mapper.contains("currDetail.ACTION_CD = 'REQUEST'"));

		String creationMapper = read(
				"src/main/resources/sqlMaps/oracle/its/controller/general/production/disposal/Disposal.xml");
		int start = creationMapper.lastIndexOf("<insert id=\"insertDocsDestroyRequestDetail\"");
		int end = creationMapper.indexOf("</insert>", start);
		assertTrue(start >= 0 && end > start);
		String detailInsert = creationMapper.substring(start, end);
		assertTrue(detailInsert.matches("(?s).*,\\s*ACTION_CD\\s*<if.*"));
		assertTrue(detailInsert.matches("(?s).*,\\s*'REQUEST'\\s*<if.*"));
	}

	private DisposalApprovalPopupParam request(String destroyRequestNo, String saveType) {
		DisposalApprovalPopupParam request = new DisposalApprovalPopupParam();
		request.setDestroyRequestNo(destroyRequestNo);
		request.setSaveType(saveType);
		return request;
	}

	private DisposalApprovalPopupParam lockedTarget() {
		DisposalApprovalPopupParam target = new DisposalApprovalPopupParam();
		target.setDestroyRequestNo("DESTROY-1");
		target.setApprovalStatusCd("APPROVAL");
		target.setApprovalGradeCd("TL");
		return target;
	}

	private DisposalApprovalPopupVO item() {
		DisposalApprovalPopupVO item = new DisposalApprovalPopupVO();
		item.setRequestNo("REQUEST-1");
		item.setObjectId("OBJECT-1");
		item.setObjectNo("OBJECT-NO-1");
		item.setDeployUserCd("DEPLOY-USER");
		return item;
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

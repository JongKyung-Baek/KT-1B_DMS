package kr.esob.fdms.controller.inside.production.acceptance;

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

import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.production.approval.ApprovalDao;
import kr.esob.fdms.controller.inside.production.approval.ApprovalService;
import kr.esob.fdms.controller.inside.production.common.DeployInfoVO;
import kr.esob.fdms.controller.inside.production.common.ProductStatusVO;
import kr.esob.fdms.controller.login.UserVO;
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

class AcceptanceAuthorizationTest {

	private UserVO actor;

	@BeforeEach
	void authenticate() {
		actor = new UserVO();
		actor.setUserCd("CURRENT_ACCEPTOR");
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
		AcceptanceDao dao = mock(AcceptanceDao.class);
		ApprovalService approvalService = mock(ApprovalService.class);
		AcceptanceService service = acceptanceService(dao, approvalService);
		AcceptancePopupParam request = request(" REQ-OTHER ");
		request.setSessionUser(maliciousActor());
		when(dao.selectAcceptanceTargetForUpdate(request)).thenReturn(null);

		assertThrows(AccessDeniedException.class, () -> service.saveAcceptance(request));

		assertEquals(actor, request.getSessionUser());
		assertEquals("REQ-OTHER", request.getRequestNo());
		verify(dao).selectAcceptanceTargetForUpdate(request);
		verify(approvalService, never()).updateProductStatus(any(AcceptancePopupParam.class));
		verify(dao, never()).updateAcceptance(any(AcceptancePopupParam.class));
	}

	@Test
	void acceptanceIgnoresClientOwnershipAndRequiresOneRequestDeployRow() {
		AcceptanceDao dao = mock(AcceptanceDao.class);
		ApprovalService approvalService = mock(ApprovalService.class);
		AcceptanceService service = acceptanceService(dao, approvalService);
		AcceptancePopupParam request = request("REQ-1");
		request.setObjectType("CLIENT_TYPE");
		request.setDeployUserCd("CLIENT_USER");
		request.setList(Collections.singletonList(maliciousItem()));
		when(dao.selectAcceptanceTargetForUpdate(request)).thenReturn(lockedTarget("DOC"));
		when(dao.updateAcceptance(request)).thenReturn(1);

		ResultVO result = service.saveAcceptance(request);

		assertTrue(result.isSuccess());
		assertEquals(actor, request.getSessionUser());
		assertEquals("DOC", request.getObjectType());
		assertEquals(actor.getUserCd(), request.getDeployUserCd());
		assertTrue(request.getList().isEmpty());

		InOrder order = inOrder(dao, approvalService);
		order.verify(dao).selectAcceptanceTargetForUpdate(request);
		order.verify(approvalService).updateProductStatus(request);
		order.verify(dao).updateAcceptance(request);
	}

	@Test
	void unexpectedAcceptanceAffectedRowsRollsBack() {
		AcceptanceDao dao = mock(AcceptanceDao.class);
		ApprovalService approvalService = mock(ApprovalService.class);
		AcceptanceService service = acceptanceService(dao, approvalService);
		AcceptancePopupParam request = request("REQ-1");
		when(dao.selectAcceptanceTargetForUpdate(request)).thenReturn(lockedTarget("SW"));
		when(dao.updateAcceptance(request)).thenReturn(0);

		assertThrows(IllegalStateException.class, () -> service.saveAcceptance(request));

		verify(approvalService).updateProductStatus(request);
		verify(dao).updateAcceptance(request);
	}

	@Test
	void productStatusUsesServerDeploymentRowsAndAuthenticatedActor() {
		ApprovalDao dao = mock(ApprovalDao.class);
		ApprovalService service = approvalService(dao);
		AcceptancePopupParam request = request("REQ-1");
		request.setObjectType("DOC");
		request.setDeployUserCd("CLIENT_USER");
		request.setList(Collections.singletonList(maliciousItem()));
		DeployInfoVO deploy = deployInfo();
		ProductStatusVO status = new ProductStatusVO();
		status.setCurrentCount(7);
		when(dao.selectDeployInfoUserList(request))
				.thenReturn(Collections.singletonList(deploy));
		when(dao.selectProductionStatus(deploy)).thenReturn(status);
		when(dao.updateProductionStatus(status)).thenReturn(1);
		when(dao.updateDeployInfo(deploy)).thenReturn(1);

		service.updateProductStatus(request);

		assertEquals(actor, request.getSessionUser());
		assertEquals(actor.getUserCd(), request.getDeployUserCd());
		assertEquals("REQ-1", deploy.getRequestNo());
		assertEquals(actor, deploy.getSessionUser());
		assertEquals(12, status.getCurrentCount());
		assertEquals("REQ-1", status.getRequestNo());
		assertEquals("OBJECT-1", status.getObjectId());
		assertEquals("OBJECT-NO-1", status.getObjectNo());
		assertEquals("DEPT-1", status.getDeptCd());
		assertEquals(actor.getUserCd(), status.getUserCd());
		assertEquals(actor, status.getSessionUser());

		InOrder order = inOrder(dao);
		order.verify(dao).selectDeployInfoUserList(request);
		order.verify(dao).selectProductionStatus(deploy);
		order.verify(dao).updateProductionStatus(status);
		order.verify(dao).updateDeployInfo(deploy);
	}

	@Test
	void productMutationMismatchStopsBeforeDeployCompletion() {
		ApprovalDao dao = mock(ApprovalDao.class);
		ApprovalService service = approvalService(dao);
		AcceptancePopupParam request = request("REQ-1");
		request.setObjectType("DOC");
		DeployInfoVO deploy = deployInfo();
		ProductStatusVO status = new ProductStatusVO();
		when(dao.selectDeployInfoUserList(request))
				.thenReturn(Collections.singletonList(deploy));
		when(dao.selectProductionStatus(deploy)).thenReturn(status);
		when(dao.updateProductionStatus(status)).thenReturn(0);

		assertThrows(IllegalStateException.class, () -> service.updateProductStatus(request));

		verify(dao, never()).updateDeployInfo(any(DeployInfoVO.class));
	}

	@Test
	void deploymentMutationMustAffectExactlyOneRow() {
		ApprovalDao dao = mock(ApprovalDao.class);
		ApprovalService service = approvalService(dao);
		AcceptancePopupParam request = request("REQ-1");
		request.setObjectType("DOC");
		DeployInfoVO deploy = deployInfo();
		ProductStatusVO status = new ProductStatusVO();
		when(dao.selectDeployInfoUserList(request))
				.thenReturn(Collections.singletonList(deploy));
		when(dao.selectProductionStatus(deploy)).thenReturn(status);
		when(dao.updateProductionStatus(status)).thenReturn(1);
		when(dao.updateDeployInfo(deploy)).thenReturn(0);

		assertThrows(IllegalStateException.class, () -> service.updateProductStatus(request));
	}

	@Test
	void insertMismatchAndMissingDeploymentFailClosed() {
		ApprovalDao dao = mock(ApprovalDao.class);
		ApprovalService service = approvalService(dao);
		AcceptancePopupParam request = request("REQ-1");
		request.setObjectType("DOC");
		DeployInfoVO deploy = deployInfo();
		when(dao.selectDeployInfoUserList(request))
				.thenReturn(Collections.singletonList(deploy));
		when(dao.selectProductionStatus(deploy)).thenReturn(null);
		when(dao.insertProductionStatus(any(ProductStatusVO.class))).thenReturn(0);

		assertThrows(IllegalStateException.class, () -> service.updateProductStatus(request));
		verify(dao, never()).updateDeployInfo(any(DeployInfoVO.class));

		ApprovalDao emptyDao = mock(ApprovalDao.class);
		ApprovalService emptyService = approvalService(emptyDao);
		when(emptyDao.selectDeployInfoUserList(request)).thenReturn(Collections.emptyList());
		assertThrows(IllegalStateException.class, () -> emptyService.updateProductStatus(request));
	}

	@Test
	void transactionsPostRouteAndSqlEnforceApprovedPendingActor() throws Exception {
		Transactional acceptanceTx = AcceptanceService.class
				.getMethod("saveAcceptance", AcceptancePopupParam.class)
				.getAnnotation(Transactional.class);
		assertNotNull(acceptanceTx);
		assertEquals(Exception.class, acceptanceTx.rollbackFor()[0]);

		Transactional productTx = ApprovalService.class
				.getMethod("updateProductStatus", AcceptancePopupParam.class)
				.getAnnotation(Transactional.class);
		assertNotNull(productTx);
		assertEquals(Exception.class, productTx.rollbackFor()[0]);

		assertNotNull(AcceptanceController.class
				.getMethod("saveAcceptance", AcceptancePopupParam.class)
				.getAnnotation(PostMapping.class));

		String acceptanceMapper = read(
				"src/main/resources/sqlMaps/oracle/its/controller/inside/production/acceptance/Acceptance.xml");
		assertTrue(acceptanceMapper.contains("selectAcceptanceTargetForUpdate"));
		assertTrue(acceptanceMapper.contains("FOR UPDATE OF req, deploy, currDetail"));
		assertTrue(acceptanceMapper.contains("req.REQUEST_TYPE = 'PRODUCT'"));
		assertTrue(acceptanceMapper.contains("req.STATUS_CD = 'APPROVAL'"));
		assertTrue(acceptanceMapper.contains("deploy.DEPLOY_USER_CD = #{sessionUser.userCd}"));
		assertTrue(acceptanceMapper.contains("currDetail.PROCESS_SEQ = req.CURRENT_PROCESS_SEQ_NO"));
		assertTrue(acceptanceMapper.contains("currDetail.ACTION_CD = 'APPROVAL'"));
		assertTrue(acceptanceMapper.contains("FROM DOCS_DEPLOY_INFO pendingInfo"));

		String approvalMapper = read(
				"src/main/resources/sqlMaps/oracle/its/controller/inside/production/approval/Approval.xml");
		assertTrue(approvalMapper.contains("FOR UPDATE OF info"));
		assertTrue(approvalMapper.contains("FOR UPDATE OF status"));
		assertTrue(approvalMapper.contains("info.DEPLOY_USER_CD = #{sessionUser.userCd}"));
		assertTrue(approvalMapper.contains("info.DEPLOY_ACCEPT_YN = 'N'"));
		assertTrue(approvalMapper.contains("currDetail.ACTION_CD = 'APPROVAL'"));
	}

	private AcceptanceService acceptanceService(
			AcceptanceDao dao, ApprovalService approvalService) {
		AcceptanceService service = new AcceptanceService();
		ReflectionTestUtils.setField(service, "dao", dao);
		ReflectionTestUtils.setField(service, "service", approvalService);
		return service;
	}

	private ApprovalService approvalService(ApprovalDao dao) {
		ApprovalService service = new ApprovalService();
		ReflectionTestUtils.setField(service, "dao", dao);
		return service;
	}

	private AcceptancePopupParam request(String requestNo) {
		AcceptancePopupParam request = new AcceptancePopupParam();
		request.setRequestNo(requestNo);
		return request;
	}

	private AcceptancePopupParam lockedTarget(String objectType) {
		AcceptancePopupParam target = new AcceptancePopupParam();
		target.setRequestNo("REQ-1");
		target.setObjectType(objectType);
		target.setDeployUserCd(actor.getUserCd());
		return target;
	}

	private AcceptancePopupParam maliciousItem() {
		AcceptancePopupParam item = new AcceptancePopupParam();
		item.setRequestNo("OTHER-REQUEST");
		item.setObjectId("OTHER-OBJECT");
		item.setObjectNo("OTHER-NO");
		item.setDeployUserCd("OTHER-USER");
		return item;
	}

	private DeployInfoVO deployInfo() {
		DeployInfoVO deploy = new DeployInfoVO();
		deploy.setObjectId("OBJECT-1");
		deploy.setObjectNo("OBJECT-NO-1");
		deploy.setDeployDeptCd("DEPT-1");
		deploy.setDeployUserCd(actor.getUserCd());
		deploy.setDeployCount(3);
		deploy.setCopy(2);
		deploy.setDestroyCount(1);
		deploy.setRevNo("R1");
		return deploy;
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

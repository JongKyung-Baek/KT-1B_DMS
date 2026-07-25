package kr.esob.fdms.controller.inside.distribution.disposalacceptance;

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

import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.controller.login.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

class DisposalAcceptanceApprovalSecurityTest {

    private DisposalAcceptanceDao dao;
    private SecurityAclService securityAclService;
    private DisposalAcceptanceService service;
    private UserVO actor;

    @BeforeEach
    void setUp() {
        dao = mock(DisposalAcceptanceDao.class);
        securityAclService = mock(SecurityAclService.class);
        service = new DisposalAcceptanceService();
        ReflectionTestUtils.setField(service, "dao", dao);
        ReflectionTestUtils.setField(service, "securityAclService", securityAclService);

        actor = new UserVO();
        actor.setUserCd("CURRENT_APPROVER");
        when(securityAclService.requireCurrentUser()).thenReturn(actor);
    }

    @Test
    void approvalUsesAuthenticatedActorAndServerLockedTarget() {
        DisposalAcceptanceParam request = request("A");
        request.setSessionUser(maliciousActor());
        when(dao.selectApprovalTargetForUpdate(request)).thenReturn("DESTROY-1");
        when(dao.updateRequestFile(request)).thenReturn(2);
        when(dao.updateApprovalFile(request)).thenReturn(2);
        when(dao.updateDestroyRequest(request)).thenReturn(1);
        when(dao.updateDestroyRequestDetail(request)).thenReturn(1);

        ResultVO result = service.saveApproval(request);

        assertTrue(result.isSuccess());
        assertEquals(actor, request.getSessionUser());
        InOrder order = inOrder(securityAclService, dao);
        order.verify(securityAclService).requireCurrentUser();
        order.verify(dao).selectApprovalTargetForUpdate(request);
        order.verify(dao).updateRequestFile(request);
        order.verify(dao).updateApprovalFile(request);
        order.verify(dao).updateDestroyRequest(request);
        order.verify(dao).updateDestroyRequestDetail(request);
    }

    @Test
    void missingCurrentApproverTargetFailsClosedBeforeMutation() {
        DisposalAcceptanceParam request = request("R");
        request.setSessionUser(maliciousActor());
        when(dao.selectApprovalTargetForUpdate(request)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> service.saveApproval(request));

        assertEquals(actor, request.getSessionUser());
        verify(dao, never()).updateRequestFile(request);
        verify(dao, never()).updateApprovalFile(request);
        verify(dao, never()).updateDestroyRequest(request);
        verify(dao, never()).updateDestroyRequestDetail(request);
    }

    @Test
    void missingChildMutationFailsBeforeCompletingApprovalState() {
        DisposalAcceptanceParam request = request("A");
        when(dao.selectApprovalTargetForUpdate(request)).thenReturn("DESTROY-1");
        when(dao.updateRequestFile(request)).thenReturn(1);
        when(dao.updateApprovalFile(request)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.saveApproval(request));

        verify(dao, never()).updateDestroyRequest(request);
        verify(dao, never()).updateDestroyRequestDetail(request);
    }

    @Test
    void mutationIsTransactionalAndMapperLocksPendingActorRow() throws Exception {
        Transactional transactional = DisposalAcceptanceService.class
                .getMethod("saveApproval", DisposalAcceptanceParam.class)
                .getAnnotation(Transactional.class);
        assertNotNull(transactional);
        assertEquals(Exception.class, transactional.rollbackFor()[0]);

        String mapper = new String(Files.readAllBytes(Paths.get(
                "src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/disposalacceptance/DisposalAcceptance.xml")),
                StandardCharsets.UTF_8);
        assertTrue(mapper.contains("selectApprovalTargetForUpdate"));
        assertTrue(mapper.contains("FOR UPDATE OF destroyRequest, currDetail"));
        assertTrue(mapper.contains("currDetail.ACTUAL_USER_CD = #{sessionUser.userCd}"));
        assertTrue(mapper.contains("currDetail.ACTION_CD = 'REQUEST'"));
        assertTrue(mapper.contains("destroyRequest.STATUS_CD = 'REQUEST'"));
    }

    private DisposalAcceptanceParam request(String saveFlag) {
        DisposalAcceptanceParam request = new DisposalAcceptanceParam();
        request.setDestroyRequestNo("DESTROY-1");
        request.setSaveFlag(saveFlag);
        return request;
    }

    private UserVO maliciousActor() {
        UserVO malicious = new UserVO();
        malicious.setUserCd("CLIENT_SUPPLIED");
        return malicious;
    }
}

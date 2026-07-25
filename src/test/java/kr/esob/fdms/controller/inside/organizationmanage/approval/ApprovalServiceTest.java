package kr.esob.fdms.controller.inside.organizationmanage.approval;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

import java.util.Collections;

import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.login.UserVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ApprovalDao dao;

    @InjectMocks
    private ApprovalService service;

    @BeforeEach
    void authenticate() {
        UserVO actor = new UserVO();
        actor.setUserCd("APPROVER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void approvalUsesServerLoadedRequestInsteadOfClientFields() throws Exception {
        ApprovalListParam clientRequest = new ApprovalListParam();
        clientRequest.setRequestNo("REQ-1");
        clientRequest.setRequestType("D");
        clientRequest.setTargetUserCd("CLIENT_TARGET");
        clientRequest.setRejectReason("client supplied bypass");

        ApprovalListParam trustedRequest = new ApprovalListParam();
        trustedRequest.setRequestNo("REQ-1");
        trustedRequest.setRequestType("I");
        trustedRequest.setTargetUserCd(null);
        trustedRequest.setProtectYn("N");
        trustedRequest.setCrYn("N");

        when(dao.selectApprovalTarget(clientRequest)).thenReturn(trustedRequest);
        when(dao.insertUser(trustedRequest)).thenReturn(1);
        when(dao.updateReqeust(trustedRequest)).thenReturn(1);

        ResultVO result = service.approvalUser(clientRequest);

        assertTrue(result.isSuccess());
        assertNull(clientRequest.getRejectReason());
        verify(dao).insertUser(trustedRequest);
        verify(dao, never()).deleteUserInfo(clientRequest);
        InOrder persistenceOrder = inOrder(dao);
        persistenceOrder.verify(dao).selectApprovalTarget(clientRequest);
        persistenceOrder.verify(dao).insertUser(trustedRequest);
        persistenceOrder.verify(dao).updateReqeust(trustedRequest);
    }

    @Test
    void failedStateTransitionDoesNotApplyUserChange() throws Exception {
        ApprovalListParam clientRequest = new ApprovalListParam();
        clientRequest.setRequestNo("REQ-1");

        ApprovalListParam trustedRequest = new ApprovalListParam();
        trustedRequest.setRequestNo("REQ-1");
        trustedRequest.setRequestType("I");
        trustedRequest.setProtectYn("N");
        trustedRequest.setCrYn("N");

        when(dao.selectApprovalTarget(clientRequest)).thenReturn(trustedRequest);
        when(dao.insertUser(trustedRequest)).thenReturn(1);
        when(dao.updateReqeust(trustedRequest)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.approvalUser(clientRequest));
        verify(dao).insertUser(trustedRequest);
    }

    @Test
    void failedUserMutationDoesNotCompleteRequest() {
        ApprovalListParam clientRequest = new ApprovalListParam();
        clientRequest.setRequestNo("REQ-1");

        ApprovalListParam trustedRequest = new ApprovalListParam();
        trustedRequest.setRequestNo("REQ-1");
        trustedRequest.setRequestType("I");
        trustedRequest.setProtectYn("N");
        trustedRequest.setCrYn("N");

        when(dao.selectApprovalTarget(clientRequest)).thenReturn(trustedRequest);
        when(dao.insertUser(trustedRequest)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.approvalUser(clientRequest));
        verify(dao, never()).updateReqeust(trustedRequest);
    }

    @Test
    void detailLookupUsesAuthenticatedActorAndDeniesMissingRequest() {
        ApprovalListParam request = new ApprovalListParam();
        request.setRequestNo("REQ-OTHER");
        UserVO clientActor = new UserVO();
        clientActor.setUserCd("CLIENT_SUPPLIED");
        request.setSessionUser(clientActor);

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.selectDetailInfo(request));
        assertEquals("APPROVER", request.getSessionUser().getUserCd());
        verify(dao).selectDetailInfo(request);
    }

    @Test
    void vendorUserLookupUsesAuthenticatedActor() throws Exception {
        ApprovalListParam request = new ApprovalListParam();
        request.setCompanyCd("COMPANY");
        UserVO clientActor = new UserVO();
        clientActor.setUserCd("CLIENT_SUPPLIED");
        request.setSessionUser(clientActor);
        when(dao.venderUser(request)).thenReturn(Collections.emptyList());

        service.venderUser(request);

        assertEquals("APPROVER", request.getSessionUser().getUserCd());
        verify(dao).venderUser(request);
    }

    @Test
    void passwordIsNotPartOfApprovalRequestDtoAndMutationsAreTransactional() throws Exception {
        assertThrows(NoSuchMethodException.class,
                () -> ApprovalListParam.class.getMethod("getUserPwd"));
        assertNotNull(ApprovalService.class
                .getMethod("approvalUser", ApprovalListParam.class)
                .getAnnotation(Transactional.class));
        assertNotNull(ApprovalService.class
                .getMethod("rejectUser", ApprovalListParam.class)
                .getAnnotation(Transactional.class));
    }
}

package kr.esob.tdms.controller.general.distribution.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import kr.esob.tdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.controller.login.UserVO;

@ExtendWith(MockitoExtension.class)
class DistributionWorkflowServiceTest {
    @Mock
    DistributionWorkflowDao dao;
    @Mock
    SecurityAclService aclService;

    DistributionWorkflowService service;
    UserVO requester;

    @BeforeEach
    void setUp() {
        service = new DistributionWorkflowService(dao, aclService);
        requester = user("USER-1", "requester", "RG_009");
    }

    @Test
    void createUsesAuthenticatedActorAndServerResolvedSnapshot() {
        DistributionRequestSaveRequest input = saveRequest("SW", "SW-OBJECT-1", "1");
        DistributionRequestItemSnapshot snapshot = snapshot("SW", "SW-OBJECT-1", "1");
        when(aclService.requireCurrentUser()).thenReturn(requester);
        when(dao.resolveItem(any())).thenReturn(snapshot);
        when(dao.insertRequest(any(), eq(requester))).thenReturn(41L);
        when(dao.insertItem(any())).thenReturn(1);
        when(dao.insertEvent(anyLong(), any(), any(), any(), any(), any())).thenReturn(1);
        when(dao.selectRequest(41L)).thenReturn(record(41L, "DRAFT", "USER-1"));
        when(dao.selectItems(41L)).thenReturn(Collections.singletonList(snapshot));

        DistributionRequestDetail result = service.create(input);

        assertEquals(41L, result.getRequest().getRequestId());
        ArgumentCaptor<FileAccessRequest> access = ArgumentCaptor.forClass(FileAccessRequest.class);
        verify(aclService, org.mockito.Mockito.atLeastOnce()).requireAccess(access.capture());
        assertEquals(SecurityAclService.VIEW, access.getValue().getActionCd());
        assertEquals("SW-OBJECT-1", access.getValue().getObjectId());
        ArgumentCaptor<DistributionRequestItemSnapshot> saved =
            ArgumentCaptor.forClass(DistributionRequestItemSnapshot.class);
        verify(dao).insertItem(saved.capture());
        assertEquals(41L, saved.getValue().getRequestId());
        assertEquals("TD-001", saved.getValue().getMaterialNo());
    }

    @Test
    void submitLocksOwnedDraftAndRevalidatesCurrentFileSnapshot() {
        DistributionRequestRecord draft = record(7L, "DRAFT", "USER-1");
        DistributionRequestItemSnapshot saved = snapshot("SW_SUB", "SW-OBJECT-1", "2");
        when(aclService.requireCurrentUser()).thenReturn(requester);
        when(dao.selectRequestForUpdate(7L)).thenReturn(draft);
        when(dao.selectItems(7L)).thenReturn(Collections.singletonList(saved));
        when(dao.resolveItem(any())).thenReturn(snapshot("SW_SUB", "SW-OBJECT-1", "2"));
        when(dao.markSubmitted(7L, requester)).thenReturn(1);
        when(dao.insertEvent(anyLong(), any(), any(), any(), any(), any())).thenReturn(1);
        when(dao.selectRequest(7L)).thenReturn(record(7L, "PENDING_APPROVAL", "USER-1"));

        service.submit(7L);

        verify(dao).selectRequestForUpdate(7L);
        verify(dao).markSubmitted(7L, requester);
        verify(dao, never()).insertOutboxHold(anyLong());
    }

    @Test
    void approvalRequiresAdminAndCreatesOnlyHoldOutboxAfterStateChange() {
        UserVO admin = user("ADMIN-1", "admin", "RG_001");
        DistributionRequestRecord pending = record(9L, "PENDING_APPROVAL", "USER-1");
        DistributionRequestItemSnapshot saved = snapshot("SW", "SW-OBJECT-1", "1");
        when(aclService.requireCurrentUser()).thenReturn(admin);
        when(dao.selectRequestForUpdate(9L)).thenReturn(pending);
        when(dao.selectItems(9L)).thenReturn(Collections.singletonList(saved));
        when(dao.resolveItem(any())).thenReturn(snapshot("SW", "SW-OBJECT-1", "1"));
        when(dao.markApproved(9L, "approved", admin)).thenReturn(1);
        when(dao.insertEvent(anyLong(), any(), any(), any(), any(), any())).thenReturn(1);
        when(dao.insertOutboxHold(9L)).thenReturn(1);
        when(dao.countOutbox(9L)).thenReturn(1);
        when(dao.selectRequest(9L)).thenReturn(record(9L, "APPROVED", "USER-1"));

        DistributionDecisionRequest decision = new DistributionDecisionRequest();
        decision.setComment("approved");
        service.approve(9L, decision);

        verify(dao).markApproved(9L, "approved", admin);
        verify(dao).insertOutboxHold(9L);
        verify(dao).countOutbox(9L);
    }

    @Test
    void administratorCannotApproveOwnRequest() {
        UserVO admin = user("ADMIN-1", "admin", "RG_001");
        when(aclService.requireCurrentUser()).thenReturn(admin);
        when(dao.selectRequestForUpdate(11L))
            .thenReturn(record(11L, "PENDING_APPROVAL", "ADMIN-1"));

        DistributionWorkflowException failure = assertThrows(
            DistributionWorkflowException.class, () -> service.approve(11L, null));

        assertEquals("SELF_APPROVAL_NOT_ALLOWED", failure.getCode());
        verify(dao, never()).markApproved(anyLong(), any(), any());
        verify(dao, never()).insertOutboxHold(anyLong());
    }

    @Test
    void approvalFailsTheTransactionWhenHoldSnapshotIsMissing() {
        UserVO admin = user("ADMIN-1", "admin", "RG_001");
        DistributionRequestItemSnapshot saved = snapshot("SW", "SW-OBJECT-1", "1");
        when(aclService.requireCurrentUser()).thenReturn(admin);
        when(dao.selectRequestForUpdate(12L))
            .thenReturn(record(12L, "PENDING_APPROVAL", "USER-1"));
        when(dao.selectItems(12L)).thenReturn(Collections.singletonList(saved));
        when(dao.resolveItem(any())).thenReturn(snapshot("SW", "SW-OBJECT-1", "1"));
        when(dao.markApproved(12L, "", admin)).thenReturn(1);
        when(dao.insertEvent(anyLong(), any(), any(), any(), any(), any())).thenReturn(1);
        when(dao.insertOutboxHold(12L)).thenReturn(0);
        when(dao.countOutbox(12L)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.approve(12L, null));
        verify(dao).countOutbox(12L);
    }

    @Test
    void nonAdministratorCannotOpenApprovalQueue() {
        when(aclService.requireCurrentUser()).thenReturn(requester);
        assertThrows(AccessDeniedException.class, () -> service.approvalQueue(20, 0));
        verify(dao, never()).selectRequests(any(), any(), any(Boolean.class), any(Boolean.class),
            any(Integer.class), any(Integer.class));
    }

    @Test
    void clientCannotSmugglePathAsIdentifier() {
        when(aclService.requireCurrentUser()).thenReturn(requester);
        DistributionRequestSaveRequest input = saveRequest("SW", "../../secret", "1");

        DistributionWorkflowException failure = assertThrows(
            DistributionWorkflowException.class, () -> service.create(input));

        assertEquals("INVALID_DISTRIBUTION_ITEM_IDENTIFIER", failure.getCode());
        verify(aclService, never()).requireAccess(any());
        verify(dao, never()).insertRequest(any(), any());
    }

    @Test
    void changedSourceIsRejectedBeforeSubmission() {
        DistributionRequestItemSnapshot saved = snapshot("SW", "SW-OBJECT-1", "1");
        DistributionRequestItemSnapshot changed = snapshot("SW", "SW-OBJECT-1", "1");
        changed.setOriginalFileName("replacement.pdf");
        when(aclService.requireCurrentUser()).thenReturn(requester);
        when(dao.selectRequestForUpdate(13L)).thenReturn(record(13L, "DRAFT", "USER-1"));
        when(dao.selectItems(13L)).thenReturn(Collections.singletonList(saved));
        when(dao.resolveItem(any())).thenReturn(changed);

        DistributionWorkflowException failure = assertThrows(
            DistributionWorkflowException.class, () -> service.submit(13L));

        assertEquals("DISTRIBUTION_ITEM_CHANGED", failure.getCode());
        verify(dao, never()).markSubmitted(anyLong(), any());
    }

    private DistributionRequestSaveRequest saveRequest(String objectType, String objectId, String fileNo) {
        DistributionRequestItemRef item = new DistributionRequestItemRef();
        item.setObjectType(objectType);
        item.setObjectId(objectId);
        item.setFileNo(fileNo);
        DistributionRequestSaveRequest request = new DistributionRequestSaveRequest();
        request.setTitle("Distribution title");
        request.setPurpose("Verification");
        request.setItems(Collections.singletonList(item));
        return request;
    }

    private DistributionRequestItemSnapshot snapshot(String objectType, String objectId, String fileNo) {
        DistributionRequestItemSnapshot item = new DistributionRequestItemSnapshot();
        item.setObjectType(objectType);
        item.setObjectId(objectId);
        item.setFileNo(fileNo);
        item.setMaterialNo("TD-001");
        item.setMaterialName("Technical data");
        item.setOriginalFileName("drawing.pdf");
        item.setFileSize(1234L);
        item.setGradeCd("GENERAL");
        return item;
    }

    private DistributionRequestRecord record(long id, String status, String owner) {
        DistributionRequestRecord record = new DistributionRequestRecord();
        record.setRequestId(id);
        record.setRequestNo("DREQ-1");
        record.setStatus(status);
        record.setRequestedByUserCd(owner);
        return record;
    }

    private UserVO user(String userCd, String userId, String roleGroup) {
        UserVO user = new UserVO();
        user.setUserCd(userCd);
        user.setUserId(userId);
        user.setUserNm(userId);
        user.setRoleGroup(roleGroup);
        return user;
    }
}

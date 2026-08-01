package kr.esob.tdms.controller.general.distribution.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import kr.esob.tdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.controller.general.organizationmanage.partner.PartnerDirectoryService;
import kr.esob.tdms.controller.general.organizationmanage.partner.PartnerRecipient;
import kr.esob.tdms.controller.login.UserVO;

@ExtendWith(MockitoExtension.class)
class DistributionWorkflowServiceTest {
    @Mock DistributionWorkflowDao dao;
    @Mock SecurityAclService aclService;
    @Mock PartnerDirectoryService partnerDirectoryService;

    DistributionWorkflowService service;
    UserVO requester;

    @BeforeEach
    void setUp() {
        service = new DistributionWorkflowService(dao, aclService, partnerDirectoryService);
        requester = user("USER-1", "requester", "RG_009");
    }

    @Test
    void createExpandsEachDocumentToMainAndAllAuxiliaryFilesAndSnapshotsRecipients() {
        DistributionRequestSaveRequest input = saveRequest("OBJ-1");
        PartnerRecipient representative = recipient(1L, 11L, "Representative", "Y");
        PartnerRecipient engineer = recipient(1L, 12L, "Engineer", "N");
        List<DistributionRequestItemSnapshot> bundle = Arrays.asList(
            snapshot("SW", "OBJ-1", "1"), snapshot("SW_SUB", "OBJ-1", "2"));
        when(aclService.requireCurrentUser()).thenReturn(requester);
        when(partnerDirectoryService.listActiveRecipients(1L))
            .thenReturn(Arrays.asList(representative, engineer));
        when(dao.selectApprover("APPROVER-1")).thenReturn(approver("APPROVER-1"));
        when(dao.resolveDocumentFiles("OBJ-1")).thenReturn(bundle);
        when(dao.insertRequest(any(), eq(requester), eq(representative), any())).thenReturn(41L);
        when(dao.insertRecipient(any())).thenReturn(1);
        when(dao.insertItem(any())).thenReturn(1);
        when(dao.insertEvent(anyLong(), any(), any(), any(), any(), any())).thenReturn(1);
        when(dao.selectRequest(41L)).thenReturn(record(41L, "DRAFT", "USER-1", "APPROVER-1"));
        when(dao.selectItems(41L)).thenReturn(bundle);
        when(dao.selectRecipients(41L)).thenReturn(Collections.emptyList());
        when(dao.selectEvents(41L)).thenReturn(Collections.emptyList());

        DistributionRequestDetail result = service.create(input);

        assertEquals(41L, result.getRequest().getRequestId());
        ArgumentCaptor<DistributionRequestSaveRequest> normalized =
            ArgumentCaptor.forClass(DistributionRequestSaveRequest.class);
        verify(dao).insertRequest(normalized.capture(), eq(requester), eq(representative), any());
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        assertEquals(today.toString(), normalized.getValue().getDistributionStartDate());
        assertEquals(today.plusDays(7).toString(), normalized.getValue().getDistributionEndDate());
        verify(dao, org.mockito.Mockito.times(2)).insertRecipient(any());
        ArgumentCaptor<DistributionRequestItemSnapshot> saved =
            ArgumentCaptor.forClass(DistributionRequestItemSnapshot.class);
        verify(dao, org.mockito.Mockito.times(2)).insertItem(saved.capture());
        assertEquals(1, saved.getAllValues().get(0).getDocumentLineNo());
        assertEquals(1, saved.getAllValues().get(0).getFileLineNo());
        assertEquals(2, saved.getAllValues().get(1).getFileLineNo());
        verify(aclService, atLeastOnce()).requireAccess(any(FileAccessRequest.class));
    }

    @Test
    void catalogUsesTheBulkAclFilteredQueryWithoutWritingFileAccessAudit() {
        DistributionRequestItemSnapshot mainA = snapshot("SW", "OBJ-A", "1");
        DistributionRequestItemSnapshot subA = snapshot("SW_SUB", "OBJ-A", "2");
        DistributionRequestItemSnapshot mainB = snapshot("SW", "OBJ-B", "1");
        when(aclService.requireCurrentUser()).thenReturn(requester);
        when(dao.selectAccessibleCatalogItems("TRB000013", requester.getUserCd()))
            .thenReturn(Arrays.asList(mainA, subA, mainB));

        List<DistributionDocumentBundle> result = service.catalog("trb000013");

        assertEquals(2, result.size());
        assertEquals("OBJ-A", result.get(0).getObjectId());
        assertEquals(2, result.get(0).getTotalFileCount());
        verify(aclService, never()).checkAccess(any());
    }

    @Test
    void submitRejectsAFileAddedAfterTheDraftWasSaved() {
        DistributionRequestRecord draft = record(7L, "DRAFT", "USER-1", "APPROVER-1");
        DistributionRequestItemSnapshot savedMain = snapshot("SW", "OBJ-1", "1");
        savedMain.setDocumentLineNo(1);
        DistributionRequestRecipientSnapshot savedRecipient = recipientSnapshot(1L, 11L);
        when(aclService.requireCurrentUser()).thenReturn(requester);
        when(dao.selectRequestForUpdate(7L)).thenReturn(draft);
        when(dao.selectApprover("APPROVER-1")).thenReturn(approver("APPROVER-1"));
        when(dao.selectRecipients(7L)).thenReturn(Collections.singletonList(savedRecipient));
        when(partnerDirectoryService.listActiveRecipients(1L))
            .thenReturn(Collections.singletonList(recipient(1L, 11L, "Representative", "Y")));
        when(dao.selectItems(7L)).thenReturn(Collections.singletonList(savedMain));
        when(dao.resolveDocumentFiles("OBJ-1")).thenReturn(Arrays.asList(
            snapshot("SW", "OBJ-1", "1"), snapshot("SW_SUB", "OBJ-1", "2")));

        DistributionWorkflowException failure = assertThrows(
            DistributionWorkflowException.class, () -> service.submit(7L));

        assertEquals("DISTRIBUTION_ITEM_CHANGED", failure.getCode());
        verify(dao, never()).markSubmitted(anyLong(), any());
    }

    @Test
    void onlyTheAssignedActiveApproverMayApprove() {
        UserVO otherAdmin = user("ADMIN-2", "other", "RG_001");
        when(aclService.requireCurrentUser()).thenReturn(otherAdmin);
        when(dao.selectRequestForUpdate(9L))
            .thenReturn(record(9L, "PENDING_APPROVAL", "USER-1", "ADMIN-1"));
        when(dao.selectApprover("ADMIN-2")).thenReturn(approver("ADMIN-2"));

        DistributionWorkflowException failure = assertThrows(
            DistributionWorkflowException.class, () -> service.approve(9L, null));

        assertEquals("ASSIGNED_DISTRIBUTION_APPROVER_REQUIRED", failure.getCode());
        verify(dao, never()).markApproved(anyLong(), any(), any());
    }

    @Test
    void assignedApproverCreatesOneHoldOutboxAfterFullRevalidation() {
        UserVO assigned = user("ADMIN-1", "assigned", "RG_001");
        DistributionRequestRecord pending =
            record(10L, "PENDING_APPROVAL", "USER-1", "ADMIN-1");
        DistributionRequestItemSnapshot main = snapshot("SW", "OBJ-1", "1");
        main.setDocumentLineNo(1);
        DistributionRequestRecipientSnapshot savedRecipient = recipientSnapshot(1L, 11L);
        when(aclService.requireCurrentUser()).thenReturn(assigned);
        when(dao.selectRequestForUpdate(10L)).thenReturn(pending);
        when(dao.selectApprover("ADMIN-1")).thenReturn(approver("ADMIN-1"));
        when(dao.selectRecipients(10L)).thenReturn(Collections.singletonList(savedRecipient));
        when(partnerDirectoryService.listActiveRecipients(1L))
            .thenReturn(Collections.singletonList(recipient(1L, 11L, "Representative", "Y")));
        when(dao.selectItems(10L)).thenReturn(Collections.singletonList(main));
        when(dao.resolveDocumentFiles("OBJ-1"))
            .thenReturn(Collections.singletonList(snapshot("SW", "OBJ-1", "1")));
        when(dao.markApproved(10L, "", assigned)).thenReturn(1);
        when(dao.insertEvent(anyLong(), any(), any(), any(), any(), any())).thenReturn(1);
        when(dao.insertOutboxHold(10L)).thenReturn(1);
        when(dao.countOutbox(10L)).thenReturn(1);
        when(dao.selectRequest(10L)).thenReturn(record(10L, "APPROVED", "USER-1", "ADMIN-1"));
        when(dao.selectEvents(10L)).thenReturn(Collections.emptyList());

        service.approve(10L, null);

        verify(dao).markApproved(10L, "", assigned);
        verify(dao).insertOutboxHold(10L);
    }

    @Test
    void approvalQueueIsRestrictedToTheCurrentAssignedApprover() {
        UserVO assigned = user("ADMIN-1", "assigned", "RG_001");
        when(aclService.requireCurrentUser()).thenReturn(assigned);
        when(dao.selectApprover("ADMIN-1")).thenReturn(approver("ADMIN-1"));
        when(dao.selectRequests(null, "ADMIN-1", null, true, false, 20, 0))
            .thenReturn(Collections.emptyList());

        service.approvalQueue(20, 0);

        verify(dao).selectRequests(null, "ADMIN-1", null, true, false, 20, 0);
    }

    @Test
    void userWithoutApprovalMenuCannotOpenApprovalQueue() {
        when(aclService.requireCurrentUser()).thenReturn(requester);
        when(dao.selectApprover("USER-1")).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> service.approvalQueue(20, 0));
    }

    @Test
    void unavailableRecipientFailsBeforeAnyRequestIsInserted() {
        when(aclService.requireCurrentUser()).thenReturn(requester);
        when(partnerDirectoryService.listActiveRecipients(1L))
            .thenReturn(Collections.singletonList(recipient(1L, 99L, "Other", "Y")));

        DistributionWorkflowException failure = assertThrows(
            DistributionWorkflowException.class, () -> service.create(saveRequest("OBJ-1")));

        assertEquals("PARTNER_RECIPIENT_UNAVAILABLE", failure.getCode());
        verify(dao, never()).insertRequest(any(), any(), any(), any());
    }

    @Test
    void defaultPeriodIsSevenDaysAndAnEndBeforeStartIsRejected() {
        DistributionRequestSaveRequest input = saveRequest("OBJ-1");
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        input.setDistributionStartDate(tomorrow.toString());
        input.setDistributionEndDate(tomorrow.minusDays(1).toString());
        when(aclService.requireCurrentUser()).thenReturn(requester);

        DistributionWorkflowException failure = assertThrows(
            DistributionWorkflowException.class, () -> service.create(input));

        assertEquals("INVALID_DISTRIBUTION_PERIOD", failure.getCode());
        verify(dao, never()).insertRequest(any(), any(), any(), any());
    }

    @Test
    void approvedDistributionIsNotOpenToUnrelatedUsersBeforeItsStartDate() {
        DistributionRequestRecord approved = record(31L, "APPROVED", "OWNER-1", "APPROVER-1");
        LocalDate tomorrow = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        approved.setDistributionStartDate(tomorrow.toString());
        approved.setDistributionEndDate(tomorrow.plusDays(7).toString());
        when(aclService.requireCurrentUser()).thenReturn(requester);
        when(dao.selectRequest(31L)).thenReturn(approved);

        DistributionWorkflowException failure = assertThrows(
            DistributionWorkflowException.class, () -> service.detail(31L));

        assertEquals("DISTRIBUTION_REQUEST_ACCESS_DENIED", failure.getCode());
        verify(dao, never()).selectItems(anyLong());
    }

    @Test
    void expiredDraftCannotBypassExpirationByBeingCancelled() {
        DistributionRequestRecord expired = record(32L, "DRAFT", "USER-1", "APPROVER-1");
        expired.setDistributionEndDate(LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1).toString());
        when(aclService.requireCurrentUser()).thenReturn(requester);
        when(dao.selectRequestForUpdate(32L)).thenReturn(expired);

        DistributionWorkflowException failure = assertThrows(
            DistributionWorkflowException.class, () -> service.cancel(32L));

        assertEquals("DISTRIBUTION_PERIOD_EXPIRED", failure.getCode());
        verify(dao, never()).markCancelled(anyLong(), any(), any());
    }

    @Test
    void expiredPendingRequestCannotBypassExpirationByBeingRejected() {
        UserVO assignedApprover = user("APPROVER-1", "approver", "RG_012");
        DistributionRequestRecord expired = record(33L, "PENDING_APPROVAL", "OWNER-1", "APPROVER-1");
        expired.setDistributionEndDate(LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1).toString());
        when(aclService.requireCurrentUser()).thenReturn(assignedApprover);
        when(dao.selectRequestForUpdate(33L)).thenReturn(expired);
        when(dao.selectApprover("APPROVER-1")).thenReturn(approver("APPROVER-1"));

        DistributionWorkflowException failure = assertThrows(
            DistributionWorkflowException.class,
            () -> service.reject(33L, new DistributionDecisionRequest()));

        assertEquals("DISTRIBUTION_PERIOD_EXPIRED", failure.getCode());
        verify(dao, never()).markRejected(anyLong(), any(), any());
    }

    @Test
    void expirationDelegatesToOneTransactionalDatabaseStatement() {
        when(dao.expireElapsedRequests()).thenReturn(3);
        assertEquals(3, service.expireElapsedRequests());
        verify(dao).expireElapsedRequests();
    }

    private DistributionRequestSaveRequest saveRequest(String objectId) {
        DistributionRequestDocumentRef document = new DistributionRequestDocumentRef();
        document.setObjectId(objectId);
        DistributionRequestSaveRequest request = new DistributionRequestSaveRequest();
        request.setTitle("Distribution title");
        request.setPurpose("Verification");
        request.setPartnerCompanyId(1L);
        request.setRecipientUserIds(Arrays.asList(11L, 12L));
        request.setApproverUserCd("APPROVER-1");
        request.setDocuments(Collections.singletonList(document));
        return request;
    }

    private DistributionRequestItemSnapshot snapshot(String objectType, String objectId, String fileNo) {
        DistributionRequestItemSnapshot item = new DistributionRequestItemSnapshot();
        item.setObjectType(objectType);
        item.setObjectId(objectId);
        item.setFileNo(fileNo);
        item.setMaterialNo("TD-001");
        item.setMaterialName("Technical data");
        item.setOriginalFileName("SW_SUB".equals(objectType) ? "attachment.pdf" : "drawing.pdf");
        item.setFileSize(1234L);
        item.setGradeCd("GENERAL");
        item.setTreeCd("TRB000013");
        item.setTreeNm("2D");
        item.setParentTreeCd("TRB000002");
        item.setParentTreeNm("Drawing");
        return item;
    }

    private PartnerRecipient recipient(Long companyId, Long userId, String name, String representativeYn) {
        PartnerRecipient recipient = new PartnerRecipient();
        recipient.setPartnerCompanyId(companyId);
        recipient.setCompanyCode("PARTNER-001");
        recipient.setCompanyName("Partner One");
        recipient.setPartnerUserId(userId);
        recipient.setUserName(name);
        recipient.setEmail(userId + "@example.com");
        recipient.setPhone("010-0000-0000");
        recipient.setRepresentativeYn(representativeYn);
        return recipient;
    }

    private DistributionRequestRecipientSnapshot recipientSnapshot(Long companyId, Long userId) {
        PartnerRecipient source = recipient(companyId, userId, "Representative", "Y");
        DistributionRequestRecipientSnapshot saved = new DistributionRequestRecipientSnapshot();
        saved.setPartnerCompanyId(source.getPartnerCompanyId());
        saved.setPartnerUserId(source.getPartnerUserId());
        saved.setUserName(source.getUserName());
        saved.setEmail(source.getEmail());
        saved.setPhone(source.getPhone());
        saved.setRepresentativeYn(source.getRepresentativeYn());
        return saved;
    }

    private DistributionApproverOption approver(String userCd) {
        DistributionApproverOption approver = new DistributionApproverOption();
        approver.setApproverUserCd(userCd);
        approver.setUserId(userCd.toLowerCase());
        approver.setUserName(userCd);
        return approver;
    }

    private DistributionRequestRecord record(long id, String status, String owner, String approver) {
        DistributionRequestRecord record = new DistributionRequestRecord();
        record.setRequestId(id);
        record.setRequestNo("DREQ-1");
        record.setStatus(status);
        record.setRequestedByUserCd(owner);
        record.setApproverUserCd(approver);
        record.setPartnerCompanyId(1L);
        record.setDistributionStartDate(LocalDate.now(ZoneId.of("Asia/Seoul")).toString());
        record.setDistributionEndDate(LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(7).toString());
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

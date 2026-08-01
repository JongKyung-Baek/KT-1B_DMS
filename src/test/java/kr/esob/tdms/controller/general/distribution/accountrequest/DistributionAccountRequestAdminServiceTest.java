package kr.esob.tdms.controller.general.distribution.accountrequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.commonlogic.securityacl.SecurityAuditWriter;
import kr.esob.tdms.controller.login.UserVO;

class DistributionAccountRequestAdminServiceTest {
    private DistributionAccountRequestDao dao;
    private SecurityAclService aclService;
    private SecurityAuditWriter auditWriter;
    private DistributionAccountRequestAdminService service;
    private UserVO admin;

    @BeforeEach
    void setUp() {
        dao = org.mockito.Mockito.mock(DistributionAccountRequestDao.class);
        aclService = org.mockito.Mockito.mock(SecurityAclService.class);
        auditWriter = org.mockito.Mockito.mock(SecurityAuditWriter.class);
        service = new DistributionAccountRequestAdminService(
            dao, aclService, auditWriter, new ObjectMapper());
        admin = new UserVO();
        admin.setUserCd("ADMIN");
        admin.setUserId("admin");
        admin.setUserNm("Administrator");
        admin.setRoleGroup("RG_001");
        when(aclService.requireCurrentUser()).thenReturn(admin);
    }

    @Test
    void approvalOnlyPersistsDecisionContractAndAudit() {
        DistributionAccountRequestRecord pending = pending(11L);
        when(dao.selectRequestForUpdate(11L)).thenReturn(pending);
        when(dao.decide(eq(11L), eq("PENDING"), eq("APPROVED"), eq("approved"), eq(admin)))
            .thenReturn(1);
        when(dao.insertDecisionEvent(eq(11L), eq("APPROVED"), eq("PENDING"),
            eq("APPROVED"), eq("approved"), eq(admin))).thenReturn(1);
        DistributionAccountRequestRecord approved = pending(11L);
        approved.setStatus("APPROVED");
        when(dao.selectRequest(11L)).thenReturn(approved);
        when(dao.selectEvents(11L)).thenReturn(Collections.emptyList());
        DistributionAccountDecisionRequest decision = new DistributionAccountDecisionRequest();
        decision.setDecisionComment("approved");

        DistributionAccountRequestRecord result = service.approve(11L, decision);

        assertEquals("APPROVED", result.getStatus());
        verify(auditWriter).writeInCurrentTransaction(eq(admin), eq("ACCOUNT_REQUEST"),
            eq("APPROVE_ACCOUNT_REQUEST"), eq("SUCCESS"), any(), eq("approved"),
            eq("DISTRIBUTION_ACCOUNT_REQUEST"), eq("11"), any(), eq("CORR-11"), any(), any());
        // No docs_user or credential DAO exists on this service boundary.
        verify(dao).decide(11L, "PENDING", "APPROVED", "approved", admin);
    }

    @Test
    void rejectionRequiresAnOperatorComment() {
        when(dao.selectRequestForUpdate(11L)).thenReturn(pending(11L));

        DistributionAccountRequestException exception = assertThrows(
            DistributionAccountRequestException.class,
            () -> service.reject(11L, new DistributionAccountDecisionRequest()));

        assertEquals("DISTRIBUTION_ACCOUNT_REJECTION_COMMENT_REQUIRED", exception.getCode());
        verify(dao, never()).decide(any(Long.class), any(), any(), any(), any());
    }

    @Test
    void identicalApprovalRetryByTheSameAdministratorReturnsExistingDetail() {
        DistributionAccountRequestRecord approved = decided(
            11L, "APPROVED", "ADMIN", "approved");
        when(dao.selectRequestForUpdate(11L)).thenReturn(approved);
        when(dao.selectRequest(11L)).thenReturn(approved);
        when(dao.selectEvents(11L)).thenReturn(Collections.emptyList());
        DistributionAccountDecisionRequest retry = new DistributionAccountDecisionRequest();
        retry.setDecisionComment("  approved  ");

        DistributionAccountRequestRecord result = service.approve(11L, retry);

        assertEquals("APPROVED", result.getStatus());
        verify(dao, never()).decide(eq(11L), any(), any(), any(), any());
        verify(dao, never()).insertDecisionEvent(eq(11L), any(), any(), any(), any(), any());
        verifyNoInteractions(auditWriter);
    }

    @Test
    void identicalRejectionRetryByTheSameAdministratorReturnsExistingDetail() {
        DistributionAccountRequestRecord rejected = decided(
            12L, "REJECTED", "ADMIN", "invalid external account");
        when(dao.selectRequestForUpdate(12L)).thenReturn(rejected);
        when(dao.selectRequest(12L)).thenReturn(rejected);
        when(dao.selectEvents(12L)).thenReturn(Collections.emptyList());
        DistributionAccountDecisionRequest retry = new DistributionAccountDecisionRequest();
        retry.setDecisionComment("invalid external account");

        DistributionAccountRequestRecord result = service.reject(12L, retry);

        assertEquals("REJECTED", result.getStatus());
        verify(dao, never()).decide(eq(12L), any(), any(), any(), any());
        verify(dao, never()).insertDecisionEvent(eq(12L), any(), any(), any(), any(), any());
        verifyNoInteractions(auditWriter);
    }

    @Test
    void changedCommentOnAnExistingDecisionRemainsAConflict() {
        when(dao.selectRequestForUpdate(11L)).thenReturn(
            decided(11L, "APPROVED", "ADMIN", "approved"));
        DistributionAccountDecisionRequest changed = new DistributionAccountDecisionRequest();
        changed.setDecisionComment("different comment");

        DistributionAccountRequestException exception = assertThrows(
            DistributionAccountRequestException.class,
            () -> service.approve(11L, changed));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("INVALID_DISTRIBUTION_ACCOUNT_STATUS_TRANSITION", exception.getCode());
        verify(dao, never()).decide(eq(11L), any(), any(), any(), any());
        verifyNoInteractions(auditWriter);
    }

    @Test
    void oppositeDecisionOnAnExistingDecisionRemainsAConflict() {
        when(dao.selectRequestForUpdate(11L)).thenReturn(
            decided(11L, "APPROVED", "ADMIN", "approved"));
        DistributionAccountDecisionRequest opposite = new DistributionAccountDecisionRequest();
        opposite.setDecisionComment("approved");

        DistributionAccountRequestException exception = assertThrows(
            DistributionAccountRequestException.class,
            () -> service.reject(11L, opposite));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("INVALID_DISTRIBUTION_ACCOUNT_STATUS_TRANSITION", exception.getCode());
        verify(dao, never()).decide(eq(11L), any(), any(), any(), any());
        verifyNoInteractions(auditWriter);
    }

    @Test
    void identicalDecisionByAnotherAdministratorRemainsAConflict() {
        when(dao.selectRequestForUpdate(11L)).thenReturn(
            decided(11L, "APPROVED", "OTHER-ADMIN", "approved"));
        DistributionAccountDecisionRequest retry = new DistributionAccountDecisionRequest();
        retry.setDecisionComment("approved");

        DistributionAccountRequestException exception = assertThrows(
            DistributionAccountRequestException.class,
            () -> service.approve(11L, retry));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("INVALID_DISTRIBUTION_ACCOUNT_STATUS_TRANSITION", exception.getCode());
        verify(dao, never()).decide(eq(11L), any(), any(), any(), any());
        verifyNoInteractions(auditWriter);
    }

    @Test
    void nonAdministratorCannotReadQueue() {
        admin.setRoleGroup("RG_012");

        assertThrows(DistributionAccountRequestException.class,
            () -> service.list(null, null, null, null, null, null));

        verify(dao, never()).selectRequests(any(), any(), any(), any(), any(Integer.class), any(Integer.class));
    }

    private DistributionAccountRequestRecord pending(long id) {
        DistributionAccountRequestRecord result = new DistributionAccountRequestRecord();
        result.setRequestId(Long.valueOf(id));
        result.setStatus("PENDING");
        result.setSourceSystemId("DIST-DEMO");
        result.setRequestType("RESET_PASSWORD");
        result.setTargetUserId("external.user");
        result.setCorrelationId("CORR-" + id);
        return result;
    }

    private DistributionAccountRequestRecord decided(long id, String status,
            String decidedByUserCd, String decisionComment) {
        DistributionAccountRequestRecord result = pending(id);
        result.setStatus(status);
        result.setDecidedByUserCd(decidedByUserCd);
        result.setDecisionComment(decisionComment);
        return result;
    }
}

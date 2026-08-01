package kr.esob.tdms.commonlogic.viewer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.controller.login.UserVO;

@ExtendWith(MockitoExtension.class)
class PrintAuditServiceTest {

    @Mock
    private PrintAuditDao printAuditDao;

    @Mock
    private CommonViewerDao viewerDao;

    @Mock
    private SecurityAclService aclService;

    @InjectMocks
    private PrintAuditService service;

    @Test
    void startFailsClosedWhenJobWasNotPersisted() {
        UserVO actor = actor();
        CommonViewerParam source = printSource();
        when(aclService.requireCurrentUser()).thenReturn(actor);
        when(aclService.normalizeObjectType("DOC")).thenReturn("DOCUMENT");
        when(printAuditDao.insertJob(any(PrintJobVO.class), eq(actor), any())).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.start(source));

        verify(printAuditDao, never()).insertItem(any(PrintJobItemVO.class));
    }

    @Test
    void startFailsClosedWhenAnyPrintItemWasNotPersisted() {
        UserVO actor = actor();
        CommonViewerParam source = printSource();
        when(aclService.requireCurrentUser()).thenReturn(actor);
        when(aclService.normalizeObjectType("DOC")).thenReturn("DOCUMENT");
        when(printAuditDao.insertJob(any(PrintJobVO.class), eq(actor), any())).thenReturn(1);
        when(printAuditDao.insertItem(any(PrintJobItemVO.class))).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.start(source));
    }

    @Test
    void successfulCallbackFailsClosedWhenLegacyPrintCountWasNotUpdated() {
        UserVO actor = actor();
        PrintResultParam result = new PrintResultParam();
        result.setPrintJobId(" JOB-1 ");
        result.setStatusCd("SUCCESS");

        PrintJobVO job = new PrintJobVO();
        job.setPrintJobId("JOB-1");
        job.setStatusCd("STARTED");
        job.setActorUserCd(actor.getUserCd());
        job.setObjectType("DOCUMENT");
        job.setObjectId("DOC-1");
        job.setFileNo("FILE-1");

        PrintJobItemVO item = new PrintJobItemVO();
        item.setPrintJobId("JOB-1");
        item.setItemSeq(Integer.valueOf(1));
        item.setObjectType("DOCUMENT");
        item.setObjectId("DOC-1");
        item.setFileNo("FILE-1");
        item.setRequestNo("REQ-1");
        item.setRequestType("DISTRIBUTION");
        item.setCountRequiredYn("Y");

        when(aclService.requireCurrentUser()).thenReturn(actor);
        when(printAuditDao.selectJobForUpdate("JOB-1")).thenReturn(job);
        when(printAuditDao.completeJob(result, actor.getUserCd())).thenReturn(1);
        when(printAuditDao.selectItems("JOB-1")).thenReturn(Collections.singletonList(item));
        when(viewerDao.updatePrintCnt(any(CommonViewerParam.class))).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.complete(result));

        verify(printAuditDao, never()).markCountApplied(anyString(), anyString());
    }

    @Test
    void successfulCallbackRequiresCountAppliedMarkerToPersist() {
        UserVO actor = actor();
        PrintResultParam result = new PrintResultParam();
        result.setPrintJobId("JOB-1");
        result.setStatusCd("SUCCESS");

        PrintJobVO job = new PrintJobVO();
        job.setPrintJobId("JOB-1");
        job.setStatusCd("STARTED");
        job.setActorUserCd(actor.getUserCd());
        job.setObjectType("DOCUMENT");
        job.setObjectId("DOC-1");
        job.setFileNo("FILE-1");

        PrintJobItemVO item = new PrintJobItemVO();
        item.setPrintJobId("JOB-1");
        item.setItemSeq(Integer.valueOf(1));
        item.setObjectType("DOCUMENT");
        item.setObjectId("DOC-1");
        item.setFileNo("FILE-1");
        item.setRequestNo("REQ-1");
        item.setRequestType("DISTRIBUTION");
        item.setCountRequiredYn("Y");

        when(aclService.requireCurrentUser()).thenReturn(actor);
        when(printAuditDao.selectJobForUpdate("JOB-1")).thenReturn(job);
        when(printAuditDao.completeJob(result, actor.getUserCd())).thenReturn(1);
        when(printAuditDao.selectItems("JOB-1")).thenReturn(Collections.singletonList(item));
        when(viewerDao.updatePrintCnt(any(CommonViewerParam.class))).thenReturn(1);
        when(printAuditDao.markCountApplied("JOB-1", actor.getUserCd())).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.complete(result));
    }

    @Test
    void nonDistributionPrintCompletesWithoutTouchingLegacyDistributionCounter() {
        UserVO actor = actor();
        PrintResultParam result = new PrintResultParam();
        result.setPrintJobId("JOB-1");
        result.setStatusCd("SUCCESS");

        PrintJobVO job = new PrintJobVO();
        job.setPrintJobId("JOB-1");
        job.setStatusCd("STARTED");
        job.setActorUserCd(actor.getUserCd());
        job.setObjectType("DOCUMENT");
        job.setObjectId("DOC-1");
        job.setFileNo("FILE-1");

        PrintJobItemVO item = new PrintJobItemVO();
        item.setPrintJobId("JOB-1");
        item.setItemSeq(Integer.valueOf(1));
        item.setObjectType("DOCUMENT");
        item.setObjectId("DOC-1");
        item.setFileNo("FILE-1");
        item.setRequestType("OBJECT");
        item.setCountRequiredYn("N");

        when(aclService.requireCurrentUser()).thenReturn(actor);
        when(printAuditDao.selectJobForUpdate("JOB-1")).thenReturn(job);
        when(printAuditDao.completeJob(result, actor.getUserCd())).thenReturn(1);
        when(printAuditDao.selectItems("JOB-1")).thenReturn(Collections.singletonList(item));
        when(printAuditDao.markCountApplied("JOB-1", actor.getUserCd())).thenReturn(1);

        service.complete(result);

        verify(viewerDao, never()).updatePrintCnt(any(CommonViewerParam.class));
    }

    @Test
    void callbackFailsClosedWhenJobHasNoPersistedItems() {
        UserVO actor = actor();
        PrintResultParam result = new PrintResultParam();
        result.setPrintJobId("JOB-1");
        result.setStatusCd("FAILED");

        PrintJobVO job = new PrintJobVO();
        job.setPrintJobId("JOB-1");
        job.setStatusCd("STARTED");
        job.setActorUserCd(actor.getUserCd());

        when(aclService.requireCurrentUser()).thenReturn(actor);
        when(printAuditDao.selectJobForUpdate("JOB-1")).thenReturn(job);
        when(printAuditDao.completeJob(result, actor.getUserCd())).thenReturn(1);
        when(printAuditDao.selectItems("JOB-1")).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> service.complete(result));
    }

    private UserVO actor() {
        UserVO actor = new UserVO();
        actor.setUserCd("USER-1");
        return actor;
    }

    private CommonViewerParam printSource() {
        CommonViewerParam source = new CommonViewerParam();
        source.setObjectType("DOC");
        source.setObjectId("DOC-1");
        source.setFileNo("FILE-1");
        source.setRequestNo("REQ-1");
        return source;
    }
}

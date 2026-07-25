package kr.esob.fdms.commonlogic.updown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeCleanupScheduler;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeState;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeStore;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeTestDao;
import kr.esob.fdms.controller.login.UserVO;

class CommonUpdownV2AuditPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void auditFailureIsNeverReportedAsSaved() {
        SecurityAclService acl = mock(SecurityAclService.class);
        CommonUpdownV2Service service = new CommonUpdownV2Service();
        ReflectionTestUtils.setField(service, "securityAclService", acl);
        UserVO actor = actor();
        DownloadRuntimeState state = completedState();

        doThrow(new IllegalStateException("database unavailable")).when(acl).recordDownloadResult(
                actor, "SUCCESS", null, "DOCUMENT", "DOC-1", "FILE-1", "REQ-1",
                "Download completed.");

        assertFalse(service.saveOutsideDistributionDownloadActLog(
                actor, state, state.getStatus().name(), "client supplied path"));
        assertFalse(state.isActLogSaved());
    }

    @Test
    void persistedAuditUsesServerStatusAndNotClientMessage() {
        SecurityAclService acl = mock(SecurityAclService.class);
        CommonUpdownV2Service service = new CommonUpdownV2Service();
        ReflectionTestUtils.setField(service, "securityAclService", acl);
        UserVO actor = actor();
        DownloadRuntimeState state = completedState();

        assertTrue(service.saveOutsideDistributionDownloadActLog(
                actor, state, state.getStatus().name(), "C:\\sensitive\\client-path.pdf"));
        verify(acl).recordDownloadResult(
                actor, "SUCCESS", null, "DOCUMENT", "DOC-1", "FILE-1", "REQ-1",
                "Download completed.");
    }

    @Test
    void expiredRuntimeCannotBeRemovedBeforeAuditIsSaved() {
        DownloadRuntimeTestDao dao = new DownloadRuntimeTestDao();
        DownloadRuntimeStore store = new DownloadRuntimeStore(dao);
        DownloadRuntimeState state = store.registerQueued(
                "0123456789abcdef0123456789abcdef",
                "REQ-1", "DOC-1", "FILE-1", "FILE-1",
                "11111111111111111111111111111111",
                "DISTRIBUTION", "DOCUMENT", "drawing.pdf",
                "USER-CD", "user-id", "사용자", "session-id");
        dao.forceExpire(state.getWsSeq(), LocalDateTime.now().minusMinutes(1));

        assertEquals(0, store.cleanupExpired());
        assertTrue(store.exists(state.getWsSeq()));
        assertEquals("user-id", state.getOwnerUserId());
        assertEquals("사용자", state.getOwnerUserNm());

        store.update(state.getWsSeq(), DownloadRuntimeState::markActLogSaved);
        dao.forceExpire(state.getWsSeq(), LocalDateTime.now().minusMinutes(1));
        assertEquals(1, store.cleanupExpired());
        assertFalse(store.exists(state.getWsSeq()));
    }

    @Test
    void expiredDownloadCapabilityCannotBeClaimed() {
        DownloadRuntimeTestDao dao = new DownloadRuntimeTestDao();
        DownloadRuntimeStore store = new DownloadRuntimeStore(dao);
        DownloadRuntimeState state = store.registerQueued(
                "abcdef0123456789abcdef0123456789",
                "REQ-2", "DOC-2", "FILE-2", "FILE-2",
                "22222222222222222222222222222222",
                "DISTRIBUTION", "DOCUMENT", "expired.pdf",
                "USER-CD", "user-id", "user-name", "session-id");
        dao.forceExpire(state.getWsSeq(), LocalDateTime.now().minusSeconds(1));

        assertEquals(null,
            store.claimByDownloadRequestKey("22222222222222222222222222222222"));
        assertFalse(state.isDownloadClaimed());
        assertTrue(store.exists(state.getWsSeq()));
    }

    @Test
    void administrativeCleanupUsesTheSameFileDeletingLifecycle() throws Exception {
        DownloadRuntimeTestDao dao = new DownloadRuntimeTestDao();
        DownloadRuntimeStore store = new DownloadRuntimeStore(dao);
        DownloadRuntimeState state = store.registerQueued(
                "fedcba9876543210fedcba9876543210",
                "REQ-3", "DOC-3", "FILE-3", "FILE-3",
                "33333333333333333333333333333333",
                "DISTRIBUTION", "DOCUMENT", "cleanup.pdf",
                "USER-CD", "user-id", "user-name", "session-id");
        Path temporaryFile = Files.write(tempDir.resolve("cleanup.pdf"), new byte[] { 1, 2, 3 });
        store.update(state.getWsSeq(),
            runtime -> runtime.markDownloading(
                "REST-1", temporaryFile.toString(), "cleanup.pdf"));
        store.update(state.getWsSeq(), DownloadRuntimeState::markActLogSaved);
        dao.forceExpire(state.getWsSeq(), LocalDateTime.now().minusSeconds(1));

        DownloadRuntimeCleanupScheduler scheduler = new DownloadRuntimeCleanupScheduler();
        ReflectionTestUtils.setField(scheduler, "runtimeStore", store);
        ReflectionTestUtils.setField(scheduler, "updownV2Service", mock(CommonUpdownV2Service.class));

        assertEquals(1, scheduler.cleanupExpiredNow());
        assertFalse(Files.exists(temporaryFile));
        assertFalse(store.exists(state.getWsSeq()));
    }

    @Test
    void restartRecoveryFailsAndAuditsAnInterruptedRuntime() throws Exception {
        DownloadRuntimeTestDao dao = new DownloadRuntimeTestDao();
        DownloadRuntimeStore store = new DownloadRuntimeStore(dao);
        DownloadRuntimeState state = store.registerQueued(
                "0011aabb2233ccdd4455eeff66778899",
                "REQ-4", "DOC-4", "FILE-4", "FILE-4",
                "44444444444444444444444444444444",
                "DISTRIBUTION", "DOCUMENT", "restart.pdf",
                "USER-CD", "user-id", "user-name", "session-id");
        Path temporaryFile = Files.write(
            tempDir.resolve("restart.pdf"), new byte[] { 4, 5, 6 });
        store.update(state.getWsSeq(),
            runtime -> runtime.markDownloading(
                "REST-4", temporaryFile.toString(), "restart.pdf"));

        CommonUpdownV2Service auditService = mock(CommonUpdownV2Service.class);
        when(auditService.saveOutsideDistributionDownloadActLog(
                org.mockito.ArgumentMatchers.any(UserVO.class),
                org.mockito.ArgumentMatchers.any(DownloadRuntimeState.class),
                org.mockito.ArgumentMatchers.eq("FAILED"),
                org.mockito.ArgumentMatchers.eq(
                    "Download interrupted by application restart.")))
            .thenReturn(true);

        DownloadRuntimeCleanupScheduler scheduler =
            new DownloadRuntimeCleanupScheduler();
        ReflectionTestUtils.setField(scheduler, "runtimeStore", store);
        ReflectionTestUtils.setField(scheduler, "updownV2Service", auditService);

        assertEquals(1, scheduler.recoverPersistedRuntimeNow());
        assertFalse(Files.exists(temporaryFile));
        assertFalse(store.exists(state.getWsSeq()));
        verify(auditService).saveOutsideDistributionDownloadActLog(
            org.mockito.ArgumentMatchers.any(UserVO.class),
            org.mockito.ArgumentMatchers.any(DownloadRuntimeState.class),
            org.mockito.ArgumentMatchers.eq("FAILED"),
            org.mockito.ArgumentMatchers.eq(
                "Download interrupted by application restart."));
    }

    private DownloadRuntimeState completedState() {
        DownloadRuntimeState state = DownloadRuntimeState.createQueued(
                "0123456789abcdef0123456789abcdef",
                "REQ-1", "DOC-1", "FILE-1", "FILE-1",
                "55555555555555555555555555555555",
                "DISTRIBUTION", "DOCUMENT", "drawing.pdf",
                "USER-CD", "user-id", "사용자", "session-id", 30L);
        state.markResult("00", "");
        return state;
    }

    private UserVO actor() {
        UserVO actor = new UserVO();
        actor.setUserCd("USER-CD");
        actor.setUserId("user-id");
        actor.setUserNm("사용자");
        return actor;
    }
}

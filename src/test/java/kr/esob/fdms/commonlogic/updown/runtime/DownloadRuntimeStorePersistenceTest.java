package kr.esob.fdms.commonlogic.updown.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;

class DownloadRuntimeStorePersistenceTest {

    @Test
    void queuedStateIsPersistedBeforeCapabilityIsReturned() {
        DownloadRuntimeDao dao = mock(DownloadRuntimeDao.class);
        when(dao.insertQueued(any(DownloadRuntimeState.class))).thenReturn(1);
        DownloadRuntimeStore store = new DownloadRuntimeStore(dao);

        DownloadRuntimeState result = register(store, "0123456789abcdef0123456789abcdef");

        ArgumentCaptor<DownloadRuntimeState> persisted =
            ArgumentCaptor.forClass(DownloadRuntimeState.class);
        verify(dao).insertQueued(persisted.capture());
        assertEquals(DownloadRuntimeStatus.QUEUED, persisted.getValue().getStatus());
        assertEquals("11112222333344445555666677778888",
            persisted.getValue().getDownloadRequestKey());
        assertNotNull(persisted.getValue().getExpireAt());
        assertEquals(DownloadRuntimeStatus.QUEUED, result.getStatus());
    }

    @Test
    void databaseFailurePreventsCapabilityIssuance() {
        DownloadRuntimeDao dao = mock(DownloadRuntimeDao.class);
        when(dao.insertQueued(any(DownloadRuntimeState.class)))
            .thenThrow(new DataAccessResourceFailureException("database unavailable"));
        DownloadRuntimeStore store = new DownloadRuntimeStore(dao);

        assertThrows(DataAccessResourceFailureException.class,
            () -> register(store, "abcdef0123456789abcdef0123456789"));
        verify(dao, never()).selectLatestByDownloadRequestKey(any(String.class));
    }

    @Test
    void stateUpdateMustReachExactlyOneDurableRow() {
        DownloadRuntimeDao dao = mock(DownloadRuntimeDao.class);
        DownloadRuntimeState queued = queued("fedcba9876543210fedcba9876543210");
        when(dao.selectByWsSeq(queued.getWsSeq()))
            .thenReturn(DownloadRuntimeState.copyOf(queued));
        when(dao.updateState(any(DownloadRuntimeState.class))).thenReturn(0);
        DownloadRuntimeStore store = new DownloadRuntimeStore(dao);

        assertThrows(IllegalStateException.class,
            () -> store.update(queued.getWsSeq(), state -> state.markFailed("failure")));
        assertEquals(DownloadRuntimeStatus.QUEUED, queued.getStatus());
    }

    @Test
    void publicCapabilityClaimUsesOnlyTheAtomicDatabaseStatement() {
        DownloadRuntimeDao dao = mock(DownloadRuntimeDao.class);
        DownloadRuntimeState claimed = queued("00112233445566778899aabbccddeeff");
        claimed.markSentToWs();
        claimed.setDownloadClaimed(true);
        claimed.setClaimedAt(LocalDateTime.now());
        when(dao.claimByDownloadRequestKey(claimed.getDownloadRequestKey()))
            .thenReturn(claimed);
        DownloadRuntimeStore store = new DownloadRuntimeStore(dao);

        DownloadRuntimeState result =
            store.claimByDownloadRequestKey(claimed.getDownloadRequestKey());

        assertNotNull(result);
        assertTrue(result.isDownloadClaimed());
        verify(dao).claimByDownloadRequestKey(claimed.getDownloadRequestKey());
        verify(dao, never()).selectLatestByDownloadRequestKey(any(String.class));
    }

    @Test
    void expiredCapabilityIsRejectedByTheDatabaseClaim() {
        DownloadRuntimeDao dao = mock(DownloadRuntimeDao.class);
        when(dao.claimByDownloadRequestKey("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
            .thenReturn(null);
        DownloadRuntimeStore store = new DownloadRuntimeStore(dao);

        assertNull(
            store.claimByDownloadRequestKey("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    private DownloadRuntimeState register(DownloadRuntimeStore store, String wsSeq) {
        return store.registerQueued(
            wsSeq, "REQ-1", "DOC-1", "FILE-1", "FILE-1",
            "11112222333344445555666677778888",
            "DISTRIBUTION", "DOCUMENT", "drawing.pdf",
            "USER-CD", "user-id", "사용자", "session-id");
    }

    private DownloadRuntimeState queued(String wsSeq) {
        return DownloadRuntimeState.createQueued(
            wsSeq, "REQ-1", "DOC-1", "FILE-1", "FILE-1",
            "11112222333344445555666677778888",
            "DISTRIBUTION", "DOCUMENT", "drawing.pdf",
            "USER-CD", "user-id", "사용자", "session-id", 30L);
    }
}

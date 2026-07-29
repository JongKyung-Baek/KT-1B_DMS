package kr.esob.fdms.commonlogic.updown;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeState;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeStatus;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeStore;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeTestDao;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.login.UserVO;

class FileDownloadControllerTest {

    private static final String WS_SEQ =
        "0123456789abcdef0123456789abcdef";
    private static final String TICKET =
        "fedcba9876543210fedcba9876543210";

    @TempDir
    Path tempDir;

    private Map<String, String> previousSystemConfig;
    private DownloadRuntimeStore runtimeStore;
    private CommonUpdownV2Service updownV2Service;
    private FileDownloadController controller;

    @BeforeEach
    void setUp() {
        previousSystemConfig = SystemConfig.systemConfig;
        Map<String, String> config = new HashMap<String, String>();
        config.put(Constant.SYSTEM_CONFIG + "|UPDOWN_PATH", tempDir.toString());
        SystemConfig.systemConfig = config;

        runtimeStore = new DownloadRuntimeStore(new DownloadRuntimeTestDao());
        updownV2Service = mock(CommonUpdownV2Service.class);
        controller = new FileDownloadController(runtimeStore, updownV2Service);
    }

    @AfterEach
    void tearDown() {
        SystemConfig.systemConfig = previousSystemConfig;
    }

    @Test
    void successfulClaimPersistsCompletionAuditAndDeliveryBeforeStreaming()
            throws Exception {
        byte[] expected = new byte[] { 1, 2, 3, 4 };
        Path temporaryFile = Files.write(tempDir.resolve("drawing.pdf"), expected);
        RequestContext context = requestContext("USER-CD");
        prepareRuntime(temporaryFile, context.session.getId());
        when(updownV2Service.saveOutsideDistributionDownloadActLog(
                eq(context.actor), any(DownloadRuntimeState.class),
                eq("COMPLETED"), isNull())).thenReturn(true);

        ResponseEntity<?> response = controller.downloadFile(
            TICKET, context.authentication, context.request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        DownloadRuntimeState persisted = runtimeStore.get(WS_SEQ);
        assertEquals(DownloadRuntimeStatus.COMPLETED, persisted.getStatus());
        assertTrue(persisted.isActLogSaved());
        verify(updownV2Service).updateOutsideDistributionDeliveryConfirm(
            any(DownloadRuntimeState.class), eq("COMPLETED"));

        InputStreamResource resource = (InputStreamResource) response.getBody();
        try (InputStream input = resource.getInputStream()) {
            assertArrayEquals(expected, input.readAllBytes());
        }
        assertFalse(Files.exists(temporaryFile));

        ResponseEntity<?> replay = controller.downloadFile(
            TICKET, context.authentication, context.request);
        assertEquals(HttpStatus.NOT_FOUND, replay.getStatusCode());
    }

    @Test
    void auditFailureDeletesTemporaryFileAndNeverReturnsDownloadBody()
            throws Exception {
        Path temporaryFile = Files.write(
            tempDir.resolve("blocked.pdf"), new byte[] { 5, 6, 7 });
        RequestContext context = requestContext("USER-CD");
        prepareRuntime(temporaryFile, context.session.getId());
        when(updownV2Service.saveOutsideDistributionDownloadActLog(
                eq(context.actor), any(DownloadRuntimeState.class),
                eq("COMPLETED"), isNull())).thenReturn(false);

        ResponseEntity<?> response = controller.downloadFile(
            TICKET, context.authentication, context.request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNull(response.getBody());
        assertFalse(Files.exists(temporaryFile));
        DownloadRuntimeState persisted = runtimeStore.get(WS_SEQ);
        assertEquals(DownloadRuntimeStatus.FAILED, persisted.getStatus());
        assertFalse(persisted.isActLogSaved());
        verify(updownV2Service, never()).updateOutsideDistributionDeliveryConfirm(
            any(DownloadRuntimeState.class), any(String.class));
    }

    @Test
    void capabilityCannotBeUsedByAnotherSession() throws Exception {
        Path temporaryFile = Files.write(
            tempDir.resolve("owned.pdf"), new byte[] { 8, 9 });
        RequestContext owner = requestContext("USER-CD");
        prepareRuntime(temporaryFile, owner.session.getId());
        RequestContext other = requestContext("OTHER-USER");

        ResponseEntity<?> response = controller.downloadFile(
            TICKET, other.authentication, other.request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        assertTrue(Files.exists(temporaryFile));
        assertEquals(
            DownloadRuntimeStatus.FAILED, runtimeStore.get(WS_SEQ).getStatus());
        verify(updownV2Service, never()).saveOutsideDistributionDownloadActLog(
            any(), any(), any(), any());
    }

    private void prepareRuntime(Path temporaryFile, String ownerSessionId) {
        runtimeStore.registerQueued(
            WS_SEQ, "REQ-1", "DOC-1", "FILE-1", "FILE-1", TICKET,
            "DISTRIBUTION", "DOCUMENT", "drawing.pdf",
            "USER-CD", "user-id", "User", ownerSessionId);
        runtimeStore.update(WS_SEQ, state -> state.markDownloading(
            "REST-1", temporaryFile.toString(), temporaryFile.getFileName().toString()));
        runtimeStore.update(WS_SEQ, DownloadRuntimeState::markSentToWs);
    }

    private RequestContext requestContext(String userCd) {
        UserVO actor = new UserVO();
        actor.setUserCd(userCd);
        actor.setUserId(userCd.toLowerCase());
        actor.setUserNm(userCd);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            actor, "", Collections.emptyList());
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = (MockHttpSession) request.getSession(true);
        return new RequestContext(actor, authentication, request, session);
    }

    private static final class RequestContext {
        private final UserVO actor;
        private final Authentication authentication;
        private final MockHttpServletRequest request;
        private final MockHttpSession session;

        private RequestContext(UserVO actor,
                               Authentication authentication,
                               MockHttpServletRequest request,
                               MockHttpSession session) {
            this.actor = actor;
            this.authentication = authentication;
            this.request = request;
            this.session = session;
        }
    }
}

package kr.esob.tdms.controller.general.distribution.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.login.UserVO;
import kr.esob.tdms.util.DateUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usesAuthenticatedActorAndDeniesMissingLockedTarget() {
        AcceptanceDao dao = mock(AcceptanceDao.class);
        AcceptanceService service = serviceWith(dao);
        AcceptanceParam request = request(" REQ-OTHER ", "R");
        request.setSessionUser(maliciousActor());
        when(dao.selectAcceptanceTargetForUpdate(request)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> service.saveAcceptance(request));

        assertEquals(actor, request.getSessionUser());
        assertEquals("REQ-OTHER", request.getRequestNo());
        verify(dao).selectAcceptanceTargetForUpdate(request);
    }

    @Test
    void acceptanceScopesClientFileRowsToLockedRequestAndRequiresSingleRows() {
        AcceptanceDao dao = mock(AcceptanceDao.class);
        AcceptanceService service = serviceWith(dao);
        AcceptanceParam request = request("REQ-1", "A");
        request.setSessionUser(maliciousActor());
        request.setPurchaseUid("NEXT_APPROVER");
        AcceptanceParam file = new AcceptanceParam();
        file.setRequestNo("ATTACKER_REQUEST");
        file.setObjectId("OBJECT-1");
        file.setFileNo("1");
        file.setDeployTerm("1");
        request.setList(Collections.singletonList(file));

        when(dao.selectAcceptanceTargetForUpdate(request)).thenReturn(lockedTarget());
        when(dao.updateTlRequestDetail(request)).thenReturn(1);
        when(dao.updateRequestFile(file)).thenReturn(1);
        when(dao.updateRequest(request)).thenReturn(1);
        when(dao.updateRequestAcceptDetail(request)).thenReturn(1);

        ResultVO result = service.saveAcceptance(request);

        assertTrue(result.isSuccess());
        assertEquals(actor, request.getSessionUser());
        assertEquals("2", request.getProcessSeq());
        assertEquals("3", request.getCurrentProcessSeqNo());
        assertEquals("ACCEPT", request.getActionCd());
        assertEquals("REQ-1", file.getRequestNo());
        assertEquals(actor, file.getSessionUser());
        assertEquals("2", file.getProcessSeq());
        verify(dao).updateRequest(request);
        verify(dao).updateRequestAcceptDetail(request);
    }

    @Test
    void unexpectedAffectedRowCountFailsClosed() {
        AcceptanceDao dao = mock(AcceptanceDao.class);
        AcceptanceService service = serviceWith(dao);
        AcceptanceParam request = request("REQ-1", "R");
        when(dao.selectAcceptanceTargetForUpdate(request)).thenReturn(lockedTarget());
        when(dao.updateRequest(request)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.saveAcceptance(request));
    }

    @Test
    void transactionPostRouteAndSqlEnforceCurrentPendingAcceptor() throws Exception {
        assertNotNull(AcceptanceService.class
                .getMethod("saveAcceptance", AcceptanceParam.class)
                .getAnnotation(Transactional.class));
        assertNotNull(AcceptanceController.class
                .getMethod("saveAcceptance", AcceptanceParam.class)
                .getAnnotation(PostMapping.class));

        String sql = new String(Files.readAllBytes(Paths.get(
                "src/main/resources/sqlMaps/oracle/its/controller/general/distribution/acceptance/Acceptance.xml")),
                StandardCharsets.UTF_8);
        assertTrue(sql.contains("FOR UPDATE OF request, currDetail"));
        assertTrue(sql.contains("request.REQUEST_TYPE = 'DISTRIBUTION'"));
        assertTrue(sql.contains("request.STATUS_CD IN ('REQUEST', 'WAITING')"));
        assertTrue(sql.contains("currDetail.PROCESS_SEQ = request.CURRENT_PROCESS_SEQ_NO"));
        assertTrue(sql.contains("currDetail.APPROVAL_STATUS_CD = 'ACCEPT'"));
        assertTrue(sql.contains("currDetail.ACTUAL_USER_CD = #{sessionUser.userCd}"));
        assertTrue(sql.contains("currDetail.ACTION_CD = 'REQUEST'"));
    }

    private AcceptanceService serviceWith(AcceptanceDao dao) {
        AcceptanceService service = new AcceptanceService();
        ReflectionTestUtils.setField(service, "dao", dao);
        ReflectionTestUtils.setField(service, "dateUtil", new DateUtil());
        return service;
    }

    private AcceptanceParam request(String requestNo, String saveType) {
        AcceptanceParam request = new AcceptanceParam();
        request.setRequestNo(requestNo);
        request.setSaveType(saveType);
        return request;
    }

    private AcceptanceParam lockedTarget() {
        AcceptanceParam target = new AcceptanceParam();
        target.setRequestNo("REQ-1");
        target.setApprovalLineId("2");
        target.setCurrentProcessSeqNo("2");
        return target;
    }

    private UserVO maliciousActor() {
        UserVO malicious = new UserVO();
        malicious.setUserCd("CLIENT_SUPPLIED");
        return malicious;
    }
}

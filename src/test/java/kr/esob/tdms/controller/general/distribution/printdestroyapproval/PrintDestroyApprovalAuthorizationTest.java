package kr.esob.tdms.controller.general.distribution.printdestroyapproval;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;

class PrintDestroyApprovalAuthorizationTest {

    private UserVO actor;

    @BeforeEach
    void authenticate() {
        actor = new UserVO();
        actor.setUserCd("CURRENT_APPROVER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usesAuthenticatedActorAndDeniesMissingLockedTarget() {
        PrintDestroyApprovalDao dao = mock(PrintDestroyApprovalDao.class);
        PrintDestroyApprovalService service = serviceWith(dao);
        PrintDestroyApprovalPopupParam request = request(" DEST-OTHER ", "A");
        request.setSessionUser(maliciousActor());
        when(dao.getDestroyRequestInfo(request)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> service.saveApproval(request));

        assertEquals(actor, request.getSessionUser());
        assertEquals("DEST-OTHER", request.getDestroyRequestNo());
        verify(dao).getDestroyRequestInfo(request);
    }

    @Test
    void approvedMutationRequiresOneRequestAndOneCurrentDetailRow() {
        PrintDestroyApprovalDao dao = mock(PrintDestroyApprovalDao.class);
        PrintDestroyApprovalService service = serviceWith(dao);
        PrintDestroyApprovalPopupParam request = request("DEST-1", "A");
        request.setSessionUser(maliciousActor());
        PrintDestroyApprovalPopupParam target = lockedTarget();
        when(dao.getDestroyRequestInfo(request)).thenReturn(target);
        when(dao.updatePrintDestroyRequestInfo(request)).thenReturn(1);
        when(dao.selectDestroyItemList(request)).thenReturn(Collections.emptyList());
        when(dao.updatePrintDestroyRequestDetail(request)).thenReturn(1);

        ResultVO result = service.saveApproval(request);

        assertTrue(result.isSuccess());
        assertEquals(actor, request.getSessionUser());
        assertEquals("2", request.getProcessSeq());
        assertEquals("APPROVAL", request.getActionCd());
        assertEquals("APPROVAL", request.getStatusCd());
        verify(dao).updatePrintDestroyRequestInfo(request);
        verify(dao).updatePrintDestroyRequestDetail(request);
    }

    @Test
    void unexpectedAffectedRowCountFailsClosed() {
        PrintDestroyApprovalDao dao = mock(PrintDestroyApprovalDao.class);
        PrintDestroyApprovalService service = serviceWith(dao);
        PrintDestroyApprovalPopupParam request = request("DEST-1", "R");
        when(dao.getDestroyRequestInfo(request)).thenReturn(lockedTarget());
        when(dao.updatePrintDestroyRequestInfo(request)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.saveApproval(request));
    }

    @Test
    void transactionPostRoutesAndSqlEnforceCurrentPendingApprover() throws Exception {
        assertNotNull(PrintDestroyApprovalService.class
                .getMethod("saveApproval", PrintDestroyApprovalPopupParam.class)
                .getAnnotation(Transactional.class));
        assertPostOnly("saveDestroyApproval");
        assertPostOnly("batchSaveApproval");

        String sql = new String(Files.readAllBytes(Paths.get(
                "src/main/resources/sqlMaps/oracle/its/controller/general/distribution/printdestroyapproval/PrintDestroyApproval.xml")),
                StandardCharsets.UTF_8);
        assertTrue(sql.contains("FOR UPDATE OF destRequest, currDetail"));
        assertTrue(sql.contains("destRequest.STATUS_CD = 'REQUEST'"));
        assertTrue(sql.contains("destRequest.REQUEST_TYPE = 'PRINT'"));
        assertTrue(sql.contains("currDetail.PROCESS_SEQ = destRequest.CURRENT_PROCESS_SEQ_NO"));
        assertTrue(sql.contains("currDetail.ACTUAL_USER_CD = #{sessionUser.userCd}"));
        assertTrue(sql.contains("currDetail.ACTION_CD = 'REQUEST'"));
    }

    private void assertPostOnly(String methodName) throws NoSuchMethodException {
        RequestMapping mapping = PrintDestroyApprovalController.class
                .getMethod(methodName, PrintDestroyApprovalPopupParam.class)
                .getAnnotation(RequestMapping.class);
        assertNotNull(mapping);
        assertEquals(1, mapping.method().length);
        assertEquals(RequestMethod.POST, mapping.method()[0]);
    }

    private PrintDestroyApprovalService serviceWith(PrintDestroyApprovalDao dao) {
        PrintDestroyApprovalService service = new PrintDestroyApprovalService();
        ReflectionTestUtils.setField(service, "dao", dao);
        return service;
    }

    private PrintDestroyApprovalPopupParam request(String destroyRequestNo, String saveType) {
        PrintDestroyApprovalPopupParam request = new PrintDestroyApprovalPopupParam();
        request.setDestroyRequestNo(destroyRequestNo);
        request.setSaveType(saveType);
        return request;
    }

    private PrintDestroyApprovalPopupParam lockedTarget() {
        PrintDestroyApprovalPopupParam target = new PrintDestroyApprovalPopupParam();
        target.setDestroyRequestNo("DEST-1");
        target.setApprovalLineId("8");
        target.setCurrentProcessSeqNo("2");
        target.setApprovalStatusCd("APPROVAL");
        target.setApprovalGradeCd("TM");
        return target;
    }

    private UserVO maliciousActor() {
        UserVO malicious = new UserVO();
        malicious.setUserCd("CLIENT_SUPPLIED");
        return malicious;
    }
}

package kr.esob.fdms.controller.inside.approval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import kr.esob.fdms.controller.login.UserVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

class LegacyApprovalAuthorizationTest {

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
    void distributionApprovalUsesAuthenticatedActorAndDeniesMissingLockedTarget() {
        kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalDao dao =
                mock(kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalDao.class);
        kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalService service =
                new kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalService();
        ReflectionTestUtils.setField(service, "dao", dao);

        kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalParam request =
                new kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalParam();
        request.setRequestNo("REQ-OTHER");
        request.setSaveType("A");
        request.setSessionUser(maliciousActor());
        when(dao.getCurrentApprovalInfo(request)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> service.saveApproval(request));
        assertEquals(actor, request.getSessionUser());
        verify(dao).getCurrentApprovalInfo(request);
    }

    @Test
    void productionApprovalUsesAuthenticatedActorAndDeniesMissingLockedTarget() {
        kr.esob.fdms.controller.inside.production.approval.ApprovalDao dao =
                mock(kr.esob.fdms.controller.inside.production.approval.ApprovalDao.class);
        kr.esob.fdms.controller.inside.production.approval.ApprovalService service =
                new kr.esob.fdms.controller.inside.production.approval.ApprovalService();
        ReflectionTestUtils.setField(service, "dao", dao);

        kr.esob.fdms.controller.inside.production.approval.ApprovalPopupParam request =
                new kr.esob.fdms.controller.inside.production.approval.ApprovalPopupParam();
        request.setRequestNo("REQ-OTHER");
        request.setSaveType("A");
        request.setSessionUser(maliciousActor());
        when(dao.getCurrentApprovalInfo(request)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> service.approval(request));
        assertEquals(actor, request.getSessionUser());
        verify(dao).getCurrentApprovalInfo(request);
    }

    @Test
    void crApprovalUsesAuthenticatedActorAndDeniesMissingLockedTarget() {
        kr.esob.fdms.controller.inside.cr.approval.ApprovalDao dao =
                mock(kr.esob.fdms.controller.inside.cr.approval.ApprovalDao.class);
        kr.esob.fdms.controller.inside.cr.approval.ApprovalService service =
                new kr.esob.fdms.controller.inside.cr.approval.ApprovalService();
        ReflectionTestUtils.setField(service, "dao", dao);

        kr.esob.fdms.controller.inside.cr.CrParam request =
                new kr.esob.fdms.controller.inside.cr.CrParam();
        request.setCrNo("CR-OTHER");
        request.setSessionUser(maliciousActor());
        when(dao.selectApprovalTargetForUpdate(request)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> service.approvalReject(request));
        assertEquals(actor, request.getSessionUser());
        verify(dao).selectApprovalTargetForUpdate(request);
    }

    @Test
    void unregisteredApprovalUsesAuthenticatedActorAndDeniesMissingLockedTarget() {
        kr.esob.fdms.controller.inside.unregisted.approval.ApprovalDao dao =
                mock(kr.esob.fdms.controller.inside.unregisted.approval.ApprovalDao.class);
        kr.esob.fdms.controller.inside.unregisted.approval.ApprovalService service =
                new kr.esob.fdms.controller.inside.unregisted.approval.ApprovalService();
        ReflectionTestUtils.setField(service, "dao", dao);

        kr.esob.fdms.controller.inside.unregisted.approval.ApprovalPopupParam request =
                new kr.esob.fdms.controller.inside.unregisted.approval.ApprovalPopupParam();
        request.setRequestNo("REQ-OTHER");
        request.setSaveType("R");
        request.setSessionUser(maliciousActor());
        when(dao.selectApprovalTargetForUpdate(request)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> service.saveApproval(request));
        assertEquals(actor, request.getSessionUser());
        verify(dao).selectApprovalTargetForUpdate(request);
    }

    @Test
    void legacyApprovalMutationsAreTransactionalAndSqlLocksCurrentActorRow() throws Exception {
        assertTransactional(
                kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalService.class,
                "saveApproval",
                kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalParam.class);
        assertTransactional(
                kr.esob.fdms.controller.inside.production.approval.ApprovalService.class,
                "approval",
                kr.esob.fdms.controller.inside.production.approval.ApprovalPopupParam.class);
        assertTransactional(
                kr.esob.fdms.controller.inside.cr.approval.ApprovalService.class,
                "approvalReject",
                kr.esob.fdms.controller.inside.cr.CrParam.class);
        assertTransactional(
                kr.esob.fdms.controller.inside.unregisted.approval.ApprovalService.class,
                "saveApproval",
                kr.esob.fdms.controller.inside.unregisted.approval.ApprovalPopupParam.class);

        assertLockedActorSql("src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/commonrequest/CommonApproval.xml");
        assertLockedActorSql("src/main/resources/sqlMaps/oracle/its/controller/inside/production/approval/Approval.xml");
        assertLockedActorSql("src/main/resources/sqlMaps/oracle/its/controller/inside/cr/approval/Approval.xml");
        assertLockedActorSql("src/main/resources/sqlMaps/oracle/its/controller/inside/unregisted/approval/Approval.xml");
    }

    private void assertTransactional(Class<?> serviceType, String method, Class<?> parameterType)
            throws NoSuchMethodException {
        assertNotNull(serviceType.getMethod(method, parameterType).getAnnotation(Transactional.class));
    }

    private void assertLockedActorSql(String path) throws Exception {
        String sql = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(sql.contains("FOR UPDATE OF"));
        org.junit.jupiter.api.Assertions.assertTrue(sql.contains("ACTUAL_USER_CD = #{sessionUser.userCd}"));
        org.junit.jupiter.api.Assertions.assertTrue(sql.contains("ACTION_CD = 'REQUEST'"));
    }

    private UserVO maliciousActor() {
        UserVO malicious = new UserVO();
        malicious.setUserCd("CLIENT_SUPPLIED");
        return malicious;
    }
}

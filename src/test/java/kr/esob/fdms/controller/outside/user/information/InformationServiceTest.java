package kr.esob.fdms.controller.outside.user.information;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;

import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.seed.PasswordUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class InformationServiceTest {

    @Mock
    private InformationDao dao;

    @InjectMocks
    private InformationService service;

    @Test
    void hashesNewUserPasswordBeforeRequestPersistenceAndClearsDto() throws Exception {
        InformationListParam param = new InformationListParam();
        param.setRequestType("I");
        param.setUserPwd("Registration9!");
        UserVO principal = new UserVO();
        principal.setUserCd("REQUEST_USER");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(dao.selectCompanyApprover(param)).thenReturn("APPROVER");
        AtomicReference<String> persistedPassword = new AtomicReference<>();
        doAnswer(invocation -> {
            persistedPassword.set(((InformationListParam) invocation.getArgument(0)).getUserPwd());
            return null;
        }).when(dao).insertInfo(param);

        ResultVO result = service.insertRequest(param, authentication);

        assertTrue(result.isSuccess());
        assertNotEquals("Registration9!", persistedPassword.get());
        assertTrue(PasswordUtils.verifyPassword(persistedPassword.get(), "Registration9!"));
        assertNull(param.getUserPwd());
    }

    @Test
    void rejectsCrossCompanyUpdateOrDeleteTargets() throws Exception {
        InformationListParam param = new InformationListParam();
        param.setRequestType("U");
        param.setUserCd("OTHER_COMPANY_USER");

        UserVO principal = new UserVO();
        principal.setUserCd("REQUEST_USER");
        principal.setCompanyCd("REQUEST_COMPANY");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(dao.selectCompanyApprover(param)).thenReturn("APPROVER");
        when(dao.selectCompanyUserCount(param)).thenReturn(0);

        ResultVO result = service.insertRequest(param, authentication);

        assertFalse(result.isSuccess());
        verify(dao, never()).insertInfo(param);
        assertNull(param.getUserPwd());
    }

    @Test
    void verifiesCurrentPasswordAgainstFreshDatabaseHashAndUsesAuthenticatedIdentity() throws Exception {
        InformationListParam param = new InformationListParam();
        param.setUserCd("CLIENT_CONTROLLED_USER");
        param.setUserPwd("Current9!");
        param.setUserNewPwd("Replacement9!");

        UserVO principal = new UserVO();
        principal.setUserCd("AUTHENTICATED_USER");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(dao.selectPasswordHash(param))
                .thenReturn(PasswordUtils.hashPasswordWithSalt("Current9!"));

        AtomicReference<String> persistedPassword = new AtomicReference<>();
        doAnswer(invocation -> {
            InformationListParam persisted = invocation.getArgument(0);
            assertEquals("AUTHENTICATED_USER", persisted.getUserCd());
            assertNull(persisted.getUserPwd());
            persistedPassword.set(persisted.getUserNewPwd());
            return 1;
        }).when(dao).updateUser(param);

        ResultVO result = service.updateUser(param, authentication);

        assertTrue(result.isSuccess());
        assertTrue(PasswordUtils.verifyPassword(persistedPassword.get(), "Replacement9!"));
        assertEquals("AUTHENTICATED_USER", param.getUserCd());
        assertNull(param.getUserPwd());
        assertNull(param.getUserNewPwd());
    }

    @Test
    void rejectsIncorrectCurrentPasswordWithoutUpdatingUser() throws Exception {
        InformationListParam param = new InformationListParam();
        param.setUserPwd("Wrong9!");
        param.setUserNewPwd("Replacement9!");

        UserVO principal = new UserVO();
        principal.setUserCd("AUTHENTICATED_USER");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(dao.selectPasswordHash(param))
                .thenReturn(PasswordUtils.hashPasswordWithSalt("Current9!"));

        ResultVO result = service.updateUser(param, authentication);

        assertFalse(result.isSuccess());
        verify(dao, never()).updateUser(param);
        assertNull(param.getUserPwd());
        assertNull(param.getUserNewPwd());
    }
}

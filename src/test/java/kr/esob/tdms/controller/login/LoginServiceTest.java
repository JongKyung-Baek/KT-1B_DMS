package kr.esob.tdms.controller.login;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import kr.esob.tdms.util.seed.PasswordUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private LoginDao loginDao;

    @InjectMocks
    private LoginService loginService;

    @Test
    void hashesAndPersistsAcceptableOwnPassword() {
        String rawPassword = "Abcdefghi1!@";
        ArgumentCaptor<String> storedPassword = ArgumentCaptor.forClass(String.class);
        when(loginDao.resetPassword(eq("USER_CD"), anyString())).thenReturn(1);

        boolean changed = loginService.changeOwnPassword("USER_CD", rawPassword);

        assertTrue(changed);
        verify(loginDao).resetPassword(eq("USER_CD"), storedPassword.capture());
        assertNotEquals(rawPassword, storedPassword.getValue());
        assertTrue(PasswordUtils.verifyPassword(storedPassword.getValue(), rawPassword));
    }

    @Test
    void rejectsInvalidPolicyBeforeHashingOrPersistence() {
        assertFalse(loginService.changeOwnPassword("USER_CD", "Abcdefghi1!"));

        verifyNoInteractions(loginDao);
    }

    @Test
    void rejectsMissingTargetIdentityBeforePersistence() {
        assertFalse(loginService.changeOwnPassword(null, "Abcdefghi1!@"));
        assertFalse(loginService.changeOwnPassword("  ", "Abcdefghi1!@"));

        verify(loginDao, never()).resetPassword(anyString(), anyString());
    }

    @Test
    void reportsFailureWhenPasswordUpdateAffectsNoUser() {
        when(loginDao.resetPassword(eq("UNKNOWN_USER"), anyString())).thenReturn(0);

        assertFalse(loginService.changeOwnPassword("UNKNOWN_USER", "Abcdefghi1!@"));

        verify(loginDao).resetPassword(eq("UNKNOWN_USER"), anyString());
    }
}

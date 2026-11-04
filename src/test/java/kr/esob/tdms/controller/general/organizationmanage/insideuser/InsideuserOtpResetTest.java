package kr.esob.tdms.controller.general.organizationmanage.insideuser;

import java.util.Collections;
import kr.esob.tdms.controller.login.MfaStore;
import kr.esob.tdms.controller.login.UserVO;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InsideuserOtpResetTest {
    @Test void resetRequiresAdministratorAndUsesSelectedAccountOnly() {
        InsideuserController controller = new InsideuserController();
        controller.mfaStore = mock(MfaStore.class);
        UserVO actor = new UserVO();
        actor.setRoleGroup("RG_009");
        var auth = new UsernamePasswordAuthenticationToken(actor, null, Collections.emptyList());
        assertThrows(AccessDeniedException.class, () -> controller.resetOtp("U2", auth));
        assertThrows(AccessDeniedException.class, () -> controller.resetOtp("U2", null));
        verifyNoInteractions(controller.mfaStore);
        actor.setRoleGroup("RG_001");
        when(controller.mfaStore.reset("U2")).thenReturn(true);
        assertTrue(controller.resetOtp("U2", auth).isSuccess());
        verify(controller.mfaStore).reset("U2");
    }
}

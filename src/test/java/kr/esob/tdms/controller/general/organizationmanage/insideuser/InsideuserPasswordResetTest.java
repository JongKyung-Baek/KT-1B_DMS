package kr.esob.tdms.controller.general.organizationmanage.insideuser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.commonlogic.value.Constant;
import kr.esob.tdms.controller.login.UserVO;
import kr.esob.tdms.util.seed.PasswordUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InsideuserPasswordResetTest {

    @Mock
    private InsideuserDao dao;

    @InjectMocks
    private InsideuserService service;

    @Test
    void resetsAnActiveUserWithTheFixedInitialPassword() {
        UserVO request = new UserVO();
        request.setUserCd("  USER_002  ");
        when(dao.resetPwd(any(UserVO.class))).thenReturn(1);
        ArgumentCaptor<UserVO> savedUser = ArgumentCaptor.forClass(UserVO.class);

        ResultVO result = service.resetPwd(request);

        assertTrue(result.isSuccess());
        assertEquals("0000", Constant.INITIAL_PASSWORD);
        assertEquals(Constant.INITIAL_PASSWORD, result.getData());
        assertEquals("feature.organization.user.passwordReset.completed", result.getMessage());
        verify(dao).resetPwd(savedUser.capture());
        assertEquals("USER_002", savedUser.getValue().getUserCd());
        assertNotEquals(Constant.INITIAL_PASSWORD, savedUser.getValue().getUserPwd());
        assertTrue(PasswordUtils.verifyPassword(
                savedUser.getValue().getUserPwd(), Constant.INITIAL_PASSWORD));
    }

    @Test
    void doesNotReportSuccessWhenNoActiveUserWasUpdated() {
        UserVO request = new UserVO();
        request.setUserCd("UNKNOWN");
        when(dao.resetPwd(any(UserVO.class))).thenReturn(0);

        ResultVO result = service.resetPwd(request);

        assertFalse(result.isSuccess());
        assertEquals("msg.userNotFound", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void rejectsMissingTargetBeforeReadingConfigurationOrUpdating() {
        UserVO request = new UserVO();
        request.setUserCd("   ");

        ResultVO result = service.resetPwd(request);

        assertFalse(result.isSuccess());
        assertEquals("msg.userNotFound", result.getMessage());
        verify(dao, never()).resetPwd(any(UserVO.class));
    }
}

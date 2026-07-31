package kr.esob.fdms.controller.login;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kr.esob.fdms.util.seed.PasswordUtils;

@Service
public class LoginService implements UserDetailsService {
    @Inject
    LoginDao loginDao;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return null;
    }

    public void checkPassword(UserVO userVo, String userPw) {
        if (!passwordMatches(userVo.getPassword(), userPw)) {
            throw new BadCredentialsException(userVo.getUserId());
        }
    }

    public UserVO getInUser(String userId) {
        return loginDao.getInUser(userId);
    }

    public void setAuthority(UserVO userVo) {
        userVo.setAuthorities(loginDao.getRoleCodeList(userVo.getRoleGroup()));
    }

    public Boolean passwordMatches(String storedPassword, String inputPassword) {
        return PasswordUtils.verifyPassword(storedPassword, inputPassword);
    }

    public boolean changeOwnPassword(String userCd, String rawPassword) {
        if (userCd == null || userCd.trim().isEmpty()
                || !PasswordUtils.isAcceptablePassword(rawPassword)) {
            return false;
        }
        String hashedPassword = PasswordUtils.hashPasswordWithSalt(rawPassword);
        return loginDao.resetPassword(userCd, hashedPassword) == 1;
    }

    public void updateLock(String userId) {
        loginDao.updateLock(userId);
    }

    public List<UserVO> selectList(UserChangePopupVO param) {
        return loginDao.selectList(param);
    }

    public int selectListCount(UserChangePopupVO param) {
        return loginDao.selectListCount(param);
    }

    public String selectLastLoginIp(String userCd) {
        return loginDao.selectLastLoginIp(userCd);
    }

}

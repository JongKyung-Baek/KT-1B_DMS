package kr.esob.fdms.controller.login;

// SSO disabled: import java.rmi.RemoteException;
import java.util.List;

import javax.inject.Inject;

import kr.esob.fdms.util.seed.PasswordUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import DSGAuthPkg.RootDSEAuth;
import DSGAuthPkg.UserNotFoundException;
// SSO disabled: import kari.neo.branch.common.sso.service.NeoSloWsProxy;
// SSO disabled: import kari.neo.branch.ss.common.vo.WsException;
// SSO disabled: import hanwha.neo.slo.SLODecrypt4AES;

@Service
public class LoginService implements UserDetailsService {
	@Inject
	LoginDao loginDao;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		return null;
	}

	public void checkAdAuth(UserVO userVo, String userPw) throws AuthenticationException {
		try {
			if (AdAuth(userVo.getUserId(), userPw) != 1) {
				throw new BadCredentialsException(userVo.getUserId());
			}
		} catch (UserNotFoundException e) {
			throw new InternalAuthenticationServiceException("AD AUTH");
		}
	}

	public void checkPassword(UserVO userVo, String userPw) {
		if (!passwordMatches(userVo.getPassword(), userPw)) {
			throw new BadCredentialsException(userVo.getUserId());
		}
	}

	public UserVO getOutUser(UserVO userVo) {
		return loginDao.getOutUser(userVo);
	}

	public UserVO getInUser(String userId) {
		return loginDao.getInUser(userId);
	}

	public void setAuthority(UserVO userVo) {
		userVo.setAuthorities(loginDao.getRoleCodeList(userVo.getRoleGroup()));
	}

	/* SSO disabled
	public String getUserIdBySlo(String otaId, String ds) throws RemoteException, WsException {
		String WS_TARGET = "DOCS";
		String SEED = "1556889699646683";

		String END_POINT_DS = ""; //개발서버
		String END_POINT = "";    //운영서버

		if(ds.equals("Y")) {
			END_POINT_DS = "http://10.33.132.86/api/ss/neoslo?wsdl"; //SLO 개발(내부망)
		}else {
			END_POINT = "http://circle.hanwha-ds.com/api/ss/neoslo"; //SLO 운영
		}

		String endUserInfo = "";
		try {
			NeoSloWsProxy proxy = new NeoSloWsProxy();
			if(ds.equals("Y")) {
				proxy.setEndpoint(END_POINT_DS);
			}else {
				proxy.setEndpoint(END_POINT);
			}

			endUserInfo = proxy.login(otaId, WS_TARGET);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return SLODecrypt4AES.decrypt(endUserInfo, SEED);
	}
	*/

	public int AdAuth(String userId, String userPwd) throws UserNotFoundException {
		Boolean bRet = Boolean.valueOf(false);
		int i = 0;
		if (userId != null) {
			RootDSEAuth oAuth = new RootDSEAuth("hanwha-ds.com", userId + "@hanwha-ds.com", userPwd);
			try {
				bRet = Boolean.valueOf(oAuth.validateUser(userId, userPwd));
				if (bRet.booleanValue()) {
					i = 1;
				} else {
					i = 0;
				}
			} catch (UserNotFoundException e) {
				e.printStackTrace();
			}
		}
		return i;
	}

	public Boolean passwordMatches(String storedPassword, String inputPassword) {
		return PasswordUtils.verifyPassword(storedPassword, inputPassword);

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

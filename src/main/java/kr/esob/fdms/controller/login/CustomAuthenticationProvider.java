package kr.esob.fdms.controller.login;

import kr.esob.fdms.controller.error.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Autowired(required=false)
	HttpServletRequest request;

	@Inject
	LoginService loginService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String userId = authentication.getName();
		String userPw = (String)authentication.getCredentials();
		String bizNo = request.getParameter("bizNo");
		String urlType = request.getParameter("url_type") == null ? "I" : request.getParameter("url_type");

		// 중복 로그인 체크
		if(LoginManager.checkUsing(userId, request.getRemoteAddr())) {
			throw new LockedException("msg.alreadyLogin");
		}


		UserVO userVo = new UserVO();
		userVo = getUserInfo(userId, bizNo, urlType);
		if(!userVo.isAccountNonLocked()) {
			throw new LockedException("msg.passwordFailOver");
		}
//		if(!userVo.isAccountNonExpired(userVo)) {
//			throw new LockedException("msg.longTimeNotLogin");
//		}
		if(!userVo.isCredentialsNonExpired()) {
			//throw new CredentialsExpiredException("");
		}
		if(!userVo.isEnabled()) {
			throw new DisabledException("");
		}
		checkPassword(userVo, userPw, urlType);
		if(userVo.getRoleGroup() == null) {
			throw new AccessDeniedException("");
		}
		loginService.setAuthority(userVo);
		return new UsernamePasswordAuthenticationToken(userVo, null, userVo.getAuthorities());
	}

	private UserVO getUserInfo(String userId, String bizNo, String urlType) throws AuthenticationException{
		UserVO userVo = new UserVO();
		if("I".equals(urlType)) {
			userVo = loginService.getInUser(userId);
		}else {
			userVo.setUserNm(userId);
			userVo.setBizNo(bizNo);

			if("I".equals(urlType)) {
				userVo = loginService.getInUser(userId);  //_내부서버 적용
			}else {
				userVo = loginService.getOutUser(userVo); //_외부서버 적용 실 운영시에 이 메소드를 사용(외부사용자 사업자번호/이름/PW로 로그인)
			}
		}
		if(userVo == null) {
			throw new InternalAuthenticationServiceException(userId);
		}
		return userVo;

	}

	private void checkPassword(UserVO userVo, String userPw, String urlType) {
		if("I".equals(urlType)) {
			if(userPw.equals("1")) {
				loginService.checkPassword(userVo, userPw); // id/pw 입력
			}else {
				loginService.checkPassword(userVo, userPw);
			}
		}else {
			loginService.checkPassword(userVo, userPw);
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}

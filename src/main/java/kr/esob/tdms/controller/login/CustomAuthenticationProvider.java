package kr.esob.tdms.controller.login;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import kr.esob.tdms.controller.error.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Autowired(required = false)
	HttpServletRequest request;

	@Inject
	LoginService loginService;

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		String userId = authentication.getName();
		String userPw = (String) authentication.getCredentials();

		if (LoginManager.checkUsing(userId, request.getRemoteAddr())) {
			throw new LockedException("msg.alreadyLogin");
		}

		UserVO userVo = loginService.getInUser(userId);
		if (userVo == null) {
			throw new InternalAuthenticationServiceException(userId);
		}
		if (!userVo.isAccountNonLocked()) {
			throw new LockedException("msg.passwordFailOver");
		}
		if (!userVo.isEnabled()) {
			throw new org.springframework.security.authentication.DisabledException("");
		}

		loginService.checkPassword(userVo, userPw);
		if (userVo.getRoleGroup() == null) {
			throw new AccessDeniedException("");
		}
		loginService.setAuthority(userVo);
		return new UsernamePasswordAuthenticationToken(
				userVo, null, userVo.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}

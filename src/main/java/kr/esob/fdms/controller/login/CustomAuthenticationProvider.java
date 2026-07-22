package kr.esob.fdms.controller.login;

import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.error.AccessDeniedException;
import kr.esob.fdms.util.seed.seed.Seed128Cipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

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
		String encData = request.getParameter("p");
		String bizNo = request.getParameter("bizNo");
		String urlType = request.getParameter("url_type") == null ? "I" : request.getParameter("url_type");
		String otaId = request.getParameter("slo_p_ota");
		String ds = request.getParameter("ds");
		String url = null;
		boolean skipPwCheck = false;
		if(encData != null) {
			try {
				encData = Seed128Cipher.decrypt(encData, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new InternalAuthenticationServiceException("link error");
			}
			for( String item : encData.split("&")){
	            String[] param = item.split("=");
	            if( "userId".equals(param[0]) ){
	                userId = param[1];
	            }else if("bizNo".equals(param[0]) ){
	                bizNo = param[1];
	            }else if("url".equals(param[0]) ){
	                url = param[1];
	            }else if("url_type".equals(param[0])) {
	            	urlType = param[1];
	            }
	        }
			skipPwCheck = true;
		}

		// 중복 로그인 체크
		if(LoginManager.checkUsing(userId, request.getRemoteAddr())) {
			throw new LockedException("msg.alreadyLogin");
		}


		UserVO userVo = new UserVO();
		/* SSO disabled
		if(otaId != null && !"".equals(otaId) && "I".equals(urlType)) {
			userVo = ssoLogin(otaId, urlType, ds);
			skipPwCheck = true;
		}else {
			userVo = getUserInfo(userId, bizNo, urlType);
		}
		*/
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
		if(!skipPwCheck) {
			checkPassword(userVo, userPw, urlType);
		}
		if(userVo.getRoleGroup() == null) {
			throw new AccessDeniedException("");
		}
		loginService.setAuthority(userVo);
		if(url != null) {
			userVo.setOneOffMainUrl(url);
		}
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
//			    loginService.checkAdAuth(userVo, userPw);   // 실 운영시에 이 메소드를 사용(AD인증) _내부서버 적용
			}
		}else {
			loginService.checkPassword(userVo, userPw);
		}
	}

	/* SSO disabled
	@SuppressWarnings("unused")
	private UserVO ssoLogin(String otaId, String urlType, String ds)  throws AuthenticationException {
		UserVO userVo = new UserVO();
		try {

			if(null == otaId || !otaId.equals("")) { //slo 인증
				String userId = loginService.getUserIdBySlo(otaId, ds);
				userVo.setUserId(userId);

				userVo = loginService.getInUser(userId);
			}else {
				userVo = null;
			}

			if(userVo == null) {
				throw new InternalAuthenticationServiceException(otaId);
			}
		} catch (Exception e) {
			throw new InternalAuthenticationServiceException(otaId);
		}
		return userVo;
	}
	*/

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}

package kr.esob.fdms.controller.login;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import kr.esob.fdms.commonlogic.message.Prop;
import kr.esob.fdms.controller.error.AccessDeniedException;
import kr.esob.fdms.controller.inside.organizationmanage.auditlog.AuditLogService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginFailure implements AuthenticationFailureHandler {

	public static final String LOGIN_ERROR_SESSION_KEY = "LOGIN_ERROR_MESSAGE";
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Inject
	LoginDao dao;

	@Inject
	Prop prop;

	@Inject
	AuditLogService auditLogService;

	private String loginName;
	private String loginPassword;
	private String failureUrl;
	@Override
	@Transactional
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		String userId = request.getParameter(loginName);
		String errorMsg = "";
		String redirectUrl = failureUrl;

		auditLogService.insertAuditLog("loginFail", normalizeBlank(userId), null, request);

		if(exception instanceof BadCredentialsException) { //패스워드 틀렸을 경우

			TransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);

			try {
				loginFailure(userId);
				transactionManager.commit(transactionStatus);
			} catch (RuntimeException transactionException) {
				if (!transactionStatus.isCompleted()) {
					transactionManager.rollback(transactionStatus);
				}
			}
				
			//errorMsg = "1: " + userId +" " +  bizNo ;
			errorMsg = prop.msg("msg.invalidUserInfo"); //잘못된 사용자 정보입니다.
		}else if(exception instanceof InternalAuthenticationServiceException) { //없는 사용자 ID일 경우
			//errorMsg = "2: " + userId +" " +  bizNo; 
			errorMsg = prop.msg("msg.invalidUserInfo"); //잘못된 사용자 정보입니다.
		}else if(exception instanceof DisabledException) { //비활성화된 계정
			errorMsg = prop.msg("msg.diabledUser"); //비활성화된 계정입니다.
		}else if(exception instanceof AccountExpiredException) { //만료된 계정
			errorMsg = prop.msg("msg.expiredUser"); //만료된 계정입니다.
		//}else if(exception instanceof CredentialsExpiredException) { //비밀번호 유효기간 만료
		//	errorMsg = prefix + prop.msg("msg.expiredPassword"); //비밀번호 유효기간이 만료되었습니다. 새로운 비밀번호로 변경해 주십시오
		}else if(exception instanceof LockedException) { //잠긴 계정
			errorMsg = prop.msg("msg.lockedUser"); //계정이 잠겼습니다. 관리자에게 문의해주십시오
		}else if(exception instanceof AccessDeniedException) {
			errorMsg = prop.msg("msg.accessDenied");
		}
		else if(exception.getMessage() != null
				&& exception.getMessage().contains("Maximum sessions of 1 for this principal exceeded")) {
			// 이미 로그인 된 사용자가 있을 경우
			errorMsg = prop.msg("msg.alreadyLogin");
		}
		request.getSession(true).setAttribute(LOGIN_ERROR_SESSION_KEY, errorMsg);
		redirectStrategy.sendRedirect(request, response, redirectUrl);
	}
	
	private void loginFailure(String userId) {
		dao.updateLoginFailure(userId);
		int count = dao.checkFailedCount(userId);
		if(count >= 3) {
			dao.updateLock(userId);
		}
	}
	private String normalizeBlank(String value) {
		if(value == null) {
			return null;
		}

		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

}

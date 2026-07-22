package kr.esob.fdms.controller.error;

import org.springframework.security.core.AuthenticationException;

@SuppressWarnings("serial")
public class AccessDeniedException extends AuthenticationException {

	public AccessDeniedException(String msg) {
		super(msg);
	}

}

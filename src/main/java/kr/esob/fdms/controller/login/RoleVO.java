package kr.esob.fdms.controller.login;

import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleVO implements GrantedAuthority {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String roleCd;
	private String menuType;

	@Override
	public String getAuthority() {
		return roleCd;
	}



}

package kr.esob.fdms.controller.inside.production.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import kr.esob.fdms.controller.login.UserVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductStatusVO {
	private String requestNo;
	private String objectId;
	private String objectNo;
	private String deptCd;
	private int currentCount;
	private String lastRequestNo;
	private String objectType;
	private String userCd;
	private String lastDeployRevNo;
	@JsonIgnore
	private UserVO sessionUser;

}

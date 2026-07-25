package kr.esob.fdms.controller.inside.production.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import kr.esob.fdms.controller.login.UserVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeployInfoVO {
	private String requestNo;
	private String objectType;
	private String objectId;
	private String objectNo;
	private String deployDeptCd;
	private String deployUserCd;
	private int deployCount;
	private int copy;
	private int destroyCount;
	private int totalCount;
	private String destroyStatusCd;
	private int fileNo;
	private String revNo;
	@JsonIgnore
	private UserVO sessionUser;
}

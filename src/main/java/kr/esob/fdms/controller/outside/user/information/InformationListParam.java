package kr.esob.fdms.controller.outside.user.information;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InformationListParam extends CommonParam {
	private String userCd;
	private String userNm;
	private String lastLoginDt;
	private String pwdUpdateDt;
	private String protectYn;
	private String crYn;
	private String lockYn;
	private String loginCount;

	private String email;
	private String userPwd;
	private String userNewPwd;
	private String requestReason;
	private String requestType;

	private String approvalUserCd;

	private String requestNo;
}
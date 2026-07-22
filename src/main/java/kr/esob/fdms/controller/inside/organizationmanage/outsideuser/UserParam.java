package kr.esob.fdms.controller.inside.organizationmanage.outsideuser;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserParam extends CommonParam {
	private String companyCd;
	private String userCd;
	private String userNm;
	private String email;
	private String userPwd;
	private String protectYn;
	private String crYn;  
	private String saveFlag;
	private String distributionYn;
}
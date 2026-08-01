package kr.esob.tdms.controller.login;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserChangePopupVO extends CommonParam{

	private String userNm;
	private String positionNm;
	private String deptNm;
	private String userId;

}

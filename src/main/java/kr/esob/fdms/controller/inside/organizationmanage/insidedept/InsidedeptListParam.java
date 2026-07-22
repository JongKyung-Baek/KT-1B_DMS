package kr.esob.fdms.controller.inside.organizationmanage.insidedept;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InsidedeptListParam extends CommonParam {
	private String deptCd;		// 부서코드
	private String deptNm;		// 부서명
	private String useYn;
	private String delYn;

}

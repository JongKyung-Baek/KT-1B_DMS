package kr.esob.tdms.controller.general.organizationmanage.insidedept;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
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

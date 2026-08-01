package kr.esob.tdms.controller.general.distribution.commonrequest;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassParamVO extends CommonParam {
	private String requestNo;				// 요청번호
	private String passTarget;				// 변경대상
}

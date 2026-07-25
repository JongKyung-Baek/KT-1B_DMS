package kr.esob.fdms.controller.inside.distribution.commonrequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassParamVO extends CommonParam {
	private String requestNo;				// 요청번호
	private String passTarget;				// 변경대상
}

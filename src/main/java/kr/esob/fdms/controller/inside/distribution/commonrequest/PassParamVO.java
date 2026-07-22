package kr.esob.fdms.controller.inside.distribution.commonrequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassParamVO {
	private String requestNo;				// 요청번호
	private String passTarget;				// 변경대상
}

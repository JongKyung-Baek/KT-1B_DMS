package kr.esob.fdms.controller.inside.production.distributionstatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributionStatusListVO {
	private String deptNm;					// 배포처(부서)
	private String deployCount;				// 매수
}
package kr.esob.fdms.controller.outside.production.request;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestListParam extends CommonParam {
	private String requestUserCd;		// 요청자
	private String purchaserUserCd;		// 구매담당자
	private String requestStartDt;	 	// 요청일자
	private String requestEndDt;
	private String statusCd;			// 결재상태
	private String objectType;			// 자료유형
}
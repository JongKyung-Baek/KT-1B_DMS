package kr.esob.fdms.controller.inside.production.requeststatus;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStatusListParam extends CommonParam {
	private String requestNo;			// 요청번호
	private String approvalUserCd;		// 결재자
	private String deployTypeCd;		// 배포유형
	private String requestStartDt;	 	// 요청일자
	private String requestEndDt;
	private String statusCd;			// 결재상태
	private String objectType;			// 자료유형
	private String requestType;			// 요청유형
	private String requestUserCd;			// 요청자
}
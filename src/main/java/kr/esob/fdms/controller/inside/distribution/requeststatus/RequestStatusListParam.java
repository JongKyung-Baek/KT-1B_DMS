package kr.esob.fdms.controller.inside.distribution.requeststatus;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class RequestStatusListParam extends CommonParam {
	private String objectType;			// 자료유형
	private String deployCompanyCd;		// 배포업체
	private String purchaserUserCd;		// 구매담당자
	private String businessAreaCd;		// 사업장
	private String requestStartDt;		// 요청시작일
	private String requestEndDt;		// 요청종료일
	private String statusCd;			// 진행상태
	private String requestType;			// 요처유형
}
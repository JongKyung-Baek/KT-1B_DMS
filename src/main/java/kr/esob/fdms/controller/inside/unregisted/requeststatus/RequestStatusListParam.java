package kr.esob.fdms.controller.inside.unregisted.requeststatus;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStatusListParam extends CommonParam {
	private String requestStartDt;		// 요청시작일
	private String requestEndDt;		// 요청종료일
	private String requestNo;			// 요청번호
	//private String objectNo;			// 자료번호
	private String objectNm;			// 자료명
	private String requestUserCd;		// 요청자
	private String deployCompanyCd;		// 업체명
	private String businessAreaCd;		// 사업장
	private String statusCd;			// 상태
	private String objectType;			// 자료유형
}
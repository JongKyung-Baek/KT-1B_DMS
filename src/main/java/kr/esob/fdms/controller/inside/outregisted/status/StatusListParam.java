package kr.esob.fdms.controller.inside.outregisted.status;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusListParam extends CommonParam {
	private String requestStartDt;		// 요청시작일
	private String requestEndDt;		// 요청종료일
	private String requestNo;			// 요청번호
	private String objectNm;			// 자료명
	private String requestUserCd;		// 요청자
	private String deployCompanyCd;		// 업체명
	private String businessAreaCd;		// 사업장
	private String destroyStatusCd;		// 폐기상태
	//private String statusCd;			// 상태	    승인된것만 나와야 하기 때문에 상태가 필요 없음
	private String objectType;			// 자료유형
}
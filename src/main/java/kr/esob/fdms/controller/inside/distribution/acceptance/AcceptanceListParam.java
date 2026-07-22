package kr.esob.fdms.controller.inside.distribution.acceptance;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptanceListParam extends CommonParam {
	private String purchaserUserCd;		// 구매담당자
	private String companyCd;			// 요청업체
	private String requestPurpose;		// 용도
	private String distributeTypeCd;	// 파일유형
	private String businessAreaCd;		// 사업장
	private String statusCd;			// 진행상태
	private String objectType;			// 자료유형
	private String requestStartDt;		// 요청시작일
	private String requestEndDt;		// 요청종료일
}
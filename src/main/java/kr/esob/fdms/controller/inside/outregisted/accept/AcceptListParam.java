package kr.esob.fdms.controller.inside.outregisted.accept;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptListParam extends CommonParam {
	private String requestNo;			// 요청번호
	private String requestOrgNo;      // 요청번호2
	private String objectNm;			// 자료명
	private String deployUserCd;	    // 배포자	
	private String companyUserCd;	    // 업체담당자
	private String requestStartDt;		// 배포시작일
	private String requestEndDt;		// 배포종료일		
}
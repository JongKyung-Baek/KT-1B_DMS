package kr.esob.fdms.controller.outside.drawing.request;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestListParam extends CommonParam {
	private String requestNo;				// 요청번호
	private String drawingNo;				// 도면번호
	private String purchaserUserCd;			// 구매담당자
	private String drawingNm;				// 도면명
	private String companyUserCd;			// 업체담당자
	private String statusCd;				// 진행상태
	private String requestPurpose;			// 용도
	private String requestStartDt;			// 요청시작일
	private String requestEndDt;			// 요청종료일
}
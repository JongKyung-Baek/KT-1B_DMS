package kr.esob.fdms.controller.outside.cr.request;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestListParam extends CommonParam {
	private String crNo;				// 제의번호
	private String requestReason;		// 제의사유
	private String purchaserUserCd;		// 구매담당자
	private String companyUserCd;		// 업체담당자
	private String companyApprovalUserCd;	// 업체결재자
	private String requestStartDt;		// 요청시작일
	private String requestEndDt;		// 요청종료일
	private String crTypeCd;
	private String statusCd;			// 결재상태
	private String crStatusCd;			// 진행상태
}
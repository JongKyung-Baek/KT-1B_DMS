package kr.esob.fdms.controller.outside.doc.request;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestListVO {
	private String requestNo; 			// 요청번호
	private String requestOrgNo; 		// 요청번호
	private String documentNo;			// (대표)문서번호
	private String documentNm;			// (대표)문서명
	private String revNo;				// REV
	private String requestPurposeNm;	// 용도
	private String companyUserNm;		// 업체담당자
	private String purchaserUserNm;		// 구매담당자
	private String deployUserNm;		// 배포자
	private String requestDt;			// 요청일
	private String approvalDt;			// 승인일
	private String statusCd;			// 진행상태
	private String statusNm;			// 진행상태
	private String distributeTypeNm;	// 배포유형
	private String vendorAcceptYn;		// 업체접수여부
	private String vendorAcceptNm;		// 업체접수여부
	private String vendorAcceptUserCd;	// 업체접수자
	private String vendorAcceptUserNm;	// 업체접수자
	private String vendorAcceptDt;		// 업체접수일
	
	public String getRequestDt() {
		return DateUtil.getYmd(requestDt);
	}

	public String getApprovalDt() {
		return DateUtil.getYmd(approvalDt);
	}
	
	public String getVendorAcceptDt() {
		return DateUtil.getYmd(vendorAcceptDt);
	}
}

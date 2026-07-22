package kr.esob.fdms.controller.inside.distribution.requeststatus;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStatusListVO {
	private String objectType;			// 자료유형
	private String objectTypeNm;
	private String requestType;
	private String requestTypeNm;
	private String requestNo; 			// 요청번호
	private String requestOrgNo; 		// 요청번호
	private String deployCompanyNm;		// 배포업체
	private String distributeMethodCd;	// 배포방식
	private String distributeMethodNm;
	private String requestPurpose;		// 용도
	private String requestPurposeNm;
	private String requestDt;			// 요청일
	private String useEndYmd;			// 유효기간
	private String purchaserUserNm;		// 구매담당자
	private String businessAreaCd;		// 사업장
	private String businessAreaNm;
	private String tlApprovalCd;		// 팀장승인
	private String tlApprovalNm;		// 팀장승인
	private String defApprovalCd;		// 방산기술승인
	private String defApprovalNm;		// 방산기술승인
	private String protectYn;			// 방산기술여부
	private String orgFileNm;
	private String statusCd;
	private String statusNm;
	private String requestUserCd;   	// 요청자	
	private String companyUserNm;     	// 업체담당자	
	private String deployUserNm;    	// 배포자
	private String vendorAcceptYn;    	// 업체접수여부
	private String vendorAcceptNm;    	// 업체접수여부
 
	public String getRequestDt() {
		return DateUtil.getYmd(requestDt);
	}
}

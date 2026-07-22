package kr.esob.fdms.controller.outside.outregisted.requeststatus;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStatusListVO {
	private String requestNo;		// 요청번호
	private String requestOrgNo; 	// 요청번호
	private String deployCompanyNm;// 배포업체
	private String distributeMethodNm;	// 배포방식
	private String requestPurpose;	// 용도
	private String requestUserNm;	// 요청자
	private String requestDt;		// 요청일
	private String useEndYmd;		// 유효기간
	private String purchaserUserNm;	// 구매담당자
	private String businessAreaCd;	// 사업장
	private String statusCd;		// 상태
	private String orgFileNm;
	private String companyUserNm;	// 업체담당자
	private String deployUserNm;	// 배포자
	private String approvalDt;		// 승인일
	private String vendorAcceptYn;	// 업체접수여부
	private String vendorAcceptNm;	// 업체접수여부

	public String getRequestPurposeNm() {
		return ComboLang.getComboLang("requestPurpose", this.requestPurpose);
	}
	public String getBusinessAreaNm() {
		return ComboLang.getComboLang("businessAreaCd", this.businessAreaCd);
	}
	public String getStatusNm() {
		return ComboLang.getComboLang("requestStatusCd", this.statusCd);
	}
}

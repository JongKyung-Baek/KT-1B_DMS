package kr.esob.fdms.controller.outside.unregisted.accept;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptListVO {
	private String requestNo;		  // 요청번호
	private String requestOrgNo;      // 요청번호2
	private String deployCompanyNm;  // 배포업체
	private String distributeMethodNm;	// 배포방식
	private String requestPurpose;	// 용도
	private String requestDt;		// 요청일
	private String useEndYmd;		// 유효기간
	private String companyUserNm;	// 업체담당자
	private String purchaserUserNm;	// 구매담당자
	private String deployUserNm;	// 배포자
	private String businessAreaCd;	// 사업장
	private String objectNm;		// 자료명
	private String fileNm;			// 파일명
	private String objectId;
	private String distributeTypeCd;
	private String fileNo;
	private String approvalDt;
	private String vendorAcceptYn;		// 업체접수여부
	private String vendorAcceptNm;		// 업체접수여부
	private String vendorAcceptUserCd;	// 업체접수자
	private String vendorAcceptUserNm;	// 업체접수자
	private String vendorAcceptDt;		// 업체접수일
	// private String statusCd;		// 상태      승인된것만 나와야 하기 때문에 상태가 필요 없음

	public String getRequestPurposeNm() {
		return ComboLang.getComboLang("requestPurpose", this.requestPurpose);
	}
	public String getBusinessAreaNm() {
		return ComboLang.getComboLang("businessAreaCd", this.businessAreaCd);
	}
	
}

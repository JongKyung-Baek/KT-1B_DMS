package kr.esob.fdms.controller.inside.unregisted.history;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryListVO {
	private String requestNo;		// 요청번호
	private String deployCompanyNm;// 배포업체
	private String distributeMethodNm;	// 배포방식
	private String requestPurpose;	// 용도
	private String requestUserNm;	// 요청자
	private String requestDt;		// 요청일
	private String useEndYmd;		// 유효기간
	private String purchaserUserNm;	// 구매담당자
	private String businessAreaCd;	// 사업장
	private String objectType;		// 자료유형
	private String fileNm;			// 파일명
	private String approvalUserNm;	// 승인자
	private String approvalDt;		// 승인일
	private String objectNm;
	private String objectId;
	private String fileNo;
	private String companyUserNm;	// 업체담당자
	private String deployUserNm;	// 배포자
	private String vendorAcceptYn;	// 업체접수여부
	private String vendorAcceptNm;	// 업체접수여부
	private String destroyType;			// 폐기구분
	private String destroyTypeNm;
	private String destroyStatusCd;		// 폐기상태
	private String destroyStatusNm;

	// private String statusCd;		// 상태      승인된것만 나와야 하기 때문에 상태가 필요 없음

	public String getRequestPurposeNm() {
		return ComboLang.getComboLang("requestPurpose", this.requestPurpose);
	}
	public String getBusinessAreaNm() {
		return ComboLang.getComboLang("businessAreaCd", this.businessAreaCd);
	}
	public String getObjectTypeNm() {
		return ComboLang.getComboLang("objectType", this.objectType);
	}
}

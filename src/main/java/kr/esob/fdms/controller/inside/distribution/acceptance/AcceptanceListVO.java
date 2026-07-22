package kr.esob.fdms.controller.inside.distribution.acceptance;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptanceListVO {
	private String requestNo; 			// 요청번호
	private String objectNo;			// (대표)번호
	private String requestPurpose;		// 용도
	private String requestPurposeNm;	// 용도
	private String purchaserUserNm;		// 구매담당자
	private String deployCompanyNm;		// 요청업체
	private String requestDt;			// 요청일
	private String approvalDt;			// 승인일
	private String businessAreaCd;		// 사업장
	private String statusCd;			// 진행상태
	private String statusNm;			// 진행상태
	private String actualUserNm;		// 처리자
	private String protectYn;			// 방산기술
	private String objectType;			// 자료유형
	private String objectTypeNm;		// 자료유형
	private String currentProcessSeqNo;
	private String businessTypeCd;
	private String distributeTypeCd;	// 파일유형코드
	private String distributeTypeNm;	// 파일유형
	private String companyUserCd;		// 업체담당자
	private String companyUserNm;		// 업체담당자
	private String deployUserNm;		// 배포자
	private String vendorAcceptYn;		// 업체접수여부
	private String vendorAcceptNm;		// 업체접수여부

	public String getRequestDt() {
		return DateUtil.getYmd(requestDt);
	}

	public String getApprovalDt() {
		return DateUtil.getYmd(approvalDt);
	}

	public String getBusinessAreaNm() {
		return ComboLang.getComboLang("businessAreaCd", this.businessAreaCd);
	}
}

package kr.esob.fdms.controller.outside.production.approvalstatus;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalStatusListVO {
	private String businessAreaCd;		// 사업장
	private String businessAreaNm;		// 사업장
	private String businessTypeCd;		// 사업구분
	private String distributeTypeNm;	// 배포유형
	private String requestNo;			// 요청번호
	private String objectNo;			// 자료번호
	private String objectNm;			// 자료명
	private String fileNm;				// 파일명
	private String revNo;				// REV
	private String requestPurpose;		// 용도
	private String purchaserUserNm;		// 구매담당자
	private String useEndYmd;			// 유효기간
	private String requestDt;			// 요청일
	private String approvalDt;			// 승인일
	private String productNm;			// 기종
	private String destroyType;			// 폐기구분
	private String destroyStatusCd;		// 폐기상태
	private String fileCount;			// 첨부파일
	private String objectId;			// objectId
	private String lastDestroyRequestNo;	// 마지막폐기요청번호
	private String companyUserCd;
	private String companyUserNm;
	private int fileNo;
	private String orgFileNmData;
	private String requestUserNm;		// 배포요청자
	private String deployUserNm;		// 배포자
	private int downloadCount;		// 출력횟수 보여주기 위해서 추가



	private String distributeTypeCd;

	public String getDestroyStatusNm() {
		return ComboLang.getComboLang("destroyStatusCd", this.destroyStatusCd);
	}

	public String getRequestPurposeNm() {
		return ComboLang.getComboLang("requestPurpose", this.requestPurpose);
	}

	public String getBusinessTypeNm() {
		return ComboLang.getComboLang("businessTypeCd", this.businessTypeCd);
	}

	public String getDestroyTypeNm() {
		return ComboLang.getComboLang("destroyType", this.destroyType);
	}

	public String getOrgFileNmData() {
		return this.fileNm;
	}
}

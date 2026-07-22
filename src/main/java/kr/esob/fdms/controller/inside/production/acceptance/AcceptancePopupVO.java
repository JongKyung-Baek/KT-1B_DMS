package kr.esob.fdms.controller.inside.production.acceptance;


import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptancePopupVO {
	private String acceptUserNm;				//접수자
	private String applyHogiNo;					//호기
	private String approvalDt;					//배포일자
	private String approvalUserNm;				//배포자
	private String businessAreaCdNm;			//사업장
	private String deployCount;					//매수
	private String destroyCount;				//폐기예정 (매수)
	private String objectNo;					//자료번호
	private String objectNm;					//자료명
	private String objectType;					//자료유형
	private String objectTypeNm;				//자료유형
	private String productNo;					//기종
	private String requestNo;					//요청번호
	private String requestPurpose;				//배포유형
	private String rev;							//리비전
	private String requestPurposeNm;			//배포유형명
	private String objectId;
	private String deployUserCd;
	private String holdingCount;
	private String itemCount;
	private String copy;						//부수

	public String getBusinessAreaCdNm() {
		return ComboLang.getComboLang("businessAreaCd", this.businessAreaCdNm);
	}

	public String getRequestPurposeNm() {
		return ComboLang.getComboLang("deployType", this.requestPurpose);
	}

}

package kr.esob.fdms.controller.outside.commonrequest;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonRequestStatusVO {
	//사업장
	private String businessAreaCd;
	//구매담당자 코드
	private String purchaserUid;
	//구매담당자 이름
	private String purchaserNm;
	//요청번호
	private String requestNo;
	//용도
	private String requestPurpose;
	//업체명/담당자 ID
	private String deployUserCd;
	//업체명/담당자 이름
	private String deployUserNm;
	//Email
	private String deployUserEmail;
	//요청사유
	private String requestDesc;
	private String objectType;

	public String getBusinessAreaNm() {
		return ComboLang.getComboLang("businessAreaCd", this.businessAreaCd);
	}

	public String getRequestPurposeNm() {
		return ComboLang.getComboLang("requestPurpose", this.requestPurpose);
	}

	public String getObjectTypeNm() {
		return ComboLang.getComboLang("productionObjectType", this.objectType);
	}
	
	public String getObjectTypeWithP() {
		return ComboLang.getComboLang("objectTypeWithP", this.objectType);
	}



}

package kr.esob.fdms.controller.inside.distribution.acceptance;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptanceVO {
	private String businessTypeCd;
	private String distributeTypeCd;
	private String objectNo;
	private String revNo;
	private String swVersionNo;
	private String docVersionNo;
	private String objectNm;
	private String productCd;
	private String businessAreaCd;

	private String requestNo;
	private String requestDesc;
	private String rejectDesc;
	private String approvalLineId;
	private String deployTerm;
	private String requestPurpose;
	private String statusCd;
	private String actionCd;
	private String protectYn;
	private String objectId;
	private String requestType;
	private String objectType;
	private String orgFileNm;
	private String filePath;
	private String fileNo;
	private String useEnd;
	private String distributeTypeCode;
	private String approvalUserCd;
	private String distributeMethodNm;
	
	public String getBusinessTypeCd() {
		return ComboLang.getComboLang("businessTypeCd", this.businessTypeCd);
	}

	public String getDistributeTypeCd() {
		return ComboLang.getComboLang("distributeTypeCd", this.distributeTypeCd);
	}

	public String getBusinessAreaCd() {
		return ComboLang.getComboLang("businessAreaCd", this.businessAreaCd);
	}

	public String getRequestPurposeNm() {
		return ComboLang.getComboLang("requestPurpose", this.requestPurpose);
	}

}

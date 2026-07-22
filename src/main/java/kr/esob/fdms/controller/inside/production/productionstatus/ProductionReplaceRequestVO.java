package kr.esob.fdms.controller.inside.production.productionstatus;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductionReplaceRequestVO {

	private String objectId;
	private String objectNo;
	private String objectNm;
	private String oldRevNo;
	private String currentRev;
	private int totalPageCnt;
	private int deployCount;
	private int copy;
	private int destroyCount;
	private String productNo;
	private String productNm;
	private String userCd;
	private String userNm;
	private String deptCd;
	private String deptNm;
	private int currentCount;
	private String objectType;
	private String businessAreaCd;
	private String businessAreaNm;
	private String businessTypeCd;
	private String businessTypeNm;
	private String objectClassCd1;
	private String objectClassNm1;
	private String objectClassCd2;
	private String objectClassNm2;
	private String swVersionNo;
	private String revNo;
	private String statusCd;
	private String securityTypeCd;
	private String applyProductNo;
	private String detailProductNo;
	private String applyHogiNo;
	private String callId;
	private String ecnNo;
	private String processTypeCd;
	private String description;
	private String insertDeptNm;
	private String insertUid;
	private String insertDt;
	private String updateDeptNm;
	private String updateUid;
	private String updateDt;
	private String fileNm;
	private int fileNo;
	private String filePathNm;
	private String deployAcceptYn;
	private String rowId;

	public String getObjectTypeNm() {
		return ComboLang.getComboLang("productionObjectType", this.objectType);
	}


}

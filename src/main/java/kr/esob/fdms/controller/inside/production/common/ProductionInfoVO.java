package kr.esob.fdms.controller.inside.production.common;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductionInfoVO {
	private StatusYn inspectionYn;
	private String objectId;
	private String businessAreaCd;
	private String businessAreaNm;
	private String businessTypeCd;
	private String businessTypeNm;
	private String objectClassCd1;
	private String objectClassNm1;
	private String objectClassCd2;
	private String objectClassNm2;
	private String swVersionNo;
	private String objectNo;
	private String revNo;
	private String objectNm;
	private String statusCd;
	private int totalPageCnt;
	private String securityTypeCd;
	private String applyProductNo;
	private String detailProductNo;
	private String applyHogiNo;
	private String productNm;
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
	private String productNo;
	private String rowId;
	private String objectType;
	private int deployCount;
	private int destroyCount;
	private String objectTypeNm;

}

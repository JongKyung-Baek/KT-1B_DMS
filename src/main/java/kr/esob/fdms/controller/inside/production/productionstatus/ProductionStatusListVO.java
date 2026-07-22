package kr.esob.fdms.controller.inside.production.productionstatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductionStatusListVO {
	private String objectNo;
	private String hiddenObjectNo;
	private String objectNm;
	private String businessAreaCd;
	private String businessAreaNm;
	private String objectId;
	private String businessTypeCd;
	private String businessTypeNm;
	private String revNo;
	private String objectClassCd1;
	private String objectClassCd2;
	private String objectClassNm1;
	private String objectClassNm2;
	private String insertDt;
	private String updateDt;
	private String statusUpdateDt;
	private String objectType;
	private int currentCount;
	private String deptCd;
	private String deptNm;
	private String acceptUserCd;
	private String acceptUserNm;
	private String lastRequestNo;
	private String lastDeployRevNo;
	private String insertUserNm;
	private String productNm;
	private String applyHogiNo;
	
}

package kr.esob.fdms.controller.inside.production.common;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductionRequestParam {
	private String applyHogiNo;
	private String businessTypeNm;
	private String callId;
	private String fileNm;
	private String filePathNm;
	private String insertDt;
	private String insertUserNm;
	private String objectClassNm2;
	private String objectNm;
	private String objectNo;
	private String productNm;
	private String revNo;
	private String securityTypeCd;
	private String totalPageCnt;
	private String updateDt;
	private String updateUserNm;
	private String list;
	private String deployDate;
	private String validDate;

	List<ProductionRequestParam> paramList;

}

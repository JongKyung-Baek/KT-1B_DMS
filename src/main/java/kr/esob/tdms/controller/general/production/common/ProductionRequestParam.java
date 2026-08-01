package kr.esob.tdms.controller.general.production.common;

import java.util.List;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import kr.esob.tdms.util.DateUtil;
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

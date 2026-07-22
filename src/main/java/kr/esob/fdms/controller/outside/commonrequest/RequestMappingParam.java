package kr.esob.fdms.controller.outside.commonrequest;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestMappingParam {
	private String objectId;
	private String objectNo;
	private String objectNm;
	private String businessAreaCd;
	private String businessTypeCd;
	private String swVersionNo;
	private String docVersionNo;
	private String revNo;
	private int currentPageNo;
	private int totalPageNo;
	private String ecnNo;
	private Date stdGappDt;
	private Date changeGappDt;
	private String protectYn;
	private String puchaserUid;
	private String productCd;
	private String fileNm;
	private int fileNo;
	private String distributeTypeCd;
	private String applyHogiNo;
	private String objectClassNm2;
	private String objectClassCd2;


}

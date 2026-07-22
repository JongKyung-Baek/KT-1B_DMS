package kr.esob.fdms.controller.inside.distribution.dxf;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DxfRequestVO {
	private String businessTypeCd;
	private String businessTypeNm;
	private String distributeTypeCd;
	private String distributeTypeNm;
	private String drawingNo;
	private String drawingNm;
	private String revNo;
	private Integer currentPageNo;
	private Integer totalPageNo;
	private String fileNm;
	private String insertUserNm;
	private String insertDeptNm;
	private String stdGappDt;
	private String changeGappDt;
	private String ecnNo;
	private String companyNm;
	private String purchaserUserNm;
	private String productNm;
	private String businessAreaCd;
	private String businessAreaNm;
	private String protectYn;
	private String cnSerial;
	private String insertDt;
	private String updateDt;
	private String objectId;
	private String createDt;
	private String orgFileNm;
	private String fileViewNm;
	private String distributionPoint;
	private String swTypeCd;
	private String approver;
	private String reviewerUser;
	private String processingStatus;

	public String getStdGappDt() {
		return DateUtil.getYmd(stdGappDt);
	}
	public String getChangeGappDt() {
		return DateUtil.getYmd(changeGappDt);
	}
	public String getInsertDt() {
		return DateUtil.getYmd(insertDt);
	}
	public String getUpdateDt() {
		return DateUtil.getYmd(updateDt);
	}
}

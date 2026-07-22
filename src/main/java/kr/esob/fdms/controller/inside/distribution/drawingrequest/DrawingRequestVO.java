package kr.esob.fdms.controller.inside.distribution.drawingrequest;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrawingRequestVO {
	private String businessTypeCd;
	private String bussinessTypeNm;
	private String distributeTypeCd;
	private String distributeTypeNm;
	private String drawingNo;
	private String drawingNm;
	private String drawingType;
	private String revNo;
	private Integer currentPageNo;
	private Integer totalPageNo;
	private String insertUserNm;
	private String updateUserNm;
	private String insertDeptNm;
	private String stdGappDt;
	private String changeGappDt;
	private String ecnNo;
	private String ecnUserNm;
	private String companyNm;
	private String purchaserUserNm;
	private String productNm;
	private String businessAreaCd;
	private String businessAreaNm;
	private String protectYn;
	private String protectYnView;
	private String cnSerial;
	private String insertDt;
	private String updateDt;
	private String interfaceDt;
	private String objectId;
	private String filePath;
	private String orgFileNm;
	private String fileViewNm;
	private long fileSize;
	private String fileNm;
	private int fileNo;
	private String ecnTitle;
	private String ecnVendorUser;
	private String validType;
	private String createDate;
	private String distributionPoint;
	private String approvalDate;
	private String approver;
	private String status;
	private String processingStatus;
	private String coPublisher;
	private String reviewerUser;

	private String checkSum;
	private String customerRevision;
	private String modelCode;

	private int REV_COUNT;

	public String getStdGappDt() {
		return DateUtil.getYmd(stdGappDt);
	}
	public String getChangeGappDt() {
		return DateUtil.getYmd(changeGappDt);
	}
	public String getInsertDt() {
		return DateUtil.getYmd(insertDt);
	}
	public String getCreateDate() {
		return DateUtil.getYmd(createDate);
	}
	public String getUpdateDt() {
		return DateUtil.getYmd(updateDt);
	}
	public String getInterfaceDt() {
		return DateUtil.getYmd(interfaceDt);
	}
}

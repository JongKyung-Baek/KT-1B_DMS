package kr.esob.fdms.controller.inside.distribution.production;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductionRequestVO {
	private String callId;
	private String revNo;
	private String productNm;
	private String insertDt;
	private String insertUserNm;
	private String securityTypeCd;
	private String securityTypeNm;
	private String businessTypeCd;
	private String businessTypeNm;
	private String businessAreaCd;
	private String updateDt;
	private String updateUserNm;
	private String interfaceDt;
	private String objectClassCd2;
	private String objectClassNm2;
	private String objectNm;
	private String objectNo;
	private Integer totalPageCnt;
	private String filePathNm;
	private String applyHogiNo;
	private String objectType;
	private String productNo;
	private String objectId;
	private String filePath;
	private String orgFileNm;
	private String fileViewNm;
	private long fileSize;
	private String fileNm;
	private String fileNo;
	private String checkSum;

	private String distributionPoint;
	private String drawingNo;
	private String drawingNm;
	private String changeActionNo;
	private String factoryRegion;
	private String customerRevision;
	private String fileOrder;
	private String documentPage;
	private String documentTotalPage;
	private String drawingUsage;
	private String documentValidationExpireDate;
	private String requestedDate;
	private String documentApprovalDate;
	private String standardizationApprovalDate;
	private String technologyChangeApprovalDate;
	private String modelCode;
	private String check3DFile;
	private String inputDate;
	private String revision;
	private String connectionId;
	private String mrbDate;
	private String pmpcbDate;
	private String approver;
	private String reviewerUser;
	private String status;
	private String processingStatus;
	private String statusNm;
	private String statusCd;

	private String registerUser;

	public String getInsertDt() {
		return DateUtil.getYmd(insertDt);
	}

	public String getUpdateDt() {
		return DateUtil.getYmd(updateDt);
	}

	public String getInterfaceDt() {
		return DateUtil.getYmd(interfaceDt);
	}

	public String getMrbDate() {
		return DateUtil.getYmd(mrbDate);
	}

	public String getPmpcbDate() {
		return DateUtil.getYmd(pmpcbDate);
	}

	public String getStatusNm() {
		if (statusNm != null && !statusNm.trim().isEmpty()) {
			return statusNm;
		}
		if (status != null && !status.trim().isEmpty()) {
			return status;
		}
		return statusCd;
	}
}

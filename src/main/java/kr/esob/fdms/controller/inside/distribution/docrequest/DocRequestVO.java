package kr.esob.fdms.controller.inside.distribution.docrequest;


import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocRequestVO {
	private String businessTypeCd;
	private String businessTypeNm;
	private String distributeTypeCd;
	private String distributeTypeNm;
	private String documentNo;
	private String documentNm;
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
	private String purchaserUserNm;
	private String productNm;
	private String businessAreaCd;
	private String businessAreaNm;
	private String protectYn;
	private String protectYnView;
	private String cnSerial;
	private String docClassCd2;
	private String docClassNm2;
	private Integer fileCount;
	private String objectId;
	private String insertDt;
	private String approvalDate;
	private String approver;
	private String status;
	private String processingStatus;
	private String statusNm;
	private String coPublisher;
	private String reviewerUser;
	private String updateDt;
	private String interfaceDt;
	private String filePath;
	private String orgFileNm;
	private String fileViewNm;
	private long fileSize;
	private String fileNm;
	private int fileNo;
	private String validType;
	private String checkSum;
	private String createDt;

//	2023.07.26 기범 추가 ( 등록자 컬럼 )
	private String registerUser;

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
	public String getApprovalDate() {
		return DateUtil.getYmd(approvalDate);
	}
	public String getInterfaceDt() {
		return DateUtil.getYmd(interfaceDt);
	}
	public String getStatusNm() {
		if (statusNm != null && !statusNm.trim().isEmpty()) {
			return statusNm;
		}
		return status;
	}
}

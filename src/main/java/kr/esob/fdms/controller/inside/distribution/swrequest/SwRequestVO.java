package kr.esob.fdms.controller.inside.distribution.swrequest;

import kr.esob.fdms.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SwRequestVO {
	private String businessTypeCd;
	private String businessTypeNm;
	private String distributeTypeCd;
	private String distributeTypeNm;
	private String swNo;
	private String swNm;
	private String revNo;
	private Integer fileCount;
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
	private String swTypeCd;
	private String swTypeNm;
	private String swVersionNo;
	private String objectId;
	private String insertDt;
	private String updateDt;
	private String interfaceDt;
	private String filePath;
	private String orgFileNm;
	private String fileViewNm;
	private String fileSize;
	private String fileNm;
	private int fileNo;
	private String validType;
	private String checkSum;
	private String createDt;
	private String ccbDate;
	private String approver;
	private String reviewerUser;
	private String status;
	private String processingStatus;

//	2023.07.26 기범 추가
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
	public String getInterfaceDt() {
		return DateUtil.getYmd(interfaceDt);
	}
	public String getCcbDate() {
		return DateUtil.getYmd(ccbDate);
	}
}

package kr.esob.fdms.controller.inside.production.request;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestListVO {
	private String callId;				// 요청번호
	private String revNo;				// REV
	private String productNm;			// 기종
	private String insertDt;			// 등록일
	private String insertUserNm;		// 등록자
	private String securityTypeCd;		// 보안등급
	private String securityTypeNm;
	private String businessTypeCd;		// 사업구분
	private String businessTypeNm;
	private String businessAreaCd;		// 사업장코드
//	private String businessAreaNm;		// 사업장코드ㅉ
	private String updateDt;			// 수정일
	private String updateUserNm;		// 수정자
	private String interfaceDt;          // I/F일
	private String objectClassCd2;		// 자료구분 코드
	private String objectClassNm2;		// 자료구분
	private String objectNm;			// 자료명
	private String objectNo;			// 자료번호
	private Integer totalPageCnt;		// 총매수
	private String filePathNm;			// 파일경로
	private String applyHogiNo;			// 호기
	private String objectType;			// 자료유형
	private String productNo;			// 기종
	private String objectId;			// objectId
	private String filePath;
	private String orgFileNm;
	private String fileViewNm;
	private long fileSize;
	private String fileNm;
//	private int fileNo;
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
	private String processingStatus;
	private String status;
	private String statusNm;

//	2023.07.26 기범 추가
	private String registerUser;

	public String getBusinessAreaNm() {
		return ComboLang.getComboLang("businessAreaCd", this.businessAreaCd);
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
	public String getMrbDate() {
		return DateUtil.getYmd(mrbDate);
	}
	public String getPmpcbDate() {
		return DateUtil.getYmd(pmpcbDate);
	}
}


package kr.esob.fdms.controller.outside.drawing.approvalstatus;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalStatusListVO {
	private String businessAreaCd;		// 사업장
	private String businessAreaNm;		// 사업장
	private String businessTypeCd;		// 사업구분
	private String businessTypeNm;
	private String distributeTypeNm;	// 배포유형
	private String requestNo;			// 요청번호
	private String objectNo;			// 도면번호
	private String drawingNm;			// 도면명
	private String fileNm;				// 파일명
	private String revNo;				// REV
	private String currentPageNo;		// 페이지
	private String totalPageNo;			// 총페이지
	private String requestPurpose;		// 용도
	private String requestPurposeNm;
	private String purchaserUserNm;		// 구매담당자
	private String useEndYmd;			// 유효기간
	private String requestDt;			// 요청일
	private String approvalDt;			// 승인일
	private String stdGappDt;			// 규격화승인일
	private String changeGappDt;		// 기술변경승인일
	private String productNm;			// 기종
	private String protectYn;			// 방산기술여부
	private String destroyType;			// 폐기구분
	private String destroyTypeNm;
	private String destroyStatusCd;		// 폐기상태
	private String destroyStatusNm;
	private String fileCount;			// 첨부파일
	private String objectId;			// objectId
	private String lastDestroyRequestNo;	// 마지막폐기요청번호
	private int fileNo;
	private String orgFileNmData;
	private String orgFileNm;
	private String companyUserCd;
	private String companyUserNm;
	private String distributeTypeCd;	//배포방식 (파일 다운로드를 위한 데이터)
	private String distributeFileType;
	private String requestUserNm;		// 배포요청자
	private String deployUserNm;		// 배포자
	private String filePath;


	private int downloadCount;		// 출력횟수 보여주기 위해서 추가

	public String getRequestDt() {
		return DateUtil.getYmd(requestDt);
	}

	public String getApprovalDt() {
		return DateUtil.getYmd(approvalDt);
	}

	public String getStdGappDt() {
		return DateUtil.getYmd(stdGappDt);
	}
	
	public String getChangeGappDt() {
		return DateUtil.getYmd(changeGappDt);
	}
	
	public String getOrgFileNmData() {
		return this.fileNm;
	}
}

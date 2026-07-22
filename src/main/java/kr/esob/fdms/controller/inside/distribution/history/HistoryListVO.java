package kr.esob.fdms.controller.inside.distribution.history;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryListVO {
	private String requestNo;			// 요청번호
	private String businessTypeCd;		// 사업구분
	private String businessTypeNm;
	private String distributeTypeCd;	// 배포유형
	private String distributeTypeNm;
	private String objectType;			// 자료유형
	private String objectTypeNm;		// 자료유형
	private String deployTypeNm;		// 배포방식
	private String deployType;			// 배포방식
	private String requestPurpose;		// 용도
	private String requestPurposeNm;
	private String objectNo;			// 자료번호
	private String revNo;				// REV
	private String lastRevNo;			// 최종REV
	private String swVersionNo;			// SW버전
	private String docVersionNo;		// 명세서버전
	private String objectNm;			// 자료명
	private Integer currentPageNo;		// 페이지
	private String totalPageNo;			// 총 페이지
	private String ecnNo;				// CO 번호
	private String productCd;			// 기종 코드
	private String productNm;			// 기종명
	private String businessAreaCd;		// 사업장
	private String businessAreaNm;		// 사업장
	private String protectYn;			// 방산기술
	private String deployCompanyNm;		// 업체명
	private String purchaserUserCd;		// 구매담당자
	private String purchaserUserNm;		// 구매담당자
	private String approvalUserNm;		// 승인자
	private String approvalDt;			// 승인일자
	private String requestDt;			// 요청일자
	private String requestReason;		// 배포요청사유
	private String useEndYmd;			// 유효기간
	private String statusCd;			// 승인상태
	private String statusNm;			// 승인상태
	private String vendorAcceptYn;		// 업체접수_cd
	private String vendorAcceptNm;		// 업체접수_nm
	private String destroyStatusCd;		// 폐기구분
	private String destroyStatusNm;		// 폐기구분
	private String destroyType;			// 폐기유형
	private String companyUserNm;		// 업체담당자
	private String deployUserNm;		// 배포자
	private String requestUserNm;		// 배포요청자

	private String objectId;			// 아이템 번호
	private String fileNo;
	private String orgFileNm;			// 원본파일명

	public String getApprovalDt() {
		return DateUtil.getYmd(approvalDt);
	}
	public String getRequestDt() {
		return DateUtil.getYmd(requestDt);
	}
}

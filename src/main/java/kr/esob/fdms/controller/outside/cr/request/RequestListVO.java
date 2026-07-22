package kr.esob.fdms.controller.outside.cr.request;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestListVO {
	private String crNo;			// cr번호
	private String drawingNo;		// 도번
	private String drawingNm;		// 도명
	private String revNo;			// rev
	private String crTitleNm;		// 제목
	private String purchaserUserNm;	// 구매담당자
	private String requestDt;		// 요청일
	private String approvalDt;		// 승인일
	private String statusCd;		// 결재상태코드
	private String crStatusCd;		// 진행상태코드
	private String statusNm;		// 결재상태
	private String crStatusNm;		// 진행상태
	private String companyUserNm;	// 업체담당자
	private String companyApprovalUserNm;	// 업체결재자
	private String crTypeCd;		// 제의사유
	private String crTypeNm;
	private String materialNo;		// 자재코드
	private String partNo;			// 규격화 품번
	private String comment;			// 검토의견

	public String getRequestDt() {
		return DateUtil.getYmd(requestDt);
	}

	public String getApprovalDt() {
		return DateUtil.getYmd(approvalDt);
	}
}

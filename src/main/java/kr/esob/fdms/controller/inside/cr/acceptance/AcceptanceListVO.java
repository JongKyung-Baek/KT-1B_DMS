package kr.esob.fdms.controller.inside.cr.acceptance;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptanceListVO {
	private String crNo	;				// 제의번호
	private String drawingNo;			/* 도번 */
	private String revNo;			    /* REV */
	private String crTitleNm;			/* 제목 */
	private String crTypeCd;			/* 제의사유 */
	private String crTypeDesc;			/* 제의사유 */
	private String purchaserUid;	    /* 구매담당자 */	
	private String purchaserUserNm;	/* 구매담당자 */
	private String requestDt;			/* 요청일 */
	private String vendorNm;			/* 업체명 */
	private String vendorUid;			/* 업체요청자 */
	private String vendorUserNm;		/* 업체요청자 */
	private String approvalDt;			/* 결재일 */
	private String statusCd	;			/* 진행상태 */
	private String statusNm;			/* 진행상태 */
	private String crStatusCd;			/* CR상태 */
	private String crStatusNm;			/* CR상태 */
	private String businessAreaCd;	/* 사업장 */
	private String businessAreaNm;	/* 사업장 */
	private String productNo;			/* 기종 */
	private String productNm;			/* 기종 */

	public String getRequestDt() {
		return DateUtil.getYmd(requestDt);
	}
	public String getApprovalDt() {
		return DateUtil.getYmd(approvalDt);
	}

}

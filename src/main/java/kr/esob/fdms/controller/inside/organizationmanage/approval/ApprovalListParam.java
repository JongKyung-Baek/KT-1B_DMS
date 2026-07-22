package kr.esob.fdms.controller.inside.organizationmanage.approval;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalListParam extends CommonParam {

	private String requestNo;				/* 요청번호 */
	private String insertCompanyNm;	    /* 업체명 */
	private String requestNm;				/* 요청구분 */
	private String requestUserNm;			/* 요청대상자 */
	private String insertUserNm;			/* 요청자 */
	private String insertDt;				    /* 요청일 */
	private String approvalUserNm;	    /* 승인자 */
	private String approvalDt;				/* 승인일 */
	private String statusCd;				    /* 처리상태 */ 
	private String statusNm;
	private String requestReason;			/* 요청사유 */
	private String rejectReason;			/* 요청반려사유 */

	private String approvalUserCd;	    /* 승인자 코드*/
	private String companyCd;			/* 승인자 코드*/
	private String requestType;
	private String requestStartDt;
	private String requestEndDt;


	private String userNm;
	private String companyNm;
	private String email;
	private String protectYn;
	private String crYn;

	private String targetUserCd;
	private String userPwd;

}
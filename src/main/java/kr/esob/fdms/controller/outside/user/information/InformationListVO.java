package kr.esob.fdms.controller.outside.user.information;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InformationListVO {
//	private String requestNo;		//요청번호
//	private String requestUserNm;	//요청자
//	private String requestDesc;		//요청구분
//	private String statusNm;		//상태
//	private String requestStartDt;		//요청일
//	private String approvalDt;		//승인일
//	private String rejectReason;	//반려사유
//	private String userNm;			//사용자성명

	private String requestNo;			/* 요청번호 */
	private String requestType;			/* 요청구분 */
	private String requestNm;			/* 요청구분 */
	private String userNm;				/* 사용자성명 */
	private String requestUserCd; 		/* 요청자 */
	private String requestUserNm;		/* 요청자 */
	private String requestDt;			/* 요청일 */
	private String approvalDt;			/* 승인일*/

	private String statusCd;				//승인상태
	private String userPwd;					//사용자 비밀번호
	private String email;					//이메일
	private String protectYn;				//보호
	private String requestReason;			//요청사유
	private String rejectReason;			//거부사유
	private String insertUserCd;			//요청한 사용자코드
	private String insertDt;				//요청날짜
	private String insertUserNm;			//요청한 사용자 이름
	private String insertCompanyNm;			//요청한 사용자 이름
	private String approvalUserCd;			//요청한 사용자 이름
	private String approvalUserNm;			//요청한 사용자 이름

}

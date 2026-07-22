package kr.esob.fdms.controller.inside.production.disposalapproval;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisposalApprovalPopupVO {
	private String destroyRequestNo;	// 폐기요청번호
	private String insertUserNm;		// 폐기 요청자
	private String requestDesc;			// 폐기 요청 사유

	private String requestNo;
	private String objectId;
	private String objectNo;
	private String deployUserCd;


}
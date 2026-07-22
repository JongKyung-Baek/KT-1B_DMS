package kr.esob.fdms.controller.inside.production.disposalapproval;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisposalApprovalPopupParam extends CommonParam {
	private String destroyRequestNo;		// 폐기 요청번호
	private String rejectDesc;				// 폐기 반려 사유
	private String saveType;				// 폐기 승인/반려 저장 유형
	private String statusCd;				// 폐기 승인 상태
	private String approvalStatusCd;
	private String approvalGradeCd;
	private String actionCd;
	private String requestNo;
	private String deployUserCd;
	private String objectId;
	private String objectNo;

}
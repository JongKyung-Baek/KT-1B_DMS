package kr.esob.fdms.controller.inside.unregisted.approval;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalPopupParam extends CommonParam {
	private String requestNo;			// 요청번호
	private String rejectDesc;			// 반려사유
	private String saveType;			// 승인 및 반려 flag
	private String actionCd;
	private String approvalStatusCd;
	private String statusCd;
	private String objectId;			// 아이템 ID
	private int fileNo;
	private StatusYn sendEmailYn = StatusYn.Y;

	private List<ApprovalPopupParam> list;

}
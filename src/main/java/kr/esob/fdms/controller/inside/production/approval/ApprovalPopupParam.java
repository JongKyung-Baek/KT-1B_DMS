package kr.esob.fdms.controller.inside.production.approval;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.value.StatusYn;
import kr.esob.fdms.controller.inside.production.common.DeployInfoVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalPopupParam extends CommonParam {
	private String requestNo;			// 요청번호
	private String rejectDesc;			// 요청번호
	private String requestPurpose;
	private String saveType;
	private String actionCd;
	private String statusCd;
	private String approvalStatusCd;
	private String approvalGradeCd;
	private String objectType;
	private String objectId;
	private String deployUserCd;
	private String objectNo;
	private int fileNo;
	private StatusYn sendEmailYn = StatusYn.Y;

	List<ApprovalPopupVO> approvalPopupList;

	List<ApprovalPopupParam> list;

	List<DeployInfoVO> deployInfoList;
}
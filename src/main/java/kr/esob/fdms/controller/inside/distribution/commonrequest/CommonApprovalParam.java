package kr.esob.fdms.controller.inside.distribution.commonrequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CommonApprovalParam extends CommonParam {
	private String currentProcessSeqNo;
	private String actionCd;
	private String statusCd;
	private String saveType;
	private String requestNo;
	private String objectType;
	private String rejectDesc;
	private String requestType;
	private String objectId;
	private String approvalStatusCd;
	private String approvalGradeCd;
	private String objectNo;
	private String rev;
	private String currentRev;			//배포할 revNo
	private String approvalLineId;
	private String currentPage;

	private String approvalType;		//승인 유형 (배포, 출력)
	private String businessAreaCd;
	private String processSeq;
	private String destroyRequestNo;
	private String protectYn;

	private String deployUserCd;
	private String deployCompanyCd;
	private String approvalUser;
	private String fileNo;
	private StatusYn sendEmailYn = StatusYn.Y;

	private List<CommonApprovalParam> list;
}

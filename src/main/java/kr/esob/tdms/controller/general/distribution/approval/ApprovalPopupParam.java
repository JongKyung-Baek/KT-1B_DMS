package kr.esob.tdms.controller.general.distribution.approval;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalPopupParam extends CommonParam {
	private String currentProcessSeqNo;
	private String actionCd;
	private String statusCd;
	private String saveType;
	private String requestNo;
	private String approvalLineId;
	private String objectType;
	private String rejectDesc;
	
}

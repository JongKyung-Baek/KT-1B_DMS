package kr.esob.fdms.controller.inside.distribution.printdestroyapproval;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintDestroyApprovalPopupParam extends CommonParam {
	private String destroyRequestNo;		// 폐기 요청 번호
	private String requestNo;				// 요청번호
	private String objectId;	
	private String saveType;				// 승인(A)/반려(R) 타입
	private String approvalStatusCd;
	private String approvalGradeCd;
	private String processSeq;
	private String actionCd;
	private String statusCd;
	private String rejectDesc;				// 반려 사유
	private String approvalLineId;			// 결재 라인
	private String currentProcessSeqNo;
	
	List<PrintDestroyApprovalPopupParam> list;
}
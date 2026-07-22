package kr.esob.fdms.controller.inside.distribution.printdestroyapproval;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintDestroyApprovalListParam extends CommonParam {
	private String destroyRequestUserCd;	// 폐기요청자
	private String businessAreaCd;			// 사업장
	private String requestStartDt;			// 요청시작일
	private String requestEndDt;			// 요청종료일
	private String destroyRequestNo;		// 폐기 요청 번호
	private String requestNo;				// 요청번호
	private String objectId;	

	
	List<PrintDestroyApprovalListParam> list;
}
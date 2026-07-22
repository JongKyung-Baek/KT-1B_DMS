package kr.esob.fdms.controller.inside.distribution.printapproval;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintApprovalListParam extends CommonParam {
	private String objectType;			// 자료유형
	private String requestUserCd;		// 출력요청자
	private String requestPurpose;		// 용도
	private String businessAreaCd;		// 사업장
	private String requestStartDt;		// 요청시작일
	private String requestEndDt;		// 요청종료일
	private String destroyTodoYmd;		// 폐기 기한
}
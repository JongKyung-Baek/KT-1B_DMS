package kr.esob.fdms.controller.inside.production.approval;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalListParam extends CommonParam {
	private String requestNo;			// 요청번호
	private String requestUserCd;		// 요청자
	private String deployTypeCd;		// 배포유형
	private String businessAreaCd;		// 사업장
	private String requestStartDt;		// 요청일자
	private String requestEndDt;
	private String objectType;			// 요청구분
	private String requestType;			// 요청유형
}
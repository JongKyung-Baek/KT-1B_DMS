package kr.esob.fdms.controller.inside.production.disposalapproval;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisposalApprovalListParam extends CommonParam {
	private String objectNo;				// 자료번호
	private String objectType;				// 자료구분
	private String requestUserCd;			// 요청자(배포자)
	private String businessAreaCd;			// 사업장
	private String deptCd;					// 요청부서(배포부서)
	private String objectClassCd2;			// 자료유형
	private String destroyRequestUserCd;	// 배포요청자
	private String destroyRequestStartDt;	// 폐기요청일자
	private String destroyRequestEndDt;
	private String objectClassNm2;
}
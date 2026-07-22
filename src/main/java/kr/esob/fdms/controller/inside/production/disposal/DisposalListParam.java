package kr.esob.fdms.controller.inside.production.disposal;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisposalListParam extends CommonParam {
	private String objectNo;			// 자료번호
	private String objectType;			// 자료구분
	private String requestUserCd;		// 요청자(배포자)
	private String objectClassCd2;		// 자료유형
	private String deployStartDt;		// 접수일자(시작)
	private String deployEndDt;			// 접수일자(종료)
}
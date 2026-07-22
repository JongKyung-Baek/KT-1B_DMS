package kr.esob.fdms.controller.inside.production.disposalstatus;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisposalStatusListParam extends CommonParam {
	private String objectNo;			// 자료번호
	private String objectType;			// 문서유형
	private String objectClassCd2;		// 문서분류
	private String destroyRequestUserCd;// 폐기요청자
	private String disposalStartDt;		// 폐기일자(시작)
	private String disposalEndDt;		// 폐기일자(종료)
}
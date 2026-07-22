package kr.esob.fdms.controller.main;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidEndListVO extends CommonParam {
	private String useEndYmd;			/* 유효기간(출력, 폐기기한) */
	private String objectType;			/* 자료유형 */
	private String objectTypeNm;		/* 자료유형명 */
	private String objectNo	;			/* 자료번호 */
	private String requestDt;			/* 요청일 */
	private String deployTypeNm;		/* 배포방식(출력,다운로드) */
}

package kr.esob.fdms.controller.inside.unregisted.request;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestListParam extends CommonParam {
	private String insertStartDt;		// 등록시작일
	private String insertEndDt;			// 등록종료일
	private String objectNo;			// 자료번호
	private String objectNm;			// 자료명
	private String objectType;			// 자료유형
	private String fileNm;				// 파일명
	private String insertUserCd;		// 등록자
	//private String purchaserUserCd;		// 구매담당자
	//private String businessAreaCd;		// 사업장
}
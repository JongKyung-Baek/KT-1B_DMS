package kr.esob.fdms.controller.main;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MainParam extends CommonParam {
	private String objectType;			// 자료유형
	private String statusCd;			// 상태
	private String requestType;			// 요청유형
	private String startDt;				// 메인화면 sum 시작일
	private String endDt;				// 메인화면 sum 종료일
	private String outUserYn;			// 외부사용자여부

	public String getOutUserYn() {
		return outUserYn == null ? "" : outUserYn;
	}

}
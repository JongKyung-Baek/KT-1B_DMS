package kr.esob.fdms.controller.inside.distribution.printhistory;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryListParam extends CommonParam {
	private String requestNo;		        // 요청번호
	private String objectType;				// 자료유형
	private String requestUserCd;			// 출력요청자
	private String destroyRequestUserCd;	// 폐기요청자
	private String requestPurpose;			// 용도
	private String businessAreaCd;			// 사업장
	private String destroyStatusCd;			// 폐기상태코드
	private String requestStartDt;			// 요청시작일
	private String requestEndDt;			// 요청종료일
	private String businessTypeCd;			// 사업유형
	private String [] termLimit;				// 출력기한임박: print, 폐기기한임박: destroy

	public String getPrintTermYn() {
		if(null == termLimit) {
			return "N";
		}
		for(int i=0; i<termLimit.length; i++) {
			if(termLimit[i].equals("print")) {
				return "Y";
			}
		}

		return "N";
	}

	public String getPrintDisposalTermYn() {
		if(null == termLimit) {
			return "N";
		}
		for(int i=0; i<termLimit.length; i++) {
			if(termLimit[i].equals("disposal")) {
				return "Y";
			}
		}

		return "N";
	}


}

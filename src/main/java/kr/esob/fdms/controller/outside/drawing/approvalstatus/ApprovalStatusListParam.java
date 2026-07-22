package kr.esob.fdms.controller.outside.drawing.approvalstatus;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalStatusListParam extends CommonParam {
	private String requestNo;			// 요청번호
	private String objectNo;			// 도면번호
	private String drawingNm;			// 도면명
	private String purchaserUserCd;		// 구매담당자
	private String companyUserCd;		// 업체담당자
	private String requestStartDt;		// 요청시작일
	private String requestEndDt;		// 요청종료일
	private String businessAreaCd;		// 사업장
	private String destroyStatusCd;		// 폐기상태
	private String [] validTerm;		// 임박: limit, 초과: over
	private boolean checkVersion;		// 임박: limit, 초과: over

	public String getValidTermLimitYn() {
		if(null == validTerm) {
			return "N";
		}
		for(int i=0; i<validTerm.length; i++) {
			if(validTerm[i].equals("limit")) {
				return "Y";
			}
		}

		return "N";
	}
	public String getValidTermOverYn() {
		if(null == validTerm) {
			return "N";
		}
		for(int i=0; i<validTerm.length; i++) {
			if(validTerm[i].equals("over")) {
				return "Y";
			}
		}

		return "N";
	}

}

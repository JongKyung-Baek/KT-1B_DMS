package kr.esob.fdms.controller.inside.distribution.oldcr;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryListParam extends CommonParam {
	private String objectId;				// 제의번호
	private String usageVal;				// 제의사유
	private String inUserId;				// 구매담당자
	private String outuserId;				// 업체담당자
	private String state2;					// 진행상태
	private String startCreationDate;		// 요청일
	private String closeCreationDate;		// 요청일
	private String outUserYn;				// 외부사용자여부
}
package kr.esob.fdms.controller.inside.cr.history;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryListParam extends CommonParam {
	private String purchaserUid;		// 구매담당자
	private String vendorNo;			// 요청업체
	private String crStatusCd;			// 진행상태
	private String requestStartDt;		// 요청시작일
	private String requestEndDt;		// 요청종료일
	private String businessAreaCd;		// 사업장
}
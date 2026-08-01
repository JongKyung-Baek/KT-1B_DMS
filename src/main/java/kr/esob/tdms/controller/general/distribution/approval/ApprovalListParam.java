package kr.esob.tdms.controller.general.distribution.approval;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalListParam extends CommonParam {
	private String objectType;
	private String deployCompanyCd;
	private String purchaserUserCd;
	private String businessAreaCd;
	private String requestStartDt;
	private String requestEndDt;
	private String requestNo;
	private String saveType;
}

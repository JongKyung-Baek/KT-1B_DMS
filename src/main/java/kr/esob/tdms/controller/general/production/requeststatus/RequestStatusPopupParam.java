package kr.esob.tdms.controller.general.production.requeststatus;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStatusPopupParam extends CommonParam {
	private String requestNo;			// 요청번호
	private String objectType;			// 자료유형
	
	//출력물 승인을 위한 parameter
	private String actionCd;
	private String statusCd;
	private String saveType;
	private String rejectDesc;
	
}
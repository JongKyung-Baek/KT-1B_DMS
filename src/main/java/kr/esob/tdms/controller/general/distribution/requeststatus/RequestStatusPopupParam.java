package kr.esob.tdms.controller.general.distribution.requeststatus;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStatusPopupParam extends CommonParam {
	private String requestNo;		// 요청번호
	private String objectType;
}
package kr.esob.fdms.controller.outside.outregisted.requeststatus;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStatusPopupParam extends CommonParam {
	private String requestNo;			//요청번호
}
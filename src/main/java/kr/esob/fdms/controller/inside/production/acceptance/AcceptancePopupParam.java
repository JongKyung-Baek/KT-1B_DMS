package kr.esob.fdms.controller.inside.production.acceptance;


import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptancePopupParam extends CommonParam {
	private String requestNo;
	private String objectId;
	private String objectNo;
	private String objectType;
	private String deployDesc;
	private String deployDt;
	private String deployTarget;
	private String deployResult;
	private String deployUserCd;
	private String destroyInfo;

	List<AcceptancePopupParam> list;

}
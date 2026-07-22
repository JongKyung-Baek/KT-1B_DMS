package kr.esob.fdms.controller.inside.production.acceptance;


import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptanceListParam extends CommonParam {
	private String businessAreaCd;
	private String objectType;
	private String requestNo;
	private String deployStartDt;
	private String deployEndDt;
	private String requestUserCd;
	private String objectId;
	
}

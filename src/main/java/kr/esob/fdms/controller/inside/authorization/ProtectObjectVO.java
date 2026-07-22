package kr.esob.fdms.controller.inside.authorization;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ProtectObjectVO extends CommonParam {
	private String objectId;
	private String objectType;
}
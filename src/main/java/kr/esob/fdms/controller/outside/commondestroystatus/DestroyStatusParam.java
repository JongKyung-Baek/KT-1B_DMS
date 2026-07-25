package kr.esob.fdms.controller.outside.commondestroystatus;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestroyStatusParam extends CommonParam {
	public String lastDestroyRequestNo;

}

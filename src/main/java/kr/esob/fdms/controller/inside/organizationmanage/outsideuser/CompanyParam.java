package kr.esob.fdms.controller.inside.organizationmanage.outsideuser;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyParam extends CommonParam {
	private String bizNo;
	private String companyCd;
}
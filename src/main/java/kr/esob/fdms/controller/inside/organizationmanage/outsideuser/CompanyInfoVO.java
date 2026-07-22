package kr.esob.fdms.controller.inside.organizationmanage.outsideuser;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyInfoVO {
	private String bizNo;
	private String companyCd;
	private String companyNm;
	private String distPurchaserUserCd1;
	private String crPurchaserUserCd1;
	private String distPurchaserUserCd2;
	private String crPurchaserUserCd2;
}
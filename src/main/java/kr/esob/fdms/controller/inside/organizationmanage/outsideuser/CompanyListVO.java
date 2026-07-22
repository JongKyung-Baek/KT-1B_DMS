package kr.esob.fdms.controller.inside.organizationmanage.outsideuser;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyListVO extends CommonParam {
	private String companyNm;
	private String distPurchaserUserNm1;
	private String crPurchaserUserNm1;
	private String distPurchaserUserNm2;
	private String crPurchaserUserNm2;
	private String companyCd;
	private String bizNo;

	//public String getBizNo() {
	//	return StringUtil.getBizNo(bizNo);
	//}
}
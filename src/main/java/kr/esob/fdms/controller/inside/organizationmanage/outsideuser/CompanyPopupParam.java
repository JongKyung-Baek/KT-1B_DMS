package kr.esob.fdms.controller.inside.organizationmanage.outsideuser;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyPopupParam extends CommonParam {
	private String bizNo;
	private String companyCd;
	private String companyNm;
	private String distPurchaserUserCd1;
	private String crPurchaserUserCd1;
	private String distPurchaserUserCd2;
	private String crPurchaserUserCd2;
	private String saveFlag;

	private String distPurchaserUserCd;			// DOCS_COMPANY_PURCHASER
	private String crPurchaserUserCd;

	private Integer cnSerial;					// ITN_VENDOR
	private String businessAreaCd;
	private String distPurchaserUid;
	private String distPurchaserUidEmail;
	private String crPurchaserUid;
	private String crPurchaserUidEmail;

	// 사용자 등록/수정
	private String userCd;
	private String userNm;
	private String email;
	private String userPwd;
}
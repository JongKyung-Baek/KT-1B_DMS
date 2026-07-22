package kr.esob.fdms.controller.inside.distribution.acceptance;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptanceParam extends CommonParam {

	private String requestNo;
	private String saveType;
	private String deployTerm;
	private String purchaseUid;
	private String protectiveOfficerUid;
	private String rejectRequestDesc;
	private String actionCd;
	private String statusCd;
	private String currentProcessSeqNo;
	private String approvalLineId;
	private String printYn;
	private String fileDistributionType;
	private String deployNormalYn;
	private String deploySpecialYn;
	private String useStartYmd;
	private String useEndYmd;
	private String requestPurpose;
	private String objectType;
	private String objectId;
	private String fileNo;

	private String companyCd;
	private String distributeMethodCd;

	private String requestPurposeTerm;
	private String purchaserUserCd;
	private String businessAreaCd;
	private String requestStartDt;

	private String businessTypeCd;
	private String requestPurposeData;

	private StatusYn sendEmailYn;

	List<AcceptanceParam> list;

}

package kr.esob.fdms.controller.inside.distribution.drawingrequest;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrawingRequestPopupParam extends CommonParam {
	private String cnSerial;
	private String protectYn;
	private String companyCd;
	private String companyUserCd;
	private String deployUserEmail;
	private String purchaseTeam;
	private String purchaseUid;
	private String protectiveOfficerUid;
	private String distributeMethodCd;
	private String requestPurpose;
	private String requestPurposeTerm;
	private String requestDesc;
	private String requestType;
	private String approvalLineId;
	private String currentProcessSeqNo;
	private String useStartYmd;
	private String useEndYmd;
	private String printYn;
	private String deployNormalYn;
	private String deploySpecialYn;
	private String objectType;
	private String requestNo;
	private String requestKeyType;
	private String processSeq;
	private String approvalStatusCd;
	private String actionCd;
	private String objectId;


	private List<DrawingRequestPopupParam> list;
}

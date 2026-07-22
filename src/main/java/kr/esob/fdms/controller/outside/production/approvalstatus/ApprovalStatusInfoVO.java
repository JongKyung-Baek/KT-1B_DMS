package kr.esob.fdms.controller.outside.production.approvalstatus;

import java.util.List;

import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalStatusInfoVO {
	private StatusYn inspectionYn;
	private int cnSerial;
	private String objectId;
	private String businessAreaCd;
	private String businessTypeCd;
	private String productNo;
	private String ecnNo;
	private String ecnInsertUid;
	private String drawingNo;
	private String drawingNm;
	private String revNo;
	private String partClassCd;
	private String distributeTypeCd;
	private String statusCd;
	private int currentPageNo;
	private int totalPageNo;
	private String effectiveDt;
	private String filePathNm;
	private String fileNm;
	private String fileSize;
	private String useTypeCd;
	private String stdGappDt;
	private String changeGappDt;
	private String materialsCd;
	private String partNo;
	private String purchaserUserCd;
	private String vendorCd;
	private String securityTypeCd;
	private String protectYn;
	private String insertDeptNm;
	private String insertUid;
	private String insertDt;
	private String updateDeptNm;
	private String updateUid;
	private String updateDt;
	private String errcod;
	private String errmsg;

	List<ApprovalStatusInfoVO> list;

}

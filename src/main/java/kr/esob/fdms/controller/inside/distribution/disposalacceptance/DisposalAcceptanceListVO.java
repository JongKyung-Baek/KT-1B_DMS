package kr.esob.fdms.controller.inside.distribution.disposalacceptance;

import java.util.Date;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisposalAcceptanceListVO {
	private String destroyRequestNo;
	private int approvalLineId;
	private int currentProcessSeqNo;
	private String statusCd;
	private String statusNm;
	private Date requestDt;
	private String destroyType;
	private String destroyStatusCd;
	private String businessAreaCd;
	private String businessAreaNm;
	private String objectNo;
	private String objectType;
	private String objectTypeNm;
	private String requestCompanyCd;
	private String requestCompanyNm;
	private String requestUserCd;
	private String requestUserNm;
	private String purchaserUserNm;
	private String destroyUserNm;
	
	public String getRequestDt() {
		return DateUtil.getDateString(requestDt);
	}

}

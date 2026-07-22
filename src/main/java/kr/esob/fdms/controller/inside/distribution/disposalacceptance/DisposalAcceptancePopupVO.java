package kr.esob.fdms.controller.inside.distribution.disposalacceptance;

import java.util.Date;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisposalAcceptancePopupVO {
	private String destroyRequestNo;
	private String requestUserCd;
	private String requestUserNm;
	private String requestCompanyCd;
	private String requestCompanyNm;
	private String businessAreaCd;
	private String businessAreaNm;
	private Date requestDt;
	private String requestDesc;
	private String rejectDesc;
	private String statusCd;
	private String purchaserUserCd;

	public String getRequestDt() {
		return DateUtil.getDateString(this.requestDt);
	}

}

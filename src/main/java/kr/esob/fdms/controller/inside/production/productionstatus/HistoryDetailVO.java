package kr.esob.fdms.controller.inside.production.productionstatus;

import java.util.Date;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryDetailVO {
	private String requestNo;
	private String objectId;
	private String revNo;
	private int deployCount;
	private int copy;
	private int destroyCount;
	private String requestUserCd;
	private String requestUserNm;
	private String approvalUserCd;
	private String approvalUserNm;
	private Date requestDt;
	private Date deployDt;

	public String getRequestDt() {
		return DateUtil.getDateString(this.requestDt);
	}

	public String getDeployDt() {
		return DateUtil.getDateString(this.deployDt);
	}

}

package kr.esob.fdms.controller.inside.production.history;

import java.util.Date;
import java.util.List;

import kr.esob.fdms.commonlogic.value.StatusYn;
import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestDeployVO {
	private String requestNo;
	private String deployUserCd;
	private String deployUserFullNm;
	private String deployUserNm;
	private String deployUserEmail;
	private StatusYn acceptYn;
	private String deployDesc;
	private Date deployDt;
	private String deployTarget;
	private String deployResult;
	private Date acceptDt;
	private String deployEmailCc;
	private String objectId;
	private int deployCount;
	private int copy;
	private int destroyCount;
	private String destroyStatusCd;
	private String destroyInfo;

	private List<DeployInfoVO> deployInfoList;

	public String getAcceptDt() {
		return DateUtil.getDateString(this.acceptDt);
	}

	public String getDeployDt() {
		return DateUtil.getDateString(this.deployDt);
	}

}

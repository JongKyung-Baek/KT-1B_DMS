package kr.esob.fdms.controller.inside.production.history;

import java.util.Date;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestDetailVO {
	private int processSeq;
	private String approvalStatusCd;
	private String originalUserCd;
	private String actualUserCd;
	private Date actionDt;
	private String actionCd;
	private String approvalGradeCd;
	private String originalUserNm;
	private String actualUserNm;
	private String actualUserDeptCd;
	private String actualUserDeptNm;
	private String requestDesc;

	public String getActionDt() {
		return DateUtil.getDateString(this.actionDt);
	}

	public String getApprovalStatusNm() {
		return ComboLang.getComboLang("approvalStatusCd", this.approvalStatusCd);
	}


}

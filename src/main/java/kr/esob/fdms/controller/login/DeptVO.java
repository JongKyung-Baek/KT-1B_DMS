package kr.esob.fdms.controller.login;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeptVO {
	private String deptCd;
	private String deptCdCustom;
	private String parentDeptCd;
	private String deptNm;
	private String deptEngNm;
	private String companyCd;
	private String useYn;
	private String delYn;
	private int sortSeq;
	private String insertUid;
	private Date insertDt;
	private String updateUid;
	private Date updateDt;
	private String teamLeaderUid;
	private String purchaseTeamYn;
	private String defenseTeamYn;
	private String ilsTeamYn;
	private String deptShortPath;

}

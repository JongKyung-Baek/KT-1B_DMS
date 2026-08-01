package kr.esob.tdms.controller.general.distribution.oldhistory;

import java.util.List;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryListParam extends CommonParam {
	private String requestType;
	private String fileCode1;
	private String useEndDate;
	private String approvalNo;
	private String companyCode;
	private String taskState;
	private String userCode;
	private String userName;
	private String project;
	private String startApprovalDate;
	private String closeApprovalDate;
	private String useEndDateYn;
	private String rowDataId;
	
	List<HistoryListParam> list;
}
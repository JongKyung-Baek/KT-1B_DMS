package kr.esob.fdms.controller.inside.pdm.deployhistory;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PdmDeployHistoryParam extends CommonParam {

	private String sendId;
	private String sendUser;
	private String receiveUser;
	private String title;
	private String contents;
	private String sendStatusGroup;
	private String sendStatusCode;
	private String sendDate;
	private String delYn;
	private String createUser;
	private String createDate;
	private String updateUser;
	private String updateDate;
	private String startDate;
	private String endDate;
	

	List<PdmDeployHistoryParam> list;

}

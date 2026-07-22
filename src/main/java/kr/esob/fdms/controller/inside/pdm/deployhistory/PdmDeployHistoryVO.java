package kr.esob.fdms.controller.inside.pdm.deployhistory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PdmDeployHistoryVO {
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

}

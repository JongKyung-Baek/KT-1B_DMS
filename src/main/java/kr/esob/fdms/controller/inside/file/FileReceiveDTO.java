package kr.esob.fdms.controller.inside.file;

import java.util.Date;

import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileReceiveDTO {

	private String sendId;
	private String sendUser;
	private String sendDeptName;
	private String sendUserName;
	private String receiveDept;
	private String receiveDeptName;
	private String receiveUser;
	private String receiveUserName;
	private String title;
	private String contents;
	private String sendStatusGroup;
	private String sendStatusCode;
	private String sendStatusName;
	private StatusYn delYn;
	private long fileCnt;
	private Date sendDate;

	private String userCode;
	private String userName;
	private String userId;
	private String deptCode;
	private String deptName;

}

package kr.esob.fdms.controller.inside.file;

import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileReceiveParam {

	private String deptCode;

	private String title;
	private String sendStatus;
	private StatusYn delYn;
	private String sendStartDate;
	private String sendEndDate;

	private String sendId;

}

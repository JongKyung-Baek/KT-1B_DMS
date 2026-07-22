package kr.esob.fdms.controller.outside.commonrequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestFileParam {
	private String objectId;
	private String filePathNm;
	private String orgFileNm;
	private String fileNm;
	private String fileSize;
	private String checkSum;
	private int fileNo;

}

package kr.esob.tdms.commonlogic.distribution.model;

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

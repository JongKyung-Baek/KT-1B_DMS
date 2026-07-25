package kr.esob.fdms.controller.outside.commondestroystatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestroyFileVO {

	private String destroyRequestNo;
	private String destroyNo;
	private int destroyFileSeq;
	private String fileName;
	private String filePath;
	private String requestNo;
	private String objectType;
	private String objectId;
	private String fileNo;

}

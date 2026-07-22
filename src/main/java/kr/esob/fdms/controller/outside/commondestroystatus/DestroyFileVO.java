package kr.esob.fdms.controller.outside.commondestroystatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestroyFileVO {

	private String destroyNo;
	private int destroyFileSeq;
	private String fileName;
	private String filePath;

}

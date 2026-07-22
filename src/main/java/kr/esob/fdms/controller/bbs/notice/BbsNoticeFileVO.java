package kr.esob.fdms.controller.bbs.notice;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BbsNoticeFileVO  extends CommonParam {
	private Long noticeCd;
	private String fileNm;
	private String filePath;
	private String fileSize;
	private String fileNo;
	private String filePathOutside;
}

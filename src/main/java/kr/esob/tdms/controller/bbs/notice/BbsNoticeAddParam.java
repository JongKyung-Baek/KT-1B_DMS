package kr.esob.tdms.controller.bbs.notice;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BbsNoticeAddParam extends CommonParam {
	private Long noticeCd;
	private String addTitle;
	private String insertUserNm;
	private String insertUserId;
	private String contents;
	private String startDt;
	private String endDt;

	private String fileNm;
	private String filePath;
	private String fileSize;
	private String fileNo;
}


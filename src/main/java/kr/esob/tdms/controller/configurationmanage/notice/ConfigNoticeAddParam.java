package kr.esob.tdms.controller.configurationmanage.notice;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigNoticeAddParam extends CommonParam {
	private Long noticeCd;
	private String title;
	private String contents;
	private String noticeType;

	private String fileNo;
	private String filePath;
	private String fileNm;
	private String orgFileNm;
	private String fileDelete;
}


package kr.esob.fdms.controller.configurationmanage.notice;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigNoticePopupVO extends CommonParam {
	private Long noticeCd;
	private String noticeType;
	private String insertUserCd;
	private String insertUserNm;
	private String title;
	private String contents;
	private String noticeHit;
	
	private String fileNo;
	private String filePath;
	private String fileNm;
	private String orgFileNm;
	
	private List<ConfigNoticePopupVO> list;
	private List<ConfigNoticePopupVO> fileList;
}


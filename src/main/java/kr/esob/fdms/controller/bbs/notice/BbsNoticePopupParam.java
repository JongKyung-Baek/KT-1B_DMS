package kr.esob.fdms.controller.bbs.notice;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BbsNoticePopupParam extends CommonParam {
	private Long noticeCd;
	private String noticeTitle;
	private String insertUid;
	private String contents;
	private int hitCount;

	private String startDt;
	private String endDt;

	private String fileNm;
	private String filePath;
	private String fileSize;
	private String fileNo;
	private String filePathOutside;

	List<BbsNoticePopupParam> list;
}

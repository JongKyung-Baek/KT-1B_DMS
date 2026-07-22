package kr.esob.fdms.controller.bbs.notice;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BbsNoticePopupVO {
	private Long noticeCd;		// 글번호
	private String noticeTitle;				// 제목명
	private String contents;			//내용
	private String insertUserNm;
	private int hitCount;
	private String startDt;
	private String endDt;

	private String fileNm;
	private String filePath;
	private List<BbsNoticeFileVO> fileList;

	public String getStartDt() {
		if(null == startDt) return "";
		if("".equals(startDt)) return "";
		if(startDt.length() != 8) return "";

		return startDt.substring(0, 4) + "-" + startDt.substring(4, 6) + "-" + startDt.substring(6,8);
	}

	public String getEndDt() {
		if(null == endDt) return "";
		if("".equals(endDt)) return "";
		if(endDt.length() != 8) return "";

		return endDt.substring(0, 4) + "-" + endDt.substring(4, 6) + "-" + endDt.substring(6,8);
	}

}


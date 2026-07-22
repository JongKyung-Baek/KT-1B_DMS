package kr.esob.fdms.controller.bbs.notice;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BbsNoticeListVO {
	private Long noticeCd;
	private String parentQnaCd;
	private String title;				// 제목명
	private String insertUserNm;			//등록자
	private String insertDt;				// 등록일
	private int hitCount;				//조회수
	private String startDt;
	private String endDt;

	public String getNoticeTerm() {
		return getStartDt() + " ~ " + getEndDt();
	}

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

	public String getinsertDt() {
		return DateUtil.getYmd(insertDt);
	}
}

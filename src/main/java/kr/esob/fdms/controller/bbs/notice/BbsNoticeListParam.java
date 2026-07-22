package kr.esob.fdms.controller.bbs.notice;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BbsNoticeListParam extends CommonParam {
	private Long noticeCd;			//글번호
	private String insertUserNm;			//등록자
	private String title;				// 제목명
	private String insertStartDt;		//등록시작일
	private String insertEndDt;			//등록종료일
	private int hitCount;			//조회수

	public String getInsertStartDt() {
		if(null == insertStartDt) {
			return "";
		}

		return this.insertStartDt.replace("-", "");
	}

	public String getInsertEndDt() {
		if(null == insertEndDt) {
			return "";
		}

		return this.insertEndDt.replace("-", "");
	}
}

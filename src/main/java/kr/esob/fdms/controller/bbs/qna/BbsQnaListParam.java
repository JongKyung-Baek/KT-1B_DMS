package kr.esob.fdms.controller.bbs.qna;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BbsQnaListParam extends CommonParam {
	private Long qnaCd;			//글번호
	private String insertUserNm;			//등록자
	private String title;				// 제목명
	private String insertStartDt;		//등록시작일
	private String insertEndDt;			//등록종료일

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

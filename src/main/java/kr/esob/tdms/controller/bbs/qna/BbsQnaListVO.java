package kr.esob.tdms.controller.bbs.qna;

import kr.esob.tdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BbsQnaListVO {
	private Long qnaCd;
	private Long parentQnaCd;
	private String title;
	private String insertUserNm;			//등록자
	private String insertDt;				// 등록일
	private int hitCount;				//조회수

	public String getinsertDt() {
		return DateUtil.getYmd(insertDt);
	}

}

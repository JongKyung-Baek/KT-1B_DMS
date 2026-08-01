package kr.esob.tdms.controller.bbs.qna;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BbsQnaPopupVO {
	private Long qnaCd;		// 요청 사유
	private String title;				// 제목명
	private String contents;			//등록자
	private String insertUserNm;
	private Long parentQnaCd;
}

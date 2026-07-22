package kr.esob.fdms.controller.bbs.qna;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BbsQnaAddParam extends CommonParam {
	private Long qnaCd;
	private String qnaTitle;
	private String insertUserNm;
	private String contents;
}

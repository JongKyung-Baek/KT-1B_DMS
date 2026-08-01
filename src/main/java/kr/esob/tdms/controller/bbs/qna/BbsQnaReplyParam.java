package kr.esob.tdms.controller.bbs.qna;

import java.util.List;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BbsQnaReplyParam extends CommonParam {
	private Long qnaCd;
	private Long parentQnaCd;
	private String qnaTitle;
	private String insertUserNm;
	private String contents;

	List<BbsQnaReplyParam> list;
}

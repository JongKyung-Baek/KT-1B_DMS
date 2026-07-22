package kr.esob.fdms.controller.bbs.qna;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BbsQnaPopupParam extends CommonParam {
	private Long parentQnaCd;
	private Long qnaCd;
	private String title;
	private String insertUid;
	private String contents;

	List<BbsQnaPopupParam> list;
}

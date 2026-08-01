package kr.esob.tdms.controller.bbs.qna;

import java.util.List;

import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
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

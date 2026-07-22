package kr.esob.fdms.controller.configurationmanage.qna;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ConfigQnaListParam extends CommonParam {
	private Long qnaCd;						// 유형
	private String title;					// 제목
	private String companyNm;				// 업체명
	private String insertUserNm;			// 등록자
	private Long qnaType;					// 유형
	private String insertStartDt;			// 등록일 시작
	private String insertEndDt;				// 등록일 끝
}

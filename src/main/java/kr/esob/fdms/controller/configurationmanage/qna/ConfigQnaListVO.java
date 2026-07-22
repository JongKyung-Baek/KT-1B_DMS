package kr.esob.fdms.controller.configurationmanage.qna;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigQnaListVO {
	private Long qnaCd;						// 번호
	private String insertDt;				// 등록일
	private String title;					// 제목
	private String companyNm;				// 업체명
	private String insertUserNm;			// 등록자
	private String qnaType;					// 유형
	private String status;					// 상태
}

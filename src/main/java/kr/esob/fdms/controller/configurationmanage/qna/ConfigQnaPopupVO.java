package kr.esob.fdms.controller.configurationmanage.qna;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigQnaPopupVO extends CommonParam {
	private Long qnaCd;						// 번호
	private String title;					// 제목
	private String contents;				// 내용
	private String companyNm;				// 업체명
	private String insertUserNm;			// 등록자
	private String qnaType;					// 유형
	private String status;					// 상태
	private String answerUserNm;			// 답변자 이름
	private String answer;					// 답변
	
	private String fileNo;
	private String filePath;
	private String fileNm;
	private String orgFileNm;
	
	private List<ConfigQnaPopupVO> list;
	private List<ConfigQnaPopupVO> fileList;
}

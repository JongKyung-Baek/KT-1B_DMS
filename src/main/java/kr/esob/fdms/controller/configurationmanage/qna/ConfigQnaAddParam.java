package kr.esob.fdms.controller.configurationmanage.qna;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigQnaAddParam extends CommonParam {
	private Long qnaCd;
	private String title;
	private String contents;
	private String qnaType;
	
	private String fileNo;
	private String filePath;
	private String fileNm;
	private String orgFileNm;
	private String fileDelete;
}

package kr.esob.fdms.controller.outside.outregisted.request;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnregisterPopupParam extends CommonParam {
	private String objectNo;		//전송 자료 번호
	private String objectId;		//자료번호
	private String dataName;		//파일명
	private String objectType;		//자료 유형 (DOC, DRAWING, SW)
	private String fileNm;			//파일명
	private String fileUploadNm;	//파일 업로드명
	private String filePath;		//파일위치
	private String fileSize;		//파일 크기
	private String fileCd;			//파일 번호
	private String fileNo;			//파일 번호

	private List<UnregisterPopupParam> paramList;
}
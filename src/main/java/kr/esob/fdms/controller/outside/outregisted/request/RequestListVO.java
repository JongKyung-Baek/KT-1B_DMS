package kr.esob.fdms.controller.outside.outregisted.request;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestListVO {
	private String objectNo;		// 자료번호
	private String objectNm;		// 자료명
	private String objectType; 		/* 자료유형 */
	private String fileViewNm;			// 파일명
	private String fileNm;			// 파일명
	private String insertUserNm;	// 등록자
	private String insertDt;		// 등록일
	private String businessAreaCd;	// 사업장코드
	private String objectId;
	private long fileSize;
	private String filePath;
	private String fileCd;
	private int fileNo;

//
	public String getObjectTypeNm() {
		return ComboLang.getComboLang("objectType", this.objectType);
	}
}

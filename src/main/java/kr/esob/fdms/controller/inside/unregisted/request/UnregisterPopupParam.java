package kr.esob.fdms.controller.inside.unregisted.request;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnregisterPopupParam extends CommonParam {
	private String objectNo;		//미등록 자료 번호
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

	@Override
	public String toString() {
		return "UnregisterPopupParam{" +
				"objectNo='" + objectNo + '\'' +
				", objectId='" + objectId + '\'' +
				", dataName='" + dataName + '\'' +
				", objectType='" + objectType + '\'' +
				", fileNm='" + fileNm + '\'' +
				", fileUploadNm='" + fileUploadNm + '\'' +
				", filePath='" + filePath + '\'' +
				", fileSize='" + fileSize + '\'' +
				", fileCd='" + fileCd + '\'' +
				", fileNo='" + fileNo + '\'' +
				", paramList=" + paramList +
				'}';
	}
}
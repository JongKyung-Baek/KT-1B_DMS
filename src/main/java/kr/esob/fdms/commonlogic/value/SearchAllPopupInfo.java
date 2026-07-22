package kr.esob.fdms.commonlogic.value;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape=Shape.OBJECT)
public enum SearchAllPopupInfo {
	DRAWING(false, "drawingNo"),
	DOC(false, "documentNo"),
	SW(false, "swNo"),
	PRODUCTION(false, "objectNo"),
	ALL(true, "");

	//배포이력에서 띄우는 팝업인지 여부. 배포이력에서 일괄검색을 할 경우는 도면,문서,SW 모든 정보가 그리드에 나타난다.
	private boolean isAllSearch;
	//도면,문서,SW번호
	private String objectId;



}

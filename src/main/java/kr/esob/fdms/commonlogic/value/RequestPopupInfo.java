package kr.esob.fdms.commonlogic.value;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 외부사용자 배포요청 팝업에서 별도로 사용되는 값을 모아놓은 enumeration
 * RequestController에서 아래값을 JSON형태로 model에 담아 view로 전달 한다.(JsonFormat annotation참고)
 * @author yukil
 *
 */
@Getter
@AllArgsConstructor
@JsonFormat(shape=Shape.OBJECT)
public enum RequestPopupInfo {
	DRAWING("DRAWING"
			,"/outside/drawing/request/"							//팝업 종류 별 URL
			, "form.drawingList"									//그리드 타이틀 표시를 위한 langCd값
			, "drawingNo"											//도면번호,문서번호,SW번호의 컬럼명
			, "distributeTypeNm"									//자료가 없을경우 해당 메시지를 표시하기 위한 컬럼명
			, "drawingInfoVoList"),									//요청목록을 담을 parameter key 이름. (RequestParam)
	DOC("DOC"
			,"/outside/doc/request/"
			, "form.docList"
			, "documentNo"
			, "objectNm"
			, "docInfoVoList"),	
	DOCPDF("DOCPDF"
			,"/outside/docpdf/request/"
			, "form.docPdfList"
			, "seqNo"
			, "objectNm"
			, "docPdfInfoVoList"),	
	SW("SW"
			, "/outside/sw/request/"
			, "form.swList"
			, "swNo"
			, "objectNm"
			, "swInfoVoList"),
	PRODUCTION("PRODUCTION"
			,"/outside/production/request/"
			, "form.objectList"
			, "objectNo"
			, "objectNm"
			, "productionList");

	private String dataType;
	//도면,문서,SW 별  URL정보
	private String urlInfo;
	//구매목록, 문서목록, SW목록 타이틀을 표시하기 위한 langCd값.
	private String listTitle;
	//도면,문서,SW 점검 시 중복되는 정보를 검증하기 위한 도면번호, 문서번호, SW번호의 column값 자세한 사용법은 commonRequestPopup.js의 checkDuplicate 함수 참고
	private String inspectionId;
	//자료가 없을 경우 해당 메시지를 출력해주는 컬럼명
	private String msgField;
	//요청 목록을 담을 parameter key 이름
	private String listName;


}

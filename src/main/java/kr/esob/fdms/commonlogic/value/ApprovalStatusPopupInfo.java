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
public enum ApprovalStatusPopupInfo {
	DRAWING("/outside/drawing/approvalStatus/destroyFileDown"),
	DOC("/outside/doc/approvalStatus/destroyFileDown"),
	SW("/outside/sw/approvalStatus/destroyFileDown"),
	PRODUCT("/outside/production/approvalStatus/destroyFileDown");

	//폐기정보 파일을 다운로드 받기 위한 url
	private String fileDownloadUrl;


}

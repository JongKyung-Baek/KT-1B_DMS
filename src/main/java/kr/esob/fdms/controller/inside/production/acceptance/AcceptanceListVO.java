package kr.esob.fdms.controller.inside.production.acceptance;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcceptanceListVO {
	private String acceptUserNm;				//접수자
	private String applyHogiNo;					//호기
	private String deployDt;					//배포일자
	private String requestUserNm;				//배포자
	private String deployCount;					//배포매수
	private String copy;						//부수
	private String destroyCount;				//폐기예정 (매수)
	private String objectNo;					//자료번호
	private String objectNm;					//자료명
	private String objectType;					//자료유형
	private String objectTypeNm;				//자료유형
	private String productNo;					//기종
	private String requestNo;					//요청번호
	private String requestPurposeNm;			//배포유형
	private String rev;							//리비전
	private String objectId;
	private String statusCd;					//상태

	List<AcceptanceListParam> list;
}

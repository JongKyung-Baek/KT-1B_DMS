package kr.esob.fdms.controller.inside.production.approval;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalListVO {
	private String requestNo;			// 요청번호
	private String hiddenRequestNo;
	private String deployTypeNm;		// 배포유형
	private String deployDt;			// 배포일
	private String requestUserNm;		// 요청자
	private String requestDt;			// 요청일
	private String requestReason;		// 요청사유
	private String requestTypeNm;		// 요청유형
	private String objectTypeNm;		// 자료구분
	private String objectType;
	private String requestPurpose;
}
package kr.esob.fdms.controller.inside.production.requeststatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStatusListVO {
	private String requestNo;			// 요청번호
	private String deployTypeNm;		// 배포유형
	private String deployDt;			// 배포일
	private String approvalUserNm;		// 결재자
	private String approvalDt;			// 결재일
	private String requestDt;			// 요청일
	private String statusNm;			// 진행상태
	private String objectTypeNm;		// 자료유형
	private String objectType;
	private String requestTypeNm;		// 요청유형
	private String deployTypeCd;
	private String requestUserNm;		// 요청자
}
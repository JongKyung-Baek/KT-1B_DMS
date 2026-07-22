package kr.esob.fdms.controller.inside.production.history;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryListVO {
	private String requestNo;			// 요청번호
	private String statusCd;			// 진행상태코드
	private String statusNm;			// 진행상태
	private String objectTypeNm;		// 자료유형
	private String objectClassCd2;		// 자료분류
	private String objectClassNm2;		// 자료분류
	private String objectNo;			// 문서번호
	private String objectNm;			// 문서명
	private String revNo;				// rev
	private String businessAreaCd;		// 사업장
	private String businessAreaNm;  	// 사업장
	private String requestDeptNm;		// 배포부서
	private String requestUserNm;		// 배포자
	private String requestDt;			// 배포일
	private String approvalUserNm;	// 결재자
	private String approvalDt;			// 결재일
	private String deployCount;			// 배포매수
	private String validPeriod;			// 유효기간
	private String acceptUser;			// 접수자
	private String deployTypeNm;		// 배포유형
}
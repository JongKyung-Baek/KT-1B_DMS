package kr.esob.fdms.controller.outside.user.status;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusListVO {
	private String requestNo;		// 요청번호
	private String requestTypeNm;	// 요청구분
	private String userNm;			// 사용자성명
	private String requestUserNm;	// 요청자
	private String requestDt;		// 요청일
	private String approvalDt;		// 승인일
	private String statusNm;		// 상태
}

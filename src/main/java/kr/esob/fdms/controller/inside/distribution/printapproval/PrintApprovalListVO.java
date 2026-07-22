package kr.esob.fdms.controller.inside.distribution.printapproval;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintApprovalListVO {
	private String requestPurpose;		// 용도
	private String requestPurposeNm;		// 용도
	private String requestNo;			// 요청번호
	private String requestUserNm;		// 출력요청자
	private String requestDt;			// 요청일
	private String destroyTodoYmd;		// 폐기기한
	private String businessAreaNm;		// 사업장
	private String objectType;			// 자료유형
	private String objectTypeNm;			// 자료유형
	private String protectYn;			// 방산기술
	@SuppressWarnings("unused")
	private String hiddenRequestNo;			// 요청번호

	public String getHiddenRequestNo() {
		return requestNo;
	}
	public String getRequestDt() {
		return DateUtil.getYmd(requestDt);
	}
}
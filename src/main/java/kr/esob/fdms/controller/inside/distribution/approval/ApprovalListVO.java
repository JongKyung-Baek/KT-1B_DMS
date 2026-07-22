package kr.esob.fdms.controller.inside.distribution.approval;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalListVO {
	private String objectType;				// 자료유형
	private String objectTypeNm;				// 자료유형
	private String requestNo;				// 요청번호
	private String deployCompanyNm;			// 배포업체
	private String deployTypeNm;			// 배포방식
	private String requestPurpose;			// 용도
	private String requestPurposeNm;			// 용도
	private String requestUserNm;			// 요청자
	private String requestDt;				// 요청일
	private String useEndYmd;				// 유효기간
	private String purchaserUserNm;			// 구매담당자
	private String businessAreaCd;			// 사업장
	private String approvalLineId;			// 결재 라인 ID
	private String currentProcessSeqNo;		// 현재 결재 라인 순서
	private String protectYn;				// 방산기술여부
	@SuppressWarnings("unused")
	private String hiddenRequestNo;			// 값을 가져다 쓰기 위한 requestNo

	public String getHiddenRequestNo() {
		return requestNo;
	}

	public String getBusinessAreaNm() {
		return ComboLang.getComboLang("businessAreaCd", this.businessAreaCd);
	}

	public String getRequestDt() {
		return DateUtil.getYmd(requestDt);
	}
}

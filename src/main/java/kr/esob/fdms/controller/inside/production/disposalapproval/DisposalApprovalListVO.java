package kr.esob.fdms.controller.inside.production.disposalapproval;

import java.util.Date;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisposalApprovalListVO {
	private String destroyRequestNo;		// 폐기요청번호
	private String statusCd;				// 결재상태
	private String statusNm;				// 결재상태
	private String destroyRequestUserCd;	// 폐기요청자(코드)
	private String destroyRequestUserNm;	// 폐기요청자
	private String businessAreaCd;			// 사업장(코드)
	private String businessAreaNm;			// 사업장
	private String productCd;				// 기종(코드)
	private String productNm;				// 기종
	private Date destroyRequestDt;			// 폐기요청일
	private String objectId;				// objectId
	private String objectNo;				// 자료번호
	private String objectNm;				// 자료명
	private String revNo;					// rev
	private String applyHogiNo;				// 호기
	private String objectType;				// 문서유형
	private String objectTypeNm;			// 문서유형

	public String getDestroyRequestDt() {
		return DateUtil.getDateString(this.destroyRequestDt);
	}
}


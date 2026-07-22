package kr.esob.fdms.controller.inside.production.disposalstatus;

import java.util.Date;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisposalStatusListVO {
	private String requestNo;				// 요청번호
	private String objectTypeNm;			// 자료유형
	private String objectClassNm2;			// 자료분류
	private String productNo;				// 기종 코드
	private String productNm;				// 기종
	private String objectNo;				// 자료번호
	private String revNo;					// rev
	private String objectNm;				// 자료명
	private String businessAreaCd;			// 사업장 코드
	private String businessAreaNm;			// 사업장
	private String requestUserCd;			// 배포자코드
	private String requestUserNm;			// 배포자
	private String acceptUserCd;			// 접수자코드(배포 수신자)
	private String acceptUserNm;			// 접수자
	private String destroyRequestUserCd;	// 폐기요청자
	private String destroyRequestUserNm;	// 폐기요청자
	private String destroyApprovalUserCd;	// 폐기결재자
	private String destroyApprovalUserNm;	// 폐기결재자
	private Date destroyApprovalDt;			// 폐기결재일
	private int currentCount;		        // 현재 보유매수 = 폐기매수
	
	public String getDestroyApprovalDt() {
		return DateUtil.getDateString(this.destroyApprovalDt);
	}

}
package kr.esob.fdms.controller.inside.production.disposal;

import java.util.Date;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisposalListVO {
	private String objectTypeNm;			// 자료유형
	private String objectClassNm2;			// 자료분류
	private String productNo;				// 기종 코드
	private String productNm;				// 기종
	private String applyHogiNo;				// 호기
	private String objectNo;				// 자료번호
	private String revNo;					// rev
	private String objectNm;				// 자료명
	private String businessAreaCd;			// 사업장 코드
	private String businessAreaNm;			// 사업장
	private String requestUserCd;			// 배포자코드
	private String requestUserNm;			// 배포자
	private Date deployDt;					// 배포일
	private String acceptUserCd;			// 접수자코드(배포 수신자)
	private String acceptUserNm;			// 접수자
	private Date acceptDt;					// 접수일
	private int currentCount;			    // 현재 보유매수 

	public String getDeployDt() {
		return DateUtil.getDateString(this.deployDt);
	}
	public String getAcceptDt() {
		return DateUtil.getDateString(this.acceptDt);
	}
}
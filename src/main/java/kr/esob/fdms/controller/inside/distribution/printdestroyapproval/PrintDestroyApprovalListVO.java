package kr.esob.fdms.controller.inside.distribution.printdestroyapproval;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintDestroyApprovalListVO {
	private String destroyRequestNo; 			// 요청번호
	private String hiddenDestroyRequestNo;
	private String destroyRequestUserNm;		// 폐기요청자
	private String destroyRequestDt;			// 요청일
	private String businessAreaCd;				// 사업장
	private String businessAreaNm;				// 사업장

	//popup Grid
	private String businessTypeNm;				// 사업구분
	private String objectType;					// 자료유형
	private String objectTypeNm;				// 자료유형
	private String objectNo;					// 자료번호
	private String revNo;						// REV
	private String swVersionNo;					// SW 버전
	private String docVersionNo;				// 명세서버전
	private String objectNm;					// 자료명
	private String currentPageNo;				// 페이지
	private String totalPageNo;					// 총페이지
	private String encNo;						// CO 번호
	private String requestPurposeNm;			// 용도
	private String requestUser;					// 요청자
	private String requestDt;					// 출력물 요청일
	private String printCount;					// 출력횟수
	private String useEndYmd;					// 출력 유효 기간
	private String destroyTodoYmd;				// 출력물 폐기 기한
	private String stdGappDt;					// 규격화 승인일
	private String changeGappDt;				// 기술 변경 승인일
	private String productNm;					// 기종
	private String protectYn;					// 방산기술자료
	private String requestDesc;					// 출력요청사유
	private String destroyApprovalDt;			// 폐기승인일
	private String destroyStatusNm;				// 폐기상태
	private String destroyDt;					// 폐기일자
//	private String destroyApprovalUser;			// 폐기 승인자
	private String objectId;
	private String requestNo;
	private String fileNo;
	private String orgFileNm;

	public String getRequestPurposeNm() {
		return ComboLang.getComboLang("requestPurpose", this.requestPurposeNm);
	}

	public String getDestroyRequestDt() {
		return DateUtil.getYmd(destroyRequestDt);
	}
	public String getRequestDt() {
		return DateUtil.getYmd(requestDt);
	}
	public String getStdGappDt() {
		return DateUtil.getYmd(stdGappDt);
	}
	public String getChangeGappDt() {
		return DateUtil.getYmd(changeGappDt);
	}
	public String getDestroyApprovalDt() {
		return DateUtil.getYmd(destroyApprovalDt);
	}
	public String getDestroyDt() {
		return DateUtil.getYmd(destroyDt);
	}

}
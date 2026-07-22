package kr.esob.fdms.controller.inside.distribution.printhistory;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryListVO {
	private String requestNo;			// 요청번호
	private String businessTypeCd;		// 사업구분
	private String businessTypeNm;
	private String objectType;			// 자료유형
	private String objectTypeNm;		// 자료유형
	private String objectNo;			// 자료번호
	private String revNo;				// REV
	private String swVersionNo;			// SW버전
	private String docVersionNo;		// 명세서버전
	private String objectNm;			// 자료명
	private Integer currentPageNo;		// 페이지
	private String totalPageNo;			// 총 페이지
	private String ecnNo;				// CO 번호
	private String requestPurpose;		// 용도
	private String requestPurposeNm;	// 용도
	private String requestUserNm;		// 요청자
	private String requestUserCd;		// 요청자
	private String requestDt;			// 요청일자
	private String printCount;			// 출력횟수
	private String useEndYmd;			// 유효기간
	private String useEndYmdYn;			// 유효기간체크
	private String destroyTodoYmd;		// 폐기기한
	private String stdGappDt;			// 규격화 승인일
	private String changeGappDt;		// 기술변경승인일
	private String productNm;			// 기종
	private String businessAreaCd;		// 사업장
	private String businessAreaNm;		// 사업장
	private String protectYn;			// 방산기술
	private String requestDesc;			// 출력요청사유
	private String destroyRequestUserNm;	// 폐기요청자
	private String destroyApprovalDt;		// 폐기승인일
	private String destroyDt;				// 폐기일자
	private String destroyStatusCd;			// 폐기상태코드
	private String destroyStatusNm;			// 폐기상태명
	private String destroyType;				// 폐기구분코드
	private String destroyTypeNm;			// 폐기구분명
	private String objectId;
	private String hiddenRequestNo;			// 데이터 표시를 위한 requestNo
	private String fileNo;
	private String orgFileNm;
	private String orgFileNmData;
	private String approvalUserNm;          //결재자

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
	public String getOrgFileNmData() {
		return this.orgFileNm;
	}
}

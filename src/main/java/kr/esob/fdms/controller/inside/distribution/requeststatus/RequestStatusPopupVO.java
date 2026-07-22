package kr.esob.fdms.controller.inside.distribution.requeststatus;

import kr.esob.fdms.commonlogic.combo.ComboLang;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestStatusPopupVO {
	private String requestDesc;		// 요청 사유
	private String rejectDesc;		// 반려 사유
	private String requestNo;		// 요청 번호


	private String businessTypeCd;		//사업구분
	private String drawingType;         // 도면종류. 2023.07.26 기범추가
	private String distributeMethodCd;	//배포방식
	private String objectType;			//자료유형
	private String distributeType;		//배포유형
	private String dataNo;				//자료번호
	private String rev;					//REV
	private String swVersion;			//S/W버전
	private String docVersionNo;		//명세서버전
	private String name;				//자료명
	private String page;				//페이지
	private String totalPage;			//총페이지
	private String stdGappDt;			//규격화승인일
	private String changeGappDt;		//기술변경승인일
	private String productCd;			//기종
	private String productNm;			//기종
	private String businessAreaCd;		//사업장구분
	private String protectYn;			//방산기술
	private String objectId;			//아이템id
	private String fileNo;
	private String orgFileNm;
	private String requestType;
	private String orgFileNmData;
	private String requestUserCd;
	private String securityTypeCd;
	private String statusCd;
	private String statusNm;

	public String getOrgFileNmData() {
		return this.orgFileNm;
	}

}

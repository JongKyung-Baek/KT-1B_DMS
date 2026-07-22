package kr.esob.fdms.controller.inside.production.requeststatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintRequestStatusVO {
	private String requestNo;
	private String requestDesc;			// 요청내용
	private String objectType;			// 자료유형
	private String rejectDesc;			// 반려사유

	private String objectNo;
	private String rev;
	private String objectNm;
	private String insertUid;
	private String insertDt;
	private String updateDt;
	private String totalCount;
	private String productNo;
	private String callId;
	private String applyProductNo;
	private String securityTypeCd;
	private String objectTypeData;
	private String requestUserCd;
	private String actionCd;

	private String objectId;
	private int fileNo;
}
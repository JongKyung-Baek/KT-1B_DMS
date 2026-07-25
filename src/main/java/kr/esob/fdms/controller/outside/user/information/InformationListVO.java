package kr.esob.fdms.controller.outside.user.information;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InformationListVO {
	private String requestNo;
	private String requestType;
	private String requestNm;
	private String userNm;
	private String requestUserCd;
	private String requestUserNm;
	private String requestDt;
	private String approvalDt;
	private String statusCd;
	private String email;
	private String protectYn;
	private String requestReason;
	private String rejectReason;
	private String insertUserCd;
	private String insertDt;
	private String insertUserNm;
	private String insertCompanyNm;
	private String approvalUserCd;
	private String approvalUserNm;
}

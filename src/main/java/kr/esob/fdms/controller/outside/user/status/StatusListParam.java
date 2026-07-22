package kr.esob.fdms.controller.outside.user.status;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusListParam extends CommonParam {
	private String requestNo;
	private String requestNm;
	private String userNm;
	private String insertUserCd;
	private String insertUserNm;
	private String insertDt;
	private String approvalDt;
	private String statusCd;
	private String statusNm;
	private String rejectReason;

	private String approvalUserNm;

	private String email;
	private String protectYn;
	private String crYn;
	private String requestReason;


	private String RequestType;

	private String requestStartDt;
	private String requestEndDt;
}

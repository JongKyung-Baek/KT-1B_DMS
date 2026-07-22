package kr.esob.fdms.controller.outside.cr.request;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutsideCrParam extends CommonParam {
	private String crNo;
	private String approvalUser;
	private String reviewResult;
	/**
	 * CR상태
	 * 1: CR요청(협력사)
	 * 2: 구매담당자 거절
	 * 3: 구매담당자 접수
	 * 4: 구매팀장 거절
	 * 5: 구매팀장 승인
	 * 6: PDM 전송(접수)
	 */
	private int statusCd;
	private String reqStatusCd;
	private String materialNo;
	private String productNo;

	private String actionCd;
	private String approvalGradeCd;
	private String requestDesc;
	private String actualUserCd;
	private String approvalStatusCd;
	private String rejectDesc;
	private int currentProcessSeqNo;
	private List<String> filePathNmList;


}

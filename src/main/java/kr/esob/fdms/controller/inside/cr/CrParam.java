package kr.esob.fdms.controller.inside.cr;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrParam extends CommonParam {
	private String crNo;
	private String approvalUser;
	private String reviewResult;
	private String rejectReason;
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
	private String materialNo;
	private String productNo;

	private String actionCd;
	private String approvalGradeCd;
	private String requestDesc;
	private String actualUserCd;
	private String approvalStatusCd;
	private int currentProcessSeqNo;
	private List<String> filePathNmList;
	private StatusYn sendEmailYn = StatusYn.Y;


}

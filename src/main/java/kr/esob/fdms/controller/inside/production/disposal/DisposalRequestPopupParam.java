package kr.esob.fdms.controller.inside.production.disposal;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisposalRequestPopupParam extends CommonParam {
	private String objectNo;			// 자료번호
	private String acceptUserCd;		// 배포 수신자
	private int currentCount;		    // 현재 보유매수 = 폐기매수

	private String processSeq;
	private String approvalStatusCd;
	private String approvalGradeCd;
	private String teamLeaderUid;
	private String requestDesc;
	private String requestUserCd;

	private String requestNo;
	private String destroyRequestNo;
	private String objectId;
	private String businessAreaCd;
	private StatusYn sendEmailYn = StatusYn.Y;

	private int fileNo;

	List<DisposalRequestPopupParam> list;
}
package kr.esob.fdms.controller.inside.distribution.printhistory;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestroyRequestParam extends CommonParam {
	private String objectId;			    // 자료번호
	private String requestNo;			    // 요청번호
	private String protectYn;			    // 방산여부
	private String destroyRequestNo;	    // 폐기 요청 번호
	private String processSeq;			    // 프로세스 순번
	private String approvalStatusCd;	    // 결재상태코드
	private String approvalGradeCd;		    // 결재등급코드
	private String originalUserCd;		    // 원래사용자코드
	private String actualUserCd;		    // 실제 action 사용자
	private String approvalLineId;		    // 결재 라인 ID
	private String currentProcessSeqNo;	    // 현재
	private String requestDesc;			    // 신청사유
	private String teamLeaderUid;		    // 결재자
	private String protectiveOfficerUid;	// 방산팀장
	
	private String businessAreaCd;			//사업장 코드
	
	private String productProtectYn;				//개발 방산보호
	private String developmentProtectYn;			//양산 방산보호
	
	private String developmentProtectUserUid;		//개발 결재자
	private String productProtectUid;				//양산 결재자
	
	private String businessTypeCd;
	
	private List<String> protectUserUid;
	private List<DestroyRequestParam> list;
}
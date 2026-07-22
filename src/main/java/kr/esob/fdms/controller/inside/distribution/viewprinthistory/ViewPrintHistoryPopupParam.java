package kr.esob.fdms.controller.inside.distribution.viewprinthistory;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ViewPrintHistoryPopupParam extends CommonParam {
	private String cnSerial;
	private String protectYn;
	private String companyCd;
	private String companyUserCd;
	private String deployUserEmail;
	private String purchaseTeam;
	private String purchaseUid;
	private String protectiveOfficerUid;
	private String distributeMethodCd;				//파일배포 유형 (VIEW, GENERAL, SECURITY)
	private String requestPurpose;
	private String requestPurposeTerm;				//유효기간 (개월)
	private String requestDesc;
	private String requestType;						//파일 유형 (DOC, DRAWING, SW)
	private String approvalRequestType;				//요청 유형 (배포, 출력)
	private String approvalLineId;
	private String currentProcessSeqNo;
	private String useStartYmd;
	private String useEndYmd;
	private String printYn;							//뷰/출력 여부
	private String deployNormalYn;
	private String deploySpecialYn;
	private String objectType;
	private String requestNo;
	private String requestKeyType;
	private String processSeq;
	private String approvalStatusCd;
	private String approvalGradeCd;
	private String actionCd;
	private String objectId;
	private String businessTypeCd;					//개발/양산

	private String fileDistributionType;			//파일 배포 방식(general, security)
	private String destroyTodoYmd;					//폐기 예정 일자

	private String deployTerm;						//유효기간(개월)
	
	private String businessAreaCd;					//사업장
	
	
	private String productProtectYn;				//개발 방산보호
	private String developmentProtectYn;			//양산 방산보호
	
	private String developmentProtectUserUid;		//개발 결재자
	private String productProtectUid;				//양산 결재자
	
	private List<String> protectUserUid;
	private List<ViewPrintHistoryPopupParam> list;
}

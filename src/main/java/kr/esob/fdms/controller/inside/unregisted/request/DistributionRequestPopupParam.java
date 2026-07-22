package kr.esob.fdms.controller.inside.unregisted.request;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributionRequestPopupParam extends CommonParam {
	private String objectType;					//자료 유형 (DOC, DRAWING, SW)
	private String objectNo;					//자료 번호
	private String objectId;					//자료 cd
	private String requestKeyType;				//요청 번호 유형
	private String requestPurpose;				//요청유형
	private String useStartYmd;					//시작 날짜
	private String useEndYmd;					//종료 날짜
	private String fileDistributionType;		//파일 배포 방식(general, security)
	private String printYn;						//출력/Viewing 여부
	private String deployNormalYn;				//일반영역
	private String deploySpecialYn;				//보안영역
	private String deployTerm;					//유효기간(개월)
	private String approvalLineId;				//결재 라인 ID
	private String currentProcessSeqNo;			//현재 결재 순서
	private String requestNo;					//요청번호
	private String companyCd;					//회사코드
	private String companyUserCd;				//회사 - 직원 (배포)
	private String deployUserEmail;				//회사 - 직원이메일(배포)
	private String requestDesc;					//요청내용
	private String purchaseTeam;				//구매담당자
	private String teamLeader;					//결재자
	private String businessAreaCd;				//사업장코드
	private String securityEmailCC;				//보안팀 이메일 참조
	private String fileNm;
	private long fileSize;
	private String filePath;
	private String fileCd;
	private int fileNo;

	private StatusYn sendEmailYn = StatusYn.Y;




	//요청 상세
	private String processSeq;					//순서
	private String ApprovalStatusCd;
	private String ApprovalGradeCd;

	List<DistributionRequestPopupParam> list;
	List<String> securityUserList;
}
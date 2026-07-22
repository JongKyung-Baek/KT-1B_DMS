package kr.esob.fdms.controller.outside.outregisted.request;

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
	private String deployTerm;					//유효기간(개월)
	private String approvalLineId;				//결재 라인 ID
	private String currentProcessSeqNo;			//현재 결재 순서
	private String requestNo;					//요청번호
	private String deployUserCd;				//전송자
	private String deployUserEmail;				//전송자 이메일
	private String receiveUserCd;				//수신자
	private String transDesc;					//파일전송사유
	private String businessAreaCd;				//사업장코드
	
	private String fileNm;
	private String orgFileNm;
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
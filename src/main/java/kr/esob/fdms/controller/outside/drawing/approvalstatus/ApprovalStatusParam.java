package kr.esob.fdms.controller.outside.drawing.approvalstatus;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.value.StatusYn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalStatusParam extends CommonParam {
	//요청용도
	private String requestPurpose;
	//요청유형(도면/문서/SW)
	private String requestType;
	//Email
	private String deployUserEmail;
	//배포요청자와 실제 사용자가 다를경우 사용됨
	private String deployUserId;
	//구매담당자
	private String acceptanceUserId;
	//구매담당자 대결자
	private String insteadAcceptanceUserId;
	//요청사유
	private String requestDesc;
	//배포기간
	private String deployTerm;
	//출력여부
	private StatusYn printYn;
	//워터마크여부
	private StatusYn watermarkYn;
	//캡쳐여부
	private StatusYn captureYn;
	//일반배포여부
	private StatusYn deployNormalYn;
	//보안배포여부
	private StatusYn deploySpecialYn;


	private boolean isNormalRequest = false;
	private boolean isProtectRequest = false;

	//도면정보
	private List<ApprovalStatusInfoVO> drawingList;

}

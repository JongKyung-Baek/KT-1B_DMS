package kr.esob.fdms.controller.outside.commonrequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.value.StatusYn;
import kr.esob.fdms.controller.inside.production.common.AcceptanceInfoVO;
import kr.esob.fdms.controller.inside.production.common.DeployInfoVO;
import kr.esob.fdms.controller.inside.production.common.ProductionInfoVO;
import kr.esob.fdms.controller.inside.production.productionstatus.ProductionReplaceRequestVO;
import kr.esob.fdms.controller.inside.production.request.PrintRequestVO;
import kr.esob.fdms.controller.outside.doc.request.DocInfoVO;
import kr.esob.fdms.controller.outside.drawing.request.DrawingInfoVO;
import kr.esob.fdms.controller.outside.sw.request.SwInfoVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestParam extends CommonParam {
	private String requestNo;
	//요청용도
	private String requestPurpose;
	//요청유형(PRINT:출력물/DISTRIBUTION:배포/USER:사용자변경/CR:CR요청/NO-REG:미등록자료/EDIT:설계변경/PRODUCT:생산기술자료)
	private String requestType;
	//요청유형코드(requestNo에 사용)
	private String objectTypeCode;
	//Email
	private String deployUserEmail;
	//배포요청자와 실제 사용자가 다를경우 사용됨
	private String deployUserCd;
	//배포요청자와 실제 사용자가 다를경우 사용됨
	private String deployUserId;
	// 배포 업체
	private String deployCompanyCd;
	//구매담당자(페이지에서 요청자가 실제 선택한 구매담당자)
	private String acceptanceUserCd;
	//구매담당자 대결자
	private String insteadAcceptanceUserCd;
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
	//배포 방식
	private String fileDistributionType;
	private String distributionNormalYn;
	private String distributionSpecialYn;
	//일반배포여부
	private StatusYn deployNormalYn;
	//보안배포여부
	private StatusYn deploySpecialYn;
	//결재 라인 종류(1 : 일반배포요청 / 2: 방산자료배포요청)
	private int approvalLineId;
	//결재유형(DRAWING:도면/DOC:문서/SW:SW)
	private String objectType;
	private String protectYn;

	//원래 구매담당자
	private String purchaserUserCd;

	private String businessAreaCd;
	private String productCd;

	private String deployType;

	private String teamLeader;
	private String deployDate;
	private String validDate;

	private String objectId;
//	private String teamLeaderUid;
	private String requestUserCd;
	private String objectNo;
	private String deployDeptCd;

	private String curObjectId;
	private String prevObjectId;
	private String curRevNo;
	private String prevRevNo;
	private String curFilePath;
	private String prevFilePath;

	private StatusYn sendEmailYn = StatusYn.Y;


	private StatusYn watermarkDeployDtYn;

	private List<RequestMappingParam> requestMappingParamList;
	private List<RequestFileParam> requestFileParamList;

	private boolean isNormalRequest = false;
	private boolean isDevelopmentRequest = false;
	private boolean isProductionRequest = false;

	private List<ObjectInfoVO> list;
	private List<ObjectInfoVO> fileList;

	private List<DrawingInfoVO> drawingInfoVoList;
	private List<DocInfoVO> docInfoVoList;
	private List<SwInfoVO> swInfoVoList;

	private List<AcceptanceInfoVO> acceptanceList;
	private List<ProductionInfoVO> productionList;
	private List<DeployInfoVO> deployInfoList;
	private List<ProductionReplaceRequestVO> replaceList;

	private List<PrintRequestVO> printRequestList;

	//외부사용자 CR 요청시 결재자 cd
	private String outApprovalTeamLeaderCd;

	public String getDeployDate() {
		return this.deployDate.replaceAll("-", "");
	}

	public String getValidDate() {
		return this.validDate.replaceAll("-", "");
	}
}

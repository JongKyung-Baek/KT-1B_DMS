package kr.esob.fdms.controller.inside.distribution.commonrequest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.util.DateUtil;

@Service
public class CommonDistributionRequestService {

	@Inject
	CommonDistributionRequestDao dao;

	@Inject
	DateUtil dateUtil;

	@Inject
	DocsMailService mailService;

	public String getUserEmail(String userCd) {
		return dao.getUserEmail(userCd);
	}

	public ResultVO saveApprovalRequest(CommonDistributionRequestParam param) {
		if(isProductTeam(param)) {
			return productTeamApprovalRequest(param);
		}else {
			return approvalRequest(param);
		}
	}

	public boolean isProductTeam(CommonDistributionRequestParam param) {
		if(dao.selectInsaDeptProductTeam(param.getSessionUser()) > 0) {
			return true;
		}else {
			return false;
		}
	}

	private ResultVO productTeamApprovalRequest(CommonDistributionRequestParam param) {
		ResultVO resultVo = new ResultVO();
		List<CommonDistributionRequestParam> arrNonProtect = new ArrayList<CommonDistributionRequestParam>();
		DocsMailEnum mailEnum = DocsMailEnum.DISTRIBUTION_PRODUCT_APPROVAL_OUTSIDE;
		for(CommonDistributionRequestParam tempParam : param.getList()) {
			arrNonProtect.add(tempParam);
		}
		param.setRequestKeyType("-P");
		param.setUseStartYmd(dateUtil.getToday("yyyyMMdd"));
		param.setDeployNormalYn("Y");
		param.setDeploySpecialYn("N");
		param.setObjectType(param.getRequestType());
		param.setProtectYn("N");
		param.setApprovalLineId("9"); //생산기술자료 외부사용자용 배포 APPROVAL_LINE 번호
		param.setCurrentProcessSeqNo("2");

		insertRequest(param, arrNonProtect);
		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				MailInfoVO mailInfoVo = mailService.selectReceiveUser(param.getPurchaseTeam());
				mailInfoVo.setMailEnum(mailEnum);
				mailService.sendDocsMail(mailInfoVo);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		resultVo.setSuccess(true);

		return resultVo;
	}

	private ResultVO approvalRequest(CommonDistributionRequestParam param) {
		ResultVO resultVo = new ResultVO();
		List<CommonDistributionRequestParam> arrNonProtect = new ArrayList<CommonDistributionRequestParam>();
		List<CommonDistributionRequestParam> arrDevelopmentProtect = new ArrayList<CommonDistributionRequestParam>();
		List<CommonDistributionRequestParam> arrProductionProtect = new ArrayList<CommonDistributionRequestParam>();

		DocsMailEnum mailEnum = DocsMailEnum.DISTRIBUTION_APPROVAL;
		for(CommonDistributionRequestParam tempParam : param.getList()) {
			if("Y".equals(tempParam.getProtectYn())) {							//방산
				//개발, 양산 구분
				if( (null != param.getProtectUserUid()) ) {
				List<Map<String, Object>> protectUserList = dao.getProtectUser(param);
					for(Map<String, Object> tempMap : protectUserList) {
						if("Production".equals(tempMap.get("BUSINESS_TYPE_CD").toString())) {
							param.setProductProtectUid(tempMap.get("USER_CD").toString());
						}else if("Development".equals(tempMap.get("BUSINESS_TYPE_CD").toString())) {
							param.setDevelopmentProtectUserUid(tempMap.get("USER_CD").toString());
						}
					}
				}

				if("PRINT".equals(param.getApprovalRequestType())) {
					//오늘 날로 부터 7일
					tempParam.setUseEndYmd(dateUtil.getAddDay(7, "yyyyMMdd"));				//특정일수더하기 + 7 (YYYYMMDD)
				}
				else if("DISTRIBUTION".equals(param.getApprovalRequestType())){				//배포승인요청
					tempParam.setUseEndYmd(dateUtil.getAddMonth(tempParam.getRequestPurposeTerm(), "yyyyMMdd"));//특정개월수더하기 (YYYYMMDD)
					tempParam.setDeployTerm(tempParam.getRequestPurposeTerm());
				}

//				param.setUseEndYmd(dateUtil.getAddMonth(param.getRequestPurposeTerm(), "yyyyMMdd"));
//				param.setDeployTerm(param.getRequestPurposeTerm().replaceAll("개월", ""));

				if("Development".equals(tempParam.getBusinessTypeCd())) {		//개발
					arrDevelopmentProtect.add(tempParam);
				}else if("Production".equals(tempParam.getBusinessTypeCd())) {	//양산
					arrProductionProtect.add(tempParam);
				}
			}else {	// 방산 아님
//			}else if("N".equals(tempParam.getProtectYn())) {					// 방산 아님
				arrNonProtect.add(tempParam);
			}
		}

		if("DRAWING".equals(param.getRequestType())) {
			param.setRequestKeyType("-D");
		}else if("DOC".equals(param.getRequestType())) {
			param.setRequestKeyType("-G");
		}else if("SW".equals(param.getRequestType())) {
			param.setRequestKeyType("-S");
		}else if("PRODUCT_DOC".equals(param.getRequestType())) {
			param.setRequestKeyType("-P");
		}else if("PRODUCT_SW".equals(param.getRequestType())) {
			param.setRequestKeyType("-P");
		}

		param.setUseStartYmd(dateUtil.getToday("yyyyMMdd"));							//오늘날짜 (YYYYMMDD)
		if("PRINT".equals(param.getApprovalRequestType())) {							//출력승인요청
			mailEnum = DocsMailEnum.DISTRIBUTION_PRINT_APPROVAL;
			param.setDeployNormalYn("N");
			param.setDeploySpecialYn("N");
			param.setPrintYn("Y");
			param.setDeployUserEmail(param.getSessionUser().getEmail());
			param.setCompanyUserCd(param.getSessionUser().getUserCd());
			param.setCompanyCd(param.getSessionUser().getCompanyCd());

			if( !(null==param.getDestroyTodoYmd()) ) {
				param.setDestroyTodoYmd(param.getDestroyTodoYmd().replace("-", ""));	//yyyy-mm-dd -> yyyymmdd
			}
//			param.setDestroyTodoYmd(dateUtil.getAddDay("7", "yyyyMMdd"));				//특정일수더하기 + 7 (YYYYMMDD)
		}else if("DISTRIBUTION".equals(param.getApprovalRequestType())){				//배포승인요청
			mailEnum = DocsMailEnum.DISTRIBUTION_APPROVAL;
			//배포유형 (체크박스)
			System.out.println("yskim:test - " + param.getFileDistributionType());
			if("general".equals(param.getFileDistributionType())) {
				param.setDeployNormalYn("Y");
				param.setDeploySpecialYn("N");
			}else if("security".equals(param.getFileDistributionType())) {
				param.setDeployNormalYn("N");
				param.setDeploySpecialYn("Y");
			}else {
				param.setDeployNormalYn("N");
				param.setDeploySpecialYn("N");
			}
		}
		param.setObjectType(param.getRequestType());
		if(arrDevelopmentProtect.size()>0) {	//방산 요청이 있을경우 (양산)
			param.setProtectYn("Y");
			param.setApprovalLineId("1");
			param.setCurrentProcessSeqNo("3");
			param.setBusinessTypeCd("Development");

			insertRequest(param, arrDevelopmentProtect);
		}

		if(arrProductionProtect.size()>0) {	//방산 요청이 있을경우 (개발)
			param.setProtectYn("Y");
			param.setApprovalLineId("1");
			param.setCurrentProcessSeqNo("3");
			param.setBusinessTypeCd("Production");

			insertRequest(param, arrProductionProtect);
		}

		if(arrNonProtect.size()>0) {	//요청이 있을경우 (방산x)
			param.setProtectYn("N");
			param.setApprovalLineId("2");
			param.setCurrentProcessSeqNo("3");

			insertRequest(param, arrNonProtect);

		}
		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				MailInfoVO mailInfoVo = mailService.selectReceiveUser(param.getPurchaseUid());

				MailInfoVO from = mailService.selectReceiveUser(param.getCompanyUserCd());

				mailInfoVo.setFromUserId(from.getToUserId());
				mailInfoVo.setFromMail(from.getToMail());

				mailInfoVo.setMailEnum(mailEnum);
				mailService.sendDocsMail(mailInfoVo);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		resultVo.setSuccess(true);
		return resultVo;
	}

	private void insertRequest(CommonDistributionRequestParam param, List<CommonDistributionRequestParam> arrList) {
		//INSERT DOCS_REQUEST
		dao.insertRequest(param);

		//INSERT DOCS_REQUEST_DEPLOY
		dao.insertRequestDeploy(param);

		@SuppressWarnings("unchecked")
		List<ApprovalLineDetailVO> appLindDetail = dao.getDocsApprovalLineDetail(param.getApprovalLineId());
		//INSERT DOCS_REQUEST_DETAIL
		for(int i=1; i<=appLindDetail.size(); i++) {
			param.setProcessSeq(Integer.toString(i));
			param.setApprovalStatusCd(appLindDetail.get(i-1).getApprovalStatusCd());
			param.setApprovalGradeCd(appLindDetail.get(i-1).getApprovalGradeCd());
			dao.insertDocsRequestDetail(param);
		}
		//INSERT DOCS_REQUEST_MAPPING
		for(CommonDistributionRequestParam vo : arrList) {
			vo.setRequestNo(param.getRequestNo());
			vo.setDeployTerm(vo.getRequestPurposeTerm());

			if("PRINT".equals(param.getApprovalRequestType())) {
				vo.setUseStartYmd(dateUtil.getToday("yyyyMMdd"));
				if( !(null==param.getDestroyTodoYmd()) ) {
					vo.setUseEndYmd(param.getDestroyTodoYmd().replace("-", ""));	//yyyy-mm-dd -> yyyymmdd
				}
			}else {
				vo.setUseStartYmd(dateUtil.getToday("yyyyMMdd"));
				vo.setUseEndYmd(dateUtil.getAddMonth(vo.getRequestPurposeTerm(), "yyyyMMdd"));	//특정개월수더하기 (YYYYMMDD)
			}
			param.setObjectId(vo.getObjectId());
			List<CommonDistributionRequestParam> check = dao.selectRequestMappingDuplicate(param);
			if(check.size() == 0) {
				dao.insertDocsRequestMapping(param);
			}
//			if(!param.getRequestType().contains("PRODUCT")) {																//not 생산기술자료
//				if( ("MANUAL".equals(param.getRequestPurpose())) || ("PRODUCT".equals(param.getRequestPurpose())) ) {			// 교범용, 제조용
//					if( !(null==vo.getDistributeTypeCd()) && ("hdCADApprovedDrawing".equals(vo.getDistributeTypeCd())) ) {
//						vo.setUseEndYmd(dateUtil.getAddMonth("60", "yyyyMMdd"));
//					}
//				}
//			}
			dao.insertDocsRequestFile(vo);
		}

	}

	public ResultVO verificationProtectUser(CommonDistributionRequestParam param) {
		ResultVO result = new ResultVO();
		int developmentCount = dao.verificationProtectUser(param);
		if(1 < developmentCount) {
			result.setFailReason("twoDevelopment");
		}
		result.isSuccess();
		return result;
	}


	public List<CommonDistributionRequestVO> selectProtectPopupList(CommonDistributionRequestParam param) {
		return dao.selectProtectPopupList(param);
	}


}



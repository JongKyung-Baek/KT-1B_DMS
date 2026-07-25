package kr.esob.fdms.controller.inside.distribution.commonrequest;

import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.login.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CommonApprovalService {

	@Inject
	CommonApprovalDao dao;

	@Inject
	DocsMailService mailService;

	public String getDistributionApprovalRequestDesc(CommonApprovalParam param) {
		return dao.getDistributionApprovalRequestDesc(param);
	}

	public List<CommonApprovalPopupListVO> selectApprovalList(CommonApprovalParam param) {
		return dao.selectApprovalList(param);
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO saveApproval(CommonApprovalParam param) {
		log.info("{} is called", this.getClass().getSimpleName());

		validateApprovalRequest(param);
		UserVO actor = requireAuthenticatedActor();
		param.setSessionUser(actor);

		ResultVO result = new ResultVO();
		CommonApprovalParam approveParam = dao.getCurrentApprovalInfo(param);
		if (approveParam == null) {
			throw new AccessDeniedException("Approval request is not accessible");
		}
		param.setApprovalStatusCd(approveParam.getApprovalStatusCd());
		param.setApprovalGradeCd(approveParam.getApprovalGradeCd());
		param.setCurrentProcessSeqNo(approveParam.getCurrentProcessSeqNo());
		param.setProtectYn(approveParam.getProtectYn());
		param.setObjectType(approveParam.getObjectType());
		param.setApprovalType(approveParam.getApprovalType());
		param.setRequestType(approveParam.getApprovalType());
		boolean isDestroyExists = false;
		if( "A".equals(param.getSaveType()) ) {			//승인
			param.setActionCd("APPROVAL");
			param.setApprovalStatusCd("APPROVAL");
			if( "TL".equals(approveParam.getApprovalGradeCd()) && "Y".equals(approveParam.getProtectYn())){	//현재 구매팀장 결재면서 방산결재면 방산팀장한테 보내기
				param.setStatusCd("ACCEPT");
				requireSingleRow(dao.updateRequestDefInfo(param), "advance approval request");

			}else {																						// 최종승인
				param.setStatusCd("APPROVAL");
				param.setApprovalStatusCd("APPROVAL");
				requireSingleRow(dao.updateRequestInfo(param), "approve request");
				//최종승인인 경우 파일 결재 정보 테이블(DOCS_APPROVAL_FILE)에 각 아이템 추가
				List<CommonApprovalPopupListVO> itemList = dao.selectItemList(param);
				for(CommonApprovalPopupListVO tempVo : itemList) {
					CommonApprovalParam tempParam = new CommonApprovalParam();
					tempParam.setSessionUser(actor);
					tempParam.setObjectId(tempVo.getObjectId());
					tempParam.setRequestNo(param.getRequestNo());
					tempParam.setDeployUserCd(tempVo.getDeployUserCd());
					tempParam.setFileNo(tempVo.getFileNo());
					requireSingleRow(dao.insertApprovalFile(tempParam), "record approved file");
				}

				//같은 object_id 중에서 가장 높은 rev 선별
				Map<String, CommonApprovalParam> destoryItemMap = new HashMap<String, CommonApprovalParam>();
				for(CommonApprovalPopupListVO tempVo : itemList) {
					String key = tempVo.getObjectNo();
					if("DRAWING".equals(tempVo.getObjectTypeCd())) {
						key = tempVo.getObjectNo() + tempVo.getCurrentPage();
					}
					if( destoryItemMap.containsKey(key)) {
						CommonApprovalParam destroyParam = destoryItemMap.get(key);
						if( destroyParam.getRev().compareTo(tempVo.getRev()) < 0 ) {
							destroyParam.setRev(tempVo.getRev());
							destroyParam.setCurrentPage(tempVo.getCurrentPage());
							destroyParam.setObjectType(tempVo.getObjectTypeCd());
							destoryItemMap.put(key, destroyParam);
						}
					}else {
						CommonApprovalParam destroyParam = new CommonApprovalParam();
						destroyParam.setObjectId(tempVo.getObjectId());
						destroyParam.setObjectNo(tempVo.getObjectNo());
						destroyParam.setRev(tempVo.getRev());
						destroyParam.setRequestNo(tempVo.getRequestNo());
						destroyParam.setBusinessAreaCd(tempVo.getBusinessArea());
						destroyParam.setDeployUserCd(tempVo.getDeployUserCd());
						destroyParam.setDeployCompanyCd(tempVo.getDeployCompanyCd());
						destroyParam.setCurrentPage(tempVo.getCurrentPage());
						destroyParam.setObjectType(tempVo.getObjectTypeCd());
						destoryItemMap.put(key, destroyParam);
					}
				}


				//배포(DISTRIBUTION)인 경우 이전 배포 내역 폐기
				if("DISTRIBUTION".equals(param.getApprovalType())) {
					//objectType이 도면일 경우 currentPage가 같은 배포건의 이전 rev를 폐기처리

					//배포 폐기 요청에 등록 (이전 버전 폐기)
					if( null != destoryItemMap ) {
						isDestroyExists = true;
						for( String key : destoryItemMap.keySet() ){
							//이전 버전 배포 폐기해야 할 파일 조회 후 폐기중으로 처리
							dao.updateRequestFileDestroy(destoryItemMap.get(key));
						}
					}

				}

			}
			param.setRejectDesc(null);

		}else if( "R".equals(param.getSaveType()) ) {	//반려
			param.setActionCd("REJECT");
			param.setStatusCd("REJECT");
			param.setApprovalStatusCd("REJECT");
			requireSingleRow(dao.updateRequestInfo(param), "reject request");
		}
//		param.setCurrentProcessSeqNo("3");
		param.setApprovalStatusCd(approveParam.getApprovalStatusCd());
		param.setApprovalGradeCd(approveParam.getApprovalGradeCd());
		param.setCurrentProcessSeqNo(approveParam.getCurrentProcessSeqNo());
		requireSingleRow(dao.updateRequestDetail(param), "complete approval step");

		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				sendMail(param, approveParam, isDestroyExists);
			}
		}catch(Exception e) {
			log.warn("Approval notification failed. cause={}", e.getClass().getSimpleName());
		}

		result.setSuccess(true);
		return result;
	}

	private void sendMail(CommonApprovalParam param, CommonApprovalParam approveParam, boolean isDestroyExists) throws Exception {
		MailInfoVO mailInfoVo = new MailInfoVO();
		if("PRINT".equals(param.getRequestType())) {
			mailInfoVo = mailService.selectRequestUserInfo(param);
			mailInfoVo.setFromMail(mailService.selectPurchaserEmail(param));
			mailInfoVo.setMailEnum(DocsMailEnum.DISTRIBUTION_PRINT_HISTORY);
		}else {
			if("A".equals(param.getSaveType())) {
				if( "TL".equals(approveParam.getApprovalGradeCd()) && "Y".equals(approveParam.getProtectYn())){
					mailInfoVo = mailService.selectDefUserInfo(param);
					mailInfoVo.setFromMail(mailService.selectPurchaserEmail(param));
					mailInfoVo.setMailEnum(DocsMailEnum.DISTRIBUTION_APPROVAL);
				}else {
					mailInfoVo = mailService.selectDeployUserInfo(param);
					mailInfoVo.setFromMail(mailService.selectPurchaserEmail(param));
					switch(param.getObjectType()) {
						case "DRAWING":
							mailInfoVo.setMailEnum(DocsMailEnum.DISTRIBUTION_DRAWING_STATUS);
							break;
						case "DOC":
							mailInfoVo.setMailEnum(DocsMailEnum.DISTRIBUTION_DOC_STATUS);
							break;
						case "SW":
							mailInfoVo.setMailEnum(DocsMailEnum.DISTRIBUTION_SW_STATUS);
							break;
						case "PRODUCT_SW":
							mailInfoVo.setMailEnum(DocsMailEnum.DISTRIBUTION_PRODUCT_STATUS);
							break;
						case "PRODUCT_DOC":
							mailInfoVo.setMailEnum(DocsMailEnum.DISTRIBUTION_PRODUCT_STATUS);
							break;
					}
					//if(isDestroyExists) {
					//	mailInfoVo.setAppendContent("이전버전 폐기를 진행해 주십시오.");
					//}
				}
			}else {
				return;
			}
		}
		mailService.sendDocsMail(mailInfoVo);

	}

	public int selectListCount(CommonApprovalParam param) {
		return dao.selectListCount(param);
	}

	/**
	 * 방산기술 결재자 이관
	 * @param param
	 */
	@Transactional(rollbackFor = Exception.class)
	public ResultVO savePass(PassParamVO param) {
		ResultVO result = new ResultVO();
		if (param == null || isBlank(param.getRequestNo()) || isBlank(param.getPassTarget())) {
			throw new IllegalArgumentException("Request number and pass target are required");
		}
		param.setSessionUser(requireAuthenticatedActor());

		String[] arrRequestNo = param.getRequestNo().split(",");

		for (String requestNo : arrRequestNo) {
			String normalizedRequestNo = requestNo == null ? null : requestNo.trim();
			if (isBlank(normalizedRequestNo)) {
				throw new IllegalArgumentException("Request number is required");
			}
			param.setRequestNo(normalizedRequestNo);
			if (dao.selectPassTargetForUpdate(param) == null) {
				throw new AccessDeniedException("Approval request is not accessible");
			}
			requireSingleRow(dao.updatePassTarget(param), "reassign approval request");
		}

		result.setSuccess(true);
		return result;
	}

	public CommonApprovalParam selectRequestInfo(CommonApprovalParam param) {
		return dao.selectRequestInfo(param);
	}

	private void validateApprovalRequest(CommonApprovalParam param) {
		if (param == null || isBlank(param.getRequestNo())
				|| (!"A".equals(param.getSaveType()) && !"R".equals(param.getSaveType()))) {
			throw new IllegalArgumentException("Invalid approval request");
		}
		param.setRequestNo(param.getRequestNo().trim());
	}

	private UserVO requireAuthenticatedActor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof UserVO)) {
			throw new AccessDeniedException("Authenticated user is required");
		}
		return (UserVO) authentication.getPrincipal();
	}

	private void requireSingleRow(int affectedRows, String operation) {
		if (affectedRows != 1) {
			throw new IllegalStateException("Unable to " + operation);
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

}


package kr.esob.fdms.controller.inside.distribution.commonrequest;

import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

	public ResultVO saveApproval(CommonApprovalParam param) {
		log.info("{} is called", this.getClass().getSimpleName());

		ResultVO result = new ResultVO();
		CommonApprovalParam approveParam = dao.getCurrentApprovalInfo(param);
		boolean isDestroyExists = false;
		if( "A".equals(param.getSaveType()) ) {			//승인
			param.setActionCd("APPROVAL");
			param.setApprovalStatusCd("APPROVAL");
			if( "TL".equals(approveParam.getApprovalGradeCd()) && "Y".equals(approveParam.getProtectYn())){	//현재 구매팀장 결재면서 방산결재면 방산팀장한테 보내기
				param.setStatusCd("ACCEPT");
				dao.updateRequestDefInfo(param);

			}else {																						// 최종승인
				param.setStatusCd("APPROVAL");
				param.setApprovalStatusCd("APPROVAL");
				dao.updateRequestInfo(param);
				//최종승인인 경우 파일 결재 정보 테이블(DOCS_APPROVAL_FILE)에 각 아이템 추가
				List<CommonApprovalPopupListVO> itemList = dao.selectItemList(param);
				for(CommonApprovalPopupListVO tempVo : itemList) {
					CommonApprovalParam tempParam = new CommonApprovalParam();
					tempParam.setObjectId(tempVo.getObjectId());
					tempParam.setRequestNo(param.getRequestNo());
					tempParam.setDeployUserCd(approveParam.getDeployUserCd());
					tempParam.setFileNo(tempVo.getFileNo());
					dao.insertApprovalFile(tempParam);
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
			dao.updateRequestInfo(param);
		}
//		param.setCurrentProcessSeqNo("3");
		param.setApprovalStatusCd(approveParam.getApprovalStatusCd());
		param.setApprovalGradeCd(approveParam.getApprovalGradeCd());
		if("REJECT".equals(param.getActionCd())) {
			param.setCurrentProcessSeqNo(approveParam.getCurrentProcessSeqNo());
		}else {
			param.setCurrentProcessSeqNo( String.valueOf((Integer.parseInt(approveParam.getCurrentProcessSeqNo())+1)) );
		}
		dao.updateRequestDetail(param);

		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				sendMail(param, approveParam, isDestroyExists);
			}
		}catch(Exception e) {
			e.printStackTrace();
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
	public ResultVO savePass(PassParamVO param) {
		ResultVO result = new ResultVO();

		String[] arrRequestNo = param.getRequestNo().split(",");

		for(int i=0; i<arrRequestNo.length; i++) {

			param.setRequestNo(arrRequestNo[i]);

			dao.updatePassTarget(param);
		}

		return result;
	}

	public CommonApprovalParam selectRequestInfo(CommonApprovalParam param) {
		return dao.selectRequestInfo(param);
	}

}


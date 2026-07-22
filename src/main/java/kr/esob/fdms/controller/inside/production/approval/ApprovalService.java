package kr.esob.fdms.controller.inside.production.approval;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.bbs.notice.BbsNoticePopupParam;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalParam;
import kr.esob.fdms.controller.inside.production.acceptance.AcceptancePopupParam;
import kr.esob.fdms.controller.inside.production.common.DeployInfoVO;
import kr.esob.fdms.controller.inside.production.common.ProductStatusVO;
import kr.esob.fdms.controller.inside.production.productionstatus.ProductionStatusService;

@Service
public class ApprovalService implements CommonService{

	@Inject
	ApprovalDao dao;

	@Inject
	ProductionStatusService productionStatusService;

	@Inject
	PlatformTransactionManager transactionManager;

	@Inject
	DocsMailService mailService;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	@SuppressWarnings("rawtypes")
	public List selectRequestUserList(ApprovalPopupParam param) {
		return dao.selectRequestUserList(param);
	}

	@SuppressWarnings("rawtypes")
	public List selectReplaceRequestList(ApprovalPopupParam param) {
		return dao.selectReplaceRequestList(param);
	}

	@SuppressWarnings("rawtypes")
	public List selectObjectList(ApprovalPopupParam param) {
		return dao.selectObjectList(param);
	}

	public int selectRequestUserListCount(ApprovalPopupParam param) {
		return dao.selectRequestUserListCount(param);
	}

	public int selectObjectListCount(ApprovalPopupParam param) {
		return dao.selectObjectListCount(param);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public ResultVO approval(ApprovalPopupParam param) {
		ResultVO result = new ResultVO();
		TransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
		List<DeployInfoVO> deployList = new ArrayList<DeployInfoVO>();
		//DISPOSAL_REQUEST_YN이 Y인 리스트를 조회하여 해당 리스트의 OBJECT_NO, DEPLOY_USER_CD가 겹치는 대상을 찾는다.

		CommonApprovalParam approveParam = dao.getCurrentApprovalInfo(param.getRequestNo());
		if(param.getList() == null) {
			List<ApprovalPopupVO> list = dao.selectObjectList(param);
			List<ApprovalPopupParam> tempList = new ArrayList<ApprovalPopupParam>();
			for(ApprovalPopupVO vo : list) {
				ApprovalPopupParam temp = new ApprovalPopupParam();
				temp.setObjectId(vo.getObjectId());
				temp.setObjectNo(vo.getObjectNo());
				tempList.add(temp);
			}
			param.setList(tempList);
		}
		if(param.getApprovalPopupList() == null)param.setApprovalPopupList(dao.selectRequestUserList(param));
		param.setApprovalStatusCd(approveParam.getApprovalStatusCd());
		param.setApprovalGradeCd(approveParam.getApprovalGradeCd());
		if( "A".equals(param.getSaveType()) ) {			//승인
			param.setActionCd("APPROVAL");
			param.setStatusCd("APPROVAL");
			param.setRejectDesc(null);
			//배포접수 단계에서 실행하기 위해 주석 처리
			//updateProductStatus(param);

			dao.updateRequestInfo(param);
			//최종승인인 경우 파일 결재 정보 테이블(DOCS_APPROVAL_FILE)에 각 아이템 추가
			for(ApprovalPopupParam itemParam : param.getList()) {
				for(ApprovalPopupVO userVo : param.getApprovalPopupList()) {
					DeployInfoVO deployInfo = new DeployInfoVO();
					ApprovalPopupParam tempParam = new ApprovalPopupParam();
					tempParam.setRequestNo(param.getRequestNo());
					tempParam.setObjectId(itemParam.getObjectId());
					tempParam.setDeployUserCd(userVo.getUserCd());
					deployInfo.setDeployUserCd(userVo.getUserCd());
					deployInfo.setObjectNo(itemParam.getObjectNo());
					deployList.add(deployInfo);
					dao.insertApprovalFile(tempParam);
				}
			}
			param.setDeployInfoList(deployList);
			result = productionStatusService.selectDisposalRequestObject(param);
			if(!result.isSuccess()) {
				transactionManager.rollback(transactionStatus);
				return result;
			}
		}else if( "R".equals(param.getSaveType()) ) {	//반려
			param.setActionCd("REJECT");
			param.setStatusCd("REJECT");
			dao.updateDeployInfoReject(param); //배포접수 상태변경
		}
		dao.updateRequestInfo(param);
		dao.updateRequestDetail(param);
		transactionManager.commit(transactionStatus);
		result.setSuccess(true);
		if(param.getSendEmailYn().isBooleanValue()) {
			sendMail(param);
		}
		return result;
	}

	private void sendMail(ApprovalPopupParam param) {
		if( "A".equals(param.getSaveType()) ) {
			for(ApprovalPopupVO userVo : param.getApprovalPopupList()) {
				MailInfoVO mailInfoVo = mailService.selectReceiveUser(userVo.getUserCd());
				mailInfoVo.setMailEnum(DocsMailEnum.PRODUCT_ACCEPT);
				mailService.sendDocsMail(mailInfoVo);
			}
		}else {
			MailInfoVO mailInfoVo = mailService.selectRequestUserInfo(param);
			mailInfoVo.setMailEnum(DocsMailEnum.PRODUCT_STATUS);
			mailService.sendDocsMail(mailInfoVo);
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public ResultVO replaceApproval(ApprovalPopupParam param) {
		ResultVO result = new ResultVO();
		TransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
		List<DeployInfoVO> deployList = new ArrayList<DeployInfoVO>();
		CommonApprovalParam approveParam = dao.getCurrentApprovalInfo(param.getRequestNo());
		if(param.getApprovalPopupList() == null)param.setApprovalPopupList(dao.selectReplaceRequestList(param));
		param.setApprovalStatusCd(approveParam.getApprovalStatusCd());
		param.setApprovalGradeCd(approveParam.getApprovalGradeCd());
		if( "A".equals(param.getSaveType()) ) {			//승인
			param.setActionCd("APPROVAL");
			param.setStatusCd("APPROVAL");
			param.setRejectDesc(null);
			//배포접수 단계에서 실행하기 위해 주석 처리
			//updateProductStatus(param);

			dao.updateRequestInfo(param);
			//최종승인인 경우 파일 결재 정보 테이블(DOCS_APPROVAL_FILE)에 각 아이템 추가
			for(ApprovalPopupVO itemParam : param.getApprovalPopupList()) {
				DeployInfoVO deployInfo = new DeployInfoVO();
				ApprovalPopupParam tempParam = new ApprovalPopupParam();
				tempParam.setRequestNo(param.getRequestNo());
				tempParam.setDeployUserCd(itemParam.getUserCd());
				tempParam.setObjectId(itemParam.getObjectId());
				tempParam.setFileNo(itemParam.getFileNo());
				deployInfo.setDeployUserCd(itemParam.getUserCd());
				deployInfo.setObjectNo(itemParam.getObjectNo());
				deployList.add(deployInfo);
				dao.insertApprovalFile(tempParam);
			}
			param.setDeployInfoList(deployList);
			result = productionStatusService.selectDisposalRequestObject(param);
			if(!result.isSuccess()) {
				transactionManager.rollback(transactionStatus);
				return result;
			}
			if(param.getSendEmailYn().isBooleanValue()) {
				sendMail(param);
			}
		}else if( "R".equals(param.getSaveType()) ) {	//반려
			param.setActionCd("REJECT");
			param.setStatusCd("REJECT");
			dao.updateDeployInfoReject(param); //배포접수 상태변경
		}
		dao.updateRequestInfo(param);
		dao.updateRequestDetail(param);
		transactionManager.commit(transactionStatus);
		result.setSuccess(true);
		return result;
	}

	public printApprovalVO getPrintRequestInfo(ApprovalPopupParam param) {
		return dao.getPrintRequestInfo(param);
	}

	public List<printApprovalVO> selectPrintApprovalrList(ApprovalPopupParam param) {
		return dao.selectPrintApprovalrList(param);
	}
	
	public void updateProductStatus(AcceptancePopupParam param) {
		for(AcceptancePopupParam data : param.getList()) {
			List<DeployInfoVO> deployInfoVoList = dao.selectDeployInfoUserList(data);
//			for(DeployInfoVO vo : deployInfoVoList) {
//				if(dao.selectProductStatusCount(vo) > 0) {
//					dao.updateProductStatus(vo);
//				}else {
//					dao.insertProductStatus(vo);
//				}
//			}
			for(DeployInfoVO vo : deployInfoVoList) {
				ProductStatusVO productStatusVo = dao.selectProductionStatus(vo);
				int currentCount = (vo.getDeployCount() * vo.getCopy()) - vo.getDestroyCount();
				if(null != productStatusVo) {
					productStatusVo.setLastRequestNo(param.getRequestNo());
					productStatusVo.setCurrentCount(currentCount + productStatusVo.getCurrentCount());
					productStatusVo.setUserCd(vo.getDeployUserCd());
					productStatusVo.setLastDeployRevNo(vo.getRevNo());
					dao.updateProductionStatus(productStatusVo);
				}else {
					productStatusVo = new ProductStatusVO();
					productStatusVo.setObjectNo(vo.getObjectNo());
					productStatusVo.setLastRequestNo(param.getRequestNo());
					productStatusVo.setObjectType(param.getObjectType());
					productStatusVo.setDeptCd(vo.getDeployDeptCd());
					productStatusVo.setUserCd(vo.getDeployUserCd());
					productStatusVo.setCurrentCount(currentCount);
					productStatusVo.setLastDeployRevNo(vo.getRevNo());
					dao.insertProductionStatus(productStatusVo);
				}
				
				dao.updateDeployInfo(vo); //배포접수 상태변경
			}
		}
	}

	public ApprovalPopupVO getRequestInfo(ApprovalPopupParam param){
		return dao.getRequestInfo(param);
	}


	public ResultVO printApproval(ApprovalPopupParam param) {
		ResultVO result = new ResultVO();

//		CommonApprovalParam approveParam = dao.getCurrentApprovalInfo(param.getRequestNo());

		param.setApprovalStatusCd("APPROVAL");
		param.setApprovalGradeCd("TL");
		if( "A".equals(param.getSaveType()) ) {			//승인
			param.setActionCd("APPROVAL");
			param.setStatusCd("APPROVAL");
			param.setRejectDesc(null);
		}else if( "R".equals(param.getSaveType()) ) {	//반려
			param.setActionCd("REJECT");
			param.setStatusCd("REJECT");
		}

		//승인, 반려 정보 업데이트
		dao.updateRequestInfo(param);
		dao.updateRequestDetail(param);

		result.setSuccess(true);
		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				MailInfoVO mailInfoVo = mailService.selectDeployUserInfo(param);
				mailInfoVo.setMailEnum(DocsMailEnum.PRODUCT_PRINT_STATUS);
				mailService.sendDocsMail(mailInfoVo);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}

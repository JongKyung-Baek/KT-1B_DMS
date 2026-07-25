package kr.esob.fdms.controller.inside.production.approval;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.production.acceptance.AcceptancePopupParam;
import kr.esob.fdms.controller.inside.production.common.DeployInfoVO;
import kr.esob.fdms.controller.inside.production.common.ProductStatusVO;
import kr.esob.fdms.controller.inside.production.productionstatus.ProductionStatusService;
import kr.esob.fdms.controller.login.UserVO;

@Service
public class ApprovalService implements CommonService{

	@Inject
	ApprovalDao dao;

	@Inject
	ProductionStatusService productionStatusService;

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
	@Transactional(rollbackFor = Exception.class)
	public ResultVO approval(ApprovalPopupParam param) {
		ResultVO result = new ResultVO();
		List<DeployInfoVO> deployList = new ArrayList<DeployInfoVO>();
		//DISPOSAL_REQUEST_YN이 Y인 리스트를 조회하여 해당 리스트의 OBJECT_NO, DEPLOY_USER_CD가 겹치는 대상을 찾는다.

		ApprovalPopupParam approveParam = loadApprovalTarget(param, "NEW");
		List<ApprovalPopupVO> list = dao.selectObjectList(param);
		List<ApprovalPopupParam> tempList = new ArrayList<ApprovalPopupParam>();
		for(ApprovalPopupVO vo : list) {
			ApprovalPopupParam temp = new ApprovalPopupParam();
			temp.setObjectId(vo.getObjectId());
			temp.setObjectNo(vo.getObjectNo());
			tempList.add(temp);
		}
		param.setList(tempList);
		param.setApprovalPopupList(dao.selectRequestUserList(param));
		param.setApprovalStatusCd(approveParam.getApprovalStatusCd());
		param.setApprovalGradeCd(approveParam.getApprovalGradeCd());
		if( "A".equals(param.getSaveType()) ) {			//승인
			param.setActionCd("APPROVAL");
			param.setStatusCd("APPROVAL");
			param.setRejectDesc(null);
			//배포접수 단계에서 실행하기 위해 주석 처리
			//updateProductStatus(param);

			requireSingleRow(dao.updateRequestInfo(param), "approve production request");
			//최종승인인 경우 파일 결재 정보 테이블(DOCS_APPROVAL_FILE)에 각 아이템 추가
			for(ApprovalPopupParam itemParam : param.getList()) {
				for(ApprovalPopupVO userVo : param.getApprovalPopupList()) {
					DeployInfoVO deployInfo = new DeployInfoVO();
					ApprovalPopupParam tempParam = new ApprovalPopupParam();
					tempParam.setSessionUser(param.getSessionUser());
					tempParam.setRequestNo(param.getRequestNo());
					tempParam.setObjectId(itemParam.getObjectId());
					tempParam.setDeployUserCd(userVo.getUserCd());
					deployInfo.setDeployUserCd(userVo.getUserCd());
					deployInfo.setObjectNo(itemParam.getObjectNo());
					deployList.add(deployInfo);
					requireSingleRow(dao.insertApprovalFile(tempParam), "record approved production file");
				}
			}
			param.setDeployInfoList(deployList);
			result = productionStatusService.selectDisposalRequestObject(param);
			if(!result.isSuccess()) {
				throw new IllegalStateException("Unable to apply production approval");
			}
		}else if( "R".equals(param.getSaveType()) ) {	//반려
			param.setActionCd("REJECT");
			param.setStatusCd("REJECT");
			requireAffectedRows(dao.updateDeployInfoReject(param), "reject production deployment");
			requireSingleRow(dao.updateRequestInfo(param), "reject production request");
		}
		requireSingleRow(dao.updateRequestDetail(param), "complete production approval step");
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
	@Transactional(rollbackFor = Exception.class)
	public ResultVO replaceApproval(ApprovalPopupParam param) {
		ResultVO result = new ResultVO();
		List<DeployInfoVO> deployList = new ArrayList<DeployInfoVO>();
		ApprovalPopupParam approveParam = loadApprovalTarget(param, "REPLACE");
		param.setApprovalPopupList(dao.selectReplaceRequestList(param));
		param.setApprovalStatusCd(approveParam.getApprovalStatusCd());
		param.setApprovalGradeCd(approveParam.getApprovalGradeCd());
		if( "A".equals(param.getSaveType()) ) {			//승인
			param.setActionCd("APPROVAL");
			param.setStatusCd("APPROVAL");
			param.setRejectDesc(null);
			//배포접수 단계에서 실행하기 위해 주석 처리
			//updateProductStatus(param);

			requireSingleRow(dao.updateRequestInfo(param), "approve replacement request");
			//최종승인인 경우 파일 결재 정보 테이블(DOCS_APPROVAL_FILE)에 각 아이템 추가
			for(ApprovalPopupVO itemParam : param.getApprovalPopupList()) {
				DeployInfoVO deployInfo = new DeployInfoVO();
				ApprovalPopupParam tempParam = new ApprovalPopupParam();
				tempParam.setSessionUser(param.getSessionUser());
				tempParam.setRequestNo(param.getRequestNo());
				tempParam.setDeployUserCd(itemParam.getUserCd());
				tempParam.setObjectId(itemParam.getObjectId());
				tempParam.setFileNo(itemParam.getFileNo());
				deployInfo.setDeployUserCd(itemParam.getUserCd());
				deployInfo.setObjectNo(itemParam.getObjectNo());
				deployList.add(deployInfo);
				requireSingleRow(dao.insertApprovalFile(tempParam), "record approved replacement file");
			}
			param.setDeployInfoList(deployList);
			result = productionStatusService.selectDisposalRequestObject(param);
			if(!result.isSuccess()) {
				throw new IllegalStateException("Unable to apply replacement approval");
			}
			if(param.getSendEmailYn().isBooleanValue()) {
				sendMail(param);
			}
		}else if( "R".equals(param.getSaveType()) ) {	//반려
			param.setActionCd("REJECT");
			param.setStatusCd("REJECT");
			requireAffectedRows(dao.updateDeployInfoReject(param), "reject replacement deployment");
			requireSingleRow(dao.updateRequestInfo(param), "reject replacement request");
		}
		requireSingleRow(dao.updateRequestDetail(param), "complete replacement approval step");
		result.setSuccess(true);
		return result;
	}

	public printApprovalVO getPrintRequestInfo(ApprovalPopupParam param) {
		return dao.getPrintRequestInfo(param);
	}

	public List<printApprovalVO> selectPrintApprovalrList(ApprovalPopupParam param) {
		return dao.selectPrintApprovalrList(param);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void updateProductStatus(AcceptancePopupParam param) {
		if (param == null || isBlank(param.getRequestNo())
				|| (!"DOC".equals(param.getObjectType()) && !"SW".equals(param.getObjectType()))) {
			throw new IllegalArgumentException("Invalid production acceptance target");
		}

		UserVO actor = requireAuthenticatedActor();
		param.setSessionUser(actor);
		param.setDeployUserCd(actor.getUserCd());

		// Load every pending deployment row from the locked request. The client list is ignored.
		List<DeployInfoVO> deployInfoVoList = dao.selectDeployInfoUserList(param);
		if (deployInfoVoList == null || deployInfoVoList.isEmpty()) {
			throw new IllegalStateException("No pending production deployment exists");
		}

		for (DeployInfoVO vo : deployInfoVoList) {
			if (vo == null || isBlank(vo.getObjectId()) || isBlank(vo.getObjectNo())
					|| isBlank(vo.getDeployDeptCd())
					|| !actor.getUserCd().equals(vo.getDeployUserCd())) {
				throw new IllegalStateException("Invalid production deployment row");
			}
			vo.setRequestNo(param.getRequestNo());
			vo.setObjectType(param.getObjectType());
			vo.setDeployUserCd(actor.getUserCd());
			vo.setSessionUser(actor);

			ProductStatusVO productStatusVo = dao.selectProductionStatus(vo);
			int acceptedCount = Math.subtractExact(
					Math.multiplyExact(vo.getDeployCount(), vo.getCopy()),
					vo.getDestroyCount());
			if (productStatusVo != null) {
				productStatusVo.setRequestNo(param.getRequestNo());
				productStatusVo.setObjectId(vo.getObjectId());
				productStatusVo.setObjectNo(vo.getObjectNo());
				productStatusVo.setDeptCd(vo.getDeployDeptCd());
				productStatusVo.setUserCd(actor.getUserCd());
				productStatusVo.setSessionUser(actor);
				productStatusVo.setLastRequestNo(param.getRequestNo());
				productStatusVo.setObjectType(param.getObjectType());
				productStatusVo.setCurrentCount(
						Math.addExact(acceptedCount, productStatusVo.getCurrentCount()));
				productStatusVo.setLastDeployRevNo(vo.getRevNo());
				requireSingleRow(dao.updateProductionStatus(productStatusVo),
						"update production status");
			} else {
				productStatusVo = new ProductStatusVO();
				productStatusVo.setRequestNo(param.getRequestNo());
				productStatusVo.setObjectId(vo.getObjectId());
				productStatusVo.setObjectNo(vo.getObjectNo());
				productStatusVo.setLastRequestNo(param.getRequestNo());
				productStatusVo.setObjectType(param.getObjectType());
				productStatusVo.setDeptCd(vo.getDeployDeptCd());
				productStatusVo.setUserCd(actor.getUserCd());
				productStatusVo.setSessionUser(actor);
				productStatusVo.setCurrentCount(acceptedCount);
				productStatusVo.setLastDeployRevNo(vo.getRevNo());
				requireSingleRow(dao.insertProductionStatus(productStatusVo),
						"insert production status");
			}

			requireSingleRow(dao.updateDeployInfo(vo), "complete production deployment");
		}
	}

	public ApprovalPopupVO getRequestInfo(ApprovalPopupParam param){
		return dao.getRequestInfo(param);
	}


	@Transactional(rollbackFor = Exception.class)
	public ResultVO printApproval(ApprovalPopupParam param) {
		ResultVO result = new ResultVO();

		ApprovalPopupParam approveParam = loadApprovalTarget(param, "PRINT");
		param.setApprovalStatusCd(approveParam.getApprovalStatusCd());
		param.setApprovalGradeCd(approveParam.getApprovalGradeCd());
		if( "A".equals(param.getSaveType()) ) {			//승인
			param.setActionCd("APPROVAL");
			param.setStatusCd("APPROVAL");
			param.setRejectDesc(null);
		}else if( "R".equals(param.getSaveType()) ) {	//반려
			param.setActionCd("REJECT");
			param.setStatusCd("REJECT");
		}

		//승인, 반려 정보 업데이트
		requireSingleRow(dao.updateRequestInfo(param), "complete print approval request");
		requireSingleRow(dao.updateRequestDetail(param), "complete print approval step");

		result.setSuccess(true);
		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				MailInfoVO mailInfoVo = mailService.selectDeployUserInfo(param);
				mailInfoVo.setMailEnum(DocsMailEnum.PRODUCT_PRINT_STATUS);
				mailService.sendDocsMail(mailInfoVo);
			}
		}catch(Exception e) {
		}
		return result;
	}

	private ApprovalPopupParam loadApprovalTarget(ApprovalPopupParam param, String expectedRequestPurpose) {
		if (param == null || isBlank(param.getRequestNo())
				|| (!"A".equals(param.getSaveType()) && !"R".equals(param.getSaveType()))) {
			throw new IllegalArgumentException("Invalid production approval request");
		}
		param.setRequestNo(param.getRequestNo().trim());
		param.setSessionUser(requireAuthenticatedActor());
		ApprovalPopupParam approvalTarget = dao.getCurrentApprovalInfo(param);
		if (approvalTarget == null || !expectedRequestPurpose.equals(approvalTarget.getRequestPurpose())) {
			throw new AccessDeniedException("Production approval request is not accessible");
		}
		param.setRequestPurpose(approvalTarget.getRequestPurpose());
		param.setObjectType(approvalTarget.getObjectType());
		return approvalTarget;
	}

	private UserVO requireAuthenticatedActor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| !(authentication.getPrincipal() instanceof UserVO)
				|| isBlank(((UserVO) authentication.getPrincipal()).getUserCd())) {
			throw new AccessDeniedException("Authenticated user is required");
		}
		return (UserVO) authentication.getPrincipal();
	}

	private void requireSingleRow(int affectedRows, String operation) {
		if (affectedRows != 1) {
			throw new IllegalStateException("Unable to " + operation);
		}
	}

	private void requireAffectedRows(int affectedRows, String operation) {
		if (affectedRows < 1) {
			throw new IllegalStateException("Unable to " + operation);
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}

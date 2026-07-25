package kr.esob.fdms.controller.inside.unregisted.approval;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import kr.esob.fdms.controller.login.UserVO;

@Service
public class ApprovalService implements CommonService {

	@Inject
	ApprovalDao dao;

	@Inject
	CommonRequestService commonRequestService;

	@Inject
	DocsMailService mailService;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public ApprovalPopupVO getRequestInfo(ApprovalPopupParam param) {
		param.setSessionUser(requireAuthenticatedActor());
		return dao.getRequestInfo(param);
	}

	public List<GridResultVO> selectPopupList(ApprovalPopupParam param) {
		return dao.selectPopupList(param);
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO saveApproval(ApprovalPopupParam param) {
		ResultVO result = new ResultVO();
		ApprovalPopupParam approvalTarget = loadApprovalTarget(param);
		param.setApprovalStatusCd(approvalTarget.getApprovalStatusCd());
		if( "A".equals(param.getSaveType()) ) {			//승인
			param.setActionCd("APPROVAL");
			param.setApprovalStatusCd("APPROVAL");
			param.setStatusCd("APPROVAL");
			//승인으로 변경
			requireSingleRow(dao.updateRequestInfo(param), "approve unregistered request");
			// 파일 결재 정보 테이블(DOCS_APPROVAL_FILE)에 각 아이템 추가
			List<ApprovalPopupVO> itemList = dao.selectItemList(param);
			for(ApprovalPopupVO tempVo : itemList) {
				ApprovalPopupParam tempParam = new ApprovalPopupParam();
				tempParam.setSessionUser(param.getSessionUser());
				tempParam.setObjectId(tempVo.getObjectId());
				tempParam.setRequestNo(param.getRequestNo());
				tempParam.setFileNo(tempVo.getFileNo());
				requireSingleRow(dao.insertApprovalFile(tempParam), "record approved unregistered file");
			}
			param.setRejectDesc(null);
			try {
				if(param.getSendEmailYn().isBooleanValue()) {
					MailInfoVO mailInfoVo = mailService.selectDeployUserInfo(param);
					mailInfoVo.setToCc(mailService.selectUnregSecurityUserInfo(param));
					mailInfoVo.setMailEnum(DocsMailEnum.UNREG_STATUS);
					mailInfoVo.setFromMail(mailService.selectPurchaserEmail(param));
					mailService.sendDocsMail(mailInfoVo);
				}
			}catch(Exception e) {
			}
		}else if( "R".equals(param.getSaveType()) ) {	//반려
			param.setActionCd("REJECT");
			param.setStatusCd("REJECT");
			param.setApprovalStatusCd("REJECT");
			requireSingleRow(dao.updateRequestInfo(param), "reject unregistered request");
		}
		requireSingleRow(dao.updateRequestDetail(param), "complete unregistered approval step");
		result.setSuccess(true);

		return result;
	}

	public int selectPopupListCount(ApprovalPopupParam param) {
		return dao.selectPopupListCount(param);
	}

	private ApprovalPopupParam loadApprovalTarget(ApprovalPopupParam param) {
		if (param == null || param.getRequestNo() == null || param.getRequestNo().trim().isEmpty()
				|| (!"A".equals(param.getSaveType()) && !"R".equals(param.getSaveType()))) {
			throw new IllegalArgumentException("Invalid unregistered approval request");
		}
		param.setRequestNo(param.getRequestNo().trim());
		param.setSessionUser(requireAuthenticatedActor());
		ApprovalPopupParam approvalTarget = dao.selectApprovalTargetForUpdate(param);
		if (approvalTarget == null) {
			throw new AccessDeniedException("Unregistered approval request is not accessible");
		}
		return approvalTarget;
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

}

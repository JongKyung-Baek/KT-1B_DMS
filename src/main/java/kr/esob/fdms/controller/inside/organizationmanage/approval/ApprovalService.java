package kr.esob.fdms.controller.inside.organizationmanage.approval;

import java.util.List;

import javax.inject.Inject;

import kr.esob.fdms.controller.login.UserVO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;

@Service
public class ApprovalService implements CommonService {

	@Inject
	ApprovalDao dao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public ApprovalListParam selectDetailInfo(ApprovalListParam param) {
		if (param == null || param.getRequestNo() == null || param.getRequestNo().trim().isEmpty()) {
			throw new AccessDeniedException("User request detail is not accessible");
		}
		param.setSessionUser(requireAuthenticatedActor());
		ApprovalListParam detail = dao.selectDetailInfo(param);
		if (detail == null) {
			throw new AccessDeniedException("User request detail is not accessible");
		}
		return detail;
	}


	@Transactional(rollbackFor = Exception.class)
	public ResultVO approvalUser(ApprovalListParam param) throws Exception {
		ResultVO resultVo = new ResultVO();
		if (param != null) {
			// A client-supplied rejection reason must never relax approval checks.
			param.setRejectReason(null);
		}
		ApprovalListParam approvalTarget = loadApprovalTarget(param, resultVo);
		if (approvalTarget == null) {
			return resultVo;
		}

		approvalTarget.setStatusCd("APPROVAL");
		approvalTarget.setRejectReason("");

		if ("I".equals(approvalTarget.getRequestType())) {
			requireSingleRow(dao.insertUser(approvalTarget), "create approved user");
		}
		else if ("U".equals(approvalTarget.getRequestType())) {
			requireSingleRow(dao.updateUserInfo(approvalTarget), "update approved user");
		}
		else {
			requireSingleRow(dao.deleteUserInfo(approvalTarget), "delete approved user");
		}

		if (!"D".equals(approvalTarget.getRequestType())) {
			if ("Y".equals(approvalTarget.getProtectYn())) {
				dao.updateUserProtectN(approvalTarget);
				requireSingleRow(dao.updateUserProtectY(approvalTarget), "assign protected user");
			}

			if ("Y".equals(approvalTarget.getCrYn())) {
				requireSingleRow(dao.updateUserCr(approvalTarget), "assign company approver");
			}
		}

		completeRequest(approvalTarget);
		resultVo.setSuccess(true);

		return resultVo;
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO rejectUser(ApprovalListParam param) throws Exception {
		ResultVO resultVo = new ResultVO();
		ApprovalListParam approvalTarget = loadApprovalTarget(param, resultVo);
		if (approvalTarget == null) {
			return resultVo;
		}

		approvalTarget.setStatusCd("REJECT");
		approvalTarget.setRejectReason(param.getRejectReason());
		completeRequest(approvalTarget);
		resultVo.setSuccess(true);

		return resultVo;
	}

	public List<ComboInfoVO> venderUser(ApprovalListParam param) throws Exception {
		if (param == null || param.getCompanyCd() == null || param.getCompanyCd().trim().isEmpty()) {
			throw new AccessDeniedException("Vendor users are not accessible");
		}
		param.setSessionUser(requireAuthenticatedActor());
		return dao.venderUser(param);
	}

	private ApprovalListParam loadApprovalTarget(ApprovalListParam param, ResultVO resultVo) {
		if (param == null || param.getRequestNo() == null || param.getRequestNo().trim().isEmpty()) {
			resultVo.setMessage("msg.invalidRequest");
			return null;
		}

		UserVO actor;
		try {
			actor = requireAuthenticatedActor();
		} catch (AccessDeniedException exception) {
			resultVo.setMessage("msg.accessDenied");
			return null;
		}
		// Never trust a sessionUser object supplied through JSON binding.
		param.setSessionUser(actor);
		ApprovalListParam approvalTarget = dao.selectApprovalTarget(param);
		if (approvalTarget == null || !isSupportedRequestType(approvalTarget.getRequestType())) {
			resultVo.setMessage("msg.invalidRequest");
			return null;
		}
		approvalTarget.setSessionUser(actor);
		return approvalTarget;
	}

	private boolean isSupportedRequestType(String requestType) {
		return "I".equals(requestType) || "U".equals(requestType) || "D".equals(requestType);
	}

	private void completeRequest(ApprovalListParam approvalTarget) {
		if (dao.updateReqeust(approvalTarget) != 1) {
			throw new IllegalStateException("Unable to complete user request");
		}
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

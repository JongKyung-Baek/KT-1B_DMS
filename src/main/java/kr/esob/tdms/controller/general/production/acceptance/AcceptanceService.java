package kr.esob.tdms.controller.general.production.acceptance;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.tdms.commonlogic.abstractclass.CommonService;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.general.production.approval.ApprovalService;
import kr.esob.tdms.controller.login.UserVO;

@Service
public class AcceptanceService implements CommonService{

	@Inject
	AcceptanceDao dao;

	@Inject
	ApprovalService service;

	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public List<AcceptancePopupVO> selectPopupList(AcceptancePopupParam param) {
		return dao.selectPopupList(param);
	}

	public int selectPopupListCount(AcceptancePopupParam param) {
		return dao.selectPopupListCount(param);
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO saveAcceptance(AcceptancePopupParam param) {
		if (param == null || isBlank(param.getRequestNo())) {
			throw new IllegalArgumentException("Invalid production acceptance request");
		}

		param.setRequestNo(param.getRequestNo().trim());
		UserVO actor = requireAuthenticatedActor();
		param.setSessionUser(actor);

		AcceptancePopupParam target = dao.selectAcceptanceTargetForUpdate(param);
		if (target == null
				|| !param.getRequestNo().equals(target.getRequestNo())
				|| !actor.getUserCd().equals(target.getDeployUserCd())) {
			throw new AccessDeniedException("Production acceptance request is not accessible");
		}
		if (!"DOC".equals(target.getObjectType()) && !"SW".equals(target.getObjectType())) {
			throw new IllegalStateException("Unsupported production object type");
		}

		// Client-supplied item ownership fields are not an authorization source.
		param.setObjectType(target.getObjectType());
		param.setDeployUserCd(actor.getUserCd());
		param.setList(Collections.<AcceptancePopupParam>emptyList());

		ResultVO result = new ResultVO();
		service.updateProductStatus(param);
		requireSingleRow(dao.updateAcceptance(param), "complete production acceptance");
		result.setSuccess(true);
		return result;
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

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

}

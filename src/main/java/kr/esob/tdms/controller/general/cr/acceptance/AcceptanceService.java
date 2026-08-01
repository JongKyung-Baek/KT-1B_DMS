package kr.esob.tdms.controller.general.cr.acceptance;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.tdms.commonlogic.abstractclass.CommonService;
import kr.esob.tdms.commonlogic.mail.DocsMailEnum;
import kr.esob.tdms.commonlogic.mail.DocsMailService;
import kr.esob.tdms.commonlogic.mail.MailInfoVO;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.commonlogic.value.Constant;
import kr.esob.tdms.commonlogic.value.CrStatusCdInfo;
import kr.esob.tdms.controller.general.cr.CommonCrDao;
import kr.esob.tdms.controller.general.cr.CrInfoVO;
import kr.esob.tdms.controller.general.cr.CrParam;
import kr.esob.tdms.controller.login.UserVO;

@Service
public class AcceptanceService implements CommonService {

	@Inject
	CommonCrDao commonCrDao;

	@Inject
	AcceptanceDao dao;

	@Inject
	DocsMailService mailService;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		AcceptanceListParam listParam = requireListParam(param);
		listParam.setSessionUser(requireAuthenticatedActor());
		return dao.selectList(listParam);
	}

	@Override
	public int selectListCount(Object param) {
		AcceptanceListParam listParam = requireListParam(param);
		listParam.setSessionUser(requireAuthenticatedActor());
		return dao.selectListCount(listParam);
	}

	public void deleteList(Object param) {
		// No delete operation for CR acceptance.
	}

	public CrInfoVO selectAcceptanceInfo(CrParam param) {
		if (param == null || isBlank(param.getCrNo())) {
			throw new IllegalArgumentException("CR number is required");
		}
		param.setCrNo(param.getCrNo().trim());
		param.setSessionUser(requireAuthenticatedActor());

		CrInfoVO vo = dao.selectAcceptanceInfo(param);
		if (vo == null) {
			throw new AccessDeniedException("CR acceptance request is not accessible");
		}
		vo.setFileList(commonCrDao.selectInsideFileList(param));
		return vo;
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO approvalRequest(CrParam param) {
		authorizeAcceptanceTarget(param, true);
		param.setActionCd(Constant.ACCEPT);
		param.setApprovalGradeCd("TL");
		param.setCurrentProcessSeqNo(4);
		param.setStatusCd(CrStatusCdInfo.PURCHASER_ACCEPT);
		param.setApprovalStatusCd(Constant.ACCEPT);
		param.setRequestDesc(param.getReviewResult());

		requireSingleRow(dao.updateAcceptance(param), "complete CR acceptance step");
		requireSingleRow(dao.updateApproval(param), "assign CR approval step");
		requireSingleRow(dao.updateRequest(param), "advance accepted CR request");
		requireSingleRow(dao.updateCr(param), "update accepted CR");

		sendApprovalMail(param);
		ResultVO result = new ResultVO();
		result.setSuccess(true);
		return result;
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO acceptanceReject(CrParam param) {
		authorizeAcceptanceTarget(param, false);
		param.setActionCd(Constant.REJECT);
		param.setApprovalStatusCd(Constant.REJECT);
		param.setCurrentProcessSeqNo(3);
		param.setStatusCd(CrStatusCdInfo.PURCHASER_REJECT);
		param.setRequestDesc(param.getRejectReason());

		requireSingleRow(dao.updateAcceptance(param), "complete CR rejection step");
		requireSingleRow(dao.updateRequest(param), "reject CR request");
		requireSingleRow(dao.updateCr(param), "update rejected CR");

		sendRejectionMail(param);
		ResultVO result = new ResultVO();
		result.setSuccess(true);
		return result;
	}

	private AcceptanceListParam requireListParam(Object param) {
		if (!(param instanceof AcceptanceListParam)) {
			throw new IllegalArgumentException("Invalid CR acceptance list request");
		}
		return (AcceptanceListParam) param;
	}

	private void authorizeAcceptanceTarget(CrParam param, boolean approval) {
		if (param == null || isBlank(param.getCrNo())) {
			throw new IllegalArgumentException("CR number is required");
		}
		if (approval && isBlank(param.getApprovalUser())) {
			throw new IllegalArgumentException("CR approval user is required");
		}

		param.setCrNo(param.getCrNo().trim());
		UserVO actor = requireAuthenticatedActor();
		param.setSessionUser(actor);
		param.setActualUserCd(actor.getUserCd());
		if (dao.selectAcceptanceTargetForUpdate(param) == null) {
			throw new AccessDeniedException("CR acceptance request is not accessible");
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

	private void sendApprovalMail(CrParam param) {
		try {
			if (param.getSendEmailYn().isBooleanValue()) {
				MailInfoVO mailInfo = mailService.selectApprovalUserInfo(param);
				mailInfo.setMailEnum(DocsMailEnum.CR_APPROVAL);
				mailService.sendDocsMail(mailInfo);
			}
		} catch (Exception ignored) {
			// Mail is fail-soft; required database mutations already succeeded.
		}
	}

	private void sendRejectionMail(CrParam param) {
		try {
			if (param.getSendEmailYn().isBooleanValue()) {
				MailInfoVO mailInfo = mailService.selectCrRequestUserInfo(param);
				mailInfo.setMailEnum(DocsMailEnum.CR_STATUS);
				mailService.sendDocsMail(mailInfo);
			}
		} catch (Exception ignored) {
			// Mail is fail-soft; required database mutations already succeeded.
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}

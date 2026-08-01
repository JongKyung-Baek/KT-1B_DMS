package kr.esob.tdms.controller.general.distribution.acceptance;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.tdms.commonlogic.abstractclass.CommonService;
import kr.esob.tdms.commonlogic.combo.ComboInfoVO;
import kr.esob.tdms.commonlogic.mail.DocsMailService;
import kr.esob.tdms.commonlogic.mail.MailInfoVO;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.login.UserVO;
import kr.esob.tdms.util.DateUtil;

@Service
public class AcceptanceService implements CommonService{

	@Inject
	AcceptanceDao dao;

	@Inject
	DateUtil dateUtil;

	@Inject
	DocsMailService mailService;

	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	public List<ComboInfoVO> getDefenseTeamLeader() {
		return dao.getDefenseTeamLeader();
	}

	public AcceptanceVO getDocRequestData(String requestNo) {
		return dao.getDocRequestData(requestNo);
	}


	@SuppressWarnings("static-access")
	@Transactional(rollbackFor = Exception.class)
	public ResultVO saveAcceptance(AcceptanceParam param) {
		validateAcceptanceRequest(param);
		UserVO actor = requireAuthenticatedActor();
		param.setSessionUser(actor);

		AcceptanceParam target = dao.selectAcceptanceTargetForUpdate(param);
		if (target == null) {
			throw new AccessDeniedException("Acceptance request is not accessible");
		}
		param.setApprovalLineId(target.getApprovalLineId());
		param.setCurrentProcessSeqNo(target.getCurrentProcessSeqNo());
		param.setProcessSeq(target.getCurrentProcessSeqNo());

		ResultVO result = new ResultVO();

		if( "A".equals(param.getSaveType()) ) {			//접수
			param.setActionCd("ACCEPT");
			param.setCurrentProcessSeqNo("3");
			requireSingleRow(dao.updateTlRequestDetail(param), "assign acceptance approver");
			if("1".equals(param.getApprovalLineId())) {	//방산팀장 결재 여부에 따른 4단계 결재 정보 수정
				param.setActionCd("ACCEPT");
				requireSingleRow(dao.saveDefRequestDetail(param), "assign defense approver");
			}
			//배포 방식
			/*
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
			*/

			List<AcceptanceParam> list = param.getList();

			for(AcceptanceParam file : list) {
				file.setSessionUser(actor);
				file.setRequestNo(param.getRequestNo());
				file.setProcessSeq(param.getProcessSeq());
				file.setUseStartYmd(dateUtil.getToday("yyyyMMdd"));								//배포 기한
				file.setUseEndYmd(dateUtil.getAddMonth(file.getDeployTerm(), "yyyyMMdd"));		//유효기간(개월)

				requireSingleRow(dao.updateRequestFile(file), "update accepted request file");
			}
		}else if( "R".equals(param.getSaveType()) ) {	//반려
			param.setActionCd("REJECT");
			param.setStatusCd("REJECT");
		}

		requireSingleRow(dao.updateRequest(param), "update acceptance request");
		requireSingleRow(dao.updateRequestAcceptDetail(param), "complete acceptance step");

		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				mailService.sendDocsMail(mailService.selectReceiveUser(param.getPurchaseUid()));
			}
		}catch(Exception e) {
		}


		result.setSuccess(true);
		return result;
	}

	private void validateAcceptanceRequest(AcceptanceParam param) {
		if (param == null || isBlank(param.getRequestNo())
				|| (!"A".equals(param.getSaveType()) && !"R".equals(param.getSaveType()))) {
			throw new IllegalArgumentException("Invalid acceptance request");
		}
		if ("A".equals(param.getSaveType()) && (param.getList() == null || param.getList().isEmpty())) {
			throw new IllegalArgumentException("Accepted files are required");
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

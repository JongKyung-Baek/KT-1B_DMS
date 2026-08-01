package kr.esob.tdms.controller.general.distribution.printdestroyapproval;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.tdms.commonlogic.abstractclass.CommonService;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.login.UserVO;
import kr.esob.tdms.util.DateUtil;

@Service
public class PrintDestroyApprovalService implements CommonService{

	@Inject
	PrintDestroyApprovalDao dao;

	@Inject
	DateUtil dateUtil;

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
	public List selectPopupList(PrintDestroyApprovalListParam param) {
//		param.setList(dao.selectSearchInfo(param));
		return dao.selectPopupList(param);
	}

	@SuppressWarnings("rawtypes")
	public int selectPopupListCount(PrintDestroyApprovalListParam param) {
		param.setList(dao.selectSearchInfo(param));
		return dao.selectPopupListCount(param);
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO saveApproval(PrintDestroyApprovalPopupParam param) {
		validateApprovalRequest(param);
		UserVO actor = requireAuthenticatedActor();
		param.setSessionUser(actor);

		ResultVO result = new ResultVO();

		// 승인/반려 정보 저장 전 기존 정보 조회
		PrintDestroyApprovalPopupParam approvalTarget = dao.getDestroyRequestInfo(param);
		if (approvalTarget == null) {
			throw new AccessDeniedException("Destroy approval request is not accessible");
		}
		param.setApprovalLineId(approvalTarget.getApprovalLineId());
		param.setApprovalStatusCd(approvalTarget.getApprovalStatusCd());
		param.setApprovalGradeCd(approvalTarget.getApprovalGradeCd());
		param.setCurrentProcessSeqNo(approvalTarget.getCurrentProcessSeqNo());
		param.setProcessSeq(approvalTarget.getCurrentProcessSeqNo());
		if( "A".equals(param.getSaveType()) ) {								//승인
			param.setActionCd("APPROVAL");
//			param.setApprovalStatusCd("APPROVAL");
			if( "APPROVAL".equals(param.getApprovalStatusCd()) && "TL".equals(param.getApprovalGradeCd()) && "4".equals(param.getApprovalLineId()) ){	//현재 구매팀장 결재면서 결재 라인이 1번이면 방산팀장 결재로 보내기
				param.setCurrentProcessSeqNo(String.valueOf(Integer.parseInt(param.getCurrentProcessSeqNo()) + 1));
				param.setStatusCd("REQUEST");
				requireSingleRow(dao.updatePrintDestroyRequestInfo(param), "advance destroy approval");
			}else {															// 최종승인
				param.setStatusCd("APPROVAL");
				requireSingleRow(dao.updatePrintDestroyRequestInfo(param), "approve destroy request");
				//최종승인인 경우 요청의 requestNo + objectId의 아이템 미사용으로 변경
				List<PrintDestroyItemListVO> itemList = dao.selectDestroyItemList(param);
				for(PrintDestroyItemListVO tempVo : itemList) {
					PrintDestroyApprovalPopupParam tempParam = new PrintDestroyApprovalPopupParam();
					tempParam.setSessionUser(actor);
					tempParam.setDestroyRequestNo(param.getDestroyRequestNo());
					tempParam.setProcessSeq(param.getProcessSeq());
					tempParam.setApprovalStatusCd(param.getApprovalStatusCd());
					tempParam.setObjectId(tempVo.getObjectId());
					tempParam.setRequestNo(tempVo.getRequestNo());
					requireSingleRow(dao.updateRequestMapping(tempParam), "disable destroyed request mapping");
				}
			}
			param.setRejectDesc(null);

		}else if( "R".equals(param.getSaveType()) ) {						//반려
			param.setActionCd("REJECT");
			param.setStatusCd("REJECT");
			requireSingleRow(dao.updatePrintDestroyRequestInfo(param), "reject destroy request");
		}

		requireSingleRow(dao.updatePrintDestroyRequestDetail(param), "complete destroy approval step");

		result.setSuccess(true);
		return result;
	}

	public PrintDestroyItemListVO getDestroyRequest(String destroyRequestNo) {
		return dao.getDestroyRequest(destroyRequestNo);
	}

	private void validateApprovalRequest(PrintDestroyApprovalPopupParam param) {
		if (param == null || isBlank(param.getDestroyRequestNo())
				|| (!"A".equals(param.getSaveType()) && !"R".equals(param.getSaveType()))) {
			throw new IllegalArgumentException("Invalid destroy approval request");
		}
		param.setDestroyRequestNo(param.getDestroyRequestNo().trim());
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

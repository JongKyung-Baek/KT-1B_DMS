package kr.esob.fdms.controller.inside.production.disposalapproval;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.controller.login.UserVO;

@Service
public class DisposalApprovalService implements CommonService{

	@Inject
	DisposalApprovalDao dao;

	@Inject
	SecurityAclService securityAclService;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	public DisposalApprovalPopupVO getDestroyRequestInfo(DisposalApprovalPopupParam param) {
		return dao.getDestroyRequestInfo(param);
	}

	@Transactional(rollbackFor = Exception.class)
	public ResultVO destroyApproval(DisposalApprovalPopupParam param) {
		validateApprovalRequest(param);
		UserVO actor = securityAclService.requireCurrentUser();
		param.setSessionUser(actor);

		DisposalApprovalPopupParam approvalTarget = dao.selectApprovalTargetForUpdate(param);
		if (approvalTarget == null) {
			throw new AccessDeniedException("Disposal approval request is not accessible");
		}
		param.setApprovalStatusCd(approvalTarget.getApprovalStatusCd());
		param.setApprovalGradeCd(approvalTarget.getApprovalGradeCd());

		ResultVO result = new ResultVO();
		List<DisposalApprovalPopupVO> destroyList = dao.selectDestroyList(param);
		if (destroyList == null || destroyList.isEmpty()) {
			throw new IllegalStateException("Unable to load disposal approval items");
		}
		Set<String> productStatusKeys = new HashSet<String>();
		if( "A".equals(param.getSaveType()) ) {			//승인
			param.setStatusCd("APPROVAL");				//FDMS_DESTROY_REQUEST의 최종 승인여부
			param.setActionCd("APPROVAL");				//FDMS_DESTROY_REQUEST_DETAIL의 해당 결재 순서의 승인 여부
			for(DisposalApprovalPopupVO destVo : destroyList) {
				DisposalApprovalPopupParam tempParam = itemParam(param, actor, destVo);
				requireSingleRow(dao.updateDestroyCount(tempParam),
						"destroy approved deployment item");	//승인 시 남은 배포수량 전량 폐기
				if (productStatusKeys.add(productStatusKey(destVo))) {
					requireSingleRow(dao.deleteProductStatus(tempParam),
							"delete approved product status");
				}
			}
			//승인으로 변경
			param.setRejectDesc(null);
		}else if( "R".equals(param.getSaveType()) ) {	//반려
			param.setStatusCd("REJECT");				//FDMS_DESTROY_REQUEST의 최종 승인여부
			param.setActionCd("REJECT");				//FDMS_DESTROY_REQUEST_DETAIL의 해당 결재 순서의 승인 여부
			for(DisposalApprovalPopupVO destVo : destroyList) {
				DisposalApprovalPopupParam tempParam = itemParam(param, actor, destVo);
				if (productStatusKeys.add(productStatusKey(destVo))) {
					requireSingleRow(dao.updateDisposalReject(tempParam),
							"reject product disposal status");
				}
			}
		}

		requireSingleRow(dao.updateDestroyRequestInfo(param),
				"complete disposal approval request");
		requireSingleRow(dao.updateDestroyRequestDetail(param),
				"complete disposal approval step");

		result.setSuccess(true);
		return result;
	}

	private DisposalApprovalPopupParam itemParam(DisposalApprovalPopupParam request, UserVO actor,
			DisposalApprovalPopupVO item) {
		if (item == null || blank(item.getRequestNo()) || blank(item.getObjectId())
				|| blank(item.getObjectNo()) || blank(item.getDeployUserCd())) {
			throw new IllegalStateException("Invalid disposal approval item");
		}
		DisposalApprovalPopupParam itemParam = new DisposalApprovalPopupParam();
		itemParam.setSessionUser(actor);
		itemParam.setDestroyRequestNo(request.getDestroyRequestNo());
		itemParam.setApprovalStatusCd(request.getApprovalStatusCd());
		itemParam.setApprovalGradeCd(request.getApprovalGradeCd());
		itemParam.setRequestNo(item.getRequestNo());
		itemParam.setObjectId(item.getObjectId());
		itemParam.setObjectNo(item.getObjectNo());
		itemParam.setDeployUserCd(item.getDeployUserCd());
		return itemParam;
	}

	private String productStatusKey(DisposalApprovalPopupVO item) {
		return item.getObjectNo() + '\u0000' + item.getDeployUserCd();
	}

	private void validateApprovalRequest(DisposalApprovalPopupParam param) {
		if (param == null || blank(param.getDestroyRequestNo())
				|| (!"A".equals(param.getSaveType()) && !"R".equals(param.getSaveType()))) {
			throw new IllegalArgumentException("Invalid disposal approval request");
		}
		param.setDestroyRequestNo(param.getDestroyRequestNo().trim());
	}

	private void requireSingleRow(int affectedRows, String operation) {
		if (affectedRows != 1) {
			throw new IllegalStateException("Unable to " + operation);
		}
	}

	private boolean blank(String value) {
		return value == null || value.trim().isEmpty();
	}

}

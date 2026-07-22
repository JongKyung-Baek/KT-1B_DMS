package kr.esob.fdms.controller.inside.production.disposalapproval;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.commonrequest.ApprovalLineDetailVO;

@Service
public class DisposalApprovalService implements CommonService{

	@Inject
	DisposalApprovalDao dao;

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

	public ResultVO destroyApproval(DisposalApprovalPopupParam param) {
		ResultVO result = new ResultVO();
		ApprovalLineDetailVO approvalLine = dao.getApprovalLineInfo(param);
		param.setApprovalStatusCd(approvalLine.getApprovalStatusCd());
		param.setApprovalGradeCd(approvalLine.getApprovalGradeCd());
		List<DisposalApprovalPopupVO> destroyList = new ArrayList<DisposalApprovalPopupVO>();
		destroyList = dao.selectDestroyList(param);
		if( "A".equals(param.getSaveType()) ) {			//승인
			param.setStatusCd("APPROVAL");				//FDMS_DESTROY_REQUEST의 최종 승인여부
			param.setActionCd("APPROVAL");				//FDMS_DESTROY_REQUEST_DETAIL의 해당 결재 순서의 승인 여부
			for(DisposalApprovalPopupVO destVo : destroyList) {
				DisposalApprovalPopupParam tempParam = new DisposalApprovalPopupParam();
				tempParam.setRequestNo(destVo.getRequestNo());
				tempParam.setObjectId(destVo.getObjectId());
				tempParam.setDeployUserCd(destVo.getDeployUserCd());
				tempParam.setDestroyRequestNo(param.getDestroyRequestNo());
				dao.updateDestroyCount(tempParam);				//승인 시 남은 배포수량 전량 폐기
				dao.deleteProductStatus(tempParam);
			}
			//승인으로 변경
			param.setRejectDesc(null);
		}else if( "R".equals(param.getSaveType()) ) {	//반려
			param.setStatusCd("REJECT");				//FDMS_DESTROY_REQUEST의 최종 승인여부
			param.setActionCd("REJECT");				//FDMS_DESTROY_REQUEST_DETAIL의 해당 결재 순서의 승인 여부
			for(DisposalApprovalPopupVO destVo : destroyList) {
				dao.updateDisposalReject(destVo);
			}
		}

		dao.updateDestroyRequestInfo(param);
		dao.updateDestroyRequestDetail(param);

		result.setSuccess(true);
		return result;
	}

}

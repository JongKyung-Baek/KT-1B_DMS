package kr.esob.fdms.controller.inside.production.requeststatus;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.production.approval.printApprovalVO;

@Service
public class RequestStatusService implements CommonService{

	@Inject
	RequestStatusDao dao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	public RequestStatusPopupVO getRequestInfo(RequestStatusPopupParam param) {
		return dao.getRequestInfo(param);
	}

	public List<RequestStatusPopupVO> selectAcceptanceUserList(RequestStatusPopupParam param) {
		return dao.selectAcceptanceUserList(param);
	}

	public List<RequestStatusPopupVO> selectProductionList(RequestStatusPopupParam param) {
		return dao.selectProductionList(param);
	}

//	public printApprovalVO getPrintRequestInfo(RequestStatusPopupParam param) {
//		return dao.getPrintRequestInfo(param);
//	}

//	public List<printApprovalVO> selectPrintApprovalrList(RequestStatusPopupParam param) {
//		return dao.selectPrintApprovalrList(param);
//	}

	public ResultVO printApproval(RequestStatusPopupParam param) {
		ResultVO result = new ResultVO();
		
//		CommonApprovalParam approveParam = dao.getCurrentApprovalInfo(param.getRequestNo());
		
//		param.setApprovalStatusCd(approveParam.getApprovalStatusCd());
//		param.setApprovalGradeCd(approveParam.getApprovalGradeCd());
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
		return result;
	}

	public PrintRequestStatusVO getPrintRequestInfo(RequestStatusPopupParam param) {
		return dao.getPrintRequestInfo(param);
	}


	public List<PrintRequestStatusVO> selectPrintRequestList(RequestStatusPopupParam param) {
		return dao.selectPrintRequestList(param);
	}
}

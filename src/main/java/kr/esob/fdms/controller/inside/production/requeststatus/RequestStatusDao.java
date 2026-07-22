package kr.esob.fdms.controller.inside.production.requeststatus;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
@Repository
public class RequestStatusDao extends AbstractDao {
	private String prefix = "sql.ProductionRequestStatus.";

	@SuppressWarnings("unchecked")
	public List<RequestStatusListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public RequestStatusPopupVO getRequestInfo(RequestStatusPopupParam param) {
		return (RequestStatusPopupVO) obj(prefix + "getRequestInfo", param);
	}

	@SuppressWarnings("unchecked")
	public List<RequestStatusPopupVO> selectAcceptanceUserList(RequestStatusPopupParam param) {
		return list(prefix + "selectAcceptanceUserList", param);
	}

	@SuppressWarnings("unchecked")
	public List<RequestStatusPopupVO> selectProductionList(RequestStatusPopupParam param) {
		return list(prefix + "selectProductionList", param);
	}

//	public printApprovalVO getPrintRequestInfo(RequestStatusPopupParam param) {
//		return (printApprovalVO) obj(prefix + "getPrintRequestInfo", param);
//	}

//	@SuppressWarnings("unchecked")
//	public List<printApprovalVO> selectPrintApprovalrList(RequestStatusPopupParam param) {
//		return list(prefix + "selectPrintApprovalrList", param);
//	}


	public void updateRequestInfo(RequestStatusPopupParam param) {
		update(prefix + "updateRequestInfo", param);
	}

	public void updateRequestDetail(RequestStatusPopupParam param) {
		update(prefix + "updateRequestDetail", param);
	}

	public PrintRequestStatusVO getPrintRequestInfo(RequestStatusPopupParam param) {
		return (PrintRequestStatusVO) obj(prefix + "getPrintRequestInfo", param);
	}

	@SuppressWarnings("unchecked")
	public List<PrintRequestStatusVO> selectPrintRequestList(RequestStatusPopupParam param) {
		return list(prefix + "selectPrintRequestList", param);
	}
}
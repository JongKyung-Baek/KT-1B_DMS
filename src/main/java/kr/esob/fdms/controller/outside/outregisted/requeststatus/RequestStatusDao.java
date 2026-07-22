package kr.esob.fdms.controller.outside.outregisted.requeststatus;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.grid.GridResultVO;

@Repository
public class RequestStatusDao extends AbstractDao{
	private String prefix = "sql.OutregistedRequestStatus.";

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
	public List<GridResultVO> selectPopupList(RequestStatusListParam param) {
		return list(prefix + "selectPopupList", param);
	}

	public String getDeployEmailCC(Object param) {
		return (String) obj(prefix + "getDeployEmailCC", param);
	}
	
	public void deleteRequest(RequestStatusPopupParam param) {
		delete(prefix + "deleteRequest", param);
	}
	
	public void deleteRequestMapping(RequestStatusPopupParam param) {
		delete(prefix + "deleteRequestMapping", param);
	}
	
	public void deleteRequestDetail(RequestStatusPopupParam param) {
		delete(prefix + "deleteRequestDetail", param);
	}
	
	public void deleteRequestFile(RequestStatusPopupParam param) {
		delete(prefix + "deleteRequestFile", param);
	}
	
	public void deleteRequestDeploy(RequestStatusPopupParam param) {
		delete(prefix + "deleteRequestDeploy", param);
	}

	public void deleteApprovalFile(RequestStatusPopupParam param) {
		delete(prefix + "deleteApprovalFile", param);
		
	}
	
	
}
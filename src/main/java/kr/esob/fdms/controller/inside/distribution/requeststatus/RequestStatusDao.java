package kr.esob.fdms.controller.inside.distribution.requeststatus;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.distribution.history.HistoryListVO;

@Repository
public class RequestStatusDao extends AbstractDao {
	private String prefix = "sql.RequestStatus.";


	@SuppressWarnings("unchecked")
	public List<RequestStatusListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public int selectPopupListCount(RequestStatusPopupParam param) {
		return (Integer) obj(prefix + "selectPopupListCount", param);
	}

	public RequestStatusPopupVO getApprovalRequestStatus(RequestStatusPopupParam param) {
		return (RequestStatusPopupVO) obj(prefix + "getApprovalRequestStatus", param);
	}

	public List requestStatusPopupList(RequestStatusPopupParam param) {
		return list(prefix + "requestStatusPopupList", param);
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
	
}

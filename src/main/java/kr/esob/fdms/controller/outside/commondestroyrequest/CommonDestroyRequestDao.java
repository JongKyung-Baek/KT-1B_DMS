package kr.esob.fdms.controller.outside.commondestroyrequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.distribution.history.DestroyPopupParam;

@Repository
public class CommonDestroyRequestDao extends AbstractDao {
	private String prefix = "sql.DestroyRequest.";

	public void insertDestroyInfo(DestroyPopupParam param) {
		insert(prefix + "insertDestroyInfo", param);
	}

	public void insertDestroyFile(DestroyPopupParam param) {
		insert(prefix + "insertDestroyFile", param);
	}

	public void insertDestroyRequest(DestroyPopupParam param) {
		insert(prefix + "insertDestroyRequest", param);
	}

	public void updateDestroyRequestDesc(DestroyPopupParam param) {
		update(prefix + "updateDestroyRequestDesc", param);
	}

	public void insertDestroyRequestDetail(Object param) {
		insert(prefix + "insertDestroyRequestDetail", param);
	}

	public void updateRequestFileDestroy(DestroyPopupParam param) {
		update(prefix + "updateRequestFileDestroy", param);
	}

	public void insertDestroyRequestMapping(DestroyPopupParam param) {
		insert(prefix + "insertDestroyRequestMapping", param);
	}

	public void updateApprovalFile(DestroyPopupParam param) {
		update(prefix + "updateApprovalFile", param);
	}
	
	public void updateUnderDestroy(DestroyRequestParam param) {
		update(prefix + "updateUnderDestroy", param);
	}
}

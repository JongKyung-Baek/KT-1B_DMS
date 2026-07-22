package kr.esob.fdms.controller.inside.distribution.printhistory;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.distribution.commonrequest.ApprovalLineDetailVO;

@Repository
public class HistoryDao extends AbstractDao {
	private String prefix = "sql.PrintHistory.";

	@SuppressWarnings("unchecked")
	public List<HistoryListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public String inserDestoryRequest(DestroyRequestParam param) {
		insert(prefix + "inserDestoryRequest", param);
		return param.getDestroyRequestNo();
	}

	@SuppressWarnings("unchecked")
	public List<ApprovalLineDetailVO> getDocsApprovalLineDetail(String approvalLineId) {
		return list(prefix + "getDocsApprovalLineDetail", approvalLineId);
	}

	public void insertDocsDestroyRequestDetail(DestroyRequestParam param) {
		insert(prefix + "insertDocsDestroyRequestDetail", param);
	}

	public void insertDocsDestoryRequestMapping(DestroyRequestParam param) {
		insert(prefix + "insertDocsDestoryRequestMapping", param);
	}

	public List<Map<String, Object>> getProtectUser(DestroyRequestParam param) {
		return list(prefix + "getProtectUser", param);
	}

	public void updateApprovalFile(DestroyRequestParam param) {
		update(prefix + "updateApprovalFile", param);
	}
}
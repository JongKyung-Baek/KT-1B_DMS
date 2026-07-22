package kr.esob.fdms.controller.inside.production.disposal;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.distribution.commonrequest.ApprovalLineDetailVO;
@Repository
public class DisposalDao extends AbstractDao {
	private String prefix = "sql.ProductionDisposal.";

	@SuppressWarnings("unchecked")
	public List<DisposalListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public void inserDestoryRequest(DisposalRequestPopupParam param) {
		insert(prefix + "inserDestoryRequest", param);
	}

	@SuppressWarnings("unchecked")
	public List<ApprovalLineDetailVO> getDocsApprovalLineDetail(String approvalLineId) {
		return list(prefix + "getDocsApprovalLineDetail", approvalLineId);
	}

	public void insertDocsDestroyRequestDetail(DisposalRequestPopupParam param) {
		insert(prefix + "insertDocsDestroyRequestDetail", param);
	}

	public void insertDocsDestoryRequestMapping(DisposalRequestPopupParam param) {
		insert(prefix + "insertDocsDestoryRequestMapping", param);
	}

	public void updateApprovalFile(DisposalRequestPopupParam param) {
		update(prefix + "updateApprovalFile", param);
	}

	@SuppressWarnings("unchecked")
	public List<DisposalRequestPopupParam> selectDisposalList(DisposalRequestPopupParam param){
		return list(prefix + "selectDisposalList", param);
	}

	public void updateProductStatusDisposal(DisposalRequestPopupParam param) {
		update(prefix + "updateProductStatusDisposal", param);
	}
}
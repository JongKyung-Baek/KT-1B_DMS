package kr.esob.fdms.controller.inside.production.disposalapproval;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.distribution.commonrequest.ApprovalLineDetailVO;
@Repository
public class DisposalApprovalDao extends AbstractDao {
	private String prefix = "sql.ProductionDisposalApproval.";

	@SuppressWarnings("unchecked")
	public List<DisposalApprovalListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public DisposalApprovalPopupVO getDestroyRequestInfo(DisposalApprovalPopupParam param) {
		return (DisposalApprovalPopupVO) obj(prefix + "getDestroyRequestInfo", param);
	}

	public ApprovalLineDetailVO getApprovalLineInfo(DisposalApprovalPopupParam param) {
		return (ApprovalLineDetailVO) obj(prefix + "getApprovalLineInfo", param);
	}

	public void updateDestroyRequestInfo(DisposalApprovalPopupParam param) {
		update(prefix + "updateDestroyRequestInfo", param);
	}

	public void updateDestroyRequestDetail(DisposalApprovalPopupParam param) {
		update(prefix + "updateDestroyRequestDetail", param);
	}

	public void updateDestroyCount(DisposalApprovalPopupParam param) {
		update(prefix + "updateDestroyCount", param);
	}

	@SuppressWarnings("unchecked")
	public List<DisposalApprovalPopupVO> selectDestroyList(DisposalApprovalPopupParam param) {
		return list(prefix + "selectDestroyList", param);
	}

	public void deleteProductStatus(DisposalApprovalPopupParam param) {
		delete(prefix + "deleteProductStatus", param);
	}

	public void updateDisposalReject(DisposalApprovalPopupVO param) {
		update(prefix + "updateDisposalReject", param);
	}
}
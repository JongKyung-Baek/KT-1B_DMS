package kr.esob.fdms.controller.inside.distribution.disposalacceptance;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.outside.commondestroystatus.DestroyFileVO;

@Repository
public class DisposalAcceptanceDao extends AbstractDao {
	private String prefix = "sql.DisposalAcceptance.";


	@SuppressWarnings("unchecked")
	public List<DisposalAcceptanceListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public DisposalAcceptancePopupVO selectDisposalInfo(DisposalAcceptanceParam param) {
		return (DisposalAcceptancePopupVO) obj(prefix + "selectDisposalInfo", param);
	}

	@SuppressWarnings("unchecked")
	public List<DisposalAcceptancePopupListVO> selectPopupList(DisposalAcceptanceParam param){
		return list(prefix + "selectPopupList", param);
	}

	@SuppressWarnings("unchecked")
	public List<DestroyFileVO> selectDisposalFileList(DisposalAcceptanceParam param){
		return list(prefix + "selectDisposalFileList", param);
	}

	public DestroyFileVO selectFileInfo(DestroyFileVO param) {
		return (DestroyFileVO) obj(prefix + "selectFileInfo", param);
	}

	public void updateDestroyRequest(DisposalAcceptanceParam param) {
		update(prefix + "updateDestroyRequest", param);
	}

	public void updateDestroyRequestDetail(DisposalAcceptanceParam param) {
		update(prefix + "updateDestroyRequestDetail", param);
	}

	public void updateRequestFile(DisposalAcceptanceParam param) {
		update(prefix + "updateRequestFile", param);
	}

	public void updateApprovalFile(DisposalAcceptanceParam param) {
		update(prefix + "updateApprovalFile", param);
	}
	
}

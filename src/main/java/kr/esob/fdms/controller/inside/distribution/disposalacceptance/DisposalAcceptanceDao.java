package kr.esob.fdms.controller.inside.distribution.disposalacceptance;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.outside.commondestroystatus.DestroyFileDownloadParam;
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

	@SuppressWarnings("unchecked")
	public List<DestroyFileVO> selectAuthorizedDownloadTargets(DestroyFileDownloadParam param) {
		return list(prefix + "selectAuthorizedDownloadTargets", param);
	}

	public String selectApprovalTargetForUpdate(DisposalAcceptanceParam param) {
		return (String) obj(prefix + "selectApprovalTargetForUpdate", param);
	}

	public int updateDestroyRequest(DisposalAcceptanceParam param) {
		return update(prefix + "updateDestroyRequest", param);
	}

	public int updateDestroyRequestDetail(DisposalAcceptanceParam param) {
		return update(prefix + "updateDestroyRequestDetail", param);
	}

	public int updateRequestFile(DisposalAcceptanceParam param) {
		return update(prefix + "updateRequestFile", param);
	}

	public int updateApprovalFile(DisposalAcceptanceParam param) {
		return update(prefix + "updateApprovalFile", param);
	}
	
}

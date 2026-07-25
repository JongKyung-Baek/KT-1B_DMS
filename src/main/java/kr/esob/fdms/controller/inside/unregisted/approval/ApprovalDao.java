package kr.esob.fdms.controller.inside.unregisted.approval;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.grid.GridResultVO;

@Repository
public class ApprovalDao extends AbstractDao{
	private String prefix = "sql.UnregistedApproval.";

	@SuppressWarnings("unchecked")
	public List<ApprovalListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public ApprovalPopupVO getRequestInfo(ApprovalPopupParam param) {
		return (ApprovalPopupVO) obj(prefix + "getRequestInfo", param);
	}

	public ApprovalPopupParam selectApprovalTargetForUpdate(ApprovalPopupParam param) {
		return (ApprovalPopupParam) obj(prefix + "selectApprovalTargetForUpdate", param);
	}

	@SuppressWarnings("unchecked")
	public List<GridResultVO> selectPopupList(ApprovalPopupParam param) {
		return list(prefix + "selectPopupList", param);
	}

	public int updateRequestInfo(ApprovalPopupParam param) {
		return update(prefix + "updateRequestInfo", param);
	}

	@SuppressWarnings("unchecked")
	public List<ApprovalPopupVO> selectItemList(ApprovalPopupParam param) {
		return list(prefix + "selectItemList", param);
	}

	public int insertApprovalFile(ApprovalPopupParam param) {
		return (Integer) insert(prefix + "insertApprovalFile", param);
	}

	public int updateRequestDetail(ApprovalPopupParam param) {
		return update(prefix + "updateRequestDetail", param);
	}

	public int selectPopupListCount(ApprovalPopupParam param) {
		return (Integer) obj(prefix + "selectPopupListCount", param);
	}

}

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

	@SuppressWarnings("unchecked")
	public List<GridResultVO> selectPopupList(ApprovalPopupParam param) {
		return list(prefix + "selectPopupList", param);
	}

	public void updateRequestInfo(ApprovalPopupParam param) {
		update(prefix + "updateRequestInfo", param);
	}

	@SuppressWarnings("unchecked")
	public List<ApprovalPopupVO> selectItemList(ApprovalPopupParam param) {
		return list(prefix + "selectItemList", param);
	}

	public void insertApprovalFile(ApprovalPopupParam param) {
		insert(prefix + "insertApprovalFile", param);
	}

	public void updateRequestDetail(ApprovalPopupParam param) {
		update(prefix + "updateRequestDetail", param);
	}

	public int selectPopupListCount(ApprovalPopupParam param) {
		return (Integer) obj(prefix + "selectPopupListCount", param);
	}

}
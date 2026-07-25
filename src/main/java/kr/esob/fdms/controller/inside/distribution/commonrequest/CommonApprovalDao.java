package kr.esob.fdms.controller.inside.distribution.commonrequest;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommonApprovalDao extends AbstractDao {
	private String prefix = "sql.commonApproval.";

	public int selectApprovalListCount(CommonApprovalParam param) {
		return (Integer) obj(prefix + "selectApprovalList", param);
	}

	public String getDistributionApprovalRequestDesc(CommonApprovalParam param) {
		return (String) obj(prefix + "getDistributionApprovalRequestDesc", param);
	}

	public int updateRequestDefInfo(CommonApprovalParam param) {
		return update(prefix + "updateRequestDefInfo", param);
	}

	public int updateRequestInfo(CommonApprovalParam param) {
		return update(prefix + "updateRequestInfo", param);
	}

	public int updateRequestDetail(CommonApprovalParam param) {
		return update(prefix + "updateRequestDetail", param);
	}

	@SuppressWarnings("unchecked")
	public List<CommonApprovalPopupListVO> selectApprovalList(CommonApprovalParam param) {
		return list(prefix + "selectApprovalList", param);
	}

	public int selectPrintApprovalListCount(CommonApprovalParam param) {
		return (Integer) obj(prefix + "selectPrintApprovalListCount", param);
	}

	public int selectListCount(CommonApprovalParam param) {
		return (Integer) obj(prefix + "selectListCount", param);
	}

	@SuppressWarnings("unchecked")
	public List<CommonApprovalPopupListVO> selectItemList(CommonApprovalParam param) {
		return list(prefix + "selectItemList", param);
	}

	public int insertApprovalFile(CommonApprovalParam param) {
		return (Integer) insert(prefix + "insertApprovalFile", param);
	}

	public List<CommonApprovalPopupListVO> selectDestroyItemList(CommonApprovalParam param) {
		return list(prefix + "selectDestroyItemList", param);
	}

	public void insertDestroyRequest(CommonApprovalParam param) {
		insert(prefix + "insertDestroyRequest", param);
	}

	public void insertDestroyRequestDetail(CommonApprovalParam param) {
		insert(prefix + "insertDestroyRequestDetail", param);
	}

	@SuppressWarnings("rawtypes")
	public List getDocsApprovalLineDetail(String approvalLineId) {
		return list(prefix + "getDocsApprovalLineDetail", approvalLineId);
	}

	public void insertDestroyRequestMapping(CommonApprovalParam param) {
		insert(prefix + "insertDestroyRequestMapping", param);
	}

	public void updateApprovalFile(CommonApprovalParam param) {
		update(prefix + "updateApprovalFile", param);
	}

	public CommonApprovalParam getCurrentApprovalInfo(CommonApprovalParam param) {
		return (CommonApprovalParam) obj(prefix + "getCurrentApprovalInfo", param);
	}

	/**
	 * 방산기술 결재자 이관
	 * @param param
	 */
	public String selectPassTargetForUpdate(PassParamVO param) {
		return (String) obj(prefix + "selectPassTargetForUpdate", param);
	}

	public int updatePassTarget(PassParamVO param) {
		return update(prefix + "updatePassTarget", param);
	}

	public CommonApprovalParam selectRequestInfo(CommonApprovalParam param) {
		return (CommonApprovalParam) obj(prefix + "selectRequestInfo", param);
	}

	public void updateRequestFileDestroy(CommonApprovalParam param) {
		update(prefix + "updateRequestFileDestroy", param);
	}

}

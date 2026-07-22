package kr.esob.fdms.controller.inside.unregisted.request;

import java.util.List;

import kr.esob.fdms.commonlogic.viewer.CommonViewerVO;
import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.distribution.commonrequest.ApprovalLineDetailVO;

@Repository
public class UnregisterRequestDao extends AbstractDao{
	private String prefix = "sql.UnregistedRequest.";

	@SuppressWarnings("unchecked")
	public List<RequestListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public void insertUnregisterInfo(UnregisterPopupParam param) {
		insert(prefix + "insertUnregisterInfo", param);
	}

	public String selectUnregisterFile(UnregisterPopupParam param){
		return (String) obj(prefix + "selectUnregisterFile", param);
	}

	public void insertUnregisterFile(UnregisterPopupParam param) {
		insert(prefix + "insertUnregisterFile", param);
	}

	public void insertUnregisterFileX(UnregisterPopupParam param) {
		insert(prefix + "insertUnregisterFileX", param);
	}

	public void insertRequest(DistributionRequestPopupParam param) {
		insert(prefix + "insertRequest", param);
	}

	public void insertRequestDeploy(DistributionRequestPopupParam param) {
		insert(prefix + "insertRequestDeploy", param);
	}

	@SuppressWarnings("unchecked")
	public List<ApprovalLineDetailVO> getDocsApprovalLineDetail(String approvalLineId) {
		return list(prefix + "getDocsApprovalLineDetail", approvalLineId);
	}

	public List<DistributionRequestPopupParam> selectRequestMappingDuplicate(DistributionRequestPopupParam param){
		return list(prefix + "selectRequestMappingDuplicate", param);
	}

	public void insertDocsRequestDetail(DistributionRequestPopupParam param) {
		insert(prefix + "insertDocsRequestDetail", param);
	}

	public void insertDocsRequestMapping(DistributionRequestPopupParam param) {
		insert(prefix + "insertDocsRequestMapping", param);
	}

	public void insertDocsRequestFile(DistributionRequestPopupParam param) {
		insert(prefix + "insertDocsRequestFile", param);
	}
}
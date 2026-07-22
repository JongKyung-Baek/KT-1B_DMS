package kr.esob.fdms.controller.inside.distribution.commonrequest;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.commonrequest.RequestFileParam;

@Repository
public class CommonDistributionRequestDao extends AbstractDao {
	private String prefix = "sql.commonRequest.";


	public String getUserEmail(String userCd) {
		return (String) obj(prefix + "getUserEmail", userCd);
	}

	public void insertRequest(CommonDistributionRequestParam param) {
		insert(prefix + "inserRequest", param);
	}

	public void insertRequestDeploy(CommonDistributionRequestParam param) {
		insert(prefix + "insertRequestDeploy", param);
	}

	public List getDocsApprovalLineDetail(String approvalLineId) {
		return list(prefix + "getDocsApprovalLineDetail", approvalLineId);
	}

	public void insertDocsRequestDetail(CommonDistributionRequestParam param) {
		insert(prefix + "insertDocsRequestDetail", param);
	}

	public void insertDocsRequestMapping(CommonDistributionRequestParam param) {
		insert(prefix + "insertDocsRequestMapping", param);
	}

	public List<CommonDistributionRequestParam> selectRequestMappingDuplicate(CommonDistributionRequestParam param){
		return list(prefix + "selectRequestMappingDuplicate", param);
	}

	public List<RequestFileParam> selectFileInfo(Object param){
		return list(prefix + "selectFileInfo", param);
	}

	public void insertDocsRequestFile(CommonDistributionRequestParam param) {
//		insert(prefix + "insertDocsRequestFile", param);
		insert(prefix + "insertDocsRequestFileUsingMap", param);
	}

	public int verificationProtectUser(CommonDistributionRequestParam param) {
		return (Integer) obj(prefix + "verificationProtectUser", param);
	}

	public List<Map<String, Object>> getProtectUser(CommonDistributionRequestParam param) {
		return list(prefix + "getProtectUser", param);
	}

	public int selectInsaDeptProductTeam(UserVO param) {
		return (Integer) obj(prefix + "selectInsaDeptProductTeam", param);
	}

	public List<CommonDistributionRequestVO> selectProtectPopupList(CommonDistributionRequestParam param) {
		return list(prefix + "selectProtectPopupList", param);
	}


}

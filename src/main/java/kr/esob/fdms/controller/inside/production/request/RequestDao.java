package kr.esob.fdms.controller.inside.production.request;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.production.common.DeployInfoVO;
import kr.esob.fdms.controller.inside.production.common.ProductStatusVO;
import kr.esob.fdms.controller.outside.commonrequest.RequestFileParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestMappingParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
@Repository
public class RequestDao extends AbstractDao {
	private String prefix = "sql.ProductionRequest.";

	@SuppressWarnings("unchecked")
	public List<RequestListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public void insertProductionRequest(RequestParam param) {
		insert(prefix + "insertProductionRequest", param);
	}

	public void insertProductionRequestMapping(RequestParam param) {
		insert(prefix + "insertProductionRequestMapping", param);
	}

	public void insertProductionRequestDetail(RequestParam param) {
		insert(prefix + "insertProductionRequestDetail", param);
	}

	public void insertProductionRequestDeploy(RequestParam param) {
		insert(prefix + "insertProductionRequestDeploy", param);
	}

	public void insertProductionRequestDeployInfo(RequestParam param) {
		insert(prefix + "insertProductionRequestDeployInfo", param);
	}

	public void insertProductionRequestFile(Object param) {
		insert(prefix + "insertProductionRequestFile", param);
	}

	@SuppressWarnings("unchecked")
	public List<RequestMappingParam> selectMappingList(RequestParam param) {
		return list(prefix + "selectMappingList", param);
	}

	public List<RequestFileParam> selectFileList(RequestParam param){
		return list(prefix + "selectFileList", param);
	}
	
	public Integer selectDeployAcceptCheck(RequestParam param){
		return (Integer) obj(prefix + "selectDeployAcceptCheck", param);
	}

}

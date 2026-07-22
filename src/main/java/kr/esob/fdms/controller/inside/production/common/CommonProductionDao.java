package kr.esob.fdms.controller.inside.production.common;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.viewer.CommonViewerParam;
import kr.esob.fdms.commonlogic.viewer.CommonViewerVO;
import kr.esob.fdms.controller.outside.commonrequest.RequestFileParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestMappingParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;

@Repository
public class CommonProductionDao extends AbstractDao {
	private String prefix = "sql.CommonProduction.";

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
}

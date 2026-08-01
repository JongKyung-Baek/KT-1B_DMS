package kr.esob.tdms.controller.general.production.common;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.tdms.commonlogic.viewer.CommonViewerParam;
import kr.esob.tdms.commonlogic.viewer.CommonViewerVO;
import kr.esob.tdms.commonlogic.distribution.model.RequestFileParam;
import kr.esob.tdms.commonlogic.distribution.model.RequestMappingParam;
import kr.esob.tdms.commonlogic.distribution.model.RequestParam;

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

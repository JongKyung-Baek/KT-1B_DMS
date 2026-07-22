package kr.esob.fdms.controller.outside.production.request;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.production.common.ProductionInfoVO;
import kr.esob.fdms.controller.outside.commonrequest.ObjectInfoVO;
import kr.esob.fdms.controller.outside.commonrequest.RequestFileParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;

@Repository
public class RequestDao extends AbstractDao {
	private String prefix = "sql.OuterProductionRequest.";

	@SuppressWarnings("unchecked")
	public List<RequestListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public List<ProductionInfoVO> selectDocumentInspectionInfo(ProductionInfoVO productionInfoVo) {
		return list(prefix + "selectDocumentInspectionInfo", productionInfoVo);
	}

	public List<ProductionInfoVO> selectSwInspectionInfo(ProductionInfoVO productionInfoVo) {
		return list(prefix + "selectSwInspectionInfo", productionInfoVo);
	}

	public List<ObjectInfoVO> selectDocumentRequestStatusList(RequestParam param){
		return list(prefix + "selectDocumentRequestStatusList", param);
	}

	public List<ObjectInfoVO> selectSwRequestStatusList(RequestParam param){
		return list(prefix + "selectSwRequestStatusList", param);
	}

	public List<ProductionInfoVO> selectProductionDetailList(ProductionInfoVO productionInfoVo){
		return list(prefix + "selectProductionDetailList", productionInfoVo);
	}

	public List<RequestFileParam> selectFileList(RequestParam param){
		return list(prefix + "selectFileList", param);
	}
}

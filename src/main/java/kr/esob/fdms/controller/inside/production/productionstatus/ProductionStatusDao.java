package kr.esob.fdms.controller.inside.production.productionstatus;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.production.request.RequestListParam;

@Repository
public class ProductionStatusDao extends AbstractDao {

	private String prefix = "sql.ProductionStatus.";

	@SuppressWarnings("unchecked")
	public List<RequestListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public List<ProductionReplaceRequestVO> selectProductionReplace(ReplaceRequestParam param){
		return list(prefix + "selectProductionReplace", param);
	}

	public List<DistributionHistoryVO> selectDeployInfoList(DistributionHistoryVO param){
		return list(prefix + "selectDeployInfoList", param);
	}

	public List<HistoryDetailVO> selectHistoryDetailList(DistributionHistoryVO param){
		return list(prefix + "selectHistoryDetailList", param);
	}

	public List<DistributionHistoryVO> selectDisposalRequestObject(Object param){
		return list(prefix + "selectDisposalRequestObject", param);
	}

}

package kr.esob.fdms.controller.inside.production.history;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.production.common.ProductionInfoVO;
@Repository
public class HistoryDao extends AbstractDao {
	private String prefix = "sql.ProductionHistory.";

	@SuppressWarnings("unchecked")
	public List<HistoryListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public HistoryDetailVO selectHistoryDetail(HistoryDetailParam param) {
		return (HistoryDetailVO) obj(prefix + "selectHistoryDetail", param);
	}

	@SuppressWarnings("unchecked")
	public List<ProductionInfoVO> selectProductionList(HistoryDetailParam param) {
		return list(prefix + "selectProductionList", param);
	}

	@SuppressWarnings("unchecked")
	public List<RequestDetailVO> selectRequestDetailList(HistoryDetailParam param) {
		return list(prefix + "selectRequestDetailList", param);
	}

	@SuppressWarnings("unchecked")
	public List<RequestDeployVO> selectRequestDeployList(HistoryDetailParam param) {
		return list(prefix + "selectRequestDeployList", param);
	}

	@SuppressWarnings("unchecked")
	public List<DeployInfoVO> selectDeployInfoList(RequestDeployVO param){
		return list(prefix + "selectDeployInfoList", param);
	}
}
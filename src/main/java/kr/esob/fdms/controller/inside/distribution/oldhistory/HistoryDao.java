package kr.esob.fdms.controller.inside.distribution.oldhistory;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.result.ResultVO;

@Repository
public class HistoryDao extends AbstractDao {
	private String prefix = "sql.OldDistributionHistory.";

	@SuppressWarnings("unchecked")
	public List<Map<String, String>> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}
	
	public void destroyOldHistory(HistoryListParam param) {
		update(prefix + "updateOldHistory", param);
	}
}
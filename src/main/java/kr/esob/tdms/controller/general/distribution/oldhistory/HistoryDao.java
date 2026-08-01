package kr.esob.tdms.controller.general.distribution.oldhistory;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.tdms.commonlogic.result.ResultVO;

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
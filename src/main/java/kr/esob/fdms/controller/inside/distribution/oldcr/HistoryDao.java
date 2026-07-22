package kr.esob.fdms.controller.inside.distribution.oldcr;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class HistoryDao extends AbstractDao {
	private String prefix = "sql.OldCrHistory.";

	@SuppressWarnings("unchecked")
	public List<Map<String, String>> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}
}
package kr.esob.fdms.controller.outside.outregisted.history;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class HistoryDao extends AbstractDao{
	private String prefix = "sql.OutregistedHistory.";

	@SuppressWarnings("unchecked")
	public List<HistoryListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}
}
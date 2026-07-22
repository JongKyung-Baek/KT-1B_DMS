package kr.esob.fdms.controller.inside.outregisted.status;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class StatusDao extends AbstractDao{
	private String prefix = "sql.OutregistedStatus.";

	@SuppressWarnings("unchecked")
	public List<StatusListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}
}
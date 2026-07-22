package kr.esob.fdms.controller.outside.unregisted.accept;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class AcceptDao extends AbstractDao{
	private String prefix = "sql.OutsideUnregistedAccept.";

	@SuppressWarnings("unchecked")
	public List<AcceptListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}
}
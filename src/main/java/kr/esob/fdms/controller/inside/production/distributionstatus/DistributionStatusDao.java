package kr.esob.fdms.controller.inside.production.distributionstatus;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
@Repository
public class DistributionStatusDao extends AbstractDao {
	private String prefix = "sql.ProductionDistributionStatus.";

	@SuppressWarnings("unchecked")
	public List<DistributionStatusListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}
}
package kr.esob.tdms.controller.general.production.distributionstatus;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;
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
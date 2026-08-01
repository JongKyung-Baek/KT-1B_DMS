package kr.esob.tdms.controller.general.production.disposalstatus;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.tdms.controller.general.distribution.commonrequest.ApprovalLineDetailVO;
@Repository
public class DisposalStatusDao extends AbstractDao {
	private String prefix = "sql.ProductionDisposalStatus.";

	@SuppressWarnings("unchecked")
	public List<DisposalStatusListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

}
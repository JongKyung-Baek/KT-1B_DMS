package kr.esob.fdms.controller.inside.production.disposalstatus;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.distribution.commonrequest.ApprovalLineDetailVO;
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
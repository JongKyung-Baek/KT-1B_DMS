package kr.esob.fdms.controller.outside.production.approvalstatus;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class ApprovalStatusDao extends AbstractDao {
	private String prefix = "sql.ProductionApprovalStatus.";

	@SuppressWarnings("unchecked")
	public List<ApprovalStatusListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}
}
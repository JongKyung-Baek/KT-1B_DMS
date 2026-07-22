package kr.esob.fdms.controller.outside.doc.approvalstatus;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class ApprovalStatusDao extends AbstractDao {
	private String prefix = "sql.OutsideDocApprovalStatus.";

	@SuppressWarnings("unchecked")
	public List<ApprovalStatusListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}
}
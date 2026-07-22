package kr.esob.fdms.controller.inside.distribution.approval;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class ApprovalDao extends AbstractDao {
	private String prefix = "sql.distributionApproval.";


	public List<ApprovalListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

//	public void updateList(Object param) {
//		update(prefix + "updateList", param);
//	}
	
}

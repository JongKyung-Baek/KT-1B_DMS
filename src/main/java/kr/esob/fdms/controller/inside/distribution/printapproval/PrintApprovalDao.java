package kr.esob.fdms.controller.inside.distribution.printapproval;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class PrintApprovalDao extends AbstractDao {
	private String prefix = "sql.PrintApproval.";

	@SuppressWarnings("unchecked")
	public List<PrintApprovalListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

}

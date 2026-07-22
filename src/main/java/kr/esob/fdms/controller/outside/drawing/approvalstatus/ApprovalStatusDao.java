package kr.esob.fdms.controller.outside.drawing.approvalstatus;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ApprovalStatusDao extends AbstractDao {
	private String prefix = "sql.ApprovalStatus.";

	@SuppressWarnings("unchecked")
	public List<ApprovalStatusListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public List<ApprovalStatusListVO> selectPopupListOutside(Object param){
		return list(prefix + "selectPopupListOutside", param);
	}


	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}
}
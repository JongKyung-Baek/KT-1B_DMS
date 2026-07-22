package kr.esob.fdms.controller.outside.user.status;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.controller.outside.user.information.InformationListParam;

@Repository
public class StatusDao extends AbstractDao{
	private String prefix = "sql.OutsideUserStatus.";

	@SuppressWarnings("unchecked")
	public List<StatusListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public StatusListParam selectInformationDetail(StatusListParam param) {
		return (StatusListParam) obj(prefix + "selectInformationDetail", param);
	}
}
package kr.esob.fdms.controller.inside.pdm.deployhistory;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class PdmDeployHistoryDao extends AbstractDao {

	public List<PdmDeployHistoryVO> selectList(Object param){
		return list("sql.PdmDeployHistory.selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj("sql.PdmDeployHistory.selectListCount", param);
	}

	public void updateList(Object param) {
		update("sql.PdmDeployHistory.updateList", param);
	}

}

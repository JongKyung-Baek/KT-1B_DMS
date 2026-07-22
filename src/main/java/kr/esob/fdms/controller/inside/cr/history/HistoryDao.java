package kr.esob.fdms.controller.inside.cr.history;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.cr.CrInfoVO;
import kr.esob.fdms.controller.inside.cr.CrParam;

@Repository
public class HistoryDao extends AbstractDao {
	private String prefix = "sql.crHistory.";


	@SuppressWarnings("unchecked")
	public List<HistoryListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public CrInfoVO selectHistoryInfo(CrParam param) {
		return (CrInfoVO) obj(prefix + "selectHistoryInfo", param);
	}
}

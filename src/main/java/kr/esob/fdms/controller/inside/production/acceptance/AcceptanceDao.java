package kr.esob.fdms.controller.inside.production.acceptance;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.grid.GridResultVO;

@Repository
public class AcceptanceDao extends AbstractDao {
	private String prefix = "sql.ProductionAcceptance.";

	@SuppressWarnings("unchecked")
	public List<AcceptanceListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	@SuppressWarnings("unchecked")
	public List<AcceptancePopupVO> selectPopupList(AcceptancePopupParam param) {
		return list(prefix + "selectPopupList", param);
	}

	public int selectPopupListCount(AcceptancePopupParam param) {
		return (Integer) obj(prefix + "selectPopupListCount", param);
	}

	public void updateAcceptance(AcceptancePopupParam param) {
		update(prefix + "updateAcceptance", param);
	}

}

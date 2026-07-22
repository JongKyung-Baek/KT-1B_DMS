package kr.esob.fdms.controller.inside.distribution.acceptance;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;

@Repository
public class AcceptanceDao extends AbstractDao {
	private String prefix = "sql.Acceptance.";


	public List<AcceptanceListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public List<AcceptanceVO> selectPopupList(Object param){
		return list(prefix +  "selectPopupList", param);
	}

	public Integer getPopupListCount(Object param){
		return (Integer) obj(prefix + "getPopupListCount", param);
	}

	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> getDefenseTeamLeader() {
		return list(prefix + "getDefenseTeamLeader");
	}

	public AcceptanceVO getDocRequestData(String requestNo) {
		return (AcceptanceVO) obj(prefix + "getDocRequestData", requestNo);
	}

	public void saveAccept(AcceptanceParam param) {
		update(prefix + "updateRequest", param);
		update(prefix + "updateRequestAcceptDetail", param);
	}

	public void saveDefRequestDetail(AcceptanceParam param) {
		update(prefix + "updateDefRequestDetail", param);
	}

	public void updateTlRequestDetail(AcceptanceParam param) {
		update(prefix + "updateTlRequestDetail", param);
	}

	public void updateRequestFile(AcceptanceParam param) {
		update(prefix + "updateRequestFile", param);
	}
}

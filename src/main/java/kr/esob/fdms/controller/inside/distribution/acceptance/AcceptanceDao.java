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

	public AcceptanceParam selectAcceptanceTargetForUpdate(AcceptanceParam param) {
		return (AcceptanceParam) obj(prefix + "selectAcceptanceTargetForUpdate", param);
	}

	public int updateRequest(AcceptanceParam param) {
		return update(prefix + "updateRequest", param);
	}

	public int updateRequestAcceptDetail(AcceptanceParam param) {
		return update(prefix + "updateRequestAcceptDetail", param);
	}

	public int saveDefRequestDetail(AcceptanceParam param) {
		return update(prefix + "updateDefRequestDetail", param);
	}

	public int updateTlRequestDetail(AcceptanceParam param) {
		return update(prefix + "updateTlRequestDetail", param);
	}

	public int updateRequestFile(AcceptanceParam param) {
		return update(prefix + "updateRequestFile", param);
	}
}

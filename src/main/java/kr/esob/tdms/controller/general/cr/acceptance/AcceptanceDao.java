package kr.esob.tdms.controller.general.cr.acceptance;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.tdms.controller.general.cr.CrInfoVO;
import kr.esob.tdms.controller.general.cr.CrParam;

@Repository
public class AcceptanceDao extends AbstractDao {
	private String prefix = "sql.crAcceptance.";


	@SuppressWarnings("unchecked")
	public List<AcceptanceListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public CrInfoVO selectAcceptanceInfo(CrParam param) {
		return (CrInfoVO) obj(prefix + "selectAcceptanceInfo", param);
	}

	public String selectAcceptanceTargetForUpdate(CrParam param) {
		return (String) obj(prefix + "selectAcceptanceTargetForUpdate", param);
	}

	public int updateAcceptance(CrParam param) {
		return update(prefix + "updateAcceptance", param);
	}

	public int updateApproval(CrParam param) {
		return update(prefix + "updateApproval", param);
	}

	public int updateRequest(CrParam param) {
		return update(prefix + "updateRequest", param);
	}

	public int updateCr(CrParam param) {
		return update(prefix + "updateCr", param);
	}

}

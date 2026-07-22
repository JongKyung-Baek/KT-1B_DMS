package kr.esob.fdms.controller.inside.cr.acceptance;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.controller.inside.cr.CrInfoVO;
import kr.esob.fdms.controller.inside.cr.CrParam;

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

	public void updateAcceptance(CrParam param) {
		update(prefix + "updateAcceptance", param);
	}

	public void updateAproval(CrParam param) {
		update(prefix + "updateAproval", param);
	}

	public void updateRequest(CrParam param) {
		update(prefix + "updateRequest", param);
	}

	public void updateCr(CrParam param) {
		update(prefix + "updateCr", param);
	}

}

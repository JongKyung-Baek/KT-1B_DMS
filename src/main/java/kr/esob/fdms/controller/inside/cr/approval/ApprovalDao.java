package kr.esob.fdms.controller.inside.cr.approval;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.cr.CrFileVO;
import kr.esob.fdms.controller.inside.cr.CrInfoVO;
import kr.esob.fdms.controller.inside.cr.CrParam;

@Repository
public class ApprovalDao extends AbstractDao {
	private String prefix = "sql.crApproval.";


	@SuppressWarnings("unchecked")
	public List<ApprovalListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public CrInfoVO selectApprovalInfo(CrParam param) {
		return (CrInfoVO) obj(prefix + "selectApprovalInfo", param);
	}

	public void updateRequestDetail(CrParam param) {
		update(prefix + "updateRequestDetail", param);
	}

	public void updateRequest(CrParam param) {
		update(prefix + "updateRequest", param);
	}

	public void updateCr(CrParam param) {
		update(prefix + "updateCr", param);
	}
	
	@SuppressWarnings("unchecked")
	public List<CrFileVO> selectFileList(CrParam param){
		return list(prefix + "selectFileList", param);
	}
	
	public void updateCrFile(CrFileVO param) {
		update(prefix + "updateCrFile", param);
	}
}

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

	public String selectApprovalTargetForUpdate(CrParam param) {
		return (String) obj(prefix + "selectApprovalTargetForUpdate", param);
	}

	public int updateRequestDetail(CrParam param) {
		return update(prefix + "updateRequestDetail", param);
	}

	public int updateRequest(CrParam param) {
		return update(prefix + "updateRequest", param);
	}

	public int updateCr(CrParam param) {
		return update(prefix + "updateCr", param);
	}
	
	@SuppressWarnings("unchecked")
	public List<CrFileVO> selectFileList(CrParam param){
		return list(prefix + "selectFileList", param);
	}
	
	public int updateCrFile(CrFileVO param) {
		return update(prefix + "updateCrFile", param);
	}
}

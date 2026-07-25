package kr.esob.fdms.controller.inside.organizationmanage.approval;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.controller.outside.commonrequest.RequestFileParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import kr.esob.fdms.controller.outside.user.information.InformationListParam;

@Repository
public class ApprovalDao extends AbstractDao {
	private String prefix = "sql.organizationmanageApproval.";

	@SuppressWarnings("unchecked")
	public List<ApprovalListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public ApprovalListParam selectDetailInfo(Object param){
		return (ApprovalListParam) obj(prefix + "selectDetailInfo", param);
	}

	public ApprovalListParam selectApprovalTarget(ApprovalListParam param) {
		return (ApprovalListParam) obj(prefix + "selectApprovalTarget", param);
	}

	public int updateReqeust(ApprovalListParam param) {
		return update(prefix + "updateReqeust", param);
	}

	public int updateUserInfo(ApprovalListParam param) {
		return update(prefix + "updateUserInfo", param);
	}

	public int deleteUserInfo(ApprovalListParam param) {
		return update(prefix + "deleteUserInfo", param);
	}

	public void updateUserProtectN(ApprovalListParam param) {
		update(prefix + "updateUserProtectN", param);
	}

	public int updateUserProtectY(ApprovalListParam param) {
		return update(prefix + "updateUserProtectY", param);
	}


	public int updateUserCr(ApprovalListParam param) {
		return update(prefix + "updateUserCr", param);
	}

	public int insertUser(ApprovalListParam param) {
		return (Integer) insert(prefix + "insertUser", param);
	}

	public List<ComboInfoVO> venderUser(ApprovalListParam param){
		return list(prefix + "venderUser", param);
	}
}

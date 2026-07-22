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

	public void updateReqeust(ApprovalListParam param) {
		delete(prefix + "updateReqeust", param);
	}

	public void updateUserInfo(ApprovalListParam param) {
		update(prefix + "updateUserInfo", param);
	}

	public void deleteUserInfo(ApprovalListParam param) {
		update(prefix + "deleteUserInfo", param);
	}

	public void updateUserProtectN(ApprovalListParam param) {
		update(prefix + "updateUserProtectN", param);
	}

	public void updateUserProtectY(ApprovalListParam param) {
		update(prefix + "updateUserProtectY", param);
	}


	public void updateUserCr(ApprovalListParam param) {
		update(prefix + "updateUserCr", param);
	}

	public void insertUser(ApprovalListParam param) {
		insert(prefix + "insertUser", param);
	}

	public List<ComboInfoVO> venderUser(ApprovalListParam param){
		return list(prefix + "venderUser", param);
	}
}

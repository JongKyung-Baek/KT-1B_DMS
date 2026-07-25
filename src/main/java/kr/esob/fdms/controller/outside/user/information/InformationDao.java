package kr.esob.fdms.controller.outside.user.information;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.outside.commonrequest.RequestFileParam;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;

@Repository
public class InformationDao extends AbstractDao {
	private String prefix = "sql.OutsideUserInformation.";

	@SuppressWarnings("unchecked")
	public List<InformationListParam> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public InformationListParam selectInformationDetail(InformationListParam param) {
		return (InformationListParam) obj(prefix + "selectInformationDetail", param);
	}

	public void insertInfo(InformationListParam param) {
		insert(prefix + "insertInfo", param);
	}

	public String selectCompanyApprover(InformationListParam param) {
		return (String) obj(prefix + "selectCompanyApprover", param);
	}

	public int selectCompanyUserCount(InformationListParam param) {
		return (Integer) obj(prefix + "selectCompanyUserCount", param);
	}

	public String selectPasswordHash(InformationListParam param) {
		return (String) obj(prefix + "selectPasswordHash", param);
	}

	public int updateUser(InformationListParam param) {
		return update(prefix + "updateUser", param);
	}

	public void updateInfo(InformationListParam param) {
		update(prefix + "updateInfo", param);
	}

	public void deleteInfo(InformationListParam param) {
		delete(prefix + "deleteInfo", param);
	}

	public Integer selectProtectCount(InformationListParam param){
		return (Integer) obj(prefix + "selectProtectCount", param);
	}

	public Integer selectCrCount(InformationListParam param){
		return (Integer) obj(prefix + "selectCrCount", param);
	}
}

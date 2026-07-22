package kr.esob.fdms.commonlogic.insa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.cr.CrInfoVO;
import kr.esob.fdms.controller.inside.cr.CrParam;
import kr.esob.fdms.controller.inside.cr.history.HistoryListVO;
import net.sf.json.JSONObject;

@Repository
public class InsaDao extends AbstractDao {
	private String prefix = "sql.insa.";


	@SuppressWarnings("unchecked")
	public List<HistoryListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectItnUser(Object param) {
		return listNotUseSession(prefix + "selectItnUser", param);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> selectDocsUser(Map<String, Object> param) throws Exception {
		return (Map<String, Object>) objNotUseSession(prefix + "selectDocsUser", param);
	}

	public Integer selectDocsUserCount(Map<String, Object> param) {
		return (Integer) obj(prefix + "selectDocsUserCount", param);
	}

	public String selectUserCd() {
		return (String) obj(prefix + "selectUserCd");
	}

	public void insertDocsUser(Map<String, Object> param) {
		insert(prefix + "insertDocsUser", param);
	}

	public void updateDocsUser(Map<String, Object> param) {
		try {
			int result = update(prefix + "updateDocsUser", param);
			System.out.println("result : " + result);
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	public void deleteDocsUser() {
		Map<String, Object> map = new HashMap<String, Object>();
		delete(prefix + "deleteDocsUser", map);
	}

	public void insertDocsAdmin() {
		Map<String, Object> map = new HashMap<String, Object>();
		insert(prefix + "insertDocsAdmin", map);
	}

	public void insertDocsOutuser() {
		Map<String, Object> map = new HashMap<String, Object>();
		insert(prefix + "insertDocsOutuser", map);
	}

	public void updateTeamLeader(Map<String, Object> param) {
		insert(prefix + "updateTeamLeader", param);
	}

	public String selectUserCdFromUserId(Map<String, Object> param) {
		return (String) obj(prefix + "selectUserCdFromUserId", param);
	}

	public Integer selectItnUserCount() {
		return (Integer) obj(prefix + "selectItnUserCount");
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectItnVendor() {
		return listNotUseSession(prefix + "selectItnVendor");
	}

	public void updateCompany(Map<String, Object> param) {
		update(prefix + "updateCompany", param);
	}

	public void insertCompany(Map<String, Object> param) {
		insert(prefix + "insertCompany", param);
	}

	public String selectCompanyCd() {
		return (String) obj(prefix + "selectCompanyCd");
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> selectDocsCompany(Map<String, Object> param) {
		return (Map<String, Object>) objNotUseSession(prefix + "selectDocsCompany", param);
	}

	public void updateDocsDept(Map<String, Object> param) {
		update(prefix + "updateDocsDept", param);
	}

	/**
	 * 방산결재가능자
	 * @param param
	 */
	public void insertProtectApprovalUser() {
		Map<String, Object> param = new HashMap<String, Object>();

		insert(prefix + "insertProtectApprovalUser", param);
	}

	/**
	 * 방산결재 가능자 수
	 * @return
	 */
	public Integer selectProtectApprovalUserCount() {
		return (Integer) obj(prefix + "selectProtectApprovalUserCount");
	}

	public List<RoleGroupVO> selectRoleGroupSetting(){
		return list(prefix + "selectRoleGroupSetting");
	}

	public void updateRoleGroup(RoleGroupVO param) {
		update(param.getQueryId(), param);
	}

	public void updateRoleGroupDept() {
		update(prefix + "updateRoleGroupDept", null);
	}

	public void updateRoleGroupUser() {
		update(prefix + "updateRoleGroupUser", null);
	}

	public void deleteCompanyPurchaser(Map<String, Object> param) {
		delete(prefix + "deleteCompanyPurchaser", param);
	}

	public void insertCompanyPurchaser(Map<String, Object> param) {
		delete(prefix + "insertCompanyPurchaser", param);
	}

	public String selectMigrationVendorYn() {
		Map<String, Object> param = new HashMap<String, Object>();

		return (String) obj(prefix + "selectMigrationVendorYn", param);
	}

	/**
	 * VENDOR 마이그레이션 여부 업데이트
	 * @param param
	 */
	public void updateMigrationVendorYn() {
		Map<String, Object> param = new HashMap<String, Object>();

		update(prefix + "updateMigrationVendorYn", param);
	}

}

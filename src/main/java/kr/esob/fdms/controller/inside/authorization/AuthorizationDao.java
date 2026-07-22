package kr.esob.fdms.controller.inside.authorization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.SearchComboInfoVO;
import kr.esob.fdms.commonlogic.combo.SearchComboParamVO;
import kr.esob.fdms.controller.inside.distribution.approval.ApprovalPopupParam;
import kr.esob.fdms.controller.inside.unregisted.request.DistributionRequestPopupParam;
import kr.esob.fdms.controller.login.UserVO;

@Repository
public class AuthorizationDao extends AbstractDao {

	private String prefix = "sql.Authorization.";

	@SuppressWarnings("unchecked")
	public List<SearchComboInfoVO> selectSearchCombo(SearchComboParamVO vo) {
		return list(vo.getQueryId(), vo);
	}


	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> selectCombo(Map<String, Object> paramMap) {
		return list(paramMap.get("queryId").toString(), paramMap);
	}


	public UserVO getPurchaseInfo(UserVO user) {
		return (UserVO) obj(prefix + "getPurchaseInfo", user);
	}


	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> getTeamLeader(Object param) {
		return list(prefix + "getTeamLeader", param);
	}


	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> getTeamLeaderList(CommonParam param) {
		return list(prefix + "getTeamLeaderList", param);
	}

	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> getTeamLeaderListById(CommonParam param) {
		return list(prefix + "getTeamLeaderListById", param);
	}


	public String getDefenseTeamLeaderUid() {
		return (String) obj(prefix + "getDefenseTeamLeaderUid", new HashMap<String, Object>());
	}


	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> selectDefenseTeamLeaderList() {
		return list(prefix + "selectDefenseTeamLeaderList");
	}


	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> getTeamLeader(DistributionRequestPopupParam param) {
		return list(prefix + "selectTeamLeader");
	}

	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> selectDeptComboListByBusinessArea(SearchComboParamVO vo){
		return list(prefix + "selectDeptComboListByBusinessArea", vo);
	}

	public List<SearchComboInfoVO> selectInsideUserComboByDept(SearchComboParamVO vo){
		return list(prefix + "selectInsideUserComboByDept", vo);
	}

	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> selectProductProtectUserList(String businessAreaCd) {
		return list(prefix + "selectProductProtectUserList", businessAreaCd);
	}

	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> selectDevelopmentProtectUserList() {
		return list(prefix + "selectDevelopmentProtectUserList");
	}

	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> selectSecurityUserList(){
		return list(prefix + "selectSecurityUserList");
	}

	@SuppressWarnings("unchecked")
	public List<String> selectSecurityUserByBusinessArea(Object obj){
		return list(prefix + "selectSecurityUserByBusinessArea", obj);
	}

	public UserVO selectPurchaserTeamLeader(Object obj) {
		return (UserVO) obj(prefix + "selectPurchaserTeamLeader", obj);
	}

	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> selectPassTargetCombo(Object obj){
		return list(prefix + "selectPassTargetCombo", obj);
	}


	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> selecTeamUserList(CommonParam param) {
		return list(prefix + "selecTeamUserList", param);
	}

	@SuppressWarnings("unchecked")
	public List<ComboInfoVO> selectVendorApprovalUser(Object obj) {
		return  list(prefix + "selectVendorApprovalUser", obj);
	}

	/**
	 * 권한 있으면 true
	 * @param vo
	 * @return
	 */
	public boolean checkProtectAuth(Object obj) {
		return ((Integer) obj(prefix + "selectProtectAuthCount", obj)) > 0 ? true : false;
	}

	/**
	 * 사용자코드로 부서명을 구한다.
	 * @param userCd
	 * @return
	 */
	public ComboInfoVO selectDept(Object obj) {
		return (ComboInfoVO) obj(prefix + "selectDept", obj);
	}

	/**
	 * 실제 결재자로 지정된 사용자만 구한다.
	 * @param obj
	 * @return
	 */
	public List<ComboInfoVO> selectOneUserCombo(String userCd) {
		return (List<ComboInfoVO>) list(prefix + "selectOneUserCombo", userCd);
	}
}

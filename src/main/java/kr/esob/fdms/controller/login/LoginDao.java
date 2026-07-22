package kr.esob.fdms.controller.login;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class LoginDao extends AbstractDao {
	private final String prefix = "sql.Login.";

	public UserVO getInUser(String userId) {
		return (UserVO) obj(prefix + "getInUser", userId);
	}

	public UserVO getOutUser(UserVO userVo) {
		return (UserVO) obj(prefix + "getOutUser", userVo);
	}

	@SuppressWarnings("unchecked")
	public List<RoleVO> getRoleCodeList(String roleGroup){
		return list(prefix + "getRoleCodeList", roleGroup);
	}

	public void updateLoginFailure(String userId) {
		update(prefix + "updateLoginFailure", userId);
	}

	public String selectUserId(UserVO userVo) {
		return (String) obj(prefix + "selectUserId", userVo);
	}

	public void updateLoginFailureOutside(String userId) {
		update(prefix + "updateLoginFailure", userId);
	}

	public void resetLoginCount(String userId) {
		update(prefix + "resetLoginCount", userId);
	}

	public void updateLastLoginDt(String userId, String clientIp) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("userId", userId);
		paramMap.put("clientIp", clientIp);
		update(prefix + "updateLastLoginDt", paramMap);
	}

	public int checkFailedCount(String userId) {
		return (Integer) obj(prefix + "checkFailedCount", userId);
	}

	public void updateLock(String userId) {
		update(prefix + "updateLock", userId);
	}

	public void updateUnlock(UserVO userVo) {
		update(prefix + "updateUnlock", userVo);
	}

	public List<UserVO> selectList(UserChangePopupVO param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(UserChangePopupVO param) {
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public String selectLastLoginIp(String userCd) {
		return (String) obj(prefix + "selectLastLoginIp", userCd);
	}

	public UserVO selectDeployUser(String userNm) {return (UserVO) obj(prefix + "selectDeployUser", userNm);}
}

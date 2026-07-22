package kr.esob.fdms.controller.inside.organizationmanage.insideuser;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.login.UserVO;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InsideuserDao extends AbstractDao {
	private String prefix = "sql.OrganizationmanageInsideuser.";


	@SuppressWarnings("unchecked")
	public List<InsideuserListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public String selectDeptCd(Object param) {
		return (String) obj(prefix + "selectDeptCd", param);
	}

	public void updateUnlock(UserVO userVo) {
		update(prefix + "updateUnlock", userVo);
	}

	public void resetPwd(UserVO userVo) {
		update(prefix + "resetPwd", userVo);
	}

	public void insertRegisterUserInfo(UserPopupParam param) {
		insert(prefix + "insertRegisterUserInfo", param);
		insert(prefix + "insertRegisterUserGroup", param);
//		insert(prefix + "insertRegisterDeptGroup", param);
	}

	public UserListVO selectUser(String userCd) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userCd", userCd);

		return (UserListVO) obj(prefix + "selectUser", param);
	}

	@Autowired
	private SqlSession sqlSession;
	public String checkUserId(String userId) {
		return sqlSession.selectOne(prefix + "checkUserId", userId);
	}
	public String checkUserNm(String userNm) {
		return sqlSession.selectOne(prefix + "checkUserNm", userNm);
	}
	public Integer checkEmail(String email) {
		return sqlSession.selectOne(prefix + "checkEmail", email);
	}

	public UserListVO getUserInfoByUserCd(String userCd) {
		return (UserListVO) obj(prefix + "getUserInfoByUserCd", userCd);
	}

	public void editUserInfo(UserPopupParam param) {
		update(prefix + "editUserInfo", param);
		update(prefix + "editUserGroup", param);
	}

	public void editUserInfo_resetPwd(UserPopupParam param) {
		update(prefix + "editUserInfo_resetPwd", param);
	}

}

package kr.esob.fdms.controller.inside.organizationmanage.outsideuser;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class OutsideuserDao extends AbstractDao {
	private String prefix = "sql.OrganizationmanageOutsideUser.";

	@SuppressWarnings("rawtypes")
	public List selectList(Object param) {
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param) {
		return (Integer) obj(prefix + "selectListCount", param);
	}

	public void insertUser(Object param) {
		insert(prefix + "insertUser", param);
	}

	public void updateUser(Object param) {
		update(prefix + "updateUser", param);
	}
	
	public void updateAppUserCd(Object param) {
		update(prefix + "updateAppUserCd", param);
	}



	public UserListVO selectUser(String userCd) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userCd", userCd);

		return (UserListVO) obj(prefix + "selectUser", param);
	}



	public String selectUserCd(Object param) {
		return (String) obj(prefix + "selectUserCd", param);
	}

	public void updateUnlockAccount(Object param) {
		update(prefix + "updateUnlockAccount", param);
	}

}

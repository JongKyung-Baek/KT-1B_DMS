package kr.esob.fdms.controller.inside.system.role;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.login.DeptVO;
import kr.esob.fdms.controller.login.UserVO;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RoleDao extends AbstractDao {
	private String prefix = "sql.Role.";

	/**
	 * 전체 부서 목록
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<DeptVO> selectDept(Object param) {
		return list(prefix + "selectDept", param);
	}

	public Integer selectDeptCount(Object param) {
		return (Integer) obj(prefix + "selectDeptCount", param);
	}

	@SuppressWarnings("unchecked")
	public List<UserVO> selectUser(Object param) {
		return list(prefix + "selectUser", param);
	}

	public Integer selectUserCount(Object param) {
		return (Integer) obj(prefix + "selectUserCount", param);
	}

	/**
	 * 권한 그룹의 멤버 ( 사용자 )
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserVO> selectGroupMemberInUser(RequestParam param) {
		return list(prefix + "selectGroupMemberInUser", param);
	}

	/**
	 * 권한 그룹의 멤버 ( 부서 )
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<DeptVO> selectGroupMemberInDept(RequestParam param) {
		return list(prefix + "selectGroupMemberInDept", param);
	}

	/**
	 * ROLE_GROUP 테이블에 데이터 추가
	 * @param param
	 */
	public void insertRoleGroup(RequestParam param) {
		insert(prefix + "insertRoleGroup", param);
	}

	/**
	 * ROLE_GROUP 테이블의 그룹명 변경
	 * @param param
	 */
	public void updateRoleGroup(RequestParam param) {
		update(prefix + "updateRoleGroup", param);
	}

	/**
	 * ROLE_GROUP 테이블의 사용여부를 N으로 변경
	 * @param groupCd
	 */
	public void deleteRoleGroup(String groupCd) {
		update(prefix + "deleteRoleGroup", groupCd);
	}

	/**
	 * 특정 그룹 정보를 구한다.
	 * @param param
	 * @return
	 */
	public RoleGroupVO selectRoleGroupInfo(RequestParam param) {
		return (RoleGroupVO) obj(prefix + "selectRoleGroupInfo", param);
	}

	/**
	 * group의 멤버를 삭제한다.
	 * @param groupCd
	 */
	public void deleteRoleGroupMember(String groupCd) {
		delete(prefix + "deleteRoleGroupMember", groupCd);
	}
	public void deleteRoleGroupMemberUserCd(List<String> userCd) {
		delete(prefix + "deleteRoleGroupMemberUserCd", userCd);
	}

	/**
	 * group의 멤버를 추가한다.
	 * @param param
	 */
	public int insertRoleGroupMember(GroupMemberVO param) {
		return (int)insert(prefix + "insertRoleGroupMember", param);
	}

	/**
	 * 관리자, 외부업체를 제외한 ROLE_GROUP
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<RoleGroupVO> selectRoleGroup() {
		Map<String, Object> param = new HashMap<>();

		return list(prefix + "selectRoleGroup", param);
	}

	public boolean checkDuplicatedRoleInsert(GroupMemberVO param) {
		return (boolean)obj(prefix + "checkDuplicatedRoleInsert", param);
	}

}

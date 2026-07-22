package kr.esob.fdms.controller.inside.system.roleassign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class RoleAssignDao extends AbstractDao {
	private String prefix = "sql.RoleAssign.";

	/**
	 * 관리자, 외부업체를 제외한 ROLE_GROUP
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<RoleGroupVO> selectRoleGroup() {
		Map<String, Object> param = new HashMap<>();

		return list(prefix + "selectRoleGroup", param);
	}

	/**
	 * 특정 그룹에 사용하는 메뉴 목록
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> selectRelRoleGroup(RequestParam param) {
		return list(prefix + "selectRelRoleGroup", param);
	}

	/**
	 * REL_ROLE_GROUP 테이블에 데이터가 존재하는지 여부. 존재한다면 true
	 * @param param
	 * @return
	 */
	public boolean existRelRoleGroup(RequestParam param) {
		Integer count = (Integer) obj(prefix + "selectRelRoleGroupCount", param);

		return count > 0 ? true : false;
	}

	/**
	 * 권한관계 등록
	 * @param param
	 */
	public void insertRelRoleGroup(RequestParam param) {
		insert(prefix + "insertRelRoleGroup", param);
	}

	public void insertRelRoleGroup(String groupCd, String roleCd) {
		Map<String, Object> map = new HashMap<>();
		map.put("groupCd", groupCd);
		map.put("roleCd", roleCd);

		insert(prefix + "insertRelRoleGroup", map);
	}

	/**
	 * 권한관계 삭제
	 * @param groupCd
	 * @param roleCd
	 */
	public void deleteRelRoleGroup(String groupCd, String roleCd) {
		Map<String, String> param = new HashMap<>();
		param.put("groupCd", groupCd);
		param.put("roleCd", roleCd);

		delete(prefix + "deleteRelRoleGroup", param);
	}

	/**
	 * 권한관계 삭제
	 * @param roleCd
	 */
	public void deleteRelRoleGroup(String roleCd) {
		Map<String, String> param = new HashMap<>();
		param.put("roleCd", roleCd);

		delete(prefix + "deleteRelRoleGroup", param);
	}

	/**
	 * 권한 정보
	 * @param param
	 * @return
	 */
	public RoleGroupVO selectRoleGroupInfo(RequestParam param) {
		return (RoleGroupVO) obj(prefix + "selectRoleGroupInfo", param);
	}

}

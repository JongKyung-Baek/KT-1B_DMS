package kr.esob.fdms.controller.inside.system.role;

import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.login.DeptVO;
import kr.esob.fdms.controller.login.UserVO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class RoleService {
	@Inject
	RoleDao dao;

	/**
	 * 권한 그룹의 멤버 ( 사용자 )
	 * @param param
	 * @return
	 */
	public List<UserVO> selectGroupMemberInUser(RequestParam param) {
		return dao.selectGroupMemberInUser(param);
	}

	/**
	 * 권한 그룹의 멤버 ( 부서 )
	 * @param param
	 * @return
	 */
	public List<DeptVO> selectGroupMemberInDept(RequestParam param) {
		return dao.selectGroupMemberInDept(param);
	}

	/**
	 * 전체 부서 목록
	 * @param param
	 * @return
	 */
	public List<DeptVO> selectDept(RequestParam param) {
		return dao.selectDept(param);
	}

	/**
	 * 권한 그룹을 저장한다.
	 * @since 2020-03-27
	 * @author younjh
	 * @param param
	 * @return
	 */
	public ResultVO saveRole(RequestParam param) {
		ResultVO result = new ResultVO();
		result.setSuccess(false);

		if("I".equals(param.getSaveFlag())) {
			dao.insertRoleGroup(param);
		}
		else if("U".equals(param.getSaveFlag())) {
			dao.updateRoleGroup(param);
		}
		else if("D".equals(param.getSaveFlag())) {
			dao.deleteRoleGroup(param.getGroupCd());
			dao.deleteRoleGroupMember(param.getGroupCd());
		}

		result.setSuccess(true);

		return result;
	}

	/**
	 * 그룹의 member를 저장한다.
	 * @param param
	 * @return
	 */
	public ResultVO saveRoleMember(RoleMemberSaveParam param) {
		ResultVO result = new ResultVO();
		result.setSuccess(false);

		// 부서와 사용자는 각각 한 개를 초과하는 권한을 가질 수 없음.
//		boolean deptCheck = checkRoleGroupMember(param.getGroupCd(), "DEPT", param.getAssignedDept());
//		boolean userCheck = checkRoleGroupMember(param.getGroupCd(), "USER", param.getAssignedUser());
//
//		System.out.println("deptCheck = " + deptCheck);
//		System.out.println("userCheck = " + userCheck);
//
//		if(deptCheck) {
//			result.setMessage("msg.duplicateRoleDept");
//			return result;
//		}
//		if(userCheck){
//			result.setMessage("msg.duplicateRoleUser");
//			return result;
//		}


		dao.deleteRoleGroupMember(param.getGroupCd());
		dao.deleteRoleGroupMemberUserCd(param.getAssignedUser());

		insertRoleGroupMember(param.getGroupCd(), "DEPT", param.getAssignedDept());
		insertRoleGroupMember(param.getGroupCd(), "USER", param.getAssignedUser());

		result.setSuccess(true);

		return result;
	}

	/**
	 * 그룹 멤버 추가
	 * @param memberVo
	 * @param groupType
	 * @param list
	 */
	private boolean insertRoleGroupMember(String groupCd, String groupType, List<String> list) {

		if(list.size() > 0) {
			GroupMemberVO memberVo = new GroupMemberVO();
			memberVo.setGroupCd(groupCd);
			memberVo.setGroupType(groupType);

			for(String memberCd : list) {
				memberVo.setMemberCd(memberCd);
				int insertResult = dao.insertRoleGroupMember(memberVo);
				if(insertResult==0){
					return false;
				}
			}
		}
		return true;
	}

	private boolean checkRoleGroupMember(String groupCd, String groupType, List<String> list) {
		boolean totalResult = true;

		if(list.size() > 0) {
			GroupMemberVO memberVo = new GroupMemberVO();
			memberVo.setGroupCd(groupCd);
			memberVo.setGroupType(groupType);


			for(String memberCd : list) {
				memberVo.setMemberCd(memberCd);
				System.out.println("memberCd = " + memberCd);
				boolean result = dao.checkDuplicatedRoleInsert(memberVo);
				if(!result) totalResult = false;
			}
		}
		else{
			totalResult = false;
		}
		System.out.println("totalResult : " + totalResult);
		return totalResult;
	}

	/**
	 * 특정 그룹 정보를 구한다.
	 * @param param
	 * @return
	 */
	public RoleGroupVO selectRoleGroupInfo(RequestParam param) {
		return dao.selectRoleGroupInfo(param);
	}

	/**
	 * 권한그룹 목록
	 * @return
	 */
	public List<RoleGroupVO> selectRoleGroup() {
		return dao.selectRoleGroup();
	}

}

package kr.esob.fdms.controller.inside.system.roleassign;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.message.Prop;
import kr.esob.fdms.commonlogic.result.ResultVO;

@Service
public class RoleAssignService {
	@Inject
	RoleAssignDao dao;
	@Inject
	Prop prop;

	/**
	 * 권한그룹 목록
	 * @return
	 */
	public List<RoleGroupVO> selectRoleGroup() {
		return dao.selectRoleGroup();
	}

	/**
	 * 특정권한그룹에서 사용하는 메뉴 목록
	 * @param param
	 * @return
	 */
	public List<String> selectRelRoleGroup(RequestParam param) {
		return dao.selectRelRoleGroup(param);
	}

	public ResultVO saveAssign(RequestParam param) {
		ResultVO result = new ResultVO();
		result.setSuccess(false);

		// START 예외처리
		if(null == param.getGroupCd() || "".equals(param.getGroupCd())) {
			result.setSuccess(false);
			result.setFailReason(prop.msg("msg.notSelectedRoleGroup"));
		}
		// END 예외처리

		for(RequestParam one : param.getList()) {
			one.setGroupCd(param.getGroupCd());

			// START 예외처리
			// roleCd가 존재하지 않는다면 pass
			if(null == one.getRoleCd() || "".equals(one.getRoleCd())) {
				continue;
			}
			// END 예외처리


			if("Y".equals(one.getSelectedYn())) {
				// 체크되었다면 insert

				if(!dao.existRelRoleGroup(one)) {
					// 존재하지 않는다면 insert
					dao.insertRelRoleGroup(one);
				}
			}
			else {
				// 체크해제되었다면 delete
				dao.deleteRelRoleGroup(one.getGroupCd(), one.getRoleCd());
			}
		}

		result.setSuccess(true);
		return result;
	}

	/**
	 * 권한정보
	 * @param param
	 * @return
	 */
	public RoleGroupVO selectRoleGroupInfo(RequestParam param) {
		return dao.selectRoleGroupInfo(param);
	}
}

package kr.esob.fdms.controller.inside.system.roleassign;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.fdms.commonlogic.message.Prop;
import kr.esob.fdms.commonlogic.result.ResultVO;

@Service
public class RoleAssignService {
	@Inject
	RoleAssignDao dao;
	@Inject
	Prop prop;

	public List<RoleGroupVO> selectRoleGroup() {
		return dao.selectRoleGroup();
	}

	public List<String> selectRelRoleGroup(RequestParam param) {
		return dao.selectRelRoleGroup(param);
	}

	@Transactional
	public ResultVO saveAssign(RequestParam param) {
		ResultVO result = new ResultVO();
		result.setSuccess(false);

		if(param == null || param.getGroupCd() == null
				|| param.getGroupCd().trim().isEmpty()
				|| dao.selectRoleGroupInfo(param) == null) {
			result.setFailReason(prop.msg("msg.notSelectedRoleGroup"));
			return result;
		}

		if(param.getList() == null || param.getList().isEmpty()) {
			result.setFailReason(prop.msg("msg.noSelectData"));
			return result;
		}

		// Collapse duplicate client rows so one role has one deterministic result.
		Map<String, RequestParam> requestedRoles = new LinkedHashMap<>();
		for(RequestParam item : param.getList()) {
			if(item == null || item.getRoleCd() == null
					|| item.getRoleCd().trim().isEmpty()) {
				continue;
			}
			item.setRoleCd(item.getRoleCd().trim());
			item.setGroupCd(param.getGroupCd());
			requestedRoles.put(item.getRoleCd(), item);
		}

		if(requestedRoles.isEmpty()) {
			result.setFailReason(prop.msg("msg.noSelectData"));
			return result;
		}

		for(RequestParam item : requestedRoles.values()) {
			if("Y".equals(item.getSelectedYn())) {
				if(!dao.existRelRoleGroup(item)) {
					dao.insertRelRoleGroup(item);
				}
			}
			else {
				dao.deleteRelRoleGroup(item.getGroupCd(), item.getRoleCd());
			}
		}

		result.setSuccess(true);
		return result;
	}

	public RoleGroupVO selectRoleGroupInfo(RequestParam param) {
		return dao.selectRoleGroupInfo(param);
	}
}

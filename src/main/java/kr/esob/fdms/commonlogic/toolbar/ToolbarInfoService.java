package kr.esob.fdms.commonlogic.toolbar;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.value.SessionValue;
import kr.esob.fdms.controller.login.RoleVO;

@Service
public class ToolbarInfoService{

	@Inject
	ToolbarInfoDao dao;


	@Inject
	SessionValue sessionValue;

	public List<ToolbarInfoVO> selectToolbarInfo(String toolbarId) {
		ToolbarParamVO vo = new ToolbarParamVO();
		List<String> roleCdList = new ArrayList<String>();
		for(Object roleObj : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
			if(roleObj instanceof RoleVO) {
				RoleVO roleVo = (RoleVO) roleObj;
				String menuType = roleVo.getMenuType();
				if("D".equalsIgnoreCase(menuType) ||
						"R".equalsIgnoreCase(menuType) ||
						"E".equalsIgnoreCase(menuType)) {
					roleCdList.add(roleVo.getRoleCd());
				}
			}
		}
		vo.setToolbarId(toolbarId);
		vo.setRoleCdList(roleCdList);
		return dao.selectToolbarInfoList(vo);
	}

}

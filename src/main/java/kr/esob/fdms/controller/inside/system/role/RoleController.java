package kr.esob.fdms.controller.inside.system.role;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.system.menu.MenuService;
import net.sf.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/inside/system/role")
public class RoleController extends AbstractController {
	@Inject
	RoleService service;
	@Inject
	MenuService menuService;
	@Inject
	RoleDeptService deptService;
	@Inject
	RoleUserService userService;

	@RequestMapping("/")
	public String inside(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);

		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarSystemRole")));

		model.addAttribute("gridRoleDept", JSONArray.fromObject(gridService.selectGridInfo("gridRoleDept")));
		model.addAttribute("gridRoleDeptAssigned", JSONArray.fromObject(gridService.selectGridInfo("gridRoleDeptAssigned")));
		model.addAttribute("gridRoleUser", JSONArray.fromObject(gridService.selectGridInfo("gridRoleUser")));
		model.addAttribute("gridRoleUserAssigned", JSONArray.fromObject(gridService.selectGridInfo("gridRoleUserAssigned")));

		return "inside/system/role/roleSide";
	}

	@RequestMapping(value="/deptList")
	public @ResponseBody GridResultVO deptList(RequestParam param) throws Exception {
		GridResultVO result = commonSelectList(param, deptService);
		return result;
	}

	@RequestMapping(value="/userList")
	public @ResponseBody GridResultVO userList(RequestParam param) throws Exception {
		GridResultVO result = commonSelectList(param, userService);
		return result;
	}

	@RequestMapping({"/roleAddPopup", "/roleModPopup"})
	public String addGroupPopup(Model model, RequestParam param, HttpServletRequest request) throws JsonProcessingException {

		String saveFlag = (null == param.getGroupCd() || "".equals(param.getGroupCd())) ? "I" : "U";
		RoleGroupVO group = null;

		model.addAttribute("saveFlag", saveFlag);

		if("I".equals(saveFlag)) {
			// 등록일 경우
			group = new RoleGroupVO();
		}
		else {
			group = service.selectRoleGroupInfo(param);
		}

		model.addAttribute("vo", group);

		return "inside/system/role/rolePopup";
	}

	@RequestMapping(value="/saveRoleGroup", method=RequestMethod.POST)
	public @ResponseBody ResultVO saveAssign(@RequestBody RequestParam param) throws Exception {
		return service.saveRole(param);
	}

	@RequestMapping(value="/saveRoleGroupMember", method=RequestMethod.POST)
	public @ResponseBody ResultVO saveRoleGroupMember(@RequestBody RoleMemberSaveParam param) throws Exception {
		return service.saveRoleMember(param);
	}

	@RequestMapping(value="/getAssignedDept", method=RequestMethod.POST)
	public @ResponseBody String getAssignedDept(@RequestBody RequestParam param) throws Exception {
		return JSONArray.fromObject(service.selectGroupMemberInDept(param)).toString();
	}

	@RequestMapping(value="/getAssignedUser", method=RequestMethod.POST)
	public @ResponseBody String getAssignedUser(@RequestBody RequestParam param) throws Exception {
		return JSONArray.fromObject(service.selectGroupMemberInUser(param)).toString();
	}

	/**
	 * 권한 그룹 목록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/getRoleGroupList", method=RequestMethod.POST)
	public @ResponseBody List<RoleGroupVO> getRoleGroupList() throws Exception {
		return service.selectRoleGroup();
	}
}

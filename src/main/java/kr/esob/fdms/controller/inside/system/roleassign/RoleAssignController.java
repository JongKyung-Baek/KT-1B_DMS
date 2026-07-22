package kr.esob.fdms.controller.inside.system.roleassign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.tree.TreeVO;
import kr.esob.fdms.controller.inside.system.menu.MenuService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/inside/system/roleassign")
public class RoleAssignController extends AbstractController {
	@Inject
	RoleAssignService service;
	@Inject
	MenuService menuService;

	@RequestMapping("/")
	public String inside(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);

		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarSystemRoleAssign")));

		return "inside/system/roleassign/roleSide";
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

	/**
	 * 권한그룹에서 사용할 메뉴 tree
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/getAssignedMenuList", method=RequestMethod.POST)
	public @ResponseBody Map<String, Object> getMenuList(@RequestBody RequestParam param) throws Exception {
		List<TreeVO> menuList = menuService.selectTree(param.getAuthSite());
		List<String> selectedValue = service.selectRelRoleGroup(param);

		Map<String, String> selectedNode = new HashMap<String, String>();

		for(String v : selectedValue) {
			selectedNode.put(v, "Y");
		}

		Map<String, Object> result = new HashMap<>();
		result.put("menuList", menuList);
		result.put("selectedValue", JSONObject.fromObject(selectedNode));

		return result;
	}

	@RequestMapping(value="/saveAssign", method=RequestMethod.POST)
	public @ResponseBody ResultVO saveAssign(@RequestBody RequestParam param) throws Exception {
		return service.saveAssign(param);
	}
}

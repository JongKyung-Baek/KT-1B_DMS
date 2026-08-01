package kr.esob.tdms.controller.general.system.menu;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.tdms.commonlogic.message.Prop;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.commonlogic.value.Constant;
import net.sf.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * 메뉴관리
 * @author younjh
 *
 */
@Controller
@RequestMapping("/general/system/menu")
public class MenuController extends AbstractController {

	@Inject
	MenuService service;
	@Inject
	Prop menuMessage;

	@RequestMapping("/")
	public String index(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("toolbarInfo",
				JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarSystemMenu")));

		return "general/system/menu/menuList";
	}

	/**
	 * 메뉴 등록, 수정
	 * @param model
	 * @param param
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping({"/menuAddPopup", "/menuModPopup"})
	public String menuPopup(Model model, MenuSaveRequestParam param, HttpServletRequest request) {
		MenuVO menuVo = null;
		String saveFlag = request.getRequestURI().indexOf("menuAddPopup") > -1 ? "I" : "U";

		if(Constant.ROOT_MENU_CD.equals(param.getMenuCd())) {
			// ROOT를 클릭했을 경우
			menuVo = new MenuVO();
			menuVo.setParentMenuCd(MenuService.PORTAL_ROOT_CD);
			menuVo.setParentMenuNm(menuMessage.msg("label.allMenus"));
		}
		else if(request.getRequestURI().indexOf("menuAddPopup") > -1) {
			// 등록일 경우 param으로 넘어온 메뉴가 부모메뉴임
			MenuVO tmp = service.selectMenuInfo(param);
			menuVo = new MenuVO();
			menuVo.setParentMenuCd(tmp.getMenuCd());
			menuVo.setParentMenuNm(tmp.getMenuNm());
		}
		else {
			menuVo = service.selectMenuInfo(param);
		}

		model.addAttribute("saveFlag", saveFlag);
		model.addAttribute("menuVo", menuVo);

		return "general/system/menu/menuPopup";
	}

	@RequestMapping(value="/saveMenu", method=RequestMethod.POST)
	public @ResponseBody ResultVO saveMenu(@RequestBody MenuSaveRequestParam param) throws Exception {
		return service.saveMenu(param);
	}

	@RequestMapping(value="/saveMenuSort", method=RequestMethod.POST)
	public @ResponseBody ResultVO saveMenuSort(@RequestBody SortRequestParam param) throws Exception {
		return service.saveMenuSort(param);
	}

	@RequestMapping(value="/getTreeList", method=RequestMethod.POST)
	public @ResponseBody JSONArray getTreeList(@RequestBody RequestParam param) throws Exception {
		return JSONArray.fromObject(service.selectAdminTree());
	}
}


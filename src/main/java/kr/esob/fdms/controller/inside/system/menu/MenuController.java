package kr.esob.fdms.controller.inside.system.menu;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.value.Constant;
import net.sf.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 메뉴관리
 * @author younjh
 *
 */
@Controller
@RequestMapping("/inside/system/menu")
public class MenuController extends AbstractController {

	@Inject
	MenuService service;

	@Inject
	ComboService comboService;

	@RequestMapping("/")
	public String inside(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("insideToolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarSystemInsideMenu")));
		model.addAttribute("outsideToolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarSystemOutsideMenu")));

		ComboInfoVO combo = new ComboInfoVO();
		combo.setComboCd("authSite");
		model.addAttribute("authSite", comboService.selectComboList(combo));
		//model.addAttribute("selectedNodeList", JSONObject.fromObject(selectedNode));

		return "inside/system/menu/menuList";
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
			menuVo.setParentMenuCd(param.getMenuCd());
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
		model.addAttribute("authSite", param.getAuthSite());
		model.addAttribute("menuVo", menuVo);

		List<ComboInfoVO> comboList = comboService.selectComboList("authSite");
		List<ComboInfoVO> resultComboList = new ArrayList<>();


		for(ComboInfoVO combo : comboList) {
			if(combo.getComboVal().equals(param.getAuthSite()) || combo.getComboVal().equals("B")) {
				resultComboList.add(combo);
			}
		}

		model.addAttribute("authSite", resultComboList);

		return "inside/system/menu/menuPopup";
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
		return JSONArray.fromObject(service.selectTree(param.getAuthSite()));
	}
}


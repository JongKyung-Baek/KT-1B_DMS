package kr.esob.fdms.controller.outside.user.status;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.controller.outside.user.information.InformationListParam;
import kr.esob.fdms.controller.outside.user.information.InformationService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/outside/user/status")
public class StatusController extends AbstractController {
//	@Inject
//	InformationService service;
	@Inject
	StatusService service;



	@Inject
	ComboService comboService;

	@RequestMapping(value="/")
	public String home(Model model) throws JsonProcessingException {
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutsideUserStatus")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutsideUserStatus")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutsideUserStatusList")));

		return "outside/user/status/statusList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(StatusListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/statusPopup")
	public String statusPopup(Model model, StatusListParam param) throws Exception {
		ComboInfoVO combo = new ComboInfoVO();
		combo.setComboCd("userRequestType");
		model.addAttribute("userRequestType", comboService.selectComboList(combo));

		combo.setComboCd("informationCr");
		model.addAttribute("informationCr", comboService.selectComboList(combo));

		combo.setComboCd("informationProtect");
		model.addAttribute("informationProtect", comboService.selectComboList(combo));

		model.addAttribute("requestNo", param.getRequestNo());

		if (!param.getRequestNo().isEmpty()) {
			model.addAttribute("DetailInfo", JSONObject.fromObject(service.selectInformationDetailInfo(param)));
		}
		return "outside/user/status/statusPopup";
	}
}

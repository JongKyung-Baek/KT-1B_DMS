package kr.esob.fdms.controller.inside.cr.history;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.controller.inside.cr.CrParam;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/inside/cr/history")
public class HistoryController extends AbstractController {
	@Inject
	HistoryService service;

	@Inject
	ComboService comboService;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formCrHistory")));
		model.addAttribute("toolbarInfo", "");
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridCrHistoryList")));

		return "inside/cr/history/historyList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(HistoryListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping(value="/historyPopup")
	public String acceptancePopup(Model model, CrParam param) {
		model.addAttribute("formData", JSONObject.fromObject(service.selectHistoryInfo(param)));
		model.addAttribute("popupInfo", "HISTORY");
		return "inside/cr/commonInsideCrPopup";
	}
}

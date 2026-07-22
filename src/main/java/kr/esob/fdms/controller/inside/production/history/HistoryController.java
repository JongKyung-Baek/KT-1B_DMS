package kr.esob.fdms.controller.inside.production.history;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/inside/production/history")
public class HistoryController extends AbstractController {
	@Inject
	HistoryService service;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formProductionHistory")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarProductionHistory")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionHistoryList")));

		return "inside/production/history/historyList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(HistoryListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/historyDetailPopup")
	public String historyDetailPopup(Model model, HistoryDetailParam param) {
		model.addAttribute("gridProductionInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionHistoryProduction")));
		model.addAttribute("gridRequestDetailInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionHistoryRequestDetail")));
		model.addAttribute("gridDetailInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionHistoryDetailInfo")));
		model.addAttribute("historyDetailInfo", JSONObject.fromObject(service.selectHistoryDetailInfo(param)));
		return "inside/production/history/historyDetailPopup";
	}

}

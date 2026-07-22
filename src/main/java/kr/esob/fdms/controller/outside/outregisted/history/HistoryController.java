package kr.esob.fdms.controller.outside.outregisted.history;

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

@Controller
@RequestMapping("/outside/outregisted/history")
public class HistoryController extends AbstractController {
	@Inject
	HistoryService service;

	@RequestMapping("/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutregistedHistory")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutregistedHistory")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutregistedHistoryList")));

		return "outside/outregisted/history/historyList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(HistoryListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}
}

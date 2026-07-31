package kr.esob.fdms.controller.inside.distribution.oldhistory;


import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import net.sf.json.JSONArray;

/**
 * 내부사용자 이력조회
 * @author younjh
 *
 */
@Controller
@RequestMapping("/inside/distribution/oldhistory")
public class HistoryController extends AbstractController {
	@Inject
	HistoryService service;

	@RequestMapping(value="/")
	public String home(Model model) throws JsonProcessingException {
		String formId = "formDistributionOldHistory";
		String gridId = "gridDistributionOldHistoryList";
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo(formId)));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo(gridId)));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarDistributionOldHistory")));
		model.addAttribute("formId", formId);
		model.addAttribute("gridId", gridId);


		return "inside/distribution/oldhistory/historyList";
	}
	
	@PostMapping(value="/destroyOldHistory", produces="application/json;charset=UTF-8")
	public @ResponseBody ResultVO destroyOldHistory(@RequestBody HistoryListParam param) {
		return service.destroyOldHistory(param);
	}
	

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(HistoryListParam param) throws Exception {
		return commonSelectList(param, service);
	}
}

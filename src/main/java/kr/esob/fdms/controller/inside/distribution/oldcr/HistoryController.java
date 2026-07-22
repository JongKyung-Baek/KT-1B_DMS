package kr.esob.fdms.controller.inside.distribution.oldcr;


import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import net.sf.json.JSONArray;

/**
 * (구) ECR이력
 * @author younjh
 *
 */
@Controller
@RequestMapping("/inside/distribution/oldcrhistory")
public class HistoryController extends AbstractController {
	@Inject
	HistoryService service;

	@RequestMapping(value="/")
	public String home(Model model) throws JsonProcessingException {
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOldCrHistory")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOldCrHistory")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOldCrHistoryList")));

		return "inside/distribution/oldcrhistory/crHistoryList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(HistoryListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}
}

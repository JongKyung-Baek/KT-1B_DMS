package kr.esob.fdms.controller.outside.cr.oldhistory;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.controller.inside.distribution.oldcr.HistoryListParam;
import kr.esob.fdms.controller.inside.distribution.oldcr.HistoryService;
import net.sf.json.JSONArray;

/**
 * (구) ECR이력
 * @author younjh
 *
 */
@Controller
@RequestMapping("/outside/cr/oldHistory")
public class HistoryController extends AbstractController {
	@Inject
	HistoryService service;

	/**
	 * 내부사요자용과 검색 조건만 다름.
	 * @param model
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value="/")
	public String home(Model model) throws JsonProcessingException {
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutsideOldCrHistory")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOldCrHistory")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOldCrHistoryList")));

		return "outside/cr/oldhistory/crHistoryList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(HistoryListParam param) throws Exception {
		param.setOutUserYn("Y");
		GridResultVO result = commonSelectList(param, service);
		return result;
	}
}

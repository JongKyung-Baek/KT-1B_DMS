package kr.esob.fdms.controller.outside.unregisted.status;

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
@RequestMapping("/outside/unregisted/status")
public class StatusController extends AbstractController {
	@Inject
	StatusService service;

	@RequestMapping("/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutsideUnregistedStatus")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutsideUnregistedStatus")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutsideUnregistedStatusList")));

		return "outside/unregisted/status/statusList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(StatusListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}
}

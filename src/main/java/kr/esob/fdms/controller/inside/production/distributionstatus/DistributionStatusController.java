package kr.esob.fdms.controller.inside.production.distributionstatus;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.controller.inside.production.history.HistoryListParam;
import kr.esob.fdms.controller.inside.production.history.HistoryService;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/inside/production/distributionstatus")
public class DistributionStatusController extends AbstractController {
	@Inject
	DistributionStatusService service;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formProductionDistributionStatus")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarProductionDistributionStatus")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionDistributionStatusList")));

		return "inside/production/distributionstatus/distributionStatusList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(DistributionStatusListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}
}

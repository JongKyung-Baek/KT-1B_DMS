package kr.esob.tdms.controller.general.production.distributionstatus;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.tdms.commonlogic.grid.GridResultVO;
import kr.esob.tdms.controller.general.production.history.HistoryListParam;
import kr.esob.tdms.controller.general.production.history.HistoryService;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/general/production/distributionstatus")
public class DistributionStatusController extends AbstractController {
	@Inject
	DistributionStatusService service;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formProductionDistributionStatus")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarProductionDistributionStatus")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionDistributionStatusList")));

		return "general/production/distributionstatus/distributionStatusList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(DistributionStatusListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}
}

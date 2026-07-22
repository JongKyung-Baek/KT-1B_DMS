package kr.esob.fdms.controller.inside.production.acceptance;


import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/inside/production/acceptance")
public class AcceptanceController extends AbstractController {

	@Inject
	AcceptanceService service;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formProductionAcceptance")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarProductionAcceptance")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionAcceptance")));
		return "inside/production/acceptance/acceptanceList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(AcceptanceListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}


	@RequestMapping(value="/acceptancePopup")
	public String acceptancePopup(AcceptancePopupParam param, HttpServletRequest request, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionAcceptancePopupList")));
		model.addAttribute("requestNo", param.getRequestNo());
		model.addAttribute("objectType", param.getObjectType());
//		List<AcceptancePopupParam> list = JSONArray.toList(JSONArray.fromObject(request.getParameter("list")), AcceptancePopupParam.class);
//		AcceptancePopupParam param = new AcceptancePopupParam();
//		param.setList(list);
//		model.addAttribute("listCount", service.selectPopupListCount(param));
		return "inside/production/acceptance/acceptancePopup";
	}

	@RequestMapping("/selectPopupList")
	public @ResponseBody GridResultVO selectPopupList(AcceptancePopupParam param) throws Exception {
//		List<AcceptancePopupParam> list = JSONArray.toList(JSONArray.fromObject(request.getParameter("list")), AcceptancePopupParam.class);
//		AcceptancePopupParam param = new AcceptancePopupParam();
//		param.setList(list);
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectPopupList(param));
		return result;
	}


	@RequestMapping("/saveAcceptance")
	public @ResponseBody ResultVO saveAcceptance(@RequestBody AcceptancePopupParam param) throws Exception {
		return service.saveAcceptance(param);
	}

}

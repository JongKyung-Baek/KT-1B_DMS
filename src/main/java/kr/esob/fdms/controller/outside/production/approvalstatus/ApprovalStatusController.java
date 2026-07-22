package kr.esob.fdms.controller.outside.production.approvalstatus;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.value.ApprovalStatusPopupInfo;
import kr.esob.fdms.controller.outside.commondestroystatus.CommonDestroyStatusService;
import kr.esob.fdms.controller.outside.commondestroystatus.DestroyFileVO;
import kr.esob.fdms.controller.outside.commondestroystatus.DestroyStatusParam;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/outside/production/approvalStatus")
public class ApprovalStatusController extends AbstractController {
	@Inject
	ApprovalStatusService service;

	@Inject
	CommonDestroyStatusService commonDestroyStatusService;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutsideProductionApprovalStatus")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutsideProductionApprovalStatus")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutsideProductionApprovalStatusList")));

		return "outside/production/approvalStatus/approvalStatusList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(ApprovalStatusListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/destroyStatusPopup")
	public String destroyStatusPopup(Model model, DestroyStatusParam param) throws JsonProcessingException {
		model.addAttribute("info", JSONObject.fromObject(commonDestroyStatusService.selectDestroyStatus(param)));
		model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(ApprovalStatusPopupInfo.DRAWING));
		return "outside/commondestroy/commonDestroyStatusPopup";

	}

	@RequestMapping(value="/destroyFileDown")
	public void destroyFileDown(DestroyFileVO param, HttpServletResponse response) throws Exception{
		commonDestroyStatusService.destroyFileDown(param, response);
	}
}

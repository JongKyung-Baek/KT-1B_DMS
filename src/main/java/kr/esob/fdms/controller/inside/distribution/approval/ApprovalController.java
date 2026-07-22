package kr.esob.fdms.controller.inside.distribution.approval;


import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/inside/distribution/approval")
public class ApprovalController extends AbstractController {
	@Inject
	ApprovalService service;

	@Inject
	AuthorizationService authorizationService;
	
	
	

	@RequestMapping(value="/approvalTab")
	public String approvalTab(Model model, CommonHomeParam param) throws JsonProcessingException {
		return "/inside/distribution/approval/approvalTab";
	}

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formDistributionApproval")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarDistributionApproval")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDistributionApproval")));

		return "/inside/distribution/approval/approvalList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(ApprovalListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping(value="/passPopup")
	public String passPopup(ApprovalPopupParam param, Model model) throws JsonProcessingException {
		model.addAttribute("list", JSONArray.fromObject(authorizationService.selectPassTargetCombo(param)));
		model.addAttribute("requestNo", param.getRequestNo());
		return "/inside/distribution/approval/passPopup";
	}

}

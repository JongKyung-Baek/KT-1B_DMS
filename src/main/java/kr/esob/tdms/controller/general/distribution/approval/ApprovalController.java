package kr.esob.tdms.controller.general.distribution.approval;


import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.tdms.commonlogic.grid.GridResultVO;
import kr.esob.tdms.controller.general.authorization.AuthorizationService;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/general/distribution/approval")
public class ApprovalController extends AbstractController {
	@Inject
	ApprovalService service;

	@Inject
	AuthorizationService authorizationService;
	
	
	

	@RequestMapping(value="/approvalTab")
	public String approvalTab(Model model, CommonHomeParam param) throws JsonProcessingException {
		return "/general/distribution/approval/approvalTab";
	}

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formDistributionApproval")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarDistributionApproval")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDistributionApproval")));

		return "/general/distribution/approval/approvalList";
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
		return "/general/distribution/approval/passPopup";
	}

}

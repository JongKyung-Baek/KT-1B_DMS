package kr.esob.fdms.controller.inside.organizationmanage.approval;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.outside.user.information.InformationListParam;
import kr.esob.fdms.controller.outside.user.information.InformationService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/inside/organizationmanage/approval")
public class ApprovalController extends AbstractController {

	@Inject
	InformationService serviceInfomation;

	@Inject
	ApprovalService service;

	@Inject
	ComboService comboService;

	@RequestMapping(value= "/")
	public String Hello(Model model) {
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOrganizationmanageApproval")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOrganizationmanageApproval")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOrganizationmanageApprovalList")));
		return "inside/organizationmanage/approval/approvalList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(ApprovalListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/approvalPopup")
	public String approvalPopup(Model model, ApprovalListParam param) throws JsonProcessingException {
		ComboInfoVO combo = new ComboInfoVO();
		combo.setComboCd("userRequestType");
		model.addAttribute("userRequestType", comboService.selectComboList(combo));

		combo.setComboCd("informationCr");
		model.addAttribute("informationCr", comboService.selectComboList(combo));

		combo.setComboCd("informationProtect");
		model.addAttribute("informationProtect", comboService.selectComboList(combo));

		model.addAttribute("requestNo", param.getRequestNo());

		model.addAttribute("DetailInfo", JSONObject.fromObject(service.selectDetailInfo(param)));

		model.addAttribute("statusCd", param.getStatusCd());

		return "inside/organizationmanage/approval/approvalPopup";
	}

	@RequestMapping(value = "/approval")
	public @ResponseBody ResultVO approval(@RequestBody ApprovalListParam param) throws Exception {
		return service.approvalUser(param);
	}

	@RequestMapping(value = "/reject")
	public @ResponseBody ResultVO reject(@RequestBody ApprovalListParam param) throws Exception {
		return service.rejectUser(param);
	}
	
	@RequestMapping(value = "/venderUser")
	public @ResponseBody List<ComboInfoVO> venderUser(@RequestBody ApprovalListParam param) throws Exception {
		return service.venderUser(param);
	}

}

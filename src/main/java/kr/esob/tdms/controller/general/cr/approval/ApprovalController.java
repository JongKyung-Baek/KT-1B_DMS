package kr.esob.tdms.controller.general.cr.approval;

import java.io.IOException;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.tdms.commonlogic.combo.ComboDao;
import kr.esob.tdms.commonlogic.combo.ComboService;
import kr.esob.tdms.commonlogic.grid.GridResultVO;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.general.cr.CommonCrService;
import kr.esob.tdms.controller.general.cr.CrParam;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/general/cr/approval")
public class ApprovalController extends AbstractController {
	@Inject
	ApprovalService service;

	@Inject
	CommonCrService commonCrService;

	@Inject
	ComboService comboService;


	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formCrApproval")));
		model.addAttribute("toolbarInfo", "");
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridCrApprovalList")));

		return "general/cr/approval/approvalList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(ApprovalListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping(value="/approvalPopup")
	public String acceptancePopup(Model model, CrParam param) {
		model.addAttribute("formData", JSONObject.fromObject(service.selectApprovalInfo(param)));
		model.addAttribute("popupInfo", "APPROVAL");
		return "general/cr/commonInsideCrPopup";
	}


	@PostMapping(value="/approve")
	public @ResponseBody ResultVO approve(@RequestBody CrParam param) throws IOException {
		return service.approve(param);
	}

	@PostMapping(value="/approvalReject")
	public @ResponseBody ResultVO approvalReject(@RequestBody CrParam param) {
		return service.approvalReject(param);
	}
}

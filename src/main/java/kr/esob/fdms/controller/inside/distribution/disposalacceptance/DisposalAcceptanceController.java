package kr.esob.fdms.controller.inside.distribution.disposalacceptance;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import kr.esob.fdms.controller.outside.commondestroystatus.DestroyFileVO;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/inside/distribution/disposalacceptance")
public class DisposalAcceptanceController extends AbstractController {

	@Inject
	DisposalAcceptanceService service;


	@Inject
	ComboService comboService;

	@Inject
	AuthorizationService authService;

	@Inject
	AuthorizationDao authorizationDao;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formDisposalAcceptance")));
		model.addAttribute("toolbarInfo", "");
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDisposalAcceptanceList")));

		return "inside/distribution/disposalacceptance/acceptanceList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(DisposalAcceptanceListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/approvalPopup")
	public String approvalPopup(Model model, DisposalAcceptanceParam param) {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDisposalAcceptancePopup")));
		model.addAttribute("fileList", service.selectDisposalFileList(param));
		model.addAttribute("formData", service.selectDisposalInfo(param));
		return "inside/distribution/disposalacceptance/approvalPopup";
	}

	@RequestMapping("/selectPopupList")
	public @ResponseBody GridResultVO selectPopupList(DisposalAcceptanceParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectPopupList(param));
		return result;
	}

	@RequestMapping("/fileDownload")
	public void fileDownload(DestroyFileVO param, HttpServletResponse response) throws Exception {
		service.fileDownload(param, response);
	}

	@RequestMapping("/saveApproval")
	public @ResponseBody ResultVO saveApproval(@RequestBody DisposalAcceptanceParam param) {
		ResultVO resultVo = service.saveApproval(param);
		return resultVo;
	}

}

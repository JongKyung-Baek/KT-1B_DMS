package kr.esob.fdms.controller.inside.production.disposalapproval;

import javax.inject.Inject;

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
@RequestMapping("/inside/production/disposalApproval")
public class DisposalApprovalController extends AbstractController {
	@Inject
	DisposalApprovalService service;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formProductionDisposalApproval")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarProductionDisposalApproval")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionDisposalApprovalList")));

		return "inside/production/disposalapproval/disposalApprovalList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(DisposalApprovalListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	/**
	 * 생산기술자료 폐기 승인/반려 팝업
	 * @param param
	 * @param model
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value="/disposalApprovalPopup")
	public String disposalApprovalPopup(DisposalApprovalPopupParam param, Model model) throws JsonProcessingException {
		model.addAttribute("data", service.getDestroyRequestInfo(param));
		return "inside/production/disposalapproval/disposalApprovalPopup";
	}

	/**
	 * 생산기술자료 폐기 승인/반려
	 * @param param
	 * @param model
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value="/destroyApproval")
	public @ResponseBody ResultVO destroyApproval(@RequestBody DisposalApprovalPopupParam param, Model model) throws JsonProcessingException {
		return service.destroyApproval(param);
	}


}

package kr.esob.fdms.controller.outside.outregisted.requeststatus;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import net.sf.json.JSONArray;

@Controller                         
@RequestMapping("/outside/outregisted/requeststatus")
public class RequestStatusController extends AbstractController {
	@Inject
	RequestStatusService service;

	@Inject
	ComboService comboService;

	@Inject
	AuthorizationService authService;

	@RequestMapping("/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutregistedRequestStatus")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutregistedRequestStatus")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutregistedRequestStatusList")));

		return "outside/outregisted/requeststatus/requestStatusList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(RequestStatusListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/requestStatusPopup")
	public String requestStatusPopup(RequestStatusPopupParam param, Model model) {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutregistedRequestStatusPopup")));
		//미등록자료 배포요청 정보
		model.addAttribute("data", service.getRequestInfo(param));

		model.addAttribute("requestNo", param.getRequestNo());

		return "outside/outregisted/requeststatus/requestStatusPopup";
	}

	@RequestMapping("/selectPopupList")
	public @ResponseBody GridResultVO selectPopupList(RequestStatusListParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectPopupList(param));

		return result;
	}
	
	@RequestMapping("/deleteRequestNo")
	public @ResponseBody ResultVO deleteRequestNo(@RequestBody RequestStatusPopupParam param) throws Exception {
		return service.deleteRequestNo(param);
	}

}

package kr.esob.fdms.controller.inside.unregisted.requeststatus;

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
@RequestMapping("/inside/unregisted/requeststatus")
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
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formUnregistedRequestStatus")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarUnregistedRequestStatus")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridUnregistedRequestStatusList")));

		return "inside/unregisted/requeststatus/requestStatusList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(RequestStatusListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/requestStatusPopup")
	public String requestStatusPopup(RequestStatusPopupParam param, Model model) {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridUnregistedRequestStatusPopup")));
		//미등록자료 배포요청 정보
		model.addAttribute("data", service.getRequestInfo(param));

		model.addAttribute("requestNo", param.getRequestNo());

		//배포 유형 콤보
		ComboInfoVO combo = new ComboInfoVO();
		combo.setComboCd("distributionMethod");
		model.addAttribute("distributionMethod", comboService.selectComboList(combo));

		//파일 배포 영역 콤보
		combo = new ComboInfoVO();
		combo.setComboCd("distributionFileType");
		model.addAttribute("distributionFileType", comboService.selectComboList(combo));

		//보안팀 정보(email 참조용)
		model.addAttribute("securityUserList", authService.selectSecurityUserList());
		model.addAttribute("deployEmailCC", service.getDeployEmailCC(param));

		return "inside/unregisted/requeststatus/requestStatusPopup";
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

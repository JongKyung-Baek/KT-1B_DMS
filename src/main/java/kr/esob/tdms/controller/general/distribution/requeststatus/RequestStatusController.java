package kr.esob.tdms.controller.general.distribution.requeststatus;

import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.tdms.commonlogic.combo.ComboService;
import kr.esob.tdms.commonlogic.grid.GridResultVO;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.general.distribution.commonrequest.CommonApprovalParam;
import net.sf.json.JSONArray;

@RequestMapping("/general/distribution/requeststatus")
@Controller
public class RequestStatusController extends AbstractController {

	@Inject
	RequestStatusService service;

	@Inject
	ComboService comboService;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formRequestStatus")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarRequestStatus")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridRequestStatusList")));

		return "general/distribution/requestStatus/requestStatusList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(RequestStatusListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping(value="/requestStatusPopup")
	public String requestStatusPopup(RequestStatusPopupParam param, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridRequestStatusPopup")));
//		model.addAttribute("listCount", service.selectPopupListCount(param));
		model.addAttribute("data", service.getApprovalRequestStatus(param));
		model.addAttribute("objectType", param.getObjectType());
		return "general/distribution/requestStatus/requestStatusPopup";
	}


	@RequestMapping("/requestStatusPopupList")
	public @ResponseBody GridResultVO requestStatusPopupList(RequestStatusPopupParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.requestStatusPopupList(param));

		BeanUtils.setProperty(result, "page", BeanUtils.getProperty(param, "page"));
		BeanUtils.setProperty(result, "size", BeanUtils.getProperty(param, "size"));
		return result;
	}
	
	@PostMapping("/deleteRequestNo")
	public @ResponseBody ResultVO deleteRequestNo(@RequestBody RequestStatusPopupParam param) throws Exception {
		return service.deleteRequestNo(param);
	}

}

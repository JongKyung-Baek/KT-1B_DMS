package kr.esob.fdms.controller.outside.drawing.approvalstatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.value.ApprovalStatusPopupInfo;
import kr.esob.fdms.commonlogic.value.RequestPopupInfo;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonDistributionRequestParam;
import kr.esob.fdms.controller.outside.commondestroystatus.CommonDestroyStatusService;
import kr.esob.fdms.controller.outside.commondestroystatus.DestroyFileVO;
import kr.esob.fdms.controller.outside.commondestroystatus.DestroyStatusParam;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/outside/drawing/approvalStatus")
public class ApprovalStatusController extends AbstractController {
	@Inject
	ApprovalStatusService service;

	@Inject
	CommonDestroyStatusService commonDestroyStatusService;

	@Inject
	VersionCheckService versionCheckService;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutsideDrawingApprovalStatus")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutsideDrawingApprovalStatus")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutsideDrawingApprovalStatusList")));

		return "outside/drawing/approvalStatus/approvalStatusList";
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

	@RequestMapping(value= {"/drawingVersionCheckPopup", "/docVersionCheckPopup", "/docpdfVersionCheckPopup", "/swVersionCheckPopup", "/productVersionCheckPopup", "/dxfVersionCheckPopup" }, method= RequestMethod.POST)
	public String commonVersionCheckPopup(CommonDistributionRequestParam param, Model model, HttpServletRequest request) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutsideDrawingApprovalStatusList")));
		model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(RequestPopupInfo.DRAWING));
//		model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(SearchAllPopupInfo.DRAWING));
		model.addAttribute("listTitle", RequestPopupInfo.DRAWING.getListTitle());
		model.addAttribute("dataType", RequestPopupInfo.DRAWING.getDataType());
		model.addAttribute("objectNo", request.getParameter("objectNo"));
		model.addAttribute("companyUserCd", request.getParameter("companyUserCd"));
		return "/outside/drawing/drawingVersionCheckPopup";
	}

	@RequestMapping(value={"/selectDrawingPopupList", "/selectProductDistributionPopupList"})
	public @ResponseBody GridResultVO selectPopupList(ApprovalStatusListParam param) throws Exception {
		param.setCheckVersion(true);

		String objectNo = param.getObjectNo();
		param.setObjectNo(objectNo);

		GridResultVO result = commonSelectList(param, versionCheckService);
		return result;
	}
}

package kr.esob.fdms.controller.outside.cr.request;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.value.RootAbsolutePath;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.cr.CommonCrService;
import kr.esob.fdms.controller.inside.cr.CrFileVO;
import kr.esob.fdms.controller.inside.cr.CrParam;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import kr.esob.fdms.controller.outside.drawing.request.DrawingInfoVO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/outside/cr/request")
public class RequestController extends AbstractController {

	@Inject
	RequestService service;

	@Inject
	ComboDao comboDao;

	@Inject
	AuthorizationDao authorizationDao;

	@Inject
	RootAbsolutePath rootAbsolutePath;

	@Inject
	CommonRequestService commonRequestService;

	@Inject
	CommonCrService commonCrService;

	@RequestMapping("/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutsideCrRequest")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutsideCrRequest")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutsideCrRequestList")));

		return "/outside/cr/request/requestList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(RequestListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/requestPopup")
	public String requestPopup(Model model, CrRequestParam param) {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridCrRequestUploadPopup")));
		model.addAttribute("crTypeCd", comboDao.selectComboListByCd("crTypeCd"));
		model.addAttribute("productNoList", comboDao.comboList("sql.docsProd.selectProdCombo", param));
		model.addAttribute("businessAreaCd", comboDao.selectComboListByCd("businessAreaCd"));
		model.addAttribute("approvalUser", authorizationDao.comboList("sql.Authorization.selectVendorApprovalUser", param));
		model.addAttribute("deployUser", authorizationDao.comboList("sql.Authorization.selectVendorUserComboById", param));
		return "/outside/cr/request/requestPopup";
	}

	@RequestMapping("/getAcceptanceUser")
	public @ResponseBody List<ComboInfoVO> getAcceptanceUser(@RequestBody CrRequestParam param){
		return authorizationDao.comboList("sql.Authorization.selectCrPurchaserComboById", param);
	}

	@RequestMapping(value="/getDrawingInfo", method=RequestMethod.POST)
	public @ResponseBody List<DrawingInfoVO> getDrawingInfo(@RequestBody DrawingInfoVO drawingInfoVo) {
		return service.getDrawingInfo(drawingInfoVo);
	}
	@RequestMapping(value="/getCrTemplate")
	public void getCrTemplate(HttpServletResponse response) throws Exception{
		String crTemplatePath = SystemConfig.getSystemConfigValue("CR_TEMPLATE_PATH");
		if("".equals(crTemplatePath) || crTemplatePath == null) {
			crTemplatePath = rootAbsolutePath + "CR_TEMPLATE.xlsx";
		}
		File file = new File(crTemplatePath);
		if(file.exists()) {
			String mimeType = URLConnection.guessContentTypeFromName(file.getName());
			if (mimeType == null) {
				mimeType = "application/octet-stream";
			}
			response.setContentType(mimeType);
			response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));
			response.setContentLength((int) file.length());
			InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			FileCopyUtils.copy(inputStream, response.getOutputStream());
		}
	}

	@RequestMapping(value="/crRequest")
	public @ResponseBody ResultVO crRequest(MultipartHttpServletRequest request) throws Exception {
		return service.insertCrRequest(request);
	}

	@RequestMapping(value="/selectUserInfo")
	public @ResponseBody String selectUserInfo(@RequestBody UserVO userVo) {
		return JSONObject.fromObject(commonRequestService.selectUserEmail(userVo)).toString();
	}

	@RequestMapping(value="/requestStatusPopup")
	public String requestStatusPopup(CrRequestParam param, Model model) {
		RequestStatusPopupVO rtn = service.getCrRequestInfo(param);
		model.addAttribute("data", rtn);
		model.addAttribute("formData", JSONObject.fromObject(service.selectAcceptanceInfo(param)));

		if( !(null==rtn)) {
			if( ("WAITING".equals(rtn.getStatusCd())) && (param.getSessionUser().getUserCd().equals(rtn.getApprovalUserCd())) ) {	//로그인한 사용자와 현재 CR의 외부사용자 팀장 USER_CD가 같은 경우에 승인 가능
				model.addAttribute("popupInfo", "APPROVAL");
				model.addAttribute("rejectDisabledYn", "N");
			}else {
				model.addAttribute("popupInfo", "STATUS");
				model.addAttribute("rejectDisabledYn", "Y");
			}
		}
		return "/outside/cr/request/requestStatusPopup";
	}

	@RequestMapping(value="/crFileDownload")
	public void crFileDownload(CrFileVO param, HttpServletResponse response) throws Exception {
		commonCrService.crFileDownload(commonCrService.selectOutsideFilePath(param), response);
	}

	@RequestMapping(value="/approve")
	public @ResponseBody ResultVO approve(@RequestBody OutsideCrParam param) throws IOException {
		return service.approve(param);
	}

	@RequestMapping(value="/approvalReject")
	public @ResponseBody ResultVO approvalReject(@RequestBody OutsideCrParam param) {
		return service.approvalReject(param);
	}



}

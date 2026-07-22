package kr.esob.fdms.controller.outside.doc.request;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.value.ObjectType;
import kr.esob.fdms.commonlogic.value.RequestPopupInfo;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import kr.esob.fdms.controller.outside.commonrequest.ObjectInfoVO;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/outside/doc/request")
public class RequestController extends AbstractController {
	@Inject
	RequestService service;

	@Inject
	CommonRequestService commonRequestService;

	@Inject
	ComboDao comboDao;

	@Inject
	AuthorizationDao authorizationDao;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutsideDocRequest")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutsideDocRequest")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutsideDocRequestList")));

		return "outside/doc/request/requestList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(RequestListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/requestPopup")
	public String requestPopup(Model model, RequestParam param) throws JsonProcessingException {
//		UserVO purchaserInfo = authorizationDao.getPurchaseInfo(param.getSessionUser());
//		String businessAreaCd = purchaserInfo != null ? purchaserInfo.getBusinessAreaCd() : "";
//		param.setBusinessAreaCd(businessAreaCd);
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDocRequestPopup")));
		model.addAttribute("requestPurpose", comboDao.selectComboListByCd("requestPurpose"));
		model.addAttribute("distributionMethod", comboDao.selectComboListByCd("distributionMethod"));
		model.addAttribute("distributionFileType", comboDao.selectComboListByCd("distributionFileType"));
		model.addAttribute("businessAreaCd", comboDao.selectComboListByCd("businessAreaCd"));
		model.addAttribute("deployUser", JSONArray.fromObject(authorizationDao.comboList("sql.Authorization.selectVendorUserCombo", param)));
		model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(RequestPopupInfo.DOC));
//		model.addAttribute("purchaserBusinessArea", businessAreaCd);
//		model.addAttribute("purchaserUserList", authorizationDao.comboList("sql.Authorization.selectPurchaserCombo", param));
		model.addAttribute("listTitle", RequestPopupInfo.DOC.getListTitle());
		model.addAttribute("dataType", RequestPopupInfo.DOC.getDataType());
		return "outside/commonrequest/commonRequestPopup";
	}

	@RequestMapping("/selectAcceptanceUser")
	public @ResponseBody List<ComboInfoVO> selectAcceptanceUser(@RequestBody RequestParam param){
		return authorizationDao.comboList("sql.Authorization.selectPurchaserCombo", param);
	}

	@RequestMapping(value="/selectInspectionInfo", method=RequestMethod.POST)
	public @ResponseBody List<ObjectInfoVO> selectInspectionInfo(ObjectInfoVO param) {
		return service.selectInspectionInfo(param);
	}

	@RequestMapping(value="/distributionRequest", method=RequestMethod.POST)
	public @ResponseBody ResultVO distributionRequest(@RequestBody RequestParam param) throws Exception {
		param.setRequestType("DISTRIBUTION");
		param.setObjectType(ObjectType.DOC.getObjectType());
		param.setObjectTypeCode(ObjectType.DOC.getCode());
		return service.distributionRequest(param);
	}

	@RequestMapping(value="/selectUserInfo")
	public @ResponseBody UserVO selectUserInfo(@RequestBody UserVO userVo) {
		return commonRequestService.selectUserInfo(userVo);
	}

	@RequestMapping(value="/requestStatusPopup")
	public String requestStatusPopup(Model model, RequestParam param) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDocRequestPopup")));
		model.addAttribute("formData", JSONObject.fromObject(commonRequestService.selectRequestStatus(param)));
		model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(RequestPopupInfo.DOC));
		model.addAttribute("listTitle", RequestPopupInfo.DOC.getListTitle());
		return "outside/commonrequest/commonRequestStatusPopup";
	}

	@RequestMapping(value="/selectRequestStatusList")
	public @ResponseBody GridResultVO selectRequestStatusList(RequestParam param) throws Exception{
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectRequestStatusList(param));
		return result;
	}
}

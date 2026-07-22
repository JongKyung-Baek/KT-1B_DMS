package kr.esob.fdms.controller.outside.production.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import kr.esob.fdms.commonlogic.value.SessionValue;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.production.common.ProductionInfoVO;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/outside/production/request")
public class RequestController extends AbstractController {
	@Inject
	RequestService service;

	@Inject
	SessionValue sessionValue;

	@Inject
	AuthorizationDao authorizationDao;

	@Inject
	CommonRequestService commonRequestService;

	@Inject
	ComboDao comboDao;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutsideProductionRequest")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutsideProductionRequest")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutsideProductionRequestList")));
		return "outside/production/request/requestList";
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
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionRequestPopup")));
		model.addAttribute("requestPurpose", comboDao.selectComboListByCd("requestPurpose"));
		model.addAttribute("distributionMethod", comboDao.selectComboListByCd("distributionMethod"));
		model.addAttribute("distributionFileType", comboDao.selectComboListByCd("distributionFileType"));
		model.addAttribute("businessAreaCd", comboDao.selectComboListByCd("businessAreaCd"));
		model.addAttribute("deployUser", authorizationDao.comboList("sql.Authorization.selectVendorUserCombo", param));
		model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(RequestPopupInfo.PRODUCTION));
//		model.addAttribute("purchaserBusinessArea", businessAreaCd);
//		model.addAttribute("purchaserUserList", authorizationDao.comboList("sql.Authorization.selectPurchaserCombo", param));
		model.addAttribute("objectType", comboDao.selectComboList("productionObjectType"));
		model.addAttribute("listTitle", RequestPopupInfo.PRODUCTION.getListTitle());
		model.addAttribute("dataType", RequestPopupInfo.PRODUCTION.getDataType());
		return "outside/commonrequest/commonRequestPopup";
	}

	@RequestMapping("/selectAcceptanceUser")
	public @ResponseBody List<ComboInfoVO> selectAcceptanceUser(@RequestBody RequestParam param){
		return authorizationDao.comboList("sql.Authorization.selectPurchaserCombo", param);
	}

	@RequestMapping(value="/selectInspectionInfo", method=RequestMethod.POST)
	public @ResponseBody List<ProductionInfoVO> selectInspectionInfo(ProductionInfoVO productionInfoVo) {
		List<ProductionInfoVO> list = new ArrayList<ProductionInfoVO>();
		if("DOC".equals(productionInfoVo.getObjectType())) {
			list = service.selectDocumentInspectionInfo(productionInfoVo);
		}else {
			list = service.selectSwInspectionInfo(productionInfoVo);
		}
		return list;
	}

	@RequestMapping(value="/distributionRequest", method=RequestMethod.POST)
	public @ResponseBody ResultVO distributionRequest(@RequestBody RequestParam param) throws Exception {
		param.setRequestType("DISTRIBUTION");
		if("DOC".equals(param.getObjectType())) {
			param.setObjectType(ObjectType.PRODUCT_DOC.getObjectType());
			param.setObjectTypeCode(ObjectType.PRODUCT_DOC.getCode());
		}else {
			param.setObjectType(ObjectType.PRODUCT_SW.getObjectType());
			param.setObjectTypeCode(ObjectType.PRODUCT_SW.getCode());
		}
		return service.distributionRequest(param);
	}

	@RequestMapping(value="/selectUserInfo")
	public @ResponseBody UserVO selectUserInfo(@RequestBody UserVO userVo) {
		return commonRequestService.selectUserInfo(userVo);
	}

	@RequestMapping(value="/requestStatusPopup")
	public String requestStatusPopup(Model model, RequestParam param) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionRequestPopup")));
		model.addAttribute("formData", JSONObject.fromObject(commonRequestService.selectRequestStatus(param)));
		model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(RequestPopupInfo.PRODUCTION));
		model.addAttribute("listTitle", RequestPopupInfo.PRODUCTION.getListTitle());
		model.addAttribute("objectType", comboDao.selectComboList("productionObjectType"));
		model.addAttribute("dataType", RequestPopupInfo.PRODUCTION.getDataType());
		return "outside/commonrequest/commonRequestStatusPopup";
	}

	@RequestMapping(value="/selectRequestStatusList")
	public @ResponseBody GridResultVO selectRequestStatusList(RequestParam param) throws Exception{
		GridResultVO result = new GridResultVO();
		if("PRODUCT_DOC".equals(param.getObjectType())) {
			result.setContents(service.selectDocumentRequestStatusList(param));
		}else {
			result.setContents(service.selectSwRequestStatusList(param));
		}
		return result;
	}

	@RequestMapping(value="/itemDetailPopup")
	public String itemDetailPopup(Model model, ProductionInfoVO productionInfoVo) throws JsonProcessingException {
		Map<String, Object> itemInfo = new HashMap<String, Object>();
		itemInfo.put("drawingNo", productionInfoVo.getObjectNo());
		itemInfo.put("revNo", productionInfoVo.getRevNo());
		itemInfo.put("rowId", productionInfoVo.getRowId());
		model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(RequestPopupInfo.PRODUCTION));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionRequestDetailPopup")));
		model.addAttribute("itemInfo", JSONObject.fromObject(itemInfo));
		return "outside/commonrequest/commonRequestDetailPopup";
	}

	@RequestMapping(value="/selectItemDetailList")
	public @ResponseBody GridResultVO selectItemDetailList(ProductionInfoVO productionInfoVo){
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectProductionDetailList(productionInfoVo));
		return result;
	}

}

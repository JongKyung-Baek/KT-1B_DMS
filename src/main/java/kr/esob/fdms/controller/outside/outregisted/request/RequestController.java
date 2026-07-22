package kr.esob.fdms.controller.outside.outregisted.request;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/outside/outregisted/request")
public class RequestController extends AbstractController {
	@Inject
	RequestService service;

	@Inject
	ComboService comboService;

	@Inject
	AuthorizationService authService;

	@Inject
	AuthorizationDao authorizationDao;
	
	@Inject
	ComboDao comboDao;

	@RequestMapping("/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutregistedRequest")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutregistedRequest")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutregistedRequestList")));

		return "outside/outregisted/request/requestList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(RequestListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}


	/**
	 * 자료 등록
	 * @param model
	 * @return
	 */
	@RequestMapping("/registerPopup")
	public String registerPopup(Model model) {
		ComboInfoVO combo = new ComboInfoVO();
		combo.setComboCd("objectType");
		model.addAttribute("objectType", comboService.selectComboList(combo));

		model.addAttribute("updownCabUrl", SystemConfig.getSystemConfigValue("UPDOWN_CAB_URL_OUT"));
		model.addAttribute("updownServerIp", SystemConfig.getSystemConfigValue("UPDOWN_SERVER_IP_OUT"));
		model.addAttribute("updownServerPort", SystemConfig.getSystemConfigValue("UPDOWN_SERVER_PORT"));
		model.addAttribute("updownPath", SystemConfig.getSystemConfigValue("OUTREG_FILE_PATH"));
		model.addAttribute("updownSecretKey", SystemConfig.getSystemConfigValue("UPDOWN_SECRET_KEY"));
		model.addAttribute("updownLangCode", SystemConfig.getSystemConfigValue("UPDOWN_LANG_CODE"));
		model.addAttribute("updownIsSecurity", SystemConfig.getSystemConfigValue("UPDOWN_IS_SECURITY"));
		model.addAttribute("updownExtension", SystemConfig.getSystemConfigValue("UPDOWN_EXTENSION"));

		return "outside/outregisted/request/registerPopup";
	}


	/**
	 *
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/saveUnregisterFile")
	public @ResponseBody ResultVO saveUnregisterFile(MultipartHttpServletRequest request) throws Exception {
		return service.saveUnregisterFile(request);
	}

	@RequestMapping(value="/saveUnregisterFileX")
	public @ResponseBody ResultVO saveUnregisterFileX(@RequestBody UnregisterPopupParam param) throws Exception {
		List<UnregisterPopupParam> lists = param.getParamList();
		param.setDataName(lists.get(0).getDataName());

		return service.saveUnregisterFileX(param);
	}

	/**
	 * 자료전송 팝업
	 * @param model
	 * @return
	 */
	@RequestMapping("/distributionRequestPopup")
	public String distributionRequestPopup(DistributionRequestPopupParam param, Model model) {
		
		model.addAttribute("requestPurpose", comboDao.selectComboListByCd("requestPurpose"));
		model.addAttribute("deployUser", JSONArray.fromObject(authorizationDao.comboList("sql.Authorization.selectVendorUserCombo", param)));
		model.addAttribute("receiveUser", authorizationDao.comboList("sql.Authorization.selectUserAll", param));

		return "outside/outregisted/request/distributionRequestPopup";
	}

	/**
	 * 자료전송
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/distributionRequest")
	public @ResponseBody ResultVO distributionRequest(@RequestBody DistributionRequestPopupParam param) throws Exception {
		return service.distributionRequest(param);
	}

}

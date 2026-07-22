package kr.esob.fdms.controller.outside.user.information;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.organizationmanage.auditlog.AuditLogService;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/outside/user/information")
public class InformationController extends AbstractController {

	@Inject
	InformationService service;

	@Inject
	CommonRequestService commonRequestService;

	@Inject
	ComboDao comboDao;

	@Inject
	AuthorizationDao authorizationDao;

	@Inject
	ComboService comboService;

	@Inject
	AuditLogService auditLogService;

	@RequestMapping(value = "/")
	public String home(Model model) throws JsonProcessingException {
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutsideUserInformation")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutsideUserInformation")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutsideUserInformationList")));
		return "outside/user/information/informationList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(InformationListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/requestPopup")
	public String requestPopup(Model model, InformationListParam param) throws JsonProcessingException {
		ComboInfoVO combo = new ComboInfoVO();
		combo.setComboCd("userRequestType2");
		model.addAttribute("userRequestType2", comboService.selectComboList(combo));

		combo.setComboCd("informationCr");
		model.addAttribute("informationCr", comboService.selectComboList(combo));

		combo.setComboCd("informationProtect");
		model.addAttribute("informationProtect", comboService.selectComboList(combo));

		model.addAttribute("approvalUser", authorizationDao.comboList("sql.Authorization.selectAllPurchaserCombo", param));

		model.addAttribute("userCd", param.getUserCd());

		if (!param.getUserCd().isEmpty()) {
			model.addAttribute("DetailInfo", JSONObject.fromObject(service.selectInformationDetailInfo(param)));
		}

		return "outside/user/information/requestPopup";
	}

	@RequestMapping("/userModPopup")
	public String userchangepopup(Model model, InformationListParam param) throws JsonProcessingException {
		return "outside/user/information/userModPopup";
	}

	@RequestMapping(value = "/request")
	public @ResponseBody ResultVO request(@RequestBody InformationListParam param) throws Exception {
		return service.insertRequest(param);
	}

	@RequestMapping(value = "/update")
	public @ResponseBody ResultVO update(@RequestBody InformationListParam param, Authentication authentication, HttpServletRequest request) throws Exception {
		ResultVO result = service.updateUser(param, authentication);

		if (result.isSuccess() && hasText(param.getUserNewPwd()) && authentication != null
				&& authentication.getPrincipal() instanceof UserVO) {
			UserVO userVo = (UserVO) authentication.getPrincipal();
			auditLogService.insertAuditLog("changePassword", userVo.getUserId(), userVo.getUserNm(), request);
		}

		return result;
	}

	@RequestMapping(value = "/selectProtectCount")
	public @ResponseBody ResultVO selectProtectCount(InformationListParam param) throws Exception {
		return service.selectProtectCount(param);
	}

	@RequestMapping(value = "/selectCrCount")
	public @ResponseBody ResultVO selectCrCount(InformationListParam param) throws Exception {
		return service.selectCrCount(param);
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

}

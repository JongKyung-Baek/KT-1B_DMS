package kr.esob.fdms.controller.inside.organizationmanage.outsideuser;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.organizationmanage.auditlog.AuditLogService;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.seed.PasswordUtils;
import net.sf.json.JSONArray;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/inside/organizationmanage/outsideuser")
public class OutsideuserController extends AbstractController {

	@Inject
	OutsideuserService service;
	@Inject
	OutsideCompanyService companyService;
	@Inject
	OutsideuserService userService;
	@Inject
	AuthorizationDao authorizationDao;
	@Inject
	ComboService comboService;

	@Inject
	AuditLogService auditLogService;

	@RequestMapping(value="/")
	public String home(Model model) throws JsonProcessingException {
		model.addAttribute("formOutsideCompany", JSONArray.fromObject(formService.selectFormInfo("formOutsideCompany")));
		model.addAttribute("formOutsideUser", JSONArray.fromObject(formService.selectFormInfo("formOutsideUser")));

		model.addAttribute("toolbarOutsideCompany", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutsideCompany")));
		model.addAttribute("toolbarOutsideUser", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutsideUser")));

		model.addAttribute("gridOutsideCompanyList", JSONArray.fromObject(gridService.selectGridInfo("gridOutsideCompanyList")));
		model.addAttribute("gridOutsideUserList", JSONArray.fromObject(gridService.selectGridInfo("gridOutsideUserList")));

		return "inside/organizationmanage/outsideuser/outsideUserHalf";
	}

	@RequestMapping(value="/selectCompanyList")
	public @ResponseBody GridResultVO selectList(CompanyParam param) throws Exception {
		GridResultVO result = commonSelectList(param, companyService);
		return result;
	}

	@RequestMapping(value="/selectUserList")
	public @ResponseBody GridResultVO selectUserList(UserParam param) throws Exception {
		GridResultVO result = commonSelectList(param, userService);
		return result;
	}

	@RequestMapping(value= {"/companyAddPopup", "/companyModPopup"})
	public String companyPopup(Model model, CompanyPopupParam param) throws JsonProcessingException {
		CompanyInfoVO info = null;
		String saveFlag = null;

		if(null == param.getCompanyCd()) {
			info = new CompanyInfoVO();
			saveFlag = "I";
		}
		else {
			info = companyService.selectCompanyInfo(param);
			saveFlag = "U";
		}

		model.addAttribute("info", info);
		model.addAttribute("saveFlag", saveFlag);

		param.setBusinessAreaCd(Constant.BUSINESS_AREA_CD_1);
		model.addAttribute("purchaser1", authorizationDao.comboList("sql.Authorization.selectPurchaserNotSelectedCombo", param, true));
		param.setBusinessAreaCd(Constant.BUSINESS_AREA_CD_2);
		model.addAttribute("purchaser2", authorizationDao.comboList("sql.Authorization.selectPurchaserNotSelectedCombo", param, true));

		return "inside/organizationmanage/outsideuser/companyPopup";
	}

	@RequestMapping(value= {"/userAddPopup", "/userModPopup"})
	public String userPopup(Model model, UserPopupParam param) throws JsonProcessingException {
		UserListVO info = null;
		String saveFlag = null;

		List<ComboInfoVO> companyCombo = new ArrayList<ComboInfoVO>();
		ComboInfoVO tempCompanyCombo = new ComboInfoVO();
		tempCompanyCombo.setComboLabel("선택하세요");
		tempCompanyCombo.setComboVal("");
		companyCombo.add(tempCompanyCombo);


		model.addAttribute("noSelect", companyCombo);


		tempCompanyCombo.setComboCd("informationCr");
		model.addAttribute("informationCr", comboService.selectComboList(tempCompanyCombo));

		tempCompanyCombo.setComboCd("informationProtect");
		model.addAttribute("informationProtect", comboService.selectComboList(tempCompanyCombo));

		tempCompanyCombo.setComboCd("informationDistribution");
		model.addAttribute("informationDistribution", comboService.selectComboList(tempCompanyCombo));

		System.out.println("param.getUserCd() >>>>>>>>>>>>>>>>> " +  param.getUserCd());

		if(null == param.getUserCd()) {
			info = new UserListVO();
			saveFlag = "I";
		}
		else {
			info = userService.selectUser(param.getUserCd());
			saveFlag = "U";
		}

		model.addAttribute("info", info);
		model.addAttribute("saveFlag", saveFlag);

		return "inside/organizationmanage/outsideuser/userPopup";
	}

	@RequestMapping(value="/saveUser")
	public @ResponseBody ResultVO saveUser(@RequestBody UserPopupParam param, Model model) throws JsonProcessingException {
		ResultVO resultVo = new ResultVO();

		userService.saveUser(param);
		userService.saveAppUserCd(param);

		resultVo.setSuccess(true);
		return resultVo;
	}

	@PostMapping(value="/saveRegisterUser")
	public @ResponseBody ResultVO saveRegisterUser(MultipartHttpServletRequest request, Authentication authentication) throws Exception {
		ResultVO resultVo = new ResultVO();
		UserPopupParam param = new UserPopupParam();

		String userPwd = request.getParameter("userPwd");
		String hashedPassword = PasswordUtils.hashPasswordWithSalt(userPwd);

		param.setUserPwd(hashedPassword);
		param.setUserCd(request.getParameter("userCd"));
		param.setSaveFlag("E");

		userService.saveUser(param);
		resultVo.setSuccess(true);

		if (authentication != null && authentication.getPrincipal() instanceof UserVO) {
			UserVO userVo = (UserVO) authentication.getPrincipal();
			auditLogService.insertAuditLog("changePassword", userVo.getUserId(), userVo.getUserNm(), request);
		}

		return resultVo;
	}



	@RequestMapping(value="/selectCompanyCheck")
	public @ResponseBody ResultVO selectCompanyCheck(@RequestBody CompanyPopupParam param) throws Exception {
		return companyService.selectCompanyCheck(param);
	}

	@RequestMapping(value="/saveCompany")
	public @ResponseBody ResultVO saveCompany(@RequestBody CompanyPopupParam param, Model model) throws JsonProcessingException {
		return companyService.saveCompany(param);
	}

	@RequestMapping(value="/unlockAccount")
	public @ResponseBody ResultVO unlockAccount(@RequestBody UserPopupParam param, Model model) throws JsonProcessingException {
		return userService.updateUnlockAccount(param);
	}

}

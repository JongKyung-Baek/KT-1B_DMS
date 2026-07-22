package kr.esob.fdms.controller.inside.organizationmanage.insideuser;


import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.organizationmanage.auditlog.AuditLogService;
import kr.esob.fdms.controller.login.UserVO;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
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

@RequestMapping("/inside/organizationmanage/insideuser")
@Controller
public class InsideuserController extends AbstractController {

	@Inject
	InsideuserService service;

	@Autowired
	ComboService comboService;

	@Inject
	ComboDao comboDao;

	@Inject
	AuditLogService auditLogService;

	@RequestMapping(value="/")
	public String home(Model model) throws JsonProcessingException {
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formInsideUser")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarInsideUser")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridInsideUserList")));

		return "inside/organizationmanage/insideuser/insideuserList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(InsideuserListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/update")
	public @ResponseBody ResultVO update(@RequestBody UserVO userVo) throws Exception {
		return service.updateUnlock(userVo);
	}

	@RequestMapping("/resetPwd")
	public @ResponseBody ResultVO resetPwd(@RequestBody UserVO userVo) throws Exception {
		return service.resetPwd(userVo);
	}

	@RequestMapping(value= {"/registerUserPopup", "/editUserPopup"})
	public String registerUser(Model model ,UserPopupParam param) throws JsonProcessingException {

		UserListVO info = null;
		String saveFlag = null;


		List<ComboInfoVO> deptCombo = new ArrayList<ComboInfoVO>();
		ComboInfoVO tempDeptCombo = new ComboInfoVO();
		tempDeptCombo.setComboLabel("선택하세요"); // 부서
		tempDeptCombo.setComboVal("");
		deptCombo.add(tempDeptCombo);

		model.addAttribute("noSelect_dept", deptCombo);


		List<ComboInfoVO> positionCombo = new ArrayList<ComboInfoVO>();
		ComboInfoVO tempPositionCombo = new ComboInfoVO();
		tempPositionCombo.setComboLabel("선택하세요"); // 직급
		tempPositionCombo.setComboVal("");
		positionCombo.add(tempPositionCombo);

		model.addAttribute("noSelect_position", positionCombo);


		List<ComboInfoVO> roleGroupCombo = new ArrayList<ComboInfoVO>();
		ComboInfoVO tempRoleGroupCombo = new ComboInfoVO();
		tempRoleGroupCombo.setComboLabel("선택하세요"); // 사용자 권한
		tempRoleGroupCombo.setComboVal("");
		roleGroupCombo.add(tempRoleGroupCombo);

		model.addAttribute("noSelect_roleGroup", roleGroupCombo);

		// 배포 권한/사업장 선택 UI 미사용 처리
//		List<ComboInfoVO> distributionAuthCombo = new ArrayList<ComboInfoVO>();
//		ComboInfoVO tempDistributionAuthCombo = new ComboInfoVO();
//		tempDistributionAuthCombo.setComboLabel("선택하세요"); // 배포 권한
//		tempDistributionAuthCombo.setComboVal("");
//		distributionAuthCombo.add(tempDistributionAuthCombo);
//
//		model.addAttribute("noSelect_distributionAuth", distributionAuthCombo);
//
//		model.addAttribute("businessAreaCd", comboDao.selectComboListByCd("businessAreaCd"));
//		model.addAttribute("distributionAuthCd", comboDao.selectComboListByCd("distributionAuthCd"));

		if(null == param.getUserCd()) {
			info = new UserListVO();
			saveFlag = "I"; // 새로운 데이터  (생성)
		}
		else {
			info = service.selectUser(param.getUserCd());
			saveFlag = "U"; // 이미 있는 데이터 (수정)
		}

		model.addAttribute("info", info);
		model.addAttribute("saveFlag", saveFlag);


		return "inside/organizationmanage/insideuser/registerUserPopup";
	}

	@PostMapping(value="/saveRegisterUser")
	public @ResponseBody ResultVO saveRegisterUser(MultipartHttpServletRequest multipartHttpServletRequest, Authentication authentication) throws Exception {
		ResultVO result = service.saveRegsiterUser(multipartHttpServletRequest);

		if (result.isSuccess() && "E".equals(multipartHttpServletRequest.getParameter("saveFlag")) && authentication != null
				&& authentication.getPrincipal() instanceof UserVO) {
			UserVO userVo = (UserVO) authentication.getPrincipal();
			auditLogService.insertAuditLog("changePassword", userVo.getUserId(), userVo.getUserNm(), multipartHttpServletRequest);
		}

		return result;
	}
















}

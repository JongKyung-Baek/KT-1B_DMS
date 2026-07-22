package kr.esob.fdms.controller.inside.unregisted.request;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/inside/unregisted/request")
public class RequestController extends AbstractController {
	@Inject
	RequestService service;

	@Inject
	ComboService comboService;

	@Inject
	AuthorizationService authService;

	@Inject
	AuthorizationDao authorizationDao;

	@RequestMapping("/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formUnregistedRequest")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarUnregistedRequest")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridUnregistedRequestList")));

		return "inside/unregisted/request/requestList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(RequestListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}


	/**
	 * 미등록 자료 등록
	 * @param model
	 * @return
	 */
	@RequestMapping("/registerPopup")
	public String registerPopup(Model model) {
		ComboInfoVO combo = new ComboInfoVO();
		combo.setComboCd("objectType");
		model.addAttribute("objectType", comboService.selectComboList(combo));

		model.addAttribute("updownCabUrl", SystemConfig.getSystemConfigValue("UPDOWN_CAB_URL"));
		model.addAttribute("updownServerIp", SystemConfig.getSystemConfigValue("UPDOWN_SERVER_IP"));
		model.addAttribute("updownServerPort", SystemConfig.getSystemConfigValue("UPDOWN_SERVER_PORT"));
		model.addAttribute("updownPath", SystemConfig.getSystemConfigValue("UPDOWN_PATH"));
		model.addAttribute("updownSecretKey", SystemConfig.getSystemConfigValue("UPDOWN_SECRET_KEY"));
		model.addAttribute("updownLangCode", SystemConfig.getSystemConfigValue("UPDOWN_LANG_CODE"));
		model.addAttribute("updownIsSecurity", SystemConfig.getSystemConfigValue("UPDOWN_IS_SECURITY"));
		model.addAttribute("updownExtension", SystemConfig.getSystemConfigValue("UPDOWN_EXTENSION"));

		return "inside/unregisted/request/registerPopup";
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

	@PostMapping(value="/saveUnregisterFileX")
	public @ResponseBody ResultVO saveUnregisterFileX(@RequestBody UnregisterPopupParam param) throws Exception {
		List<UnregisterPopupParam> lists = param.getParamList();
		param.setDataName(lists.get(0).getDataName());
//		System.out.println("파일명 = " + lists.get(0).getFileNm());
//		param.setFileNm(lists.get(0).getFileNm());
//		System.out.println("asdasd");

		return service.saveUnregisterFileX(param);
	}

	@PostMapping(value="/uploadUnregFile")
	public @ResponseBody ResultVO uploadUnregFile(MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
		// System.out.println(multipartHttpServletRequest.getFile("file").getName());

		return service.saveUnregisterFileX2(multipartHttpServletRequest);
	}

	/**
	 * 미등록 자료 등록
	 * @param model
	 * @return
	 */
	@RequestMapping("/distributionRequestPopup")
	public String distributionRequestPopup(DistributionRequestPopupParam param, Model model) {

		//요청 유형 (DRAWING, DOC, SW)
		model.addAttribute("objectType", param.getObjectType());
		model.addAttribute("businessAreaCd", param.getBusinessAreaCd());

		//업체 리스트
		List<ComboInfoVO> companyCombo = new ArrayList<ComboInfoVO>();
		ComboInfoVO tempCompanyCombo = new ComboInfoVO();
		tempCompanyCombo.setComboLabel("선택하세요");
		tempCompanyCombo.setComboVal("");
		companyCombo.add(tempCompanyCombo);
		model.addAttribute("noSelect", companyCombo);

		//배포 유형 콤보
		ComboInfoVO combo = new ComboInfoVO();
		combo.setComboCd("distributionMethod");
		model.addAttribute("distributionMethod", comboService.selectComboList(combo));

		//파일 배포 영역 콤보
		combo = new ComboInfoVO();
		combo.setComboCd("distributionFileType");
		model.addAttribute("distributionFileType", comboService.selectComboList(combo));
		
		//구매팀 정보
		model.addAttribute("purchaseTeam", authorizationDao.comboList("sql.Authorization.selectPurchaserCombo", param.getSessionUser().getBusinessAreaCd()));

		//결재자 정보 (로그인한 사용자의 팀장)
		CommonParam comParam = new CommonParam();
		//결재자 리스트 (로그인한 사용자의 팀장 + 팀장의 대결자)
		model.addAttribute("teamLeaderList", authorizationDao.comboList("sql.Authorization.selectApprovalUserList", param));
		List<ComboInfoVO> teamLeader = authService.getTeamLeader(comParam);
		model.addAttribute("teamLeaderUid", teamLeader.size() > 0 ? teamLeader.get(0).getComboVal() : "");

		//보안팀 정보(email 참조용)
		model.addAttribute("securityUserList", authService.selectSecurityUserList());
		model.addAttribute("defaultSecurityUser", authService.selectSecurityUserByBusinessArea(param));
		//용도 (유효기간) - selectBox
		combo = new ComboInfoVO();
		combo.setComboCd("requestPurpose");
		model.addAttribute("deployList", comboService.selectComboList(combo));

		return "inside/unregisted/request/distributionRequestPopup";
	}

	/**
	 * 미등록자료 > 자료 배포 요청
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/distributionRequest")
	public @ResponseBody ResultVO distributionRequest(@RequestBody DistributionRequestPopupParam param) throws Exception {
		return service.distributionRequest(param);
	}

}

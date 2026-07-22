package kr.esob.fdms.controller.inside.production.request;

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
import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.value.SearchAllPopupInfo;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import kr.esob.fdms.controller.inside.production.common.CommonProductionService;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/inside/production/request")
public class RequestController extends AbstractController {
	@Inject
	RequestService service;


	@Inject
	AuthorizationService authService;

	@Inject
	AuthorizationDao authorizationDao;

	@Inject
	ComboDao comboDao;

	@Inject
	CommonProductionService commonProductionService;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formProductionRequest")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarProductionRequest")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionRequestList")));

		return "inside/production/request/requestList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(RequestListParam param) throws Exception {
		service.setSearchAllParam(param);
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/requestPopup")
	public String requestPopup(Model model, CommonParam param) {
		model.addAttribute("gridAcceptance", JSONArray.fromObject(gridService.selectGridInfo("gridProductionRequestAcceptance")));
		model.addAttribute("gridProduction", JSONArray.fromObject(gridService.selectGridInfo("gridProductionRequestProduction")));
		model.addAttribute("teamLeaderList", authorizationDao.comboList("sql.Authorization.selectApprovalUserList", param));
		model.addAttribute("deployType", comboDao.selectComboListByCd("deployType"));
		model.addAttribute("watermarkYn", comboDao.selectComboListByCd("comboYn"));
		model.addAttribute("watermarkDeployDtYn", comboDao.selectComboListByCd("comboYn"));
		return "inside/production/request/requestPopup";
	}

	/**
	 * 출력 승인요청 팝업
	 * @param model
	 * @param param
	 * @return
	 */
	@RequestMapping("/printRequestPopup")
	public String printRequestPopup(Model model) {
		CommonParam param = new CommonParam();
		model.addAttribute("teamList", authorizationDao.comboList("sql.Authorization.selectApprovalUserList", param));
		List<ComboInfoVO> teamLeader = authService.getTeamLeader(param);
		model.addAttribute("teamLeaderUid", teamLeader.size() > 0 ? teamLeader.get(0).getComboVal() : "");
		return "inside/production/request/printRequestPopup";
	}

	@RequestMapping(value="/distributionRequest", method=RequestMethod.POST)
	public @ResponseBody ResultVO distributionRequest(@RequestBody RequestParam param) throws Exception {
		return service.distributionRequest(param);
	}

	@RequestMapping(value="/printRequest", method=RequestMethod.POST)
	public @ResponseBody ResultVO printRequest(@RequestBody RequestParam param) throws Exception {
		return service.printRequest(param);
	}

	@RequestMapping("/searchAllPopup")
	public String searchAll(Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionRequestSearchAll")));
		model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(SearchAllPopupInfo.PRODUCTION));
		return "/inside/common/searchAllPopup";
	}

	@RequestMapping("/selectObjectClassCd")
	public @ResponseBody List<ComboInfoVO> selectObjectClassCd(@RequestBody Map<String, Object> param){
		if("DOC".equals(param.get("objectType").toString())) {
			param.put("queryId", "sql.Combo.selectObjectClassCdDoc");
		}else {
			param.put("queryId", "sql.Combo.selectObjectClassCdSw");
		}
		return comboDao.selectCombo(param);
	}
	
	@RequestMapping(value="/selectDeployAcceptCheck")
	public @ResponseBody ResultVO selectDeployAcceptCheck(@RequestBody RequestParam param) throws Exception {
		return service.selectDeployAcceptCheck(param);
	}

}

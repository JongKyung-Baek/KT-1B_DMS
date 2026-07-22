package kr.esob.fdms.controller.inside.production.productionstatus;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import net.sf.json.JSONArray;

@Controller
@RequestMapping(value="/inside/production/productionstatus")
public class ProductionStatusController extends AbstractController {

	@Inject
	ComboDao comboDao;

	@Inject
	AuthorizationService authService;

	@Inject
	AuthorizationDao authorizationDao;

	@Inject
	ProductionStatusService service;


	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formProductionStatus")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarProductionStatus")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionStatusList")));

		return "inside/production/productionstatus/statusList";
	}

	@RequestMapping(value="/selectList")
	public @ResponseBody GridResultVO selectList(ProductionStatusListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping(value="/requestPopup")
	public String requestPopup(Model model, CommonParam param) {
		model.addAttribute("gridProduction", JSONArray.fromObject(gridService.selectGridInfo("gridProductionReplaceRequest")));
		model.addAttribute("teamLeaderList", authorizationDao.comboList("sql.Authorization.selectApprovalUserList", param));
		List<ComboInfoVO> teamLeader = authService.getTeamLeader(param);
		model.addAttribute("teamLeaderUid", teamLeader.size() > 0 ? teamLeader.get(0).getComboVal() : "");
		model.addAttribute("deployType", comboDao.selectComboListByCd("deployType"));
		model.addAttribute("watermarkYn", comboDao.selectComboListByCd("comboYn"));
		model.addAttribute("watermarkDeployDtYn", comboDao.selectComboListByCd("comboYn"));
		return "inside/production/productionstatus/requestPopup";
	}

	@RequestMapping("/selectProductionReplace")
	public @ResponseBody GridResultVO selectProductionReplace(ReplaceRequestParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		Gson gson = new Gson();
		param.setList(gson.fromJson(param.getParam().replace("&quot;","'"), new TypeToken<List<ReplaceRequestParam>>() {}.getType()));
		result.setContents(service.selectProductionReplace(param));
		return result;
	}

	@RequestMapping(value="/distributionRequest", method=RequestMethod.POST)
	public @ResponseBody ResultVO distributionRequest(@RequestBody RequestParam param) throws Exception {
		return service.distributionRequest(param);
	}

	@RequestMapping(value="/distributionHistoryPopup")
	public String distributionHistoryPopup(Model model, DistributionHistoryVO param) {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionDistributionHistory")));
		model.addAttribute("historyList", JSONArray.fromObject(service.selectDistributionList(param)));
		return "/inside/production/productionstatus/distributionHistoryPopup";
	}

}

package kr.esob.tdms.controller.general.cr.acceptance;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.tdms.commonlogic.combo.ComboDao;
import kr.esob.tdms.commonlogic.combo.ComboInfoVO;
import kr.esob.tdms.commonlogic.combo.ComboService;
import kr.esob.tdms.commonlogic.grid.GridResultVO;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.general.authorization.AuthorizationDao;
import kr.esob.tdms.controller.general.authorization.AuthorizationService;
import kr.esob.tdms.controller.general.cr.CommonCrService;
import kr.esob.tdms.controller.general.cr.CrFileDownloadParam;
import kr.esob.tdms.controller.general.cr.CrParam;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/general/cr/acceptance")
public class AcceptanceController extends AbstractController {
	@Inject
	AcceptanceService service;

	@Inject
	CommonCrService commonCrService;

	@Inject
	ComboService comboService;

	@Inject
	ComboDao comboDao;

	@Inject
	AuthorizationService authService;

	@Inject
	AuthorizationDao authorizationDao;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formCrAcceptance")));
		model.addAttribute("toolbarInfo", "");
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridCrAcceptanceList")));

		return "general/cr/acceptance/acceptanceList";
	}

	@RequestMapping(value="/selectList")
	public @ResponseBody GridResultVO selectList(AcceptanceListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping(value="/acceptancePopup")
	public String acceptancePopup(Model model, CrParam param) {
		model.addAttribute("teamLeaderList", authorizationDao.comboList("sql.Authorization.selectApprovalUserList", param));
		List<ComboInfoVO> teamLeader = authService.getTeamLeader(param);
		model.addAttribute("teamLeaderUid", teamLeader.size() > 0 ? teamLeader.get(0).getComboVal() : "");
		model.addAttribute("productNoList", comboDao.comboList("sql.docsProd.selectProdCombo", param));
		model.addAttribute("formData", JSONObject.fromObject(service.selectAcceptanceInfo(param)));
		model.addAttribute("popupInfo", "ACCEPTANCE");
		return "general/cr/commonInsideCrPopup";
	}

	@GetMapping(value="/crFileDownload")
	public void crFileDownload(@RequestParam String crNo, @RequestParam int fileNo,
							   HttpServletResponse response) throws Exception {
		CrFileDownloadParam param = new CrFileDownloadParam();
		param.setCrNo(crNo);
		param.setFileNo(fileNo);
		commonCrService.downloadInside(param, response);
	}

	@PostMapping(value="/approvalRequest")
	public @ResponseBody ResultVO approvalRequest(@RequestBody CrParam param) {
		return service.approvalRequest(param);
	}

	@PostMapping(value="/acceptanceReject")
	public @ResponseBody ResultVO acceptanceReject(@RequestBody CrParam param) {
		return service.acceptanceReject(param);
	}


}

package kr.esob.tdms.controller.general.production.disposal;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import kr.esob.tdms.commonlogic.combo.ComboInfoVO;
import kr.esob.tdms.commonlogic.grid.GridResultVO;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.general.authorization.AuthorizationDao;
import kr.esob.tdms.controller.general.authorization.AuthorizationService;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/general/production/disposal")
public class DisposalController extends AbstractController {

	@Inject
	DisposalService service;

	@Inject
	AuthorizationService authService;

	@Inject
	AuthorizationDao authorizationDao;
	
	@RequestMapping(value="/disposalTab")
	public String disposalTab(Model model, CommonHomeParam param) throws JsonProcessingException {
		return "/general/production/approval/disposalTab";
	}

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formProductionDisposal")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarProductionDisposal")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionDisposalList")));

		return "general/production/disposal/disposalList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(DisposalListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	/*
	 * 생산기술자료 폐기 팝업
	 */
	@RequestMapping(value="/disposalRequestPopup")
	public String disposalRequestPopup(Model model) throws JsonProcessingException {
		//결재자 정보 (로그인한 사용자의 팀장 - 생산기술자료 배포 현황은 로그인한 사용자한테 배포된것만 보이므로 로그인한 사용자의 팀장)
		CommonParam param = new CommonParam();
		model.addAttribute("teamLeaderList", authorizationDao.comboList("sql.Authorization.selectApprovalUserAll", param));
//		authService.getTeamLeaderList(param)
		List<ComboInfoVO> teamLeader = authService.getTeamLeader(param);
		model.addAttribute("teamLeaderUid", teamLeader.size() > 0 ? teamLeader.get(0).getComboVal() : "");
		return "general/production/disposal/disposalRequestPopup";
	}


	/**
	 * 폐기 승인 요청
	 * @param param
	 * @return
	 */
	@RequestMapping(value="/destroyRequest", method = RequestMethod.POST)
	public @ResponseBody ResultVO destroyRequest(@RequestBody DisposalRequestPopupParam param) {
		ResultVO result = new ResultVO();
		result = service.destroyRequest(param);
		return result;
	}
}

package kr.esob.fdms.controller.inside.distribution.acceptance;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/inside/distribution/acceptance")
public class AcceptanceController extends AbstractController {

	@Inject
	AcceptanceService service;

	@Inject
	AcceptancePopupListService popupService;

	@Inject
	ComboService comboService;

	@Inject
	AuthorizationService authService;

	@Inject
	AuthorizationDao authorizationDao;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formAcceptance")));
		model.addAttribute("toolbarInfo", "");
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridAcceptanceList")));

		return "inside/distribution/acceptance/acceptanceList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(AcceptanceListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/selectPopupList")
	public @ResponseBody GridResultVO selectPopupList(AcceptanceParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(popupService.selectList(param));
		return result;
	}


	@RequestMapping("/approvePopup")
	public String approvePopup(AcceptanceParam param, Model model, HttpServletRequest request) {
		//그리드 정보
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDistributionApprovalPopup")));

		model.addAttribute("objectType", request.getParameter("objectType"));
		model.addAttribute("statusCd", request.getParameter("statusCd"));
		if("REQUEST".equals(request.getParameter("statusCd")) && "2".equals(request.getParameter("currentProcessSeqNo"))) {
			model.addAttribute("requestCd", "REQUEST");
		}else {
			model.addAttribute("requestCd", "NO-REQUEST");
		}
		//배포 유형 콤보
		ComboInfoVO combo = new ComboInfoVO();
		combo.setComboCd("distributionMethod");
		model.addAttribute("distributionMethod", comboService.selectComboList(combo));

		//파일 배포 영역 콤보
		combo = new ComboInfoVO();
		combo.setComboCd("distributionFileType");
		model.addAttribute("distributionFileType", comboService.selectComboList(combo));

		//배포요청 승인/반려 정보
		AcceptanceVO acceptVo = service.getDocRequestData(request.getParameter("requestNo"));

		String defYn = "Y";	//방산 대결자 Disabled 여부
		if("Y".equals(acceptVo.getProtectYn())) {
			defYn = "N";
			//방산 기술 팀장 정보
			if("Development".equals(param.getBusinessTypeCd())) {				//개발
				model.addAttribute("defendTeamLeaderUid", authService.selectDevelopmentProtectUserList());
			}else if("Production".equals(param.getBusinessTypeCd())) {		//양산
				model.addAttribute("defendTeamLeaderUid", authService.selectProductProtectUserList(param.getSessionUser().getBusinessAreaCd()));
			}
//			model.addAttribute("defendTeamLeaderUid", authService.getDefenseTeamLeaderUid());
//			model.addAttribute("defendTeamLeaderList", authService.selectDefenseTeamLeaderList());
		}else {
			defYn = "Y";
		}
		model.addAttribute("defDisabledYn", defYn);
		model.addAttribute("data", acceptVo);

		//용도
		combo = new ComboInfoVO();
		combo.setComboCd("requestPurpose");
		model.addAttribute("requestPurpose", comboService.selectComboList(combo));

		//결재자 정보 (로그인한 사용자의 팀장)
//		model.addAttribute("teamLeaderList", authService.getTeamLeaderList(param));
		//결재자 정보 조회 변경(11.29) 자신을 제외한 자신의 팀원 전체 조회. 기본값으로 팀장 선택
		if("REQUEST".equals(request.getParameter("statusCd")) && "2".equals(request.getParameter("currentProcessSeqNo"))) {
			model.addAttribute("teamLeaderList", authorizationDao.comboList("sql.Authorization.selectApprovalUserList", param));
		}
		else {
			// 상태가 요청이 아닐 경우 이미 팀장이 정해졌기 때문에 실제 지정된 팀장이 나와야 함.
			if(null != acceptVo.getApprovalUserCd()) {
				model.addAttribute("teamLeaderList", authorizationDao.selectOneUserCombo(acceptVo.getApprovalUserCd()));
			}
		}
		List<ComboInfoVO> teamLeader = authService.getTeamLeader(param);

		model.addAttribute("teamLeaderUid", teamLeader.size() > 0 ? teamLeader.get(0).getComboVal() : "");
		//요청 접수 목록 갯수
		model.addAttribute("listCount", popupService.getPopupListCount(param));
		//요청 번호 requestNo
		model.addAttribute("requestNo", request.getParameter("requestNo"));
		return "/inside/distribution/acceptance/approvalPopup";
	}



	@RequestMapping(value="/saveAcceptance", produces="application/json;charset=UTF-8")
	public @ResponseBody ResultVO saveAcceptance(@RequestBody AcceptanceParam param) {
		return service.saveAcceptance(param);
	}


	@RequestMapping("/productAcceptPopup")
	public String productAcceptPopup(AcceptanceParam param, Model model, HttpServletRequest request) {
		//그리드 정보
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDistributionApprovalPopup")));

		model.addAttribute("objectType", request.getParameter("objectType"));

		//배포요청 승인/반려 정보
		AcceptanceVO acceptVo = service.getDocRequestData(request.getParameter("requestNo"));

		if("REQUEST".equals(acceptVo.getStatusCd()) && "ACCEPT".equals(acceptVo.getActionCd())) {
			model.addAttribute("requestCd", "REQUEST");
		}else {
			model.addAttribute("requestCd", "NO-REQUEST");
		}

		//배포 유형 콤보
		ComboInfoVO combo = new ComboInfoVO();
		combo.setComboCd("distributionMethod");
		model.addAttribute("distributionMethod", comboService.selectComboList(combo));


		model.addAttribute("data", acceptVo);

		//용도
		combo = new ComboInfoVO();
		combo.setComboCd("requestPurpose");
		model.addAttribute("requestPurpose", comboService.selectComboList(combo));

		//결재자 정보 (로그인한 사용자의 팀장)
		model.addAttribute("teamLeaderList", authService.getTeamLeaderList(param));
		List<ComboInfoVO> teamLeader = authService.getTeamLeader(param);
		model.addAttribute("teamLeaderUid", teamLeader.size() > 0 ? teamLeader.get(0).getComboVal() : "");

		//요청 번호 requestNo
		model.addAttribute("requestNo", request.getParameter("requestNo"));

		return "/inside/distribution/acceptance/productAcceptPopup";
	}


}

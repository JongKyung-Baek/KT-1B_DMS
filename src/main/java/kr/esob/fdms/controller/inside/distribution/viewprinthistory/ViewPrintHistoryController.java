package kr.esob.fdms.controller.inside.distribution.viewprinthistory;


import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import net.sf.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/inside/distribution/viewPrintHistory")
public class ViewPrintHistoryController extends AbstractController {

	@Inject
    HistoryService service;

	@Inject
	AuthorizationService authService;
	
	@Inject
	AuthorizationDao authorizationDao;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formViewPrintHistoryList")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarViewPrintHistory")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridViewPrintHistoryList")));

		return "inside/distribution/viewPrintHistory/historyList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(HistoryListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);

		service.formatLogType(result);

		return result;
	}

	/**
	 * 출력물 폐기 요청 팝업
	 * @param model
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value="/disposalRequestPopup")
	public String disposalRequestPopup(ViewPrintHistoryPopupParam param, Model model, HttpServletRequest request) throws JsonProcessingException {
		CommonParam comParam = new CommonParam();
		//결재자 정보 (로그인한 사용자의 팀장)
		model.addAttribute("teamLeaderList", authorizationDao.comboList("sql.Authorization.selectApprovalUserList", param));
		List<ComboInfoVO> teamLeader = authService.getTeamLeader(param);
		model.addAttribute("teamLeaderUid", teamLeader.size() > 0 ? teamLeader.get(0).getComboVal() : "");
		
		model.addAttribute("protectYn", "Y".equals(request.getParameter("protectYn")) ? "N" : "Y");	//방산 팀장 disabled 여부


		if("Y".equals(request.getParameter("protectYn"))) {		//방산이 하나라도 포함되어 있으면
			//방산팀장 정보
			model.addAttribute("defendTeamLeaderUid", authService.getDefenseTeamLeaderUid());
			model.addAttribute("defendTeamLeaderList", authService.selectDefenseTeamLeaderList());
		}


		if("Y".equals(request.getParameter("protectYn"))) {		//방산이 하나라도 포함되어 있으면
			//방산기술보호 결재자
			//양산
			model.addAttribute("productProtectYn", "Y".equals(request.getParameter("productProtectYn")) ? "N" : "Y" );
			//개발
			model.addAttribute("developmentProtectYn", "Y".equals(request.getParameter("developmentProtectYn")) ? "N" : "Y" );

			//양산 결재자 리스트
			if("Y".equals(request.getParameter("productProtectYn"))){
				model.addAttribute("productProtectUserList", authService.selectProductProtectUserList(""));
			}
			//개발 결재자 리스트
			if("Y".equals(request.getParameter("developmentProtectYn"))){
				model.addAttribute("developmentProtectUserList", authService.selectDevelopmentProtectUserList());
			}
		}else {
			//양산
			model.addAttribute("productProtectYn",  "Y");
			//개발
			model.addAttribute("developmentProtectYn", "Y");
		}

//		/* 방산 팀장 ( 최신 )
		model.addAttribute("protectYn", "Y".equals(request.getParameter("protectYn")) ? "N" : "Y");	//방산 팀장 disabled 여부
		if("Y".equals(request.getParameter("protectYn"))) {		//방산이 하나라도 포함되어 있으면
			List<ComboInfoVO> list = new ArrayList<ComboInfoVO>();
			List<String> defaultUserList = new ArrayList<String>();
			int proectUserCnt= 0;
			if("Y".equals(param.getProductProtectYn())){	//양산
				proectUserCnt++;
				list = authService.selectProductProtectUserList(param.getSessionUser().getBusinessAreaCd());
				for(ComboInfoVO tempVo : list) {
					defaultUserList.add(tempVo.getComboVal());
				}
			}

			if("Y".equals(param.getDevelopmentProtectYn())){	//개발
				proectUserCnt++;
				if(list.size() == 0) {
					list = authService.selectDevelopmentProtectUserList();
				}else {
					list.addAll(authService.selectDevelopmentProtectUserList());
				}
				defaultUserList.add("USER_0000012085");

				for(ComboInfoVO tempVO : list) {
					if("USER_0000012085".equals(tempVO.getComboVal())) {
						defaultUserList.add(tempVO.getComboVal());
					}
				}
			}
			//방산팀장 정보
			model.addAttribute("defaultProtectUserList", defaultUserList);
			model.addAttribute("protectUserList", list);
			model.addAttribute("protectUserCount", proectUserCnt);
		}
//		*/

		return "inside/distribution/printHistory/disposalRequestPopup";
	}


	/**
	 * 폐기 승인 요청
	 * @param param
	 * @return
	 */
	@RequestMapping(value="/destroyRequest", method = RequestMethod.POST)
	public @ResponseBody ResultVO destroyRequest(@RequestBody DestroyRequestParam param) {
		ResultVO result = new ResultVO();
		result = service.destroyRequest(param);
		return result;
	}
}

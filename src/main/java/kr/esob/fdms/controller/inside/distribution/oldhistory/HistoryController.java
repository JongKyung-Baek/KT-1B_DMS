package kr.esob.fdms.controller.inside.distribution.oldhistory;


import javax.inject.Inject;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.acceptance.AcceptanceParam;
import kr.esob.fdms.controller.login.UserVO;
import net.sf.json.JSONArray;

/**
 * 내부사용자 이력조회
 * @author younjh
 *
 */
@Controller
@RequestMapping({"/inside/distribution/oldhistory", "/outside/distribution/oldhistory"})
public class HistoryController extends AbstractController {
	@Inject
	HistoryService service;

	@RequestMapping(value="/")
	public String home(Model model) throws JsonProcessingException {
		UserVO user = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo(getFormId(user))));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo(getGridId(user))));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarDistributionOldHistory")));
		model.addAttribute("formId", getFormId(user));
		model.addAttribute("gridId", getGridId(user));


		return "inside/distribution/oldhistory/historyList";
	}
	
	@RequestMapping(value="/destroyOldHistory", produces="application/json;charset=UTF-8")
	public @ResponseBody ResultVO destroyOldHistory(@RequestBody HistoryListParam param) {
		return service.destroyOldHistory(param);
	}
	

	private String getFormId(UserVO user) {
		if("I".equals(user.getAuthSite())) {
			return "formDistributionOldHistory";
		}
		else {
			return "formOutDistributionOldHistory";
		}
	}

	private String getGridId(UserVO user) {
		if("I".equals(user.getAuthSite())) {
			return "gridDistributionOldHistoryList";
		}
		else {
			return "gridOutDistributionOldHistoryList";
		}
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(HistoryListParam param) throws Exception {
		UserVO user = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if("E".equals(user.getAuthSite())) {
			param.setCompanyCode(user.getCompanyCd());
		}

		GridResultVO result = commonSelectList(param, service);
		return result;
	}
}

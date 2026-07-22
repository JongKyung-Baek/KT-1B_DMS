package kr.esob.fdms.controller.inside.production.disposalstatus;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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

@Controller
@RequestMapping("/inside/production/disposalStatus")
public class DisposalStatusController extends AbstractController {

	@Inject
	DisposalStatusService service;

	@Inject
	AuthorizationService authService;

	@Inject
	AuthorizationDao authorizationDao;
	
	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formProductionDisposalStatus")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarProductionDisposalStatus")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionDisposalStatusList")));

		return "inside/production/disposalstatus/disposalStatusList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(DisposalStatusListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}
}

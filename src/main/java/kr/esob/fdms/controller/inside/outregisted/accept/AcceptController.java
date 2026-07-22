package kr.esob.fdms.controller.inside.outregisted.accept;

import java.util.HashMap;
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
import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.value.ObjectType;
import kr.esob.fdms.commonlogic.value.RequestPopupInfo;
import kr.esob.fdms.commonlogic.value.SessionValue;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import kr.esob.fdms.controller.outside.unregisted.status.StatusListParam;
import kr.esob.fdms.controller.outside.unregisted.status.StatusService;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/inside/outregisted/accept")
public class AcceptController extends AbstractController {
	
	@Inject
	AcceptService service;
	
	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);

		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formInsideOutregistedAccept")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarInsideOutregistedAccept")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridInsideOutregistedAcceptList")));
		return "inside/outregisted/accept/acceptList";
	}
	
	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(AcceptListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	
	
	
}

package kr.esob.fdms.controller.inside.pdm.deployhistory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.form.FormInfoService;
import kr.esob.fdms.commonlogic.grid.GridInfoDao;
import kr.esob.fdms.commonlogic.grid.GridInfoService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.toolbar.ToolbarInfoService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/inside/pdm/deployhistory")
public class PdmDeployHistoryController extends AbstractController{

	@Inject
	PdmDeployHistoryService service;

	@Inject
	GridInfoDao dao;

	@Inject
	FormInfoService formService;
	
	@Inject
	ToolbarInfoService toolbarService;
	
	@Inject
	GridInfoService gridService;

	@RequestMapping(value="/deployhistorylist")
	public String home(Model model) throws JsonProcessingException {
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formItsSend")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarItsSend")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("itsSend")));
		
//		return "/inside/pdm/deployhistory/inner0101";
		return "/inside/pdm/deployhistory/deployhistorylist";
	}
	/*
	@RequestMapping(value="/list")
	public String list(Model model) throws JsonProcessingException {
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formItsSend")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarItsSend")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("itsSend")));
		model.addAttribute("gridId", "itsSend");
		return "/inside/pdm/deployhistory/inner0601";
	}
	*/
	@RequestMapping("/list") // decoratorList
	public String list() {
		return "/inside/pdm/deployhistory/inner0701";
	}
	@RequestMapping("/Popup")
	public String popup(Model model, HttpServletRequest request) {
		return "/inside/pdm/deployhistory/inner0701popup2"; // external0501popup // inner0503popup // inner010201popup1
	}
	
	@RequestMapping("/half") // decoratorHalf
	public String half() {
		return "/inside/pdm/deployhistory/inner0502"; // inner0502
	}	
	@RequestMapping("/side") // decoratorSide
	public String side() {
		return "/inside/pdm/deployhistory/inner0701"; // external0801 // inner0701
	}	
	@RequestMapping("/tree") // decoratorTree
	public String tree() {
		return "/inside/pdm/deployhistory/inner0702"; // inner0702
	}	
	@RequestMapping("/main") // decoratorMain
	public String main() {
		return "/inside/pdm/deployhistory/main"; // main
	}
	
	@RequestMapping(value="/getPageInfo")
	public @ResponseBody Map<String, Object> getPageInfo() {
		Map<String, Object> rtn = new HashMap<String, Object>();
		rtn.put("list", dao.selectGridInfoList("docsSend"));
		return rtn;
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(PdmDeployHistoryParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/updateList")
	public String updateList(@RequestBody PdmDeployHistoryParam param) {
		service.updateList(param);
		return "";
	}
	
	@RequestMapping("/datePickerTest")
	public String datePickerTest() {
		return "/inside/pdm/deployhistory/test";
	}
}

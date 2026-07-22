package kr.esob.fdms.controller.outside.duanzong.pdm;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/outside/duanzong/pdm")
public class PdmController extends AbstractController {
	
	@Inject
	PdmService service;
	
	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutsideDuanzongPdm")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutsideDuanzongPdm")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutsideDuanzongPdmList")));
		return "outside/duanzong/pdm/pdmList";
	}
	
	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(PdmListParam param) throws Exception {
		param.setDelivery_vendor_nm(param.getSessionUser().getCompanyNm());
		GridResultVO result = commonSelectList(param, service);
		return result;
	}
}
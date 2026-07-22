package kr.esob.fdms.controller.inside.distribution.docpdfrequest;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.value.RequestPopupInfo;
import kr.esob.fdms.commonlogic.value.SearchAllPopupInfo;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/inside/distribution/docPdfRequest")
public class DocPdfRequestController extends AbstractController {
	@Inject
	DocPdfRequestService service;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		//model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formDocPdfRequest")));
		//model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarDocPdfRequest")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDocPdfRequestList")));

		return "/inside/distribution/docPdfRequestList";
		//return "/inside/distribution/docRequestList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(DocPdfRequestParam param) throws Exception {
		service.setSearchAllParam(param);
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

}

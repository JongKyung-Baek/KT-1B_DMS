package kr.esob.fdms.controller.inside.distribution.printapproval;

import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.controller.inside.distribution.approval.ApprovalListParam;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/inside/distribution/printApproval")
public class PrintApprovalController extends AbstractController {

	@Inject
	PrintApprovalService service;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formPrintApproval")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarPrintApproval")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridPrintApprovalList")));

		return "/inside/distribution/printApproval/printApprovalList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(PrintApprovalListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

}

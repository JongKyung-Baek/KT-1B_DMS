package kr.esob.tdms.controller.general.distribution.printapproval;

import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.tdms.commonlogic.grid.GridResultVO;
import kr.esob.tdms.controller.general.distribution.approval.ApprovalListParam;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/general/distribution/printApproval")
public class PrintApprovalController extends AbstractController {

	@Inject
	PrintApprovalService service;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formPrintApproval")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarPrintApproval")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridPrintApprovalList")));

		return "/general/distribution/printApproval/printApprovalList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(PrintApprovalListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

}

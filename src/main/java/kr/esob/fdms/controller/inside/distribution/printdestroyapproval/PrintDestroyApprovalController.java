package kr.esob.fdms.controller.inside.distribution.printdestroyapproval;


import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalParam;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/inside/distribution/printDestroyApproval")
public class PrintDestroyApprovalController extends AbstractController {
	@Inject
	PrintDestroyApprovalService service;

	@RequestMapping(value = "/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formPrintDestroyApproval")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarPrintDestroyApproval")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridPrintDestroyApprovalList")));

		return "inside/distribution/printDestroyApproval/printDestroyApprovalList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(PrintDestroyApprovalListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/selecDestroyApprovalList")
	public @ResponseBody GridResultVO selecDestroyApprovalList(PrintDestroyApprovalListParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectPopupList(param));
//		result.setRecords(service.selectListCount(param));
//		BeanUtils.setProperty(result, "page", BeanUtils.getProperty(obj, "page"));
//		BeanUtils.setProperty(result, "size", BeanUtils.getProperty(obj, "size"));
		return result;
	}

	@RequestMapping(value = "/approvalPopup")
	public String approvalPopup(PrintDestroyApprovalListParam param, Model model, HttpServletRequest request) throws JsonProcessingException {
		model.addAttribute("gridInfo",JSONArray.fromObject(gridService.selectGridInfo("gridPrintDestroyApprovalPopupList")));
		model.addAttribute("destroyRequestNo", request.getParameter("destroyRequestNo"));
		model.addAttribute("data", service.getDestroyRequest(request.getParameter("destroyRequestNo")));
//		model.addAttribute("listCount", service.selectPopupListCount(param));
		return "inside/distribution/printDestroyApproval/printDestroyApprovalPopup";
	}

	@RequestMapping(value="/saveDestroyApproval", method = RequestMethod.POST)
	public @ResponseBody ResultVO saveDestroyApproval(@RequestBody PrintDestroyApprovalPopupParam param) {
		return service.saveApproval(param);
	}

	@RequestMapping(value="/batchSaveApproval", method = RequestMethod.POST)
	public @ResponseBody ResultVO batchSaveApproval(@RequestBody PrintDestroyApprovalPopupParam param) {
		ResultVO resultVo = new ResultVO();
		for(PrintDestroyApprovalPopupParam vo : param.getList()) {
			vo.setSaveType(param.getSaveType());
			resultVo = service.saveApproval(vo);
		}
		return resultVo;
	}

}

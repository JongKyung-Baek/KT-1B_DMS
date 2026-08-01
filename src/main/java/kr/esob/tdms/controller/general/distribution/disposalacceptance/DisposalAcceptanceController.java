package kr.esob.tdms.controller.general.distribution.disposalacceptance;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.tdms.commonlogic.combo.ComboService;
import kr.esob.tdms.commonlogic.grid.GridResultVO;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.general.authorization.AuthorizationDao;
import kr.esob.tdms.controller.general.authorization.AuthorizationService;
import kr.esob.tdms.commonlogic.distribution.model.DestroyFileDownloadParam;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/general/distribution/disposalacceptance")
public class DisposalAcceptanceController extends AbstractController {

	@Inject
	DisposalAcceptanceService service;


	@Inject
	ComboService comboService;

	@Inject
	AuthorizationService authService;

	@Inject
	AuthorizationDao authorizationDao;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formDisposalAcceptance")));
		model.addAttribute("toolbarInfo", "");
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDisposalAcceptanceList")));

		return "general/distribution/disposalacceptance/acceptanceList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(DisposalAcceptanceListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/approvalPopup")
	public String approvalPopup(Model model, DisposalAcceptanceParam param) {
		DisposalAcceptancePopupVO info = service.selectDisposalInfo(param);
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDisposalAcceptancePopup")));
		model.addAttribute("fileList", service.selectDisposalFileList(param));
		model.addAttribute("formData", info);
		return "general/distribution/disposalacceptance/approvalPopup";
	}

	@RequestMapping("/selectPopupList")
	public @ResponseBody GridResultVO selectPopupList(DisposalAcceptanceParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectPopupList(param));
		return result;
	}

	@GetMapping("/fileDownload")
	public void fileDownload(@RequestParam String destroyRequestNo,
							 @RequestParam int destroyFileSeq,
							 HttpServletResponse response) throws Exception {
		DestroyFileDownloadParam param = new DestroyFileDownloadParam();
		param.setDestroyRequestNo(destroyRequestNo);
		param.setDestroyFileSeq(destroyFileSeq);
		service.fileDownload(param, response);
	}

	@PostMapping("/saveApproval")
	public @ResponseBody ResultVO saveApproval(@RequestBody DisposalAcceptanceParam param) {
		ResultVO resultVo = service.saveApproval(param);
		return resultVo;
	}

}

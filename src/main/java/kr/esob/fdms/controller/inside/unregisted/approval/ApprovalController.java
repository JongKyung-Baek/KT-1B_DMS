package kr.esob.fdms.controller.inside.unregisted.approval;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import kr.esob.fdms.controller.inside.unregisted.requeststatus.RequestStatusService;
import net.sf.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/inside/unregisted/approval")
public class ApprovalController extends AbstractController {
	@Inject
	ApprovalService service;

	@Inject
	ComboService comboService;

	@Inject
	AuthorizationService authService;

	@Inject
	RequestStatusService requestStatusService;

	@RequestMapping("/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formUnregistedApproval")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarUnregistedApproval")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridUnregistedApprovalList")));

		return "inside/unregisted/approval/approvalList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(ApprovalListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}


	@RequestMapping("/approvalPopup")
	public String approvalPopup(ApprovalPopupParam param, Model model) {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridUnregistedRequestStatusPopup")));
		//미등록자료 배포요청 정보
		model.addAttribute("data", service.getRequestInfo(param));

		model.addAttribute("requestNo", param.getRequestNo());

		model.addAttribute("listCount", service.selectPopupListCount(param));
		//배포 유형 콤보
		ComboInfoVO combo = new ComboInfoVO();
		combo.setComboCd("distributionMethod");
		model.addAttribute("distributionMethod", comboService.selectComboList(combo));

		//파일 배포 영역 콤보
		combo = new ComboInfoVO();
		combo.setComboCd("distributionFileType");
		model.addAttribute("distributionFileType", comboService.selectComboList(combo));

		//보안팀 정보(email 참조용)
		model.addAttribute("securityUserList", authService.selectSecurityUserList());
		model.addAttribute("deployEmailCC", requestStatusService.getDeployEmailCC(param));

		return "inside/unregisted/approval/approvalPopup";
	}


	@RequestMapping("/selectPopupList")
	public @ResponseBody GridResultVO selectPopupList(ApprovalPopupParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectPopupList(param));
		return result;
	}

	/**
	 *  미등록자료 배포 > 배포승인
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/saveApproval", method = RequestMethod.POST)
	public @ResponseBody ResultVO saveApproval(@RequestBody ApprovalPopupParam param) {
		return service.saveApproval(param);
	}

	@RequestMapping(value="/batchSaveApproval", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> batchSaveApproval(@RequestBody ApprovalPopupParam param){
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		ResultVO resultVo = new ResultVO();
		for(ApprovalPopupParam vo : param.getList()) {
			vo.setSaveType(param.getSaveType());
			resultVo = service.saveApproval(vo);
		}
		rtnMap.put("result", resultVo);
		return rtnMap;
	}

}

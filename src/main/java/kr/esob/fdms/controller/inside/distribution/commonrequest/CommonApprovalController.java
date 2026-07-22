package kr.esob.fdms.controller.inside.distribution.commonrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import net.sf.json.JSONArray;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/inside/distribution")
public class CommonApprovalController extends AbstractController {

	@Inject
	CommonApprovalService service;

	@Inject
	AuthorizationService authService;

	@Inject
	ComboService comboService;

	/**
	 * 배포 승인 팝업
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/approval/approvalPopup", method = RequestMethod.POST)
	public String approvalPopup(CommonApprovalParam param, Model model, HttpServletRequest request) {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridApprovalPopup")));
		model.addAttribute("listCount", service.selectListCount(param));
		model.addAttribute("requestDesc", service.getDistributionApprovalRequestDesc(param));
		model.addAttribute("requestNo", param.getRequestNo());
		model.addAttribute("approvalLineId", param.getApprovalLineId());
		model.addAttribute("currentProcessSeqNo", param.getCurrentProcessSeqNo());
		model.addAttribute("objectType", param.getObjectType());
		return "/inside/distribution/approval/approvalPopup";
	}

	@RequestMapping("/approval/selecApprovalList")
	public @ResponseBody GridResultVO selecDistributionApprovalList(CommonApprovalParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectApprovalList(param));
		BeanUtils.setProperty(result, "page", BeanUtils.getProperty(param, "page"));
		BeanUtils.setProperty(result, "size", BeanUtils.getProperty(param, "size"));
		return result;
	}

	/**
	 * 자료배포 > 배포승인, 출력물승인
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/saveApproval", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> saveApproval(@RequestBody CommonApprovalParam param) {
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		rtnMap.put("result", service.saveApproval(param));
		return rtnMap;
	}

	@RequestMapping(value="/savePass", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> savePass(@RequestBody PassParamVO param) {
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		rtnMap.put("result", service.savePass(param));
		return rtnMap;
	}
	
	@RequestMapping(value="/batchSaveApproval", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> batchSaveApproval(@RequestBody CommonApprovalParam param){
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		ResultVO resultVo = new ResultVO();
		for(CommonApprovalParam vo : param.getList()) {
			vo = service.selectRequestInfo(vo);
			vo.setSaveType(param.getSaveType());
			resultVo = service.saveApproval(vo);
		}
		rtnMap.put("result", resultVo);
		return rtnMap;
	}

	/**
	 * 출력물 승인 팝업
	 * @param param
	 * @param model
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value="/printApproval/approvePopup")
	public String printApprovalPopup(CommonApprovalParam param, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridPrintApprovalPopupList")));
		model.addAttribute("requestDesc", service.getDistributionApprovalRequestDesc(param));
		model.addAttribute("listCount", service.selectListCount(param));
		model.addAttribute("requestNo", param.getRequestNo());
		model.addAttribute("objectType", param.getObjectType());
		return "/inside/distribution/printApproval/printApprovalPopup";
	}


}

package kr.esob.fdms.controller.inside.production.approval;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

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
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/inside/production/approval")
public class ApprovalController extends AbstractController {
	@Inject
	ApprovalService service;

	@RequestMapping(value="/approvalTab")
	public String approvalTab(Model model, CommonHomeParam param) throws JsonProcessingException {
		return "/inside/production/approval/approvalTab";
	}

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formProductionApproval")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarProductionApproval")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionApprovalList")));

		return "inside/production/approval/approvalList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(ApprovalListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}


	/**
	 * 생산기술자료 배포 승인/반려 팝업
	 * @param model
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value="/approvalPopup")
	public String approvalPopup(Model model, ApprovalPopupParam param) throws JsonProcessingException {
		//접수자 정보 목록 GridInfo
		model.addAttribute("gridRequestInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionApprovalRequestList")));
		//접수자 정보 목록 listCount
//		model.addAttribute("requestListCount", service.selectRequestUserListCount(param));


		//배포승인 자료 목록 GridInfo
		model.addAttribute("gridObjectInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionApprovalObjectList")));
		//배포승인 자료 목록 listCount
//		model.addAttribute("objectListCount", service.selectObjectListCount(param));
		model.addAttribute("objectType", param.getObjectType());
		model.addAttribute("requestNo", param.getRequestNo());
		model.addAttribute("data", service.getRequestInfo(param));
		return "inside/production/approval/approvalPopup";
	}

	@RequestMapping(value="/replaceApprovalPopup")
	public String replaceApprovalPopup(Model model, ApprovalPopupParam param) throws JsonProcessingException {
		model.addAttribute("gridObjectInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionReplaceApprovalList")));
		model.addAttribute("objectType", param.getObjectType());
		model.addAttribute("requestNo", param.getRequestNo());
		model.addAttribute("data", service.getRequestInfo(param));
		return "inside/production/approval/replaceApprovalPopup";
	}

	@RequestMapping("/selectReplaceRequestList")
	public @ResponseBody GridResultVO selectReplaceRequestList(ApprovalPopupParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectReplaceRequestList(param));
		return result;
	}


	@RequestMapping("/selectRequestUserList")
	public @ResponseBody GridResultVO selectRequestUserList(ApprovalPopupParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectRequestUserList(param));
		return result;
	}

	@RequestMapping("/selectObjectList")
	public @ResponseBody GridResultVO selectObjectList(ApprovalPopupParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectObjectList(param));
		return result;
	}

	@RequestMapping("/approval")
	public @ResponseBody ResultVO approval(@RequestBody ApprovalPopupParam param) throws Exception {
		return service.approval(param);
	}

	@RequestMapping("/replaceApproval")
	public @ResponseBody ResultVO replaceApproval(@RequestBody ApprovalPopupParam param) throws Exception {
		return service.replaceApproval(param);
	}

	/**
	 * 생산기술자료 배포요청 상세보기 팝업
	 * @param model
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value="/printApprovalPopup")
	public String printApprovalPopup(ApprovalPopupParam param, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridPrintApproval")));
		model.addAttribute("data", service.getPrintRequestInfo(param));
		return "inside/production/approval/printApprovalPopup";
	}


	/**
	 * 생산기술자료 접수자 리스트 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectPrintApprovalrList")
	public @ResponseBody GridResultVO selectPrintApprovalrList(ApprovalPopupParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectPrintApprovalrList(param));
		return result;
	}

	/**
	 * 생산기술자료 출력 승인
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/printApproval")
	public @ResponseBody ResultVO printApproval(@RequestBody ApprovalPopupParam param) throws Exception {
		return service.printApproval(param);
	}

	@RequestMapping(value="/batchSaveApproval", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> batchSaveApproval(@RequestBody ApprovalPopupParam param) {
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		ResultVO resultVo = new ResultVO();
		for(ApprovalPopupParam vo : param.getList()) {
			vo.setSaveType(param.getSaveType());
			if("NEW".equals(vo.getRequestPurpose())) {
				resultVo = service.approval(vo);
				if(!resultVo.isSuccess())break;
			}else if("PRINT".equals(vo.getRequestPurpose())){
				resultVo = service.printApproval(vo);
			}else {
				resultVo = service.replaceApproval(vo);
				if(!resultVo.isSuccess())break;
			}
		}
		rtnMap.put("result", resultVo);
		return rtnMap;
	}



}

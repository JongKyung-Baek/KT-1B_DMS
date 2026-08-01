package kr.esob.tdms.controller.general.production.requeststatus;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.tdms.commonlogic.combo.ComboDao;
import kr.esob.tdms.commonlogic.grid.GridResultVO;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.general.production.approval.ApprovalPopupParam;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/general/production/requeststatus")
public class RequestStatusController extends AbstractController {
	@Inject
	RequestStatusService service;
	@Inject
	ComboDao comboDao;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formProductionRequestStatus")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarProductionRequestStatus")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionRequestStatusList")));

		return "general/production/requestStatus/requestStatusList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(RequestStatusListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}


	/**
	 * 생산기술자료 배포요청 상세보기 팝업
	 * @param model
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value="/requestStatusPopup")
	public String requestStatusPopup(RequestStatusPopupParam param, Model model) throws JsonProcessingException {
		model.addAttribute("gridAcceptance", JSONArray.fromObject(gridService.selectGridInfo("gridRequestStatusAcceptance")));
		model.addAttribute("gridProduction", JSONArray.fromObject(gridService.selectGridInfo("gridRequestStatusProduction")));
		model.addAttribute("watermarkYn", comboDao.selectComboListByCd("comboYn"));
		model.addAttribute("requestNo", param.getRequestNo());
		model.addAttribute("objectType", param.getObjectType());
		model.addAttribute("data", service.getRequestInfo(param));
		return "general/production/requestStatus/requestStatusPopup";
	}
	
	
	/**
	 * 생산기술자료 > 출력 요청 팝업
	 * @param param
	 * @param model
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value="/printRequestStatusPopup")
	public String printRequestStatusPopup(RequestStatusPopupParam param, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridPrintApproval")));
		model.addAttribute("data", service.getPrintRequestInfo(param));
		return "general/production/requestStatus/printRequestStatusPopup";
	}



	/**
	 * 생산기술자료 배포요청 상세보기 팝업
	 * @param model
	 * @return
	 * @throws JsonProcessingException
	 */
//	@RequestMapping(value="/printApprovalPopup")
//	public String printApprovalPopup(RequestStatusPopupParam param, Model model) throws JsonProcessingException {
//		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridPrintApproval")));
//		model.addAttribute("data", service.getPrintRequestInfo(param));
//		return "general/production/requestStatus/printApprovalPopup";
//	}


	/**
	 * 생산기술자료 접수자 리스트 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectAcceptanceUserList")
	public @ResponseBody GridResultVO selectAcceptanceUserList(RequestStatusPopupParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectAcceptanceUserList(param));
		return result;
	}

	/**
	 * 생산기술자료 아이템 리스트 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectProductionList")
	public @ResponseBody GridResultVO selectProductionList(RequestStatusPopupParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectProductionList(param));
		return result;
	}



	/**
	 * 생산기술자료 접수자 리스트 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
//	@RequestMapping("/selectPrintApprovalrList")
//	public @ResponseBody GridResultVO selectPrintApprovalrList(RequestStatusPopupParam param) throws Exception {
//		GridResultVO result = new GridResultVO();
//		result.setContents(service.selectPrintApprovalrList(param));
//		return result;
//	}

//	/**
//	 * 생산기술자료 출력 승인
//	 * @param param
//	 * @return
//	 * @throws Exception
//	 */
//	@RequestMapping("/printApproval")
//	public @ResponseBody ResultVO printApproval(@RequestBody RequestStatusPopupParam param) throws Exception {
//		return service.printApproval(param);
//	}

	
	@RequestMapping("/selectPrintRequestList")
	public @ResponseBody GridResultVO selectPrintRequestList(RequestStatusPopupParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectPrintRequestList(param));
		return result;
	}


}

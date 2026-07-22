package kr.esob.fdms.controller.inside.distribution.history;


import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.value.SearchAllPopupInfo;
import kr.esob.fdms.controller.outside.commondestroyrequest.DestroyRequestParam;
import net.sf.json.JSONArray;

/**
 * 내부사용자 이력조회
 * @author younjh
 *
 */
@Controller
@RequestMapping("/inside/distribution/history")
public class HistoryController extends AbstractController {
	@Inject
	HistoryService service;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formDistributionHistory")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarDistributionHistory")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDistributionHistoryList")));

		return "/inside/distribution/history/historyList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(HistoryListParam param) throws Exception {
		service.setSearchAllParam(param);
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	/*
	 * 거래 중단 팝업
	 */
	@RequestMapping(value="/stopDealPopup")
	public String stopDealPopup(Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridStopDealList")));
		return "/inside/distribution/history/stopDealPopup";
	}

	/*
	 * 폐기 팝업
	 */
	@RequestMapping(value="/destroyPopup")
	public String destroyPopup(DestroyPopupParam param, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridCrRequestUploadPopup")));
		if("1".equals(param.getDestroyType())) {		// 유효기간 만료
			model.addAttribute("destroyType", "자동폐기");
			model.addAttribute("destroyDesc", "자동폐기");
		}else if("3".equals(param.getDestroyType())) {	// 업체 거래 중단
			model.addAttribute("destroyDesc", "업체거래중단으로 폐기 처리함.");
		}

		return "/inside/distribution/history/destroyPopup";
	}

	/*
	 * 폐기중
	 */
	@RequestMapping(value="/underDestroy")
	public @ResponseBody ResultVO underDestroy(@RequestBody HistoryListVO param) throws Exception {
		return service.updateUnderDestroy(param);
	}
	
	/**
	 * 업체 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/selectCompanyList")
	public @ResponseBody GridResultVO selectCompanyList(stopDealPopupParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectCompanyList(param));
		result.setRecords(service.selectCompanyListCount(param));
		BeanUtils.setProperty(result, "page", BeanUtils.getProperty(param, "page"));
		BeanUtils.setProperty(result, "size", BeanUtils.getProperty(param, "size"));
		return result;
	}

	@RequestMapping("/deleteCompany")
	public @ResponseBody ResultVO deleteCompany(@RequestBody CompanyListVO param) throws Exception {
		return service.deleteCompany(param);
	}

	@RequestMapping(value="/destory")
	public @ResponseBody ResultVO crRequest(MultipartHttpServletRequest request) throws Exception {
		return service.insertDestory(request);
	}

	@RequestMapping(value="/searchAllPopup")
	public String searchAllPopup(Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDistributionHistorySearchAll")));
		model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(SearchAllPopupInfo.ALL));
		return "/inside/common/searchAllPopup";
	}



}

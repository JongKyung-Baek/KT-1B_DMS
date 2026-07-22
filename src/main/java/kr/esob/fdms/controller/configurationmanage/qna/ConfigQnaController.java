package kr.esob.fdms.controller.configurationmanage.qna;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.DateUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/configurationmanage/qna")
public class ConfigQnaController extends AbstractController {
	@Inject
	ConfigQnaService service;
	
	@Inject
	ComboService comboService;

	@Inject
	DateUtil dateUtil;

	@RequestMapping(value="/")
	public String home(ConfigQnaListParam param, Model model) throws JsonProcessingException {
		UserVO user = param.getSessionUser();
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formConfigQna")));
		
		if(!"E".equals(user.getAuthSite()) || "RG_001".equals(user.getRoleGroup())) {
			toolbarService.selectToolbarInfo("toolbarConfigQna").remove(0);
		}
		
		if(!"RG_001".equals(user.getRoleGroup())) {
			toolbarService.selectToolbarInfo("toolbarConfigQna").remove(1);
		}
		
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarConfigQna")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridConfigQnaList")));

		return "configurationmanage/qna/qnaList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(ConfigQnaListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}
	
	/*
	 * 등록 팝업
	 */
	@RequestMapping(value="/addPopup")
	public String addPopup(ConfigQnaPopupVO param, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridAddConfigQna")));
		model.addAttribute("qnaType", comboService.selectComboList("qnaType"));
		return "configurationmanage/qna/addPopup";
	}
	
	/*	
	 * 삽입
	 * */
	@RequestMapping(value="/configQna")
	public @ResponseBody ResultVO addQna(MultipartHttpServletRequest request) throws Exception {
		return service.insertConfigQna(request);
	}
	
	/*
	 * 확인 팝업
	 * */
	@RequestMapping(value="/qnaPopup")
	public String noticePopup(ConfigQnaPopupVO param, Model model) throws JsonProcessingException {
		UserVO user = param.getSessionUser();
		ConfigQnaPopupVO data = service.getQnaInfo(param);
		
		if("I".equals(user.getAuthSite())) {
			data.setAnswerUserNm(user.getUserNm());
		}
		
		model.addAttribute("companyNm", data.getCompanyNm());
		model.addAttribute("insertUserNm", data.getInsertUserNm());
		model.addAttribute("data", data);
		model.addAttribute("qnaType", comboService.selectComboList("qnaType"));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridConfigQnaPopup")));
		model.addAttribute("formData", JSONObject.fromObject(service.selectQnaFileInfo(param)));
		
		return "configurationmanage/qna/qnaPopup";
	}
	
	/*
	 * 수정
	 * */
	@RequestMapping(value="/updateQna")
	public @ResponseBody ResultVO updateQna(MultipartHttpServletRequest request) throws Exception {
		return service.updateConfigQna(request);
	}
	
	/*
	 * 제거
	 * */
	@RequestMapping(value="/deleteQna", method = RequestMethod.POST)
	public @ResponseBody ResultVO deleteQna(@RequestBody ConfigQnaPopupVO param) {
		ResultVO resultVo = new ResultVO();
		for(ConfigQnaPopupVO vo : param.getList()) {
			vo = service.getQnaInfo(vo);
			resultVo = service.saveQna(vo);
		}
		return resultVo;
	}
	
	/*
	 * 답글 삽입
	 * */
	@RequestMapping(value="/replyQna")
	public @ResponseBody ResultVO replyQna(MultipartHttpServletRequest request) throws Exception {
		return service.replyQna(request);
	}
	
	@RequestMapping("/fileDownload")
	public void fileDownload(ConfigQnaPopupVO param, HttpServletResponse response, HttpServletRequest request) throws Exception {
		service.fileDownload(param, response, request);
	}
}

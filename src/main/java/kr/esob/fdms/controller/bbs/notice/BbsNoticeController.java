package kr.esob.fdms.controller.bbs.notice;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
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
import kr.esob.fdms.commonlogic.menu.MenuService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.login.UserVO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/bbs/notice")
public class BbsNoticeController extends AbstractController {
	@Inject
	BbsNoticeService service;
	@Inject
	MenuService menuService;

	@Inject
	ComboService comboService;

	@RequestMapping(value="/")
	public String home(BbsNoticeListParam param,Model model) throws JsonProcessingException {
		UserVO user = param.getSessionUser();
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formBbsNotice")));
		if("RG_001".equals(user.getRoleGroup()) || "RG_002".equals(user.getRoleGroup()) ) {
			model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarBbsNotice")));
		}
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridBbsNoticeList")));

		return "bbs/notice/noticeList";
	}

	@RequestMapping(value="/noticePopup")
	public String noticePopup(HttpServletRequest request, HttpServletResponse response, BbsNoticeListParam listParam,BbsNoticePopupParam param, Model model) throws JsonProcessingException {
		UserVO user = param.getSessionUser();
		String updateFlag = null;

		if("RG_001".equals(user.getRoleGroup())) {
			updateFlag = "N";
		}else {
			updateFlag = "Y";
		}
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridNoticePopup")));
		model.addAttribute("updateFlag", updateFlag);
		model.addAttribute("data", service.getRequestInfo(param));
		model.addAttribute("noticeCd", param.getNoticeCd());
		if("RG_001".equals(user.getRoleGroup()) == false) {
			model.addAttribute("hitCount",service.updateHitCount(param));
		}

		model.addAttribute("formData", JSONObject.fromObject(service.selectNoticePopupInfo(param)));
		return "bbs/notice/noticePopup";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(BbsNoticeListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/selecNoticeList")
	public GridResultVO selecNoticeList(BbsNoticeListParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectList(param));
		return result;
	}

	/*
	 * 등록 팝업
	 */
	@RequestMapping(value="/addPopup")
	public String addPopup(HttpServletRequest request, HttpServletResponse response, BbsNoticeAddParam param,Model model) throws JsonProcessingException {
		UserVO user = param.getSessionUser();
		if(!"RG_001".equals(user.getRoleGroup()) && !"RG_002".equals(user.getRoleGroup()) ) {
			response.setStatus(403);
			return null;
		}

		model.addAttribute("gridInfo",JSONArray.fromObject(gridService.selectGridInfo("gridAddNotice")));
		//로그인한 userName
		model.addAttribute("insertUserNm",param.getSessionUser().getUsername());
		return "/bbs/notice/addPopup";
	}
	/*
	 * 삽입
	 * */
	@RequestMapping(value="/bbsNotice")
	public @ResponseBody ResultVO bbsNotice(MultipartHttpServletRequest request, HttpServletResponse response) throws Exception {
		UserVO user = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if(!"RG_001".equals(user.getRoleGroup()) && !"RG_002".equals(user.getRoleGroup()) ) {
			response.setStatus(403);
			return null;
		}
		return service.insertBbsNotice(request);
	}

	@RequestMapping(value="/updateNotice")
	public @ResponseBody ResultVO updateNotice(MultipartHttpServletRequest request, HttpServletResponse response) throws Exception {
		UserVO user = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if(!"RG_001".equals(user.getRoleGroup()) && !"RG_002".equals(user.getRoleGroup()) ) {
			response.setStatus(403);
			return null;
		}

		return service.updateNoticeInfo(request);
	}

	/*
	 * 제거
	 * */
	@RequestMapping(value="/deleteNotice", method = RequestMethod.POST)
	public @ResponseBody ResultVO deleteNotice(@RequestBody BbsNoticePopupParam param, HttpServletResponse response) {
		UserVO user = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if(!"RG_001".equals(user.getRoleGroup()) && !"RG_002".equals(user.getRoleGroup()) ) {
			response.setStatus(403);
			return null;
		}

		ResultVO resultVo = new ResultVO();
		for(BbsNoticePopupParam vo : param.getList()) {
			vo = service.selectNoticeInfo(vo);
			resultVo = service.saveNotice(vo);
		}
		return resultVo;
	}

	@RequestMapping("/fileDownload")
	public void fileDownload(BbsNoticeFileVO param, HttpServletResponse response, HttpServletRequest request) throws Exception {
		service.fileDownload(param, response, request);
	}
}

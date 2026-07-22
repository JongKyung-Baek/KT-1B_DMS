package kr.esob.fdms.controller.configurationmanage.notice;

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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/configurationmanage/notice")
public class ConfigNoticeController extends AbstractController {
	
	@Inject
	ConfigNoticeService service;

	@Inject
	ComboService comboService;

	@RequestMapping(value="/")
	public String home(ConfigNoticeListParam param, Model model) throws JsonProcessingException {
		UserVO user = param.getSessionUser();
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formConfigNotice")));
		
		if("I".equals(user.getAuthSite())) {
			model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarConfigNotice")));
		}

		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridConfigNoticeList")));

		return "configurationmanage/notice/noticeList";
	}
	
	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(ConfigNoticeListParam param) throws Exception {
		UserVO user = param.getSessionUser();
		if("I".equals(user.getAuthSite()) && param.getInsertUserNm().equals("")) {
			param.setInsertUserNm(user.getUserCd());
		}
		GridResultVO result = commonSelectList(param, service);
		return result;
	}
	
	/*
	 * 등록 팝업
	 */
	@RequestMapping(value="/addPopup")
	public String addPopup(ConfigNoticeAddParam param, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridAddConfigNotice")));
		model.addAttribute("insertUserNm", param.getSessionUser().getUsername());
		model.addAttribute("noticeType", comboService.selectComboList("noticeType"));
		return "configurationmanage/notice/addPopup";
	}
	
	/*
	 * 삽입
	 * */
	@RequestMapping(value="/configNotice")
	public @ResponseBody ResultVO configNotice(MultipartHttpServletRequest request) throws Exception {
		return service.insertConfigNotice(request);
	}
	
	/*
	 * 확인 팝업
	 * */
	@RequestMapping(value="/noticePopup")
	public String noticePopup(ConfigNoticePopupVO param, Model model) throws JsonProcessingException {
		UserVO user = param.getSessionUser();
		String updateFlag = null;

		if("E".equals(user.getAuthSite())) {
			updateFlag = "Y";
			model.addAttribute("hitCount", service.updateHitCount(param));
		} else {
			updateFlag = "N";
		}
		
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridConfigNoticePopup")));
		model.addAttribute("formData", JSONObject.fromObject(service.selectNoticeFileInfo(param)));
		model.addAttribute("updateFlag", updateFlag);
		model.addAttribute("data", service.getNoticeInfo(param));
		model.addAttribute("noticeType", comboService.selectComboList("noticeType"));
		model.addAttribute("noticeCd", param.getNoticeCd());
		
		return "configurationmanage/notice/noticePopup";
	}
	
	/*
	 * 수정
	 * */
	@RequestMapping(value="/updateNotice")
	public @ResponseBody ResultVO updateNotice(MultipartHttpServletRequest request) throws Exception {
		return service.updateConfigNotice(request);
	}
	
	/*
	 * 제거
	 * */
	@RequestMapping(value="/deleteNotice", method = RequestMethod.POST)
	public @ResponseBody ResultVO deleteNotice(@RequestBody ConfigNoticePopupVO param) {
		ResultVO resultVo = new ResultVO();
		UserVO user = param.getSessionUser();
		for(ConfigNoticePopupVO vo : param.getList()) {
			vo = service.getNoticeInfo(vo);
			if(user.getUserCd().equals(vo.getInsertUserCd())) {
				resultVo = service.saveNotice(vo);
			} else {
				resultVo.setSuccess(false);
			}
		}
		return resultVo;
	}
	
	@RequestMapping("/fileDownload")
	public void fileDownload(ConfigNoticeAddParam param, HttpServletResponse response, HttpServletRequest request) throws Exception {
		service.fileDownload(param, response, request);
	}
}

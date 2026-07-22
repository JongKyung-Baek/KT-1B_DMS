package kr.esob.fdms.controller.bbs.qna;

import javax.inject.Inject;

import kr.esob.fdms.commonlogic.toolbar.ToolbarInfoVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.DateUtil;
import net.sf.json.JSONArray;

import java.util.List;

@Controller
@RequestMapping("/bbs/qna")
public class BbsQnaController extends AbstractController {
	@Inject
	BbsQnaService service;

	@Inject
	DateUtil dateUtil;


	@RequestMapping(value="/")
	public String home(BbsQnaListParam param, Model model) throws JsonProcessingException {
		UserVO user = param.getSessionUser();
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formBbsQna")));

		// 툴바 정보를 변수에 저장
		List<ToolbarInfoVO> toolbarInfo = toolbarService.selectToolbarInfo("toolbarBbsQna");

		System.out.println("아오");
		System.out.println("user.getRoleGroup() >>> " + user.getRoleGroup());
		System.out.println(toolbarInfo.get(1).getButtonId());
		System.out.println("아오");

		if ("btnDelete".equals(toolbarInfo.get(1).getButtonId())) {
			if (!"RG_001".equals(user.getRoleGroup())) {
				toolbarInfo.remove(1);
			}
		}

		// 변경된 툴바 정보를 모델에 추가
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarInfo));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridBbsQnaList")));

		return "bbs/qna/qnaList";
	}

//	@RequestMapping(value="/")
//	public String home(BbsQnaListParam param, Model model) throws JsonProcessingException {
//		UserVO user = param.getSessionUser();
//		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formBbsQna")));
//
//		System.out.println("아오");
//		System.out.println("user.getRoleGroup() >>> " + user.getRoleGroup());
//		System.out.println(toolbarService.selectToolbarInfo("toolbarBbsQna").get(1).getButtonId());
//		System.out.println("아오");
//
//		if("btnDelete".equals(toolbarService.selectToolbarInfo("toolbarBbsQna").get(1).getButtonId())) {
//			if("RG_001".equals(user.getRoleGroup()) == false) {
//				toolbarService.selectToolbarInfo("toolbarBbsQna").remove(1);
//			}
//		}
//		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarBbsQna")));
//		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridBbsQnaList")));
//
//		return "bbs/qna/qnaList";
//	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(BbsQnaListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping(value="/qnaPopup")
	public String qnaPopup(BbsQnaPopupParam param, Model model) throws JsonProcessingException {
		UserVO user = param.getSessionUser();
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridQnaPopup")));
		model.addAttribute("data", service.getRequestInfo(param));
		model.addAttribute("qnaCd", param.getQnaCd());
		if("RG_001".equals(user.getRoleGroup()) == false) {
			model.addAttribute("hitCount", service.updateHitCount(param));
		}
		return "bbs/qna/qnaPopup";
	}
	/*
	 * 추가 팝업
	 */
	@RequestMapping(value="/addPopup")
	public String addPopup(BbsQnaAddParam param, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridAddQna")));
		model.addAttribute("insertUserNm",param.getSessionUser().getUsername());
		return "/bbs/qna/addPopup";
	}
	/*
	 * 삽입
	 * */
	@RequestMapping(value="/bbsQna")
	public @ResponseBody ResultVO bbsQna(MultipartHttpServletRequest request) throws Exception {
		return service.insertBbsQna(request);
	}

	/*
	 * 답변 팝업
	 */
	@RequestMapping(value="/replyPopup")
	public String replyPopup(BbsQnaPopupParam param,Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridReplyQna")));
		model.addAttribute("data", service.getRequestInfo(param));
		model.addAttribute("insertUserNm",param.getSessionUser().getUsername());
		model.addAttribute("qnaCd", param.getQnaCd());

		return "/bbs/qna/replyPopup";
	}

	//답글 삽입
	@RequestMapping(value="/replyQna")
	public @ResponseBody ResultVO replyQna(MultipartHttpServletRequest request) throws Exception {
		return service.replyBbsQna(request);
	}

	/*
	 * 제거
	 * */
	@RequestMapping(value="/deleteQna", method = RequestMethod.POST)
	public @ResponseBody ResultVO deleteQna(@RequestBody BbsQnaPopupParam param) {
		ResultVO resultVo = new ResultVO();
		for(BbsQnaPopupParam vo : param.getList()) {
			vo = service.selectQnaInfo(vo);
			resultVo = service.saveQna(vo);
		}
		return resultVo;
	}
}

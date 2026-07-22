package kr.esob.fdms.controller.outside.commondestroyrequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.bbs.notice.BbsNoticeService;
import kr.esob.fdms.controller.bbs.qna.BbsQnaService;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.distribution.history.DestroyPopupParam;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.DateUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/outside/commonDestroyRequest")
public class CommonDestroyRequestController extends AbstractController {

	@Inject
	CommonDestroyRequestService destroyRequestService;

	@Inject
	AuthorizationDao authorizationDao;

	@RequestMapping("/destroyRequestPopup")
	public String destroyRequestPopup(Model model, DestroyRequestParam param) throws JsonProcessingException {
		model.addAttribute("destroyType", param.getDestroyType());
		model.addAttribute("destroyTypeNm", param.getDestroyTypeNm());
		model.addAttribute("requestType", param.getRequestType());
//		model.addAttribute("list", JSONArray.fromObject(param.getList()).toString());
		//model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(ApprovalStatusPopupInfo.DRAWING));
		model.addAttribute("approvalUser", authorizationDao.comboList("sql.Authorization.selectPurchaserCombo", param));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDestroyRequestUploadPopup")));
		return "outside/commondestroy/commonDestroyRequestPopup";
	}

	@RequestMapping(value="/underDestroy")
	public @ResponseBody ResultVO underDestroy(@RequestBody DestroyRequestParam param) throws Exception {
		return destroyRequestService.updateUnderDestroy(param);
	}
	
	@RequestMapping(value="/destoryRequest")
	public @ResponseBody ResultVO destoryRequest(MultipartHttpServletRequest request) throws Exception {

		// 임시 코드
		String destroyParamStr = request.getParameter("destroyParam");
		System.out.println("destroyParam 입력 값: " + destroyParamStr);

		return destroyRequestService.insertDestory(request);
	}
}

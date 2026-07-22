package kr.esob.fdms.controller.main;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.config.SessionExtendController;
import kr.esob.fdms.controller.bbs.notice.BbsNoticePopupParam;
import kr.esob.fdms.controller.bbs.notice.BbsNoticePopupVO;
import kr.esob.fdms.controller.bbs.notice.BbsNoticeService;
import kr.esob.fdms.controller.bbs.qna.BbsQnaService;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.DateUtil;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import java.util.Iterator;

@Controller
@RequestMapping("/main")
public class MainController extends AbstractController {

	@Inject
	MainService service;

	@Inject
	BbsNoticeService noticeService;

	@Inject
	BbsQnaService qnaService;

	@Inject
	DateUtil dateUtil;


	@RequestMapping("")
	public String home(MainParam param, Model model) {
		String format = "yyyy-MM-dd";

		//param.setStartDt(dateUtil.getAddDay(-30, format));
		//param.setEndDt(dateUtil.getToday(format));

		JSONObject result = new JSONObject();

		UserVO userVo = param.getSessionUser();

		if("I".equals(userVo.getAuthSite())) {
			// 내부사용자
			if("Y".equals(userVo.getPurchaseTeamYn()) || "직".equals(userVo.getDeptType())) {
//				"Y".equals(userVo.getProductTeamYn()) ||
				// 생기팀, '직' 만 접수를 보여주고 나머지 팀은 요청을 보여준다.
				result.put("accept", 		getAccept(param));				// 접수
			}
			else {
				result.put("request", 		getRequest(param));				// 요청
			}

			result.put("approval", 			getApproval(param));			// 승인
			if(!isApprovalUser(param)) {
				// 팀장이 아닐 경우에만 거절 내역이 의미 있음
				result.put("reject", 			getReject(param));				// 반려
			}
			model.addAttribute("pendingApprovalList", service.selectPendingApprovalList(param));	// 미승인 목록
			model.addAttribute("pendingReviewList", service.selectPendingReviewList(param));		// 미검토 목록
		}
		else if("E".equals(userVo.getAuthSite())) {
			// 외부 사용자
			result.put("accept", 			getOutsideAccept(param));		// 접수 - 내가 요청한 것 중 현재 상태가 접수
			result.put("approval", 			getOutsideApproval(param));		// 승인 - 내가 요청한 것 중 현재 상태가 승인
			result.put("reject", 			getOutsideReject(param));		// 반려 - 내가 요청한 것 중 현재 상태가 반려
			model.addAttribute("disposalValidEnd", 	service.selectDisposalValidEnd(param));		// 폐기기한 임박(표시는 유효기간 임박으로 표시)
		}

		model.addAttribute("data", 			result);
		model.addAttribute("notice", 		noticeService.selectMainList(param));
		model.addAttribute("qna", 			qnaService.selectMainList(param));
		model.addAttribute("startDt", 		param.getStartDt());
		model.addAttribute("endDt", 		param.getEndDt());

		BbsNoticePopupVO notice = noticeService.selectAlertNotice();

		if(null != notice) {
			model.addAttribute("noticeAlert", notice);
			BbsNoticePopupParam noticeParam = new BbsNoticePopupParam();
			noticeParam.setNoticeCd(notice.getNoticeCd());
			model.addAttribute("formData", JSONObject.fromObject(noticeService.selectNoticePopupInfo(noticeParam)));
		}

		SessionExtendController.sessionTime = service.selectSessionTime();

		return "/main/main";
	}

	/**
	 * 내가 요청한 내역 - 구매/생기 외 모든 팀
	 * @param param
	 * @return
	 */
	private JSONObject getRequest(MainParam param) {
		JSONObject obj = new JSONObject();

		if("Y".equals(param.getSessionUser().getPurchaseYn())) {
			//구매가능팀의 경우 배포 요청 표시
			obj.put("distribution", service.selectRequestCount(param));		// 배포요청 수
		}

		obj.put("print",			service.selectRequestPrintCount(param));						// 출력요청
		//obj.put("printDisposal",	service.selectRequestPrintDisposalCount(param));				// 출력 폐기 요청


		setSum(obj);

		return obj;
	}

	private JSONObject getAccept(MainParam param) {
		JSONObject obj = new JSONObject();

		if(isApprovalUser(param)) {
			// 팀장은 어떤경우에도 접수가 있을 수 없음. 아무처리 안함.
		}
		else {
			// 팀장이 아닌 경우 구매팀과 생산기술팀만 '접수' 항목이 있음.
			if("Y".equals(param.getSessionUser().getPurchaseTeamYn())) {
				obj.put("distribution", service.selectHaveToAcceptDistributionCount(param));		// 배포요청 수
				obj.put("cr", service.selectHaveToAcceptCrCount(param));							// CR 수
			}
//			else if("Y".equals(param.getSessionUser().getProductTeamYn())) {
			else if("직".equals(param.getSessionUser().getDeptType())) {
				obj.put("production",service.selectHaveToAcceptProductionCount(param));
			}
			setSum(obj);
		}


		return obj;
	}

	@SuppressWarnings("unchecked")
	private void setSum(JSONObject obj) {
		Iterator<String> it = obj.keySet().iterator();

		int sum = 0;

		while(it.hasNext()) {
			String key = it.next();
			sum += obj.getInt(key);
		}

		obj.put("sum", sum);
	}

	/**
	 * 반려
	 * @param param
	 * @return
	 */
	private JSONObject getReject(MainParam param) {
		JSONObject obj = new JSONObject();

		if(isApprovalUser(param)) {
//			if("Y".equals(param.getSessionUser().getPurchaseYn())) {
//				// 구매가능한 팀일 경우
//				obj.put("distribution", 		service.selectDistributionRejectCount(param));
//				obj.put("cr", service.selectRejectCrCount(param));
//				obj.put("unreg",service.selectRejectUnregCount(param));
//			}
//			if("Y".equals(param.getSessionUser().getProductTeamYn())) {
//				// 생산기술팀일 경우
//				obj.put("production",			service.selectRejectProductionCount(param));
//				obj.put("productionPrint",		service.selectRejectProductionPrintCount(param));
//				obj.put("productionDisposal",service.selectRejectProductionDisposalCount(param));
//			}
//
//			// 아래 두개는 모든 팀 가능
//			obj.put("print",service.selectPrintRejectCount(param));
//			obj.put("printDisposal",service.selectRejectPrintDisposalCount(param));
		}
		else {
			// 팀장이 아닐 경우
			if("Y".equals(param.getSessionUser().getPurchaseYn())) {
				// 구매가능한 팀일 경우
				obj.put("distribution", service.selectRejectedDistributionCount(param));
				//obj.put("cr", 			service.selectRejectedCrCount(param));
				obj.put("unreg",		service.selectRejectedUnregCount(param));
			}
			if("Y".equals(param.getSessionUser().getProductTeamYn())) {
				// 생산기술팀일 경우
				obj.put("production",			service.selectRejectedProductCount(param));
				//obj.put("productionPrint",		service.selectRejectedProductPrintCount(param));
				//obj.put("productionDisposal",	service.selectRejectedProductionDisposalCount(param));
			}

			obj.put("print",			service.selectPrintRejectedCount(param));
			//obj.put("printDisposal",service.selectRejectedPrintDisposalCount(param));
		}

		setSum(obj);

		return obj;
	}

	/**
	 * 승인
	 * @param param
	 * @return
	 */
	private JSONObject getApproval(MainParam param) {
		JSONObject obj = new JSONObject();

		if(isApprovalUser(param)) {
			if("Y".equals(param.getSessionUser().getPurchaseYn())) {
				// 구매가능한 팀일 경우
				obj.put("distribution", service.selectHaveToApprovalDistributionCount(param));
				obj.put("cr", service.selectHaveToApprovalCrCount(param));
				obj.put("unreg",service.selectHaveToApprovalUnregCount(param));
			}
			if("Y".equals(param.getSessionUser().getProductTeamYn())) {
				obj.put("production",			service.selectHaveToApprovalProductionCount(param));
				//obj.put("productionPrint",		service.selectHaveToApprovalProductionPrintCount(param));
				obj.put("productionDisposal",service.selectHaveToProductionDisposalCount(param));
			}

			// 아래 두개는 모든 팀 가능
			obj.put("print",service.selectHaveToApprovalPrintCount(param));
			obj.put("printDisposal",service.selectHaveToPrintDisposalCount(param));
		}
		else {
			// 팀장이 아닐 경우
			if("Y".equals(param.getSessionUser().getPurchaseYn())) {
				// 구매가능한 팀일 경우
				obj.put("distribution", service.selectApprovedDistributionCount(param));
				//obj.put("cr", 			service.selectApprovedCrCount(param));
				obj.put("unreg",		service.selectApprovedUnregCount(param));
			}
			if("Y".equals(param.getSessionUser().getProductTeamYn())) {
				obj.put("production",			service.selectApprovedProductionCount(param));
				//obj.put("productionPrint",		service.selectApprovedProductionPrintCount(param));
				//obj.put("productionDisposal",service.selectApprovedProductionDisposalCount(param));
			}

			// 아래 두개는 모든 팀 가능
			obj.put("print",		service.selectApprovedPrintCount(param));
			//obj.put("printDisposal",service.selectApprovedPrintDisposalCount(param));
		}

		setSum(obj);

		return obj;
	}

	/**
	 * 결재권한자인지 여부
	 * @return
	 */
	private boolean isApprovalUser(MainParam param) {
		UserVO userVo = param.getSessionUser();

		// 팀장이면 결재권한 있음.
		if(userVo.getUserCd().equals(userVo.getTeamLeaderUid())) {
			return true;
		}

		// 방산자료의 경우는 방산결재권한 테이블을 조회
		if(service.isProtectApprovalUser(param)) {
			return true;
		}

		return false;
	}

	/**
	 * 외부 사용자 - 접수
	 * @param param
	 * @return
	 */
	private JSONObject getOutsideAccept(MainParam param) {
		JSONObject obj = new JSONObject();

		param.setOutUserYn("Y");

		obj.put("drawing", 		service.selectAcceptedDrawingCount(param));			// 도면
		obj.put("doc", 			service.selectAcceptedDocCount(param));				// 문서
		obj.put("sw", 			service.selectAcceptedSwCount(param));				// sw
		obj.put("cr", 			service.selectAcceptedCrCount(param));				// cr
		obj.put("production", 	service.selectAcceptedProductCount(param));			// 생산기술자료
		// 미등록자료는 배포요청하지 않음. 배포 받기만 함.

		setSum(obj);

		return obj;
	}

	/**
	 * 외부사요자 승인된것
	 * @param param
	 * @return
	 */
	private JSONObject getOutsideApproval(MainParam param) {
		JSONObject obj = new JSONObject();

		param.setOutUserYn("Y");

		obj.put("drawing", 		service.selectApprovedDrawingCount(param));			// 도면
		obj.put("doc", 			service.selectApprovedDocCount(param));				// 문서
		obj.put("sw", 			service.selectApprovedSwCount(param));				// sw
		obj.put("cr", 			service.selectApprovedCrCount(param));				// cr
		obj.put("production", 	service.selectApprovedProductCount(param));			// 생산기술자료

		setSum(obj);

		return obj;
	}

	/**
	 * 외부사용자 - 반려된것
	 * @param param
	 * @return
	 */
	private JSONObject getOutsideReject(MainParam param) {
		JSONObject obj = new JSONObject();

		param.setOutUserYn("Y");

		obj.put("drawing", 		service.selectRejectedDrawingCount(param));			// 도면
		obj.put("doc", 			service.selectRejectedDocCount(param));				// 문서
		obj.put("sw", 			service.selectRejectedSwCount(param));				// sw
		obj.put("cr", 			service.selectRejectedCrCount(param));				// cr
		obj.put("production", 	service.selectRejectedProductCount(param));			// 생산기술자료

		setSum(obj);

		return obj;
	}

}

package kr.esob.fdms.controller.inside.distribution.commonrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.value.RequestPopupInfo;
import kr.esob.fdms.commonlogic.value.SearchAllPopupInfo;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import kr.esob.fdms.controller.inside.distribution.acceptance.AcceptancePopupListService;
import kr.esob.fdms.controller.inside.distribution.drawingrequest.DrawingPopupRequestService;
import kr.esob.fdms.controller.inside.distribution.drawingrequest.DrawingRequestParam;
import kr.esob.fdms.controller.inside.distribution.drawingrequest.DrawingRequestService;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import net.sf.json.JSONArray;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/inside/distribution/commonRequest")
public class CommonRequestController extends AbstractController {

	@Inject
	CommonDistributionRequestService service;

	@Inject
	AuthorizationService authService;

	@Inject
	ComboService comboService;

	@Inject
	AuthorizationDao authorizationDao;

	@Inject	// 임시
	AcceptancePopupListService popupService;

	@Inject
	DrawingRequestService drawingRequestService;

	@Inject
	DrawingPopupRequestService drawingPopupRequestService;

	@RequestMapping("/searchAllPopup")
	public String searchAll(Model model, HttpServletRequest request) throws JsonProcessingException {
		if("DRAWING".equals(request.getParameter("type"))) {
			model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDrawingRequestSearchAll")));
			model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(SearchAllPopupInfo.DRAWING));
		}else if("DOC".equals(request.getParameter("type"))) {
			model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDocRequestSearchAll")));
			model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(SearchAllPopupInfo.DOC));
		}else if("DOCPDF".equals(request.getParameter("type"))) {
			model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDocPdfRequestSearchAll")));
			// model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(SearchAllPopupInfo.DOCPDF));
		}else if("SW".equals(request.getParameter("type"))) {
			model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridSwRequestSearchAll")));
			model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(SearchAllPopupInfo.SW));
		}else if("PRODUCTION".equals(request.getParameter("type"))) {
			model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProductionRequestSearchAll")));
			model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(SearchAllPopupInfo.PRODUCTION));
		}else if("HISTORY".equals(request.getParameter("type"))) {
			model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDistributionHistorySearchAll")));
			model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(SearchAllPopupInfo.ALL));
		}
		return "/inside/common/searchAllPopup";
	}

	/**
	 * 자료배포 > 검색 및 배포요청 > 도면/문서/SW > 배포승인요청 팝업
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value= {"/drawingRequestPopup", "/docRequestPopup", "/docPdfRequestPopup", "/swRequestPopup", "/productionRequestPopup", "/dxfRequestPopup" }, method=RequestMethod.POST)
	public String commonRequestPopup(CommonDistributionRequestParam param, Model model, HttpServletRequest request) {
		//요청 유형 (DRAWING, DOC, DOCPDF(변환) SW, PRODUCT_DOC, PRODUCT_SW)
		model.addAttribute("requestType", param.getRequestType());

		//업체 리스트
		List<ComboInfoVO> companyCombo = new ArrayList<ComboInfoVO>();
		ComboInfoVO tempCompanyCombo = new ComboInfoVO();
		tempCompanyCombo.setComboLabel("선택하세요");
		tempCompanyCombo.setComboVal("");
		companyCombo.add(tempCompanyCombo);
		model.addAttribute("noSelect", companyCombo);

		ComboInfoVO combo = new ComboInfoVO();
		
		if("SW".equals(param.getRequestType())) {
			//배포 유형 콤보(SW)
			combo = new ComboInfoVO();
			combo.setComboCd("distributionMethod_sw");
			model.addAttribute("distributionMethod_sw", comboService.selectComboList(combo));
			
			//파일 배포 영역 콤보(SW)
			combo = new ComboInfoVO();
			combo.setComboCd("distributionFileType_sw");
			model.addAttribute("distributionFileType_sw", comboService.selectComboList(combo));
			
		}else {
			//배포 유형 콤보
			combo = new ComboInfoVO();
			combo.setComboCd("distributionMethod");
			model.addAttribute("distributionMethod", comboService.selectComboList(combo));
			
			//파일 배포 영역 콤보
			combo = new ComboInfoVO();
			combo.setComboCd("distributionFileType");
			model.addAttribute("distributionFileType", comboService.selectComboList(combo));
		}

//		if("Y".equals(param.getProtectYn())) {
			//방산기술보호 결재자
			//개발
//			model.addAttribute("productProtectYn", "Y".equals(param.getProductProtectYn()) ? "N" : "Y" );
			//양산
//			model.addAttribute("developmentProtectYn", "Y".equals(param.getDevelopmentProtectYn()) ? "N" : "Y" );

			//양산 결재자 리스트
//			if("Y".equals(param.getProductProtectYn())){
//				model.addAttribute("productProtectUserList", authService.selectProductProtectUserList());
//			}
			//개발 결재자 리스트
//			if("Y".equals(param.getDevelopmentProtectYn())){
//				model.addAttribute("developmentProtectUserList", authService.selectDevelopmentProtectUserList());
//			}
//		}else {
			//양산
//			model.addAttribute("productProtectYn",  "Y");
			//개발
//			model.addAttribute("developmentProtectYn", "Y");
//		}
//		model.addAttribute("protectYn", "Y".equals(request.getParameter("protectYn")) ? "N" : "Y");	//방산 팀장 disabled 여부
//		if("Y".equals(request.getParameter("protectYn"))) {		//방산이 하나라도 포함되어 있으면
//			//방산팀장 정보
//			model.addAttribute("defendTeamLeaderUid", authService.getDefenseTeamLeaderUid());
//			model.addAttribute("defendTeamLeaderList", authService.selectDefenseTeamLeaderList());
//		}


		model.addAttribute("protectYn", "Y".equals(request.getParameter("protectYn")) ? "N" : "Y");	//방산 팀장 disabled 여부
		if("Y".equals(request.getParameter("protectYn"))) {		//방산이 하나라도 포함되어 있으면
			List<ComboInfoVO> list = new ArrayList<ComboInfoVO>();
			List<String> defaultUserList = new ArrayList<String>();
			int proectUserCnt= 0;
			if("Y".equals(param.getProductProtectYn())){	//양산
				proectUserCnt++;
				System.out.println(param.getSessionUser().getBusinessAreaCd());
				list = authService.selectProductProtectUserList(param.getSessionUser().getBusinessAreaCd());
				for(ComboInfoVO tempVo : list) {
					defaultUserList.add(tempVo.getComboVal());
				}
			}

			if("Y".equals(param.getDevelopmentProtectYn())){	//개발
				proectUserCnt++;
				if(list.size() == 0) {
					list = authService.selectDevelopmentProtectUserList();
				}else {
					list.addAll(authService.selectDevelopmentProtectUserList());
				}
				defaultUserList.add("USER_0000012085");

//				for(ComboInfoVO tempVO : list) {
//					if("USER_0000012085".equals(tempVO.getComboVal())) {
//						defaultUserList.add(tempVO.);
//					}
//				}
			}
			//방산팀장 정보
			model.addAttribute("defaultProtectUserList", defaultUserList);
			model.addAttribute("protectUserList", list);
			model.addAttribute("protectUserCount", proectUserCnt);
		}

		//결재자 정보 (로그인한 사용자의 팀장)
		model.addAttribute("teamLeaderList", authorizationDao.comboList("sql.Authorization.selectApprovalUserList", param));
		List<ComboInfoVO> teamLeader = authService.getTeamLeader(param);
		model.addAttribute("teamLeaderUid", teamLeader.size() > 0 ? teamLeader.get(0).getComboVal() : "");
		//구매팀 정보
//		List<ComboInfoVO> purchaseTeamCombo = new ArrayList<ComboInfoVO>();
//		ComboInfoVO tempPurchaseTeamCombo = new ComboInfoVO();
//		tempPurchaseTeamCombo.setComboCd(param.getSessionUser().getUserCd());
//		tempPurchaseTeamCombo.setComboLabel(param.getSessionUser().getUserNm());
//		purchaseTeamCombo.add(tempPurchaseTeamCombo);
		model.addAttribute("purchaseTeam", authorizationDao.comboList("sql.Authorization.selectPurchaserCombo", param.getSessionUser().getBusinessAreaCd()));
//		model.addAttribute("purchaseTeam", purchaseTeamCombo);

		//용도 (유효기간) - selectBox
		combo = new ComboInfoVO();
		combo.setComboCd("requestPurpose");
		model.addAttribute("deployList", comboService.selectComboList(combo));
		model.addAttribute("isProductTeam", service.isProductTeam(param));
		String gridId = getPopupGridId(param.getRequestType());

		// 유효기간 설정을 위한 grid
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo(gridId)));
		return "/inside/distribution/commonRequest/commonRequestPopup";
	}

	@RequestMapping(value="/approvalStatusPopup", method={RequestMethod.GET, RequestMethod.POST})
	public String approvalStatusPopup() {
		return "/inside/distribution/commonRequest/approvalStatusPopup";
	}

	private String getPopupGridId(String requestType) {
		if("DRAWING".equals(requestType)) { return "gridDrawingRequestUseEndYmdPopup"; }
		if("DOC".equals(requestType)) { return "gridDocRequestUseEndYmdPopup"; }
		if("DOCPDF".equals(requestType)) { return "gridDocPdfRequestUseEndYmdPopup"; }
		if("SW".equals(requestType)) { return "gridSwRequestUseEndYmdPopup"; }

		return "gridProductRequestUseEndYmdPopup";
	}

	/**
	 * 사용자 이메일 조회
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/getUserEmail")
	public @ResponseBody Map<String, Object> getUserEmail(HttpServletRequest request) {
		Map<String, Object> rtnMap = new HashMap<String, Object>();
		rtnMap.put("email", service.getUserEmail(request.getParameter("userCd")));
		return rtnMap;
	}


	/**
	 * 배포 승인 요청
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/approvalRequest", method = RequestMethod.POST)
	public @ResponseBody ResultVO approvalRequest(@RequestBody CommonDistributionRequestParam param) {
		System.out.println("approvalRequest");System.out.println("approvalRequest");System.out.println("approvalRequest");
		ResultVO resultVo = service.saveApprovalRequest(param);
		return resultVo;
	}

	@RequestMapping(value="/verificationProtectUser", method = RequestMethod.POST)
	public @ResponseBody ResultVO verificationProtectUser(@RequestBody CommonDistributionRequestParam param) {
		return service.verificationProtectUser(param);
	}


	/**
	 * 출력 승인 요청
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/commonPrintRequestPopup", method = RequestMethod.POST)
	public String commonPrintRequestPopup(CommonDistributionRequestParam param, Model model, HttpServletRequest request) {
		//요청 유형 (DRAWING, DOC, SW)
		model.addAttribute("requestType", param.getRequestType());

		//요청자 (로그인한 사용자
		model.addAttribute("requestUser", param.getSessionUser().getUsername());

		//배포 유형 콤보
		ComboInfoVO combo = new ComboInfoVO();
//		combo.setComboCd("distributionType");
//		model.addAttribute("distributionType", comboService.selectComboList(combo));

//		if("Y".equals(param.getProtectYn())) {
//			//방산기술보호 결재자
//			//개발
//			model.addAttribute("productProtectYn", "Y".equals(param.getProductProtectYn()) ? "N" : "Y" );
//			//양산
//			model.addAttribute("developmentProtectYn", "Y".equals(param.getDevelopmentProtectYn()) ? "N" : "Y" );
//
//			//양산 결재자 리스트
//			if("Y".equals(param.getProductProtectYn())){
//				model.addAttribute("productProtectUserList", authService.selectProductProtectUserList(param.getSessionUser().getBusinessAreaCd()));
//			}
//			//개발 결재자 리스트
//			if("Y".equals(param.getDevelopmentProtectYn())){
//				model.addAttribute("developmentProtectUserList", authService.selectDevelopmentProtectUserList());
//			}
//		}else {
//			//양산
//			model.addAttribute("productProtectYn",  "Y");
//			//개발
//			model.addAttribute("developmentProtectYn", "Y");
//		}

		model.addAttribute("protectYn", "Y".equals(request.getParameter("protectYn")) ? "N" : "Y");	//방산 팀장 disabled 여부
		if("Y".equals(request.getParameter("protectYn"))) {		//방산이 하나라도 포함되어 있으면
			List<ComboInfoVO> list = new ArrayList<ComboInfoVO>();
			List<String> defaultUserList = new ArrayList<String>();
			int proectUserCnt= 0;
			if("Y".equals(param.getProductProtectYn())){	//양산
				proectUserCnt++;
				System.out.println(param.getSessionUser().getBusinessAreaCd());
				list = authService.selectProductProtectUserList(param.getSessionUser().getBusinessAreaCd());
				for(ComboInfoVO tempVo : list) {
					defaultUserList.add(tempVo.getComboVal());
				}
			}

			if("Y".equals(param.getDevelopmentProtectYn())){	//개발
				proectUserCnt++;
				if(list.size() == 0) {
					list = authService.selectDevelopmentProtectUserList();
				}else {
					list.addAll(authService.selectDevelopmentProtectUserList());
				}
				defaultUserList.add("USER_0000012085");

//				for(ComboInfoVO tempVO : list) {
//					if("USER_0000012085".equals(tempVO.getComboVal())) {
//						defaultUserList.add(tempVO.);
//					}
//				}
			}
			//방산팀장 정보
			model.addAttribute("defaultProtectUserList", defaultUserList);
			model.addAttribute("protectUserList", list);
			model.addAttribute("protectUserCount", proectUserCnt);
		}

//		model.addAttribute("protectYn", "Y".equals(request.getParameter("protectYn")) ? "N" : "Y");	//방산 팀장 disabled 여부
//		if("Y".equals(request.getParameter("protectYn"))) {		//방산이 하나라도 포함되어 있으면
//			//방산팀장 정보
//			model.addAttribute("defendTeamLeaderUid", authService.getDefenseTeamLeaderUid());
//			model.addAttribute("defendTeamLeaderList", authService.selectDefenseTeamLeaderList());
//		}

		//결재자 정보 (로그인한 사용자의 팀장)
		model.addAttribute("teamLeaderList", authorizationDao.comboList("sql.Authorization.selectApprovalUserList", param));
		List<ComboInfoVO> teamLeader = authService.getTeamLeader(param);
		model.addAttribute("teamLeaderUid", teamLeader.size() > 0 ? teamLeader.get(0).getComboVal() : "");

		//용도 (유효기간) - selectBox
		combo = new ComboInfoVO();
		combo.setComboCd("printRequestType");
		model.addAttribute("destroyType", comboService.selectComboList(combo));
		return "/inside/distribution/commonRequest/commonPrintRequestPopup";
	}

	
	
	
	
	
	@RequestMapping(value="/protectPopup")
	public String protectPopup(CommonDistributionRequestParam param, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridProtectPopupList")));
		model.addAttribute("objectId", param.getObjectId());
		model.addAttribute("objectType", param.getObjectType());
		return "/inside/common/protectPopup";
	}

	@RequestMapping("/selectProtectPopupList")
	public @ResponseBody GridResultVO selectProtectPopupList(CommonDistributionRequestParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectProtectPopupList(param));
		BeanUtils.setProperty(result, "page", BeanUtils.getProperty(param, "page"));
		BeanUtils.setProperty(result, "size", BeanUtils.getProperty(param, "size"));
		return result;
	}

	@RequestMapping(value= {"/drawingRevisionUpdatePopup", "/docRevisionUpdatePopup", "/docpdfRevisionUpdatePopup", "/swRevisionUpdatePopup", "/productRevisionUpdatePopup", "/dxfRevisionUpdatePopup" }, method=RequestMethod.POST)
	public String commonRevisionUpdatePopup(CommonDistributionRequestParam param, Model model, HttpServletRequest request) {
		String uri = request.getRequestURI();
		String drawingNo = "";

		LocalDate now = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String date = now.format(formatter);

		// 등록자, 등록일
		model.addAttribute("registerUser", param.getSessionUser().getUsername());
		model.addAttribute("date", date);

		String htmlDrawingNo = request.getParameter("drawingNo");
		String drawingNm = request.getParameter("drawingNm");
		String objectNm = request.getParameter("objectNm");
		String curRevNo = request.getParameter("revNo");

		int newRevNoInt = Integer.parseInt(curRevNo) + 1;
		String newRevNo = Integer.toString(newRevNoInt);

		// String prevCustomerRevision = request.getParameter("customerRevision");
		String hiddenObjectId = param.getObjectId();

		ComboInfoVO distributeTypeCd = new ComboInfoVO();
		distributeTypeCd = new ComboInfoVO();
		distributeTypeCd.setComboCd("distributeTypeCd");

		if (uri.contains("drawingRevisionUpdatePopup")) {
			drawingNo = extractDrawingNo(htmlDrawingNo);	// html형식에서 도번만 추출
		} else{
			drawingNo = htmlDrawingNo;
		}

//		String nextCustomerRevision = getNextCustomerRevision(prevCustomerRevision);

		ComboInfoVO distributionPoint = new ComboInfoVO();
		distributionPoint = new ComboInfoVO();
		distributionPoint.setComboCd("distributionPoint");
		distributionPoint.setComboLabel("선택하세요");

		model.addAttribute("distributionPoint", comboService.selectComboList(distributionPoint));
		model.addAttribute("drawingNo", drawingNo);
		model.addAttribute("drawingNm", drawingNm);	// 도면 - 자료명
		model.addAttribute("objectNm", objectNm);    // 생산기술자료 - 자료명
		model.addAttribute("curRevNo", curRevNo);
		model.addAttribute("newRevNo", newRevNo);
//		model.addAttribute("customerRevision", nextCustomerRevision);
		model.addAttribute("hiddenObjectId", hiddenObjectId);
		model.addAttribute("distributeTypeCd", comboService.selectComboList(distributeTypeCd));

		// 이 아래 뭔지 모름
		model.addAttribute("updownCabUrl", SystemConfig.getSystemConfigValue("UPDOWN_CAB_URL"));
		model.addAttribute("updownServerIp", SystemConfig.getSystemConfigValue("UPDOWN_SERVER_IP"));
		model.addAttribute("updownServerPort", SystemConfig.getSystemConfigValue("UPDOWN_SERVER_PORT"));
		model.addAttribute("updownPath", SystemConfig.getSystemConfigValue("UPDOWN_PATH"));
		model.addAttribute("updownSecretKey", SystemConfig.getSystemConfigValue("UPDOWN_SECRET_KEY"));
		model.addAttribute("updownLangCode", SystemConfig.getSystemConfigValue("UPDOWN_LANG_CODE"));
		model.addAttribute("updownIsSecurity", SystemConfig.getSystemConfigValue("UPDOWN_IS_SECURITY"));
		model.addAttribute("updownExtension", SystemConfig.getSystemConfigValue("UPDOWN_EXTENSION"));

		System.out.println("uri: " + uri);
		if (uri.contains("drawingRevisionUpdatePopup")) {
			return "inside/distribution/drawingRevisionUpdatePopup";
		} else if(uri.contains("productionRevisionUpdatePopup")){
			return "inside/distribution/production/productRevisionUpdatePopup";
		} else if(uri.contains("dxfRevisionUpdatePopup")){
			return "inside/distribution/dxf/dxfRevisionUpdatePopup";
		} else{
			return "inside/distribution/drawingRevisionUpdatePopup";	// 우선 도면 리비전 갱신 뷰 반환
		}

	}

	@RequestMapping(value= {"/drawingVersionCheckPopup", "/docVersionCheckPopup", "/docpdfVersionCheckPopup", "/swVersionCheckPopup", "/productVersionCheckPopup", "/dxfVersionCheckPopup" }, method=RequestMethod.POST)
	public String commonVersionCheckPopup(CommonDistributionRequestParam param, Model model, HttpServletRequest request) throws JsonProcessingException {

		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDrawingRequestList")));
		model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(RequestPopupInfo.DRAWING));
//		model.addAttribute("popupInfo", new ObjectMapper().writeValueAsString(SearchAllPopupInfo.DRAWING));
		model.addAttribute("listTitle", RequestPopupInfo.DRAWING.getListTitle());
		model.addAttribute("dataType", RequestPopupInfo.DRAWING.getDataType());
		model.addAttribute("drawingNo", request.getParameter("drawingNo"));
		return "/inside/distribution/drawingVersionCheckPopup";
	}

	@RequestMapping(value={"/selectDrawingPopupList", "/selectProductDistributionPopupList"})
	public @ResponseBody GridResultVO selectList(DrawingRequestParam param) throws Exception {
		param.setCheckVersion(true);

		String htmlDrawingNo = param.getDrawingNo();
		String drawingNo = extractDrawingNo(htmlDrawingNo);
		param.setDrawingNo(drawingNo);

		GridResultVO result = commonSelectList(param, drawingPopupRequestService);
		return result;
	}

	@RequestMapping(value="/compareImageRequest", method=RequestMethod.POST)
	public @ResponseBody ResultVO distributionRequest(@RequestBody RequestParam param, Authentication authentication) throws Exception {
		ResultVO resultVO = new ResultVO();

		String result = drawingRequestService.compareImageRequest(param, authentication);

		if(result.startsWith("redirect:")) {
			// "redirect:" 접두어 이후의 URL을 별도 필드로 담음
			String redirectUrl = result.substring("redirect:".length());
			resultVO.setSuccess(true);
			resultVO.setRedirectUrl(redirectUrl);
		}
		else {
			resultVO.setFailReason(result);
			resultVO.setMessage("Request failed: " + result);
			resultVO.setSuccess(false);
		}
		return resultVO;
	}

	private String extractDrawingNo(String html){
		String decoded = StringEscapeUtils.unescapeHtml4(html);
		int start = decoded.indexOf(">") + 1;
		int end = decoded.lastIndexOf("<");
		String drawingNoOnly = decoded.substring(start, end);
		return drawingNoOnly;
	}

	private String getNextCustomerRevision(String prevCustomerRevision){
		char currentRevision = prevCustomerRevision.charAt(0);
		char nextRevision = (currentRevision == 'Z') ? 'Z' : (char)(currentRevision + 1);
		String result = String.valueOf(nextRevision);

		return result;
	}
}

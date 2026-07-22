package kr.esob.fdms.controller.inside.distribution.production;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.form.FormInfoService;
import kr.esob.fdms.commonlogic.grid.GridInfoService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.toolbar.ToolbarInfoService;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import kr.esob.fdms.controller.login.UserVO;
import net.sf.json.JSONArray;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
@RequestMapping("/inside/distribution/productionRequest")
public class ProductionRequestController extends AbstractController {
	@Inject
	FormInfoService formService;

	@Inject
	ToolbarInfoService toolbarService;

	@Inject
	GridInfoService gridService;

	@Inject
	ProductionRequestService service;

	@Inject
	AuthorizationService authService;

	@Inject
	ComboService comboService;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formProductionRequest")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarDistributionProductionRequest")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDistributionProductionRequestList")));

		return "/inside/distribution/production/productionRequestList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(ProductionRequestParam param, Authentication authentication) throws Exception {
		UserVO userVo = (UserVO) authentication.getPrincipal();
		param.setDeptNm(userVo.getDeptNm());

		param.setObjectType("DOC");
		service.setSearchAllParam(param);
		GridResultVO result = commonSelectList(param, service);
		return result;
	}

	@RequestMapping("/selectTree")
	public @ResponseBody List<ProductionRequestTreeVO> selectTree(@RequestBody ProductionRequestParam param) {
		param.setObjectType("DOC");
		return service.selectTree(param);
	}

	@RequestMapping("/nextProductionNo")
	public @ResponseBody Map<String, Object> nextProductionNo(@RequestBody Map<String, String> param) {
		String levelNo = param == null ? "" : param.get("levelNo");
		int nextNo = service.selectNextProductionRegisterNo(levelNo);

		Map<String, Object> result = new HashMap<>();
		result.put("nextRegisterNo", String.format("%03d", nextNo));
		result.put("documentNo", "K8-MRB-L" + (levelNo == null ? "" : levelNo.trim()) + "-" + String.format("%03d", nextNo));
		return result;
	}

	@RequestMapping("/delete")
	@ResponseBody
	public Map<String, Object> deletePrd(@RequestBody Map<String, List<Map<String, String>>> param,
			Authentication authentication) {
		Map<String, Object> result = new HashMap<>();

		// 사용자 정보 가져오기
		UserVO userVo = (UserVO) authentication.getPrincipal();
		List<Map<String, String>> list = param.get("list");
		int success = 0;
		int fail = 0;
		List<String> messages = new java.util.ArrayList<>();

		if (list != null) {
			for (Map<String, String> drawing : list) {
				String objectId = drawing.get("objectId");
				if (objectId != null && !objectId.isEmpty()) {
					ResultVO validation = service.validateDeletePrd(objectId, userVo);
					if (!validation.isSuccess()) {
						fail++;
						if (validation.getMessage() != null && !validation.getMessage().trim().isEmpty()) {
							messages.add(validation.getMessage());
						}
						continue;
					}
					success += service.deletePrd(objectId);
				}
			}
		}

		result.put("successCount", success);
		result.put("failCount", fail);
		result.put("error", fail > 0 ? "Y" : "N");
		if (!messages.isEmpty()) {
			result.put("message", String.join("\n", messages));
		}
		return result;
	}

	@PostMapping("/approve")
	@ResponseBody
	public Map<String, Object> approve(@RequestBody Map<String, List<Map<String, String>>> param,
									  Authentication authentication) {
		Map<String, Object> result = new HashMap<>();
		UserVO userVo = (UserVO) authentication.getPrincipal();

		List<Map<String, String>> list = param.get("list");
		int success = 0;
		int fail = 0;
		List<String> messages = new java.util.ArrayList<>();

		if (list != null) {
			for (Map<String, String> item : list) {
				String objectId = item.get("objectId");
				if (objectId == null || objectId.trim().isEmpty()) {
					fail++;
					continue;
				}

				ResultVO validation = service.validateApproveProduction(objectId, userVo);
				if (!validation.isSuccess()) {
					fail++;
					if (validation.getMessage() != null && !validation.getMessage().trim().isEmpty()) {
						messages.add(validation.getMessage());
					}
					continue;
				}

				ResultVO approveResult = service.approveProduction(objectId, userVo);
				if (approveResult.isSuccess()) {
					success++;
				} else {
					fail++;
					if (approveResult.getMessage() != null && !approveResult.getMessage().trim().isEmpty()) {
						messages.add(approveResult.getMessage());
					}
				}
			}
		}

		result.put("successCount", success);
		result.put("failCount", fail);
		result.put("error", fail > 0 ? "Y" : "N");
		if (!messages.isEmpty()) {
			result.put("message", String.join("\n", messages));
		}
		return result;
	}

	@PostMapping("/approveStatusMessage")
	@ResponseBody
	public Map<String, Object> approveStatusMessage(@RequestBody Map<String, Object> param) {
		Map<String, Object> result = new HashMap<>();
		String objectId = param == null ? null : (String) param.get("objectId");
		result.put("success", true);
		result.put("message", service.getApprovalStatusMessage(objectId));
		return result;
	}

	@PostMapping("/approveStatusRows")
	@ResponseBody
	public Map<String, Object> approveStatusRows(@RequestBody Map<String, String> param, Authentication authentication) {
		Map<String, Object> result = new HashMap<>();
		UserVO userVo = (UserVO) authentication.getPrincipal();
		result.put("rows", service.getApprovalStatusRows(param.get("objectId"), userVo));
		return result;
	}

	@PostMapping("/saveApprovalComment")
	@ResponseBody
	public ResultVO saveApprovalComment(@RequestBody Map<String, String> param, Authentication authentication) {
		UserVO userVo = (UserVO) authentication.getPrincipal();
		return service.saveApprovalComment(param.get("objectId"), param.get("comment"), userVo);
	}

	@RequestMapping("/productionFilePopup")
	public String productionFilePopup(ProductionRequestParam param, Model model) {
		List<Map<String, Object>> mainFileList = service.selectMainFileInfo(param.getObjectId());
		List<Map<String, Object>> subFileList = service.selectSubFileInfo(param.getObjectId());
		model.addAttribute("objectId", param.getObjectId());
		model.addAttribute("productionNo", param.getObjectNo());
		model.addAttribute("mainFileList", mainFileList);
		model.addAttribute("subFileList", subFileList);
		model.addAttribute("mainFileJson", JSONArray.fromObject(mainFileList));
		model.addAttribute("subFileJson", JSONArray.fromObject(subFileList));
		return "inside/distribution/production/productionFilePopup";
	}

	@RequestMapping("/selectRevisionUsers")
	public @ResponseBody Map<String, Object> selectRevisionUsers(@RequestParam(value = "objectId", required = false) String objectId) {
		objectId = objectId == null ? "" : objectId.trim();
		Map<String, Object> info = service.selectProductionApprovalInfo(objectId);
		Map<String, Object> result = new HashMap<>();
		String approver = "";
		if (info.get("approver") != null) approver = String.valueOf(info.get("approver"));
		else if (info.get("APPROVER") != null) approver = String.valueOf(info.get("APPROVER"));

		String reviewerUser = "";
		if (info.get("reviewerUser") != null) reviewerUser = String.valueOf(info.get("reviewerUser"));
		else if (info.get("revieweruser") != null) reviewerUser = String.valueOf(info.get("revieweruser"));
		else if (info.get("REVIEWERUSER") != null) reviewerUser = String.valueOf(info.get("REVIEWERUSER"));

		result.put("approver", approver);
		result.put("reviewerUser", reviewerUser);
		return result;
	}

	@GetMapping("/downloadFile")
	public ResponseEntity<byte[]> downloadFile(@RequestParam("objectId") String objectId,
			@RequestParam(value = "fileNo", required = false) String fileNo,
			@RequestParam(value = "watermarkYn", required = false, defaultValue = "Y") String watermarkYn,
			Authentication authentication) throws Exception {
		System.out.println("[MRB_VIEWER] downloadFile request objectId=" + objectId + ", fileNo=" + fileNo);
		Map<String, Object> fileInfo = service.getProductionFileDownloadInfo(objectId, fileNo);
		if (fileInfo == null || fileInfo.isEmpty()) {
			System.out.println("[MRB_VIEWER] downloadFile not found: fileInfo is empty");
			return ResponseEntity.notFound().build();
		}
		String filePath = fileInfo.get("filePath") == null ? "" : String.valueOf(fileInfo.get("filePath"));
		String orgFileNm = fileInfo.get("orgFileNm") == null ? "download.bin" : String.valueOf(fileInfo.get("orgFileNm"));
		if (!isPdfFilePath(filePath) && !isAdminRole(authentication)) {
			return ResponseEntity.status(403).build();
		}
		System.out.println("[MRB_VIEWER] downloadFile db.filePath=" + filePath + ", orgFileNm=" + orgFileNm);
		if (filePath.isEmpty() || !Files.exists(Paths.get(filePath))) {
			System.out.println("[MRB_VIEWER] downloadFile not found: file does not exist on disk");
			return ResponseEntity.notFound().build();
		}
		byte[] bytes = null;
		if ("Y".equalsIgnoreCase(watermarkYn)) {
			bytes = requestWatermarkPdf(filePath, orgFileNm, authentication);
		}
		if (bytes == null || bytes.length == 0) {
			bytes = Files.readAllBytes(Paths.get(filePath));
		}
		System.out.println("[MRB_VIEWER] downloadFile success bytes=" + bytes.length);
		String downloadFileName = buildDownloadFileName(filePath, orgFileNm);
		String encodedFileName = URLEncoder.encode(downloadFileName, "UTF-8").replaceAll("\\+", "%20");
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName)
				.contentLength(bytes.length)
				.body(bytes);
	}

	private String buildDownloadFileName(String filePath, String orgFileNm) {
		String downloadFileName = orgFileNm == null || orgFileNm.trim().isEmpty() ? "download.bin" : orgFileNm.trim();
		if (isPdfFilePath(filePath) && !downloadFileName.toLowerCase().endsWith(".pdf")) {
			int dotIndex = downloadFileName.lastIndexOf('.');
			downloadFileName = (dotIndex > 0 ? downloadFileName.substring(0, dotIndex) : downloadFileName) + ".pdf";
		}
		return downloadFileName;
	}

	private boolean isPdfFilePath(String filePath) {
		return filePath != null && filePath.trim().toLowerCase().endsWith(".pdf");
	}

	private byte[] requestWatermarkPdf(String inputPdfPath, String orgFileNm, Authentication authentication) {
		try {
			String watermarkApiUrl = resolveViewerWatermarkUrl();
			if (watermarkApiUrl == null || watermarkApiUrl.isEmpty()) {
				return null;
			}

			String watermarkText = buildWatermarkText(authentication);
			Map<String, Object> requestBody = new LinkedHashMap<>();
			requestBody.put("inputPdfPath", inputPdfPath);
			requestBody.put("watermarkText", watermarkText);
			requestBody.put("watermarkType", "text");
			requestBody.put("outputFileName", orgFileNm);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
			ResponseEntity<byte[]> response = restTemplate.exchange(
					watermarkApiUrl,
					HttpMethod.POST,
					entity,
					byte[].class
			);

			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().length > 0) {
				return response.getBody();
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	private String resolveViewerWatermarkUrl() {
		String viewerUrl = SystemConfig.getSystemConfigValue("VIEWER_URL");
		if (viewerUrl == null || viewerUrl.trim().isEmpty()) {
			return null;
		}

		String base = viewerUrl.trim();
		int webIdx = base.indexOf("/web");
		if (webIdx > -1) {
			base = base.substring(0, webIdx);
		}
		while (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base + "/watermark_download";
	}

	private String buildWatermarkText(Authentication authentication) {
		String userNm = "";
		String positionNm = "";
		try {
			UserVO userVo = (UserVO) authentication.getPrincipal();
			userNm = userVo.getUsername() == null ? "" : userVo.getUsername();
			positionNm = userVo.getPositionNm() == null ? "" : userVo.getPositionNm();
			if (positionNm.trim().isEmpty()) {
				positionNm = userVo.getPositionCd() == null ? "" : userVo.getPositionCd();
			}
		} catch (Exception ignored) {
		}
		String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		return userNm + " / " + positionNm + " / " + now;
	}
	//2023.07.24 기범추가 ( 등록 )

	private boolean isAdminRole(Authentication authentication) {
		try {
			UserVO userVo = (UserVO) authentication.getPrincipal();
			return "RG_001".equals(userVo.getRoleGroup());
		} catch (Exception ignored) {
			return false;
		}
	}

	private boolean isNonConvertibleFile(String fileName) {
		if (fileName == null) {
			return true;
		}
		String lower = fileName.toLowerCase();
		int dot = lower.lastIndexOf('.');
		if (dot < 0 || dot == lower.length() - 1) {
			return true;
		}
		String ext = lower.substring(dot + 1);
		return !java.util.Arrays.asList(
				"pdf"
		).contains(ext);
	}
	@RequestMapping("/productionRegisterPopup")
	public String registerPopup(ProductionRegisterPopupParam param, Model model) {
		LocalDate now = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String date = now.format(formatter);
//		List<String > distributeType = new ArrayList<>();
//		distributeType.add("개발");
//		distributeType.add("양산");
//		model.addAttribute("distributeType",distributeType);

		String registerUser = "";
		if (param.getSessionUser() != null) {
			if (param.getSessionUser().getUserNm() != null && !param.getSessionUser().getUserNm().trim().isEmpty()) {
				registerUser = param.getSessionUser().getUserNm();
			} else if (param.getSessionUser().getUsername() != null && !param.getSessionUser().getUsername().trim().isEmpty()) {
				registerUser = param.getSessionUser().getUsername();
			} else if (param.getSessionUser().getUserId() != null) {
				registerUser = param.getSessionUser().getUserId();
			}
		}
		model.addAttribute("registerUser", registerUser);
		model.addAttribute("date", date);
		model.addAttribute("treeCd", param.getTreeCd());

		ComboInfoVO distributeTypeCd = new ComboInfoVO();
		distributeTypeCd.setComboCd("distributeTypeCd");
		model.addAttribute("distributeTypeCd", comboService.selectComboList(distributeTypeCd));
		model.addAttribute("businessTypeCd", service.selectLevelOptions(param));
		model.addAttribute("swTypeCd", comboService.selectActiveUserList());// 임시 공동발행자 조회 -> docs_user where use_yn = 'Y'
	
		// 업다운 서버 설정값
		model.addAttribute("updownCabUrl", SystemConfig.getSystemConfigValue("UPDOWN_CAB_URL"));
		model.addAttribute("updownServerIp", SystemConfig.getSystemConfigValue("UPDOWN_SERVER_IP"));
		model.addAttribute("updownServerPort", SystemConfig.getSystemConfigValue("UPDOWN_SERVER_PORT"));
		model.addAttribute("updownPath", SystemConfig.getSystemConfigValue("UPDOWN_PATH"));
		model.addAttribute("updownSecretKey", SystemConfig.getSystemConfigValue("UPDOWN_SECRET_KEY"));
		model.addAttribute("updownLangCode", SystemConfig.getSystemConfigValue("UPDOWN_LANG_CODE"));
		model.addAttribute("updownIsSecurity", SystemConfig.getSystemConfigValue("UPDOWN_IS_SECURITY"));
		model.addAttribute("updownExtension", SystemConfig.getSystemConfigValue("UPDOWN_EXTENSION"));

		return "inside/distribution/production/productionRegisterPopup";
	}

	@PostMapping(value="/uploadProductionRegisFile")
	public @ResponseBody ResultVO uploadProductionRegisFile(MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
		// System.out.println(multipartHttpServletRequest.getFile("file").getName());

		return service.saveProductionRegisterFileX2(multipartHttpServletRequest);
	}
}

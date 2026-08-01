package kr.esob.tdms.controller.general.distribution.drawingrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.tdms.commonlogic.combo.ComboInfoVO;
import kr.esob.tdms.commonlogic.combo.ComboService;
import kr.esob.tdms.commonlogic.form.FormInfoService;
import kr.esob.tdms.commonlogic.grid.GridInfoService;
import kr.esob.tdms.commonlogic.grid.GridResultVO;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.tdms.commonlogic.toolbar.ToolbarInfoService;
import kr.esob.tdms.controller.general.authorization.AuthorizationService;
import kr.esob.tdms.controller.login.UserVO;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/general/distribution/drawingRequest")
public class DrawingRequestController extends AbstractController {
	@Inject
	FormInfoService formService;

	@Inject
	ToolbarInfoService toolbarService;

	@Inject
	GridInfoService gridService;

	@Inject
	DrawingRequestService service;

	@Inject
	SecurityAclService securityAclService;

	@Inject
	AuthorizationService authService;

	@Inject
	ComboService comboService;

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);

		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formDrawingRequest")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarDrawingRequest")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDrawingRequestList")));
		model.addAttribute("viewerUrl", SystemConfig.getSystemConfigValue("VIEWER_URL"));

		return "/general/distribution/drawingRequestList";
	}

	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(DrawingRequestParam param, Authentication authentication) throws Exception {
		UserVO userVo = (UserVO) authentication.getPrincipal();
		param.setDeptNm(userVo.getDeptNm());

		service.setSearchAllParam(param);
		GridResultVO result = commonSelectList(param, service);

		return result;
	}

	@RequestMapping("/selectTree")
	public @ResponseBody List<DrawingRequestTreeVO> selectTree(@RequestBody DrawingRequestParam param) {
		return service.selectTree(param);
	}

	@PostMapping("/delete")
	@ResponseBody
	public Map<String, Object> deleteDrawing(@RequestBody Map<String, List<Map<String, String>>> param, Authentication authentication) {
		Map<String, Object> result = new HashMap<>();
		UserVO userVo = (UserVO) authentication.getPrincipal();

		List<Map<String, String>> list = param.get("list");
		if (list == null || list.isEmpty()) {
			result.put("successCount", 0);
			result.put("error", "철회할 대상을 선택하세요.");
			return result;
		}

		for (Map<String, String> drawing : list) {
			String objectId = drawing.get("objectId");
			ResultVO validateResult = service.validateDeleteDrawing(objectId, userVo);
			if (!validateResult.isSuccess()) {
				result.put("successCount", 0);
				result.put("error", validateResult.getMessage());
				return result;
			}
		}

		int success = 0;
		for (Map<String, String> drawing : list) {
			String objectId = drawing.get("objectId");
			if (objectId != null && !objectId.isEmpty()) {
				success += service.deleteDrawing(objectId);
			}
		}

		if (success == 0) {
			result.put("error", "철회 처리에 실패했습니다.");
		}
		result.put("successCount", success);
		return result;

		}

// 승인 처리
	@PostMapping("/approve")
	@ResponseBody
	public Map<String, Object> approveDrawing(@RequestBody Map<String, List<Map<String, String>>> param, Authentication authentication) {
		Map<String, Object> result = new HashMap<>();
		UserVO userVo = (UserVO) authentication.getPrincipal();

		List<Map<String, String>> list = param.get("list");
		int successCount = 0;
		int failCount = 0;
		String message = "";

		if (list == null || list.isEmpty()) {
			result.put("successCount", 0);
			result.put("failCount", 0);
			result.put("message", "승인할 대상을 선택하세요.");
			return result;
		}

		// 하나라도 승인 조건 미충족이면 전체 승인 중단
		for (Map<String, String> drawing : list) {
			String objectId = drawing.get("objectId");
			ResultVO validateResult = service.validateApproveDrawing(objectId, userVo);
			if (!validateResult.isSuccess()) {
				result.put("successCount", 0);
				result.put("failCount", list.size());
				result.put("message", validateResult.getMessage());
				return result;
			}
		}

		// 전체 검증 통과 시에만 승인 실행
		for (Map<String, String> drawing : list) {
			String objectId = drawing.get("objectId");
			ResultVO approveResult = service.approveDrawing(objectId, userVo);
			if (approveResult.isSuccess()) {
				successCount++;
			} else {
				failCount++;
				if (message.isEmpty() && approveResult.getMessage() != null) {
					message = approveResult.getMessage();
				}
			}
		}

		result.put("successCount", successCount);
		result.put("failCount", failCount);
		result.put("message", message);
		return result;
	}

	@RequestMapping("/approveStatusMessage")
	@ResponseBody
	public Map<String, Object> approveStatusMessage(@RequestBody Map<String, String> param) {
		Map<String, Object> result = new HashMap<>();
		String objectId = param.get("objectId");
		result.put("message", service.getApprovalStatusMessage(objectId));
		return result;
	}

	@RequestMapping("/approveStatusRows")
	@ResponseBody
	public Map<String, Object> approveStatusRows(@RequestBody Map<String, String> param, Authentication authentication) {
		Map<String, Object> result = new HashMap<>();
		UserVO userVo = (UserVO) authentication.getPrincipal();
		String objectId = param.get("objectId");
		result.put("rows", service.getApprovalStatusRows(objectId, userVo));
		return result;
	}

	@PostMapping("/saveApprovalComment")
	@ResponseBody
	public ResultVO saveApprovalComment(@RequestBody Map<String, String> param, Authentication authentication) {
		UserVO userVo = (UserVO) authentication.getPrincipal();
		String objectId = param.get("objectId");
		String comment = param.get("comment");
		return service.saveApprovalComment(objectId, comment, userVo);
	}
//drawingNo + versionType 기준 다음 버전 조회
	@RequestMapping("/nextVersionNo")
	@ResponseBody
	public Map<String, Object> nextVersionNo(@RequestBody Map<String, String> param) {
		Map<String, Object> result = new HashMap<>();
		String drawingNo = param.get("drawingNo");
		String versionType = param.get("versionType");
		result.put("versionNo", service.getNextVersionNo(drawingNo, versionType));
		return result;
	}
//문서번호 클릭했을 때 팝업
	@RequestMapping("/drawingFilePopup")
	public String drawingFilePopup(DrawingRequestParam param, Model model) {
		Map<String, Object> popupResource = service.getDrawingFileDownloadInfo(param.getObjectId(), null);
		String resolvedObjectId = requirePopupViewAccess(popupResource, "DRAWING");
		List<Map<String, Object>> mainFileList = filterAccessiblePopupRows(
				service.selectMainFileInfo(resolvedObjectId), "DRAWING", false);
		List<Map<String, Object>> subFileList = filterAccessiblePopupRows(
				service.selectSubFileInfo(resolvedObjectId), "DRAWING_SUB", true);

		model.addAttribute("objectId", resolvedObjectId);
		model.addAttribute("drawingNo", firstRowValue(mainFileList, "drawingNo"));
		model.addAttribute("mainFileList", mainFileList);
		model.addAttribute("subFileList", subFileList);
		model.addAttribute("mainFileJson", JSONArray.fromObject(mainFileList));
		model.addAttribute("subFileJson", JSONArray.fromObject(subFileList));
		return "general/distribution/drawingFilePopup";
	}

	private String requirePopupViewAccess(Map<String, Object> resource, String defaultObjectType) {
		if (resource == null || resource.isEmpty()) {
			throw new AccessDeniedException("File resource was not resolved from the database.");
		}
		String objectId = mapValue(resource, "aclObjectId", mapValue(resource, "objectId", ""));
		if (objectId.isEmpty()) {
			throw new AccessDeniedException("File resource identifier is missing.");
		}
		FileAccessRequest access = new FileAccessRequest();
		access.setActionCd(SecurityAclService.VIEW);
		access.setObjectType(mapValue(resource, "aclObjectType", defaultObjectType));
		access.setObjectId(objectId);
		access.setFileNo(mapValue(resource, "fileNo", "*"));
		securityAclService.requireAccess(access);
		return objectId;
	}

	private List<Map<String, Object>> filterAccessiblePopupRows(List<Map<String, Object>> rows,
			String objectType, boolean subFile) {
		List<Map<String, Object>> safeRows = new java.util.ArrayList<>();
		if (rows == null) return safeRows;
		for (Map<String, Object> row : rows) {
			if (row == null) continue;
			String objectId = mapValue(row, subFile ? "parentObjectId" : "objectId", "");
			if (objectId.isEmpty()) continue;
			FileAccessRequest access = new FileAccessRequest();
			access.setActionCd(SecurityAclService.VIEW);
			access.setObjectType(objectType);
			access.setObjectId(objectId);
			access.setFileNo(mapValue(row, "fileNo", "*"));
			if (securityAclService.checkAccess(access).isAllowed()) {
				safeRows.add(withoutServerPath(row));
			}
		}
		return safeRows;
	}

	private Map<String, Object> withoutServerPath(Map<String, Object> row) {
		Map<String, Object> safe = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : row.entrySet()) {
			String key = entry.getKey() == null ? "" : entry.getKey().replace("_", "").replace("-", "").toLowerCase();
			if (!"filepath".equals(key) && !"filepathnm".equals(key)
					&& !"fullpath".equals(key) && !"physicalpath".equals(key) && !"storagepath".equals(key)) {
				safe.put(entry.getKey(), entry.getValue());
			}
		}
		return safe;
	}

	private String firstRowValue(List<Map<String, Object>> rows, String key) {
		return rows == null || rows.isEmpty() ? "" : mapValue(rows.get(0), key, "");
	}

	@GetMapping("/downloadFile")
	public ResponseEntity<byte[]> downloadFile(
			@RequestParam("objectId") String objectId,
			@RequestParam(value = "fileNo", required = false) String fileNo,
			@RequestParam(value = "watermarkYn", required = false, defaultValue = "Y") String watermarkYn,
			Authentication authentication) throws Exception {
		Map<String, Object> fileInfo = service.getDrawingFileDownloadInfo(objectId, fileNo);
		if (fileInfo == null || fileInfo.isEmpty()) {
			recordDirectDownloadResult(fileInfo, "DRAWING", objectId, fileNo,
					"FAIL", "RESOURCE_NOT_FOUND", "Direct download resource was not found.");
			return ResponseEntity.notFound().build();
		}
		requireDownloadAccess(fileInfo, "DRAWING", objectId, fileNo);
		String filePath = fileInfo.get("filePath") == null ? "" : String.valueOf(fileInfo.get("filePath"));
		String orgFileNm = fileInfo.get("orgFileNm") == null ? "download.bin" : String.valueOf(fileInfo.get("orgFileNm"));
		if (!isPdfFilePath(filePath) && !isAdminRole(authentication)) {
			recordDirectDownloadResult(fileInfo, "DRAWING", objectId, fileNo,
					"FAIL", "FILE_TYPE_DENIED", "Direct download file type was denied.");
			return ResponseEntity.status(403).build();
		}
		if (filePath.isEmpty() || !Files.exists(Paths.get(filePath))) {
			recordDirectDownloadResult(fileInfo, "DRAWING", objectId, fileNo,
					"FAIL", "FILE_NOT_FOUND", "Direct download file was not found.");
			return ResponseEntity.notFound().build();
		}

		byte[] bytes;
		try {
			bytes = "Y".equalsIgnoreCase(watermarkYn)
					? requestWatermarkPdf(filePath, orgFileNm, authentication) : null;
			if (bytes == null || bytes.length == 0) {
				bytes = Files.readAllBytes(Paths.get(filePath));
			}
		} catch (Exception exception) {
			recordDirectDownloadResult(fileInfo, "DRAWING", objectId, fileNo,
					"FAIL", "READ_ERROR", "Direct download response preparation failed.");
			throw exception;
		}
		String downloadFileName = buildDownloadFileName(filePath, orgFileNm);
		String encodedFileName = URLEncoder.encode(downloadFileName, "UTF-8").replaceAll("\\+", "%20");
		recordDirectDownloadResult(fileInfo, "DRAWING", objectId, fileNo,
				"SUCCESS", null, "Direct download response prepared.");

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName)
				.contentLength(bytes.length)
				.body(bytes);
	}

	private void requireDownloadAccess(Map<String, Object> fileInfo, String defaultObjectType,
			String requestedObjectId, String requestedFileNo) {
		FileAccessRequest access = new FileAccessRequest();
		access.setActionCd(SecurityAclService.DOWNLOAD_ORIGINAL);
		access.setObjectType(mapValue(fileInfo, "aclObjectType", defaultObjectType));
		access.setObjectId(mapValue(fileInfo, "aclObjectId", mapValue(fileInfo, "objectId", requestedObjectId)));
		access.setFileNo(mapValue(fileInfo, "fileNo", requestedFileNo));
		securityAclService.requireAccess(access);
	}

	private void recordDirectDownloadResult(Map<String, Object> fileInfo, String defaultObjectType,
			String requestedObjectId, String requestedFileNo, String resultCd, String reasonCd,
			String message) {
		securityAclService.recordDownloadResult(null, resultCd, reasonCd,
				mapValue(fileInfo, "aclObjectType", defaultObjectType),
				mapValue(fileInfo, "aclObjectId", mapValue(fileInfo, "objectId", requestedObjectId)),
				mapValue(fileInfo, "fileNo", requestedFileNo),
				mapValue(fileInfo, "requestNo", mapValue(fileInfo, "REQUEST_NO", "")),
				message);
	}

	private String mapValue(Map<String, Object> source, String key, String fallback) {
		Object value = source == null ? null : source.get(key);
		if (value == null || String.valueOf(value).trim().isEmpty()) return fallback;
		return String.valueOf(value).trim();
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
//워터마크 파이썬 호출
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
		} catch (Exception e) {
			log.warn("watermark_download call failed. fallback to original file. path={}, reason={}", inputPdfPath, e.getMessage());
		}
		return null;
	}
//워터마크
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


// 2023.07.20 기범( 등록을 위한 Controller)

	@RequestMapping("/drawingRegisterPopup")
	public String registerPopup(DrawingRegisterPopupParam param, Model model) {

		LocalDate now = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String date = now.format(formatter);

		// 등록자, 등록일	
		model.addAttribute("registerUser", param.getSessionUser().getUsername());
		model.addAttribute("date", date);

		// 파일유형, 사업단계
		// DOCS_COMBO에 있는 값 사용
		ComboInfoVO distributeTypeCd = new ComboInfoVO();
		ComboInfoVO businessTypeCd = new ComboInfoVO();
		ComboInfoVO protectYn = new ComboInfoVO();
		ComboInfoVO drawingType = new ComboInfoVO();

		distributeTypeCd = new ComboInfoVO();
		businessTypeCd = new ComboInfoVO();
		protectYn = new ComboInfoVO();
		drawingType = new ComboInfoVO();

		distributeTypeCd.setComboCd("distributeTypeCd");
		businessTypeCd.setComboCd("businessTypeCd");
		protectYn.setComboCd("protectYn");
		drawingType.setComboCd("drawingType");

		model.addAttribute("distributeTypeCd", comboService.selectComboList(distributeTypeCd));
		model.addAttribute("businessTypeCd", comboService.selectComboList(businessTypeCd));
		model.addAttribute("protectYn", comboService.selectComboList(protectYn));
		model.addAttribute("drawingType", comboService.selectComboList(drawingType));
		model.addAttribute("coPublisherUsers", comboService.selectActiveUserList()); // 공동발행자 값 조회

		ComboInfoVO distributionPoint = new ComboInfoVO();
		distributionPoint = new ComboInfoVO();
		distributionPoint.setComboCd("distributionPoint");
		distributionPoint.setComboLabel("선택하세요");
		model.addAttribute("distributionPoint", comboService.selectComboList(distributionPoint));

		// 업다운 관련 시스템 설정
		model.addAttribute("updownCabUrl", SystemConfig.getSystemConfigValue("UPDOWN_CAB_URL"));
		model.addAttribute("updownServerIp", SystemConfig.getSystemConfigValue("UPDOWN_SERVER_IP"));
		model.addAttribute("updownServerPort", SystemConfig.getSystemConfigValue("UPDOWN_SERVER_PORT"));
		model.addAttribute("updownPath", SystemConfig.getSystemConfigValue("UPDOWN_PATH"));
		model.addAttribute("updownLangCode", SystemConfig.getSystemConfigValue("UPDOWN_LANG_CODE"));
		model.addAttribute("updownIsSecurity", SystemConfig.getSystemConfigValue("UPDOWN_IS_SECURITY"));
		model.addAttribute("updownExtension", SystemConfig.getSystemConfigValue("UPDOWN_EXTENSION"));

		return "general/distribution/drawingRegisterPopup";
	}

	@PostMapping(value="/uploadDrawingRegisFile")
	public @ResponseBody ResultVO uploadDrawingRegisFile(MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
		// System.out.println(multipartHttpServletRequest.getFile("file").getName());

		return service.saveDrawingRegisterFileX2(multipartHttpServletRequest);
	}

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





}

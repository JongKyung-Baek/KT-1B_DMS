package kr.esob.tdms.controller.general.distribution.production;

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
import net.sf.json.JSONArray;
import org.springframework.security.access.AccessDeniedException;
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
@RequestMapping("/general/distribution/productionRequest")
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
	SecurityAclService securityAclService;

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

		return "/general/distribution/production/productionRequestList";
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

	@PostMapping("/delete")
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
		Map<String, Object> popupResource = service.getProductionFileDownloadInfo(param.getObjectId(), null);
		String resolvedObjectId = requirePopupViewAccess(popupResource, "PRODUCT_DOCUMENT");
		List<Map<String, Object>> mainFileList = filterAccessiblePopupRows(
				service.selectMainFileInfo(resolvedObjectId), "PRODUCT_DOCUMENT", false);
		List<Map<String, Object>> subFileList = filterAccessiblePopupRows(
				service.selectSubFileInfo(resolvedObjectId), "PRODUCT_DOCUMENT_SUB", true);
		model.addAttribute("objectId", resolvedObjectId);
		model.addAttribute("productionNo", firstRowValue(mainFileList, "productionNo"));
		model.addAttribute("mainFileList", mainFileList);
		model.addAttribute("subFileList", subFileList);
		model.addAttribute("mainFileJson", JSONArray.fromObject(mainFileList));
		model.addAttribute("subFileJson", JSONArray.fromObject(subFileList));
		return "general/distribution/production/productionFilePopup";
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
		Map<String, Object> fileInfo = service.getProductionFileDownloadInfo(objectId, fileNo);
		if (fileInfo == null || fileInfo.isEmpty()) {
			recordDirectDownloadResult(fileInfo, "PRODUCT_DOCUMENT", objectId, fileNo,
					"FAIL", "RESOURCE_NOT_FOUND", "Direct download resource was not found.");
			return ResponseEntity.notFound().build();
		}
		requireDownloadAccess(fileInfo, "PRODUCT_DOCUMENT", objectId, fileNo);
		String filePath = fileInfo.get("filePath") == null ? "" : String.valueOf(fileInfo.get("filePath"));
		String orgFileNm = fileInfo.get("orgFileNm") == null ? "download.bin" : String.valueOf(fileInfo.get("orgFileNm"));
		if (!isPdfFilePath(filePath) && !isAdminRole(authentication)) {
			recordDirectDownloadResult(fileInfo, "PRODUCT_DOCUMENT", objectId, fileNo,
					"FAIL", "FILE_TYPE_DENIED", "Direct download file type was denied.");
			return ResponseEntity.status(403).build();
		}
		if (filePath.isEmpty() || !Files.exists(Paths.get(filePath))) {
			recordDirectDownloadResult(fileInfo, "PRODUCT_DOCUMENT", objectId, fileNo,
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
			recordDirectDownloadResult(fileInfo, "PRODUCT_DOCUMENT", objectId, fileNo,
					"FAIL", "READ_ERROR", "Direct download response preparation failed.");
			throw exception;
		}
		String downloadFileName = buildDownloadFileName(filePath, orgFileNm);
		String encodedFileName = URLEncoder.encode(downloadFileName, "UTF-8").replaceAll("\\+", "%20");
		recordDirectDownloadResult(fileInfo, "PRODUCT_DOCUMENT", objectId, fileNo,
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
		model.addAttribute("updownLangCode", SystemConfig.getSystemConfigValue("UPDOWN_LANG_CODE"));
		model.addAttribute("updownIsSecurity", SystemConfig.getSystemConfigValue("UPDOWN_IS_SECURITY"));
		model.addAttribute("updownExtension", SystemConfig.getSystemConfigValue("UPDOWN_EXTENSION"));

		return "general/distribution/production/productionRegisterPopup";
	}

	@PostMapping(value="/uploadProductionRegisFile")
	public @ResponseBody ResultVO uploadProductionRegisFile(MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
		// System.out.println(multipartHttpServletRequest.getFile("file").getName());

		return service.saveProductionRegisterFileX2(multipartHttpServletRequest);
	}
}

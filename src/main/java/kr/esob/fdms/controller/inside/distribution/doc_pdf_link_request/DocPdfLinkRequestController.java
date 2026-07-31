package kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.convert.ConvertLogDao;
import kr.esob.fdms.commonlogic.fileapi.FileApiClient;
import kr.esob.fdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.viewerintegration.ViewerDocumentMetadata;
import kr.esob.fdms.commonlogic.viewerintegration.ViewerIntegrationService;
import kr.esob.fdms.controller.inside.distribution.swrequest.TechnicalFileTypePolicy;
import kr.esob.fdms.commonlogic.viewerintegration.ViewerPreparedLaunch;
import kr.esob.fdms.commonlogic.viewer.CommonViewerParam;
import kr.esob.fdms.commonlogic.viewer.CommonViewerService;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.StoragePathUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;



@Controller
@RequestMapping("/inside/distribution/docPdfLinkRequest")
@Log4j2
public class DocPdfLinkRequestController extends AbstractController {

	@Autowired
	DocPdfLinkRequestDao dao;

	@Autowired
	CommonViewerService commonViewerService;

	@Autowired
	ConvertLogDao convertLogDao;

	@Autowired
	SecurityAclService securityAclService;

	@Autowired
	ViewerIntegrationService viewerIntegrationService;

	private final FileApiClient fileApiClient = new FileApiClient();

	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		//model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formDocPdfRequest")));
		//model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarDocPdfRequest")));
		//model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridDocPdfRequestList")));

		return "/inside/distribution/docPdfLinkRequestList";
		//return "/inside/distribution/docRequestList";
	}


	// SecureRandom 클래스를 이용한 랜덤난수 17자 생성
	private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final SecureRandom RANDOM = new SecureRandom();

	private String getRandomString(int length) {
		StringBuilder result = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			result.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
		}
		return result.toString();
	}

	private String getConfigCd(Map<String, Object> config) {
		Object value = config.get("SYSTEM_CONFIG_CD");
		if (value == null) {
			value = config.get("system_config_cd");
		}
		return value == null ? "" : value.toString();
	}

	private String getConfigValue(Map<String, Object> config) {
		Object value = config.get("SYSTEM_CONFIG_VALUE");
		if (value == null) {
			value = config.get("system_config_value");
		}
		return value == null ? "" : value.toString();
	}

	private String toFileNameOnly(String pathOrName) {
		if (pathOrName == null || pathOrName.isEmpty()) {
			return pathOrName;
		}
		int slash = pathOrName.lastIndexOf('/');
		int backslash = pathOrName.lastIndexOf('\\');
		int idx = Math.max(slash, backslash);
		return idx >= 0 ? pathOrName.substring(idx + 1) : pathOrName;
	}

	private void requireViewAccess(String objectType, String objectId, String fileNo, String requestNo) {
		FileAccessRequest access = new FileAccessRequest();
		access.setActionCd(SecurityAclService.VIEW);
		access.setObjectType(objectType);
		access.setObjectId(objectId);
		access.setFileNo(fileNo);
		access.setRequestNo(requestNo);
		securityAclService.requireAccess(access);
	}
	//

	//

	@RequestMapping(value="/selectItem2", method=RequestMethod.POST)
	public String selectItem2(
			@RequestParam("file") String file,
			@RequestParam("objectType") String objectType,
			@RequestParam("requestNo") String requestNo,
			@RequestParam("fileNo") String fileNo,
			Authentication authentication,
			Model model) {
		UserVO userVo = (UserVO) authentication.getPrincipal();
		String sourceObjectId = requireText(file, "object identifier");
		String normalizedFileNo = isPeerReviewType(objectType)
				? defaultText(fileNo, "1") : requirePositiveFileNo(fileNo);
		String correlationId = UUID.randomUUID().toString();

		Map<String, Object> fileParam = new HashMap<String, Object>();
		fileParam.put("OBJECT_ID", sourceObjectId);
		fileParam.put("FILE_NO", normalizedFileNo);

		String orgFileNm;
		String baseAclObjectType;
		if (isDocumentType(objectType)) {
			orgFileNm = dao.selectFilePathNmDoc(fileParam);
			baseAclObjectType = "DOCUMENT";
		} else if (isDrawingType(objectType)) {
			orgFileNm = dao.selectFilePathNmDrawing(fileParam);
			baseAclObjectType = "DRAWING";
		} else if ("SW".equalsIgnoreCase(objectType)) {
			orgFileNm = dao.selectSwFile(fileParam);
			baseAclObjectType = "SW";
		} else if (isProductionType(objectType)) {
			orgFileNm = dao.selectProduction(fileParam);
			baseAclObjectType = "PRODUCT_DOCUMENT";
		} else if ("DXF".equalsIgnoreCase(objectType)) {
			orgFileNm = dao.selectDxf(fileParam);
			baseAclObjectType = "DXF";
		} else if (isPeerReviewType(objectType)) {
			orgFileNm = dao.selectPeerReview(fileParam);
			baseAclObjectType = "PEER_REVIEW";
		} else {
			throw new AccessDeniedException(
					"This resource type is not available in the secured viewer.");
		}
		if (orgFileNm == null || orgFileNm.trim().isEmpty()) {
			throw new AccessDeniedException("The secured viewer source file is unavailable.");
		}

		String aclObjectId = sourceObjectId;
		String aclObjectType = baseAclObjectType;
		String subFileParent = dao.selectSubFileParent(
				baseAclObjectType, sourceObjectId, normalizedFileNo);
		if (subFileParent != null && !subFileParent.trim().isEmpty()) {
			aclObjectId = subFileParent.trim();
			aclObjectType = subFileObjectType(baseAclObjectType);
		}
		requireViewAccess(aclObjectType, aclObjectId, normalizedFileNo, requestNo);
		if ("SW".equals(baseAclObjectType) && !TechnicalFileTypePolicy.isPdf(orgFileNm)) {
			throw new AccessDeniedException(
					"Only PDF technical-data files are available in the secured viewer.");
		}

		java.nio.file.Path requestPdf = viewerIntegrationService.createRequestPdf(correlationId);
		String cvrtFilePathNm = requestPdf.toString();
		String viewerWorkDirectory = requestPdf.getParent().toString();
		try {
			boolean prepared = false;
			if ("SW".equals(baseAclObjectType)) {
				prepared = cacheSwFileApiPdfForViewer(orgFileNm, requestPdf);
			}
			if (!prepared) {
				File sourceFile = new File(orgFileNm);
				if (convertToViewerPdf(
						sourceFile, cvrtFilePathNm, viewerWorkDirectory, correlationId) == 0) {
					model.addAttribute("convertFailRestricted", "Y");
					return "/inside/distribution/docConvertFail";
				}
			}
			if (!Files.isRegularFile(requestPdf)) {
				model.addAttribute("convertFailRestricted", "Y");
				return "/inside/distribution/docConvertFail";
			}

			Map<String, String> metadataParam = new HashMap<String, String>();
			metadataParam.put("OBJECT_ID", sourceObjectId);
			metadataParam.put("FILE_NO", normalizedFileNo);
			String distributionType = "";
			String drawingNo = "";
			String fileName = "";
			String revision = "";
			if ("DOCUMENT".equals(baseAclObjectType)) {
				distributionType = "IOC";
				drawingNo = dao.selectDocumentNoDoc(metadataParam);
				fileName = dao.selectFileNmDoc(metadataParam);
			} else if ("DRAWING".equals(baseAclObjectType)) {
				distributionType = "FunctionCode";
				drawingNo = dao.selectDrawingNoDrawing(metadataParam);
				fileName = dao.selectFileNmDrawing(metadataParam);
				revision = dao.selectRevisionDrawing(metadataParam);
			} else if ("PRODUCT_DOCUMENT".equals(baseAclObjectType)) {
				distributionType = "MRB";
				drawingNo = dao.selectDrawingNoCP(metadataParam);
				fileName = dao.selectFileNmCP(metadataParam);
				revision = dao.selectRevisionCP(metadataParam);
			} else if ("DXF".equals(baseAclObjectType)) {
				distributionType = "PMPCB";
				drawingNo = dao.selectDrawingNoDXF(metadataParam);
				fileName = dao.selectFileNmDXF(metadataParam);
				revision = dao.selectRevisionDXF(metadataParam);
			} else if ("PEER_REVIEW".equals(baseAclObjectType)) {
				distributionType = "PEERREVIEW";
				drawingNo = dao.selectPeerReviewNo(metadataParam);
				fileName = dao.selectFileNmPeerReview(metadataParam);
			} else if ("SW".equals(baseAclObjectType)) {
				distributionType = "CCB";
				drawingNo = dao.selectSwNo(metadataParam);
				fileName = dao.selectFileNmSW(metadataParam);
				revision = dao.selectRevisionSW(metadataParam);
			}

			ViewerDocumentMetadata metadata = new ViewerDocumentMetadata();
			metadata.setCorrelationId(correlationId);
			metadata.setObjectType(baseAclObjectType);
			metadata.setObjectId(sourceObjectId);
			metadata.setAclObjectType(aclObjectType);
			metadata.setAclObjectId(aclObjectId);
			metadata.setFileNo(normalizedFileNo);
			metadata.setFileName(toFileNameOnly(firstNonBlank(fileName, orgFileNm)));
			metadata.setUserCd(userVo.getUserCd());
			metadata.setUserId(userVo.getUserId());
			metadata.setUserName(firstNonBlank(userVo.getUserNm(), userVo.getUserId()));
			metadata.setAuthority("2");
			metadata.setRevision(revision);
			metadata.setRequestNo(requestNo);
			metadata.setDistributionType(distributionType);
			metadata.setDrawingNo(drawingNo);

			ViewerPreparedLaunch launch =
					viewerIntegrationService.prepareLaunch(requestPdf, metadata);
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("url", launch.getLaunchUri().toString());
			params.put("launchToken", launch.getLaunchToken());
			return handleRedirect(params, model);
		} finally {
			try {
				Files.deleteIfExists(requestPdf);
			} catch (Exception exception) {
				log.warn("Viewer request PDF cleanup failed. correlationId={}", correlationId);
			}
		}
	}

	boolean isDocumentType(String objectType) {
		return "DOC".equalsIgnoreCase(objectType)
				|| "DOCUMENT".equalsIgnoreCase(objectType)
				|| "문서".equals(objectType);
	}

	boolean isDrawingType(String objectType) {
		return "DRAWING".equalsIgnoreCase(objectType)
				|| "도면".equals(objectType)
				|| "도면·공정서".equals(objectType)
				|| "도면/공정서".equals(objectType);
	}

	boolean isProductionType(String objectType) {
		return "PRODUCTION".equalsIgnoreCase(objectType)
				|| "PRODUCT_DOCUMENT".equalsIgnoreCase(objectType)
				|| "생산기술자료".equals(objectType);
	}

	private boolean isPeerReviewType(String objectType) {
		return "PEERREVIEW".equalsIgnoreCase(objectType)
				|| "PEER_REVIEW".equalsIgnoreCase(objectType);
	}

	private String subFileObjectType(String baseType) {
		if ("DOCUMENT".equals(baseType)) return "DOCUMENT_SUB";
		if ("DRAWING".equals(baseType)) return "DRAWING_SUB";
		if ("SW".equals(baseType)) return "SW_SUB";
		if ("PRODUCT_DOCUMENT".equals(baseType)) return "PRODUCT_DOCUMENT_SUB";
		if ("DXF".equals(baseType)) return "DXF_SUB";
		return baseType;
	}

	private String requireText(String value, String label) {
		String normalized = value == null ? "" : value.trim();
		if (normalized.isEmpty()) {
			throw new IllegalArgumentException(label + " is required.");
		}
		return normalized;
	}

	private String defaultText(String value, String fallback) {
		String normalized = value == null ? "" : value.trim();
		return normalized.isEmpty() ? fallback : normalized;
	}

	private String requirePositiveFileNo(String value) {
		String normalized = requireText(value, "file number");
		if (!normalized.matches("[1-9][0-9]{0,9}")) {
			throw new IllegalArgumentException("file number must be a positive integer.");
		}
		return normalized;
	}

	private String firstNonBlank(String first, String second) {
		return first == null || first.trim().isEmpty()
				? defaultText(second, "") : first.trim();
	}

	private String handleRedirect(Map<String, String> params, Model model) {
		model.addAttribute("params", params);

		return "/inside/distribution/redirectPost";
	}

	private int convertToViewerPdf(File inputFile, String targetPdfPath, String outputDir, String objectId) {
		try {
			if (inputFile == null || !inputFile.isFile()) {
				return 0;
			}

			File targetFile = new File(targetPdfPath);
			File parent = targetFile.getParentFile();
			if (parent != null && !parent.exists() && !parent.mkdirs()) {
				return 0;
			}

			String lowerName = inputFile.getName().toLowerCase(Locale.ROOT);
			if (lowerName.endsWith(".pdf")) {
				Files.copy(inputFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				return targetFile.isFile() ? 1 : 0;
			}

			String convertedPdfPath = requestConvertServer(inputFile, outputDir, objectId);
			if (convertedPdfPath == null || convertedPdfPath.trim().isEmpty()) {
				return 0;
			}

			File convertedFile = new File(convertedPdfPath);
			if (!convertedFile.isFile()) {
				return 0;
			}

			try {
				Files.copy(convertedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				return targetFile.isFile() ? 1 : 0;
			} finally {
				if (!convertedFile.toPath().equals(targetFile.toPath())) {
					Files.deleteIfExists(convertedFile.toPath());
				}
			}
		} catch (Exception e) {
			log.error("Viewer PDF convert failed. input={}, target={}", inputFile, targetPdfPath, e);
			return 0;
		}
	}

	private String requestConvertServer(File inputFile, String outputDir, String objectId) {
		HttpURLConnection connection = null;
		DataOutputStream requestStream = null;
		try {
			String endpoint = SystemConfig.getSystemConfigValue("CONVERT_SERVER_URL");
			if (endpoint == null || endpoint.trim().isEmpty()) {
				endpoint = "http://localhost:9001/api/internal/convert";
			}

			String boundary = "----FDMS-VIEWER-" + System.currentTimeMillis();
			connection = (HttpURLConnection) new URL(endpoint).openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

			requestStream = new DataOutputStream(connection.getOutputStream());
			writeFormField(requestStream, boundary, "outputDir", outputDir);

			String fileName = inputFile.getName();
			int dot = fileName.lastIndexOf('.');
			String ext = dot > -1 ? fileName.substring(dot) : "";
			writeFilePart(requestStream, boundary, "files", objectId + ext, inputFile);

			requestStream.writeBytes("--" + boundary + "--\r\n");
			requestStream.flush();

			int status = connection.getResponseCode();
			if (status >= 200 && status < 300) {
				return findLatestConvertedPdf(outputDir, objectId);
			}
			log.error("Convert server failed. status={}, endpoint={}", status, endpoint);
		} catch (Exception e) {
			log.error("Convert server request failed. input={}", inputFile, e);
		} finally {
			try {
				if (requestStream != null) requestStream.close();
			} catch (Exception ignored) {}
			if (connection != null) connection.disconnect();
		}
		return null;
	}

	private void writeFormField(DataOutputStream stream, String boundary, String name, String value) throws Exception {
		stream.writeBytes("--" + boundary + "\r\n");
		stream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
		stream.write(value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8));
		stream.writeBytes("\r\n");
	}

	private void writeFilePart(DataOutputStream stream, String boundary, String fieldName, String fileName, File file) throws Exception {
		stream.writeBytes("--" + boundary + "\r\n");
		stream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
		stream.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
		try (FileInputStream inputStream = new FileInputStream(file)) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = inputStream.read(buffer)) != -1) {
				stream.write(buffer, 0, read);
			}
		}
		stream.writeBytes("\r\n");
	}

	private String findLatestConvertedPdf(String outputDir, String objectId) {
		try {
			File dir = new File(outputDir);
			File[] candidates = dir.listFiles((parent, name) -> {
				String lower = name.toLowerCase(Locale.ROOT);
				return lower.startsWith(objectId.toLowerCase(Locale.ROOT) + "_") && lower.endsWith(".pdf");
			});
			if (candidates == null || candidates.length == 0) {
				return null;
			}
			Arrays.sort(candidates, (left, right) -> Long.compare(right.lastModified(), left.lastModified()));
			return candidates[0].getAbsolutePath();
		} catch (Exception e) {
			return null;
		}
	}

	private String handleRedirectMultiple(Map<String, Object> params, Model model) {
		
		model.addAttribute("params", params);

		return "/inside/distribution/redirectPostMultiple";
	}


	//@RequestMapping(value="/selectItem", method=RequestMethod.GET)
	@RequestMapping(value="/selectItem")
	@ResponseBody
	public ResponseEntity<Void> selectItemGone() {
		return ResponseEntity.status(HttpStatus.GONE).build();
	}

	//	@RequestMapping("/selectList")
//	public @ResponseBody GridResultVO selectList(DocPdfRequestParam param) throws Exception {
	//service.setSearchAllParam(param);
	//GridResultVO result = commonSelectList(param, service);
	//return result;
//	}
	public String formatInsertDt(String insertDt) {
		if (insertDt == null) {
			throw new IllegalArgumentException("insertDt cannot be null");
		}

		// 두 가지 포맷 정의: 마이크로초 포함 / 미포함
		DateTimeFormatter originalFormatWithMicros = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
		DateTimeFormatter originalFormatWithoutMicros = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		DateTimeFormatter targetFormat = DateTimeFormatter.ofPattern("yy.MM.dd");

		LocalDateTime dateTime;

		// 입력값이 마이크로초를 포함하는지 여부에 따라 처리
		try {
			dateTime = LocalDateTime.parse(insertDt, originalFormatWithMicros);
		} catch (Exception e) {
			dateTime = LocalDateTime.parse(insertDt, originalFormatWithoutMicros);
		}

		return dateTime.format(targetFormat);
	}

	@SuppressWarnings("deprecation")
	@RequestMapping(value="/pdfConvert", method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Void> pdfConvertGone() {
		return ResponseEntity.status(HttpStatus.GONE).build();
	}

	public Map<String, Object> pdfConverter(
			@RequestParam("file") String file,
			@RequestParam("objectType") String objectType,
			@RequestParam("requestNo") String requestNo,
			@RequestParam("fileNo") String fileNo,
			@RequestParam("userId") String userId,
			@RequestParam("filePath") String filePath,
			Model model) throws UnsupportedEncodingException, ParseException {

		//config table에서, 대상파일 경로 조희
		List<Map<String,Object>> dbConfig = dao.selectDbConfig();
		String adapPdfPath="";
		String orgFileNm="";
		String chkcvrtFilePathNm="";
		String orgFilePathNm="";
		String cvrtFilePathNm="";

		String objectID = "";
		objectID = file;

		for(Map<String,Object> config : dbConfig) {
			if("ADAP_PDF_PATH".equals(getConfigCd(config))) {
				adapPdfPath = getConfigValue(config);

			}
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("OBJECT_ID",objectID);
		orgFileNm = filePath;

//		2023.06.29 (yskim)
		orgFilePathNm = /*adapViewerPath + */orgFileNm;
		log.debug("Legacy PDF converter invoked.");

		cvrtFilePathNm = StoragePathUtils.resolve(
				adapPdfPath.replace("$", ""), objectID + ".pdf").toString();

		chkcvrtFilePathNm = StoragePathUtils.resolve(
				adapPdfPath.replace("$", ""), objectID + ".esob").toString();

		//키 생성 & DOCS_VIEWER_KEY에 insert
		String disposableKey = getRandomString(17);

		Date now = new Date();

		// ****임시 주석처리****
		// dao.insertKey(disposableKey, objectID, now);

		int intCvrt = 10;

		File fileExist = new File(chkcvrtFilePathNm);

		if (fileExist.isFile()) {
			map.put("PATH_NM", fileExist.getPath());
			map.put("ORG_FILE_NM", fileExist.getName());
			map.put("FILE_SIZE", fileExist.length());
			map.put("USER_ID", userId);
			map.put("CONVERT_TYPE", "2D");
			map.put("RESULT_CODE", intCvrt);
			// convert_log table removed: skip convert log insert.
		} else {
			try{
				File fileOrgFilePath = new File(orgFileNm);
				if(fileOrgFilePath.exists()){
					map.put("PATH_NM", fileOrgFilePath.getPath());
					map.put("ORG_FILE_NM", fileOrgFilePath.getName());
					map.put("FILE_SIZE", fileOrgFilePath.length());
					map.put("USER_ID", userId);

					for(Map<String,Object> config : dbConfig) {
						String path = getConfigValue(config);
						String configCD = getConfigCd(config);

						// 해당 파일의 경로와 디비 상에 있는 경로가 일치할 경우
						if(path.equals(fileOrgFilePath.getParent())){
							// 2D 라면
							if(configCD.equals("2D_FILE_PATH") || configCD.equals("DOCUMENT_PATH")||configCD.equals("PRODUCTION_PATH")||configCD.equals("DXF_PATH")){
								intCvrt = 0;
								map.put("CONVERT_TYPE", "2D");
								map.put("RESULT_CODE", intCvrt);
								// convert_log table removed: skip convert log insert.
							}
						}
						else {
							// 나머지 Production, SW 등등..
							log.info("checking dbConfig..");
						}
					}
				} else {
					// 파일이 존재하지 않는다면
					log.info("file doesn't exist");
				}

			}catch(Exception e){
				log.warn("Legacy PDF conversion preparation failed. cause={}",
						e.getClass().getSimpleName());
			}
		}

		Map<String, Object> result = new HashMap<>();
		if (intCvrt == 0) {
			log.info("convertfail");
			result.put("status", "convertfail");
		} else {
			log.info(objectID);
			log.info(requestNo);
			log.info(fileNo);

			log.info("suceess");
			result.put("status", "success");
			result.put("cvrtFilePathNm", cvrtFilePathNm);
		}
		return result;
	}
	
	private boolean cacheSwFileApiPdfForViewer(
			String filePathNm, java.nio.file.Path requestPdf) {
		try {
			if (filePathNm != null && Files.isRegularFile(java.nio.file.Path.of(filePathNm.trim()))) {
				return false;
			}
		} catch (java.nio.file.InvalidPathException ignored) {
			// Non-local repository identifiers are parsed by splitFileApiPath below.
		}
		String[] fileApiPath = splitFileApiPath(filePathNm);
		if (fileApiPath == null) {
			return false;
		}
		byte[] bytes = fileApiClient.download(fileApiPath[1], fileApiPath[0]);
		try {
			java.nio.file.Path parent = requestPdf.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.write(requestPdf, bytes);
			return true;
		} catch (Exception e) {
			throw new IllegalStateException("Viewer request PDF write failed.", e);
		}
	}

	private String[] splitFileApiPath(String filePath) {
		String path = normalizeFileApiPath(filePath);
		if (path.isEmpty() || path.startsWith("/") || path.matches("^[A-Za-z]:/.*")) {
			return null;
		}
		int separator = path.indexOf("/");
		if (separator <= 0 || separator == path.length() - 1) {
			return null;
		}
		String folder = path.substring(0, separator);
		String fileName = path.substring(separator + 1);
		if (!fileName.toLowerCase().endsWith(".pdf")) {
			return null;
		}
		return new String[] { folder, fileName };
	}

	private String normalizeFileApiPath(String filePath) {
		return filePath == null ? "" : filePath.trim().replace("\\", "/");
	}

	private String requireSecureViewerUrl(String rawUrl) {
		try {
			URI uri = URI.create(rawUrl == null ? "" : rawUrl.trim());
			String host = uri.getHost();
			boolean loopbackHttp = "http".equalsIgnoreCase(uri.getScheme())
					&& ("localhost".equalsIgnoreCase(host)
							|| "127.0.0.1".equals(host)
							|| "::1".equals(host));
			if (host == null || uri.getUserInfo() != null || uri.getFragment() != null
					|| (!"https".equalsIgnoreCase(uri.getScheme()) && !loopbackHttp)) {
				throw new IllegalArgumentException("Viewer endpoint must use HTTPS.");
			}
			return uri.toString();
		} catch (RuntimeException exception) {
			throw new IllegalStateException("Viewer endpoint is not securely configured.", exception);
		}
	}

private String getReleasedToRIWatermarkYn(UserVO userVo) {
    return "Y";
}
}

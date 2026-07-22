package kr.esob.fdms.controller.inside.distribution.production;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.controller.inside.distribution.approvaldetail.DistributionApprovalDetailDao;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.inside.production.common.ProductionInfoVO;
import kr.esob.fdms.util.RandomStringGenerator;
import kr.esob.fdms.util.StringUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ProductionRequestService implements CommonService{
	private static final String MRB_OBJECT_TYPE = "MRB";

	@Inject
	ProductionRequestDao dao;
	@Inject
	DistributionApprovalDetailDao approvalDetailDao;
	@Inject
	DocsMailService mailService;

	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	public List<ProductionRequestTreeVO> selectTree(Object param) {
		return dao.selectTree(param);
	}

	public List<ComboInfoVO> selectLevelOptions(Object param) {
		return dao.selectLevelOptions(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

//	public void updateList(Object param) {
//		dao.updateList(param);
//	}

	public void deleteList(Object param) {

	}

	public void setSearchAllParam(ProductionRequestParam param) {
		if(!"".equals(param.getSearchAllParam()) && param.getSearchAllParam() != null){
			Gson gson = new Gson();
			param.setProductionList(gson.fromJson(param.getSearchAllParam().replace("&quot;","'"), new TypeToken<List<ProductionInfoVO>>() {}.getType()));
		}
	}



	// 2020.07.24 기범추가( 등록 )
	public ResultVO saveProductionRegisterFileX2(MultipartHttpServletRequest request) throws Exception {
		ResultVO resultVo = new ResultVO();
		System.out.println("들어오는지 체크 = " + request.getParameterMap());
		System.out.println("들어오는지 체크 = " + request.getParameter("fileName"));
		System.out.println("objectType -> "+request.getParameter("objectType"));
		MultipartFile file = request.getFile("file");
		List<MultipartFile> subFiles = request.getFiles("subFiles");

//		System.out.println("protectYn 잘 들어오나 " + request.getParameter("protectYn"));

		String drawingType = request.getParameter("drawingType");
		String distributionPoint = request.getParameter("distributionPoint");
		String check3DFile = "N";

		String objectId = RandomStringGenerator.generateRandomString(32);

		String revNo = request.getParameter("revNo");
		if (revNo == null || revNo.trim().isEmpty()) {
			revNo = "0";
		}
		String fileNo = "1"; // 파일순서. DOCUMENT_FILE에만 있는거
		String docVersion = "1";
		String approver = request.getParameter("swTypeCd");
		String reviewerUser = request.getParameter("reviewerUser");
		if (approver == null) {
			approver = "";
		}
		approver = approver.replace(APPROVER_EMPTY_VALUE, "").replace(",,", ",");
		approver = trimCsv(approver);
		if (reviewerUser == null) {
			reviewerUser = "";
		}
		reviewerUser = reviewerUser.replace(APPROVER_EMPTY_VALUE, "").replace(",,", ",");
		reviewerUser = trimCsv(reviewerUser);

		String filePathNm = SystemConfig.getSystemConfigValue("PRODUCTION_PATH")+"\\"+ objectId+"." + FilenameUtils.getExtension(request.getParameter("orgFileNm"));

		if (drawingType != null && !drawingType.isEmpty() && !"2D".equalsIgnoreCase(drawingType)) {
			check3DFile = "Y";
		}

		// DrawingRegisterPopupParam 객체 만들어서 저장
		String issueDt = request.getParameter("mrbIssueDt");
		if (issueDt == null || issueDt.trim().isEmpty()) {
			issueDt = request.getParameter("ccbIssueDt");
		}
		ProductionRegisterPopupParam productionRegisterPopupParam = ProductionRegisterPopupParam.builder()

				.pdm(request.getParameter("pdm"))
				.fileSize(String.valueOf(Objects.requireNonNull(file).getSize()))
				.fileNo(fileNo)
				.filePath(filePathNm)
				.docVersion(docVersion)
				.orgFileNm(request.getParameter("orgFileNm"))
				.businessTypeCd(request.getParameter("businessTypeCd"))
				.distributeTypeCd(request.getParameter("distributeTypeCd"))
				.treeCd(request.getParameter("treeCd"))
				.objectId(objectId)
				.productionNo(request.getParameter("productionNo"))
				.fileNm(request.getParameter("fileName"))
				.revNo(revNo)
				.ccbIssueDt(issueDt)
				.protectYn(request.getParameter("protectYn"))
				.distributionPoint(distributionPoint)
				.modelCode(request.getParameter("modelCode"))
//				.customerRevision(request.getParameter("customerRevision"))
				.check3DFile(check3DFile)
				.changeActionNo(request.getParameter("changeActionNo"))
				.statusCd(STATUS_APPROVING)
				.approver(approver)
				.reviewerUser(reviewerUser)
				.build();

		String existingFileName = dao.getProductionRegisterByOrgFileNm(objectId);
		if (existingFileName != null) {
			resultVo.setSuccess(false);
			resultVo.setMessage("이미 있는 파일입니다");
			return resultVo;
		} else {
			dao.insertProductionRegisterInfo(productionRegisterPopupParam);
			dao.insertProductionRegisterInfoFile(productionRegisterPopupParam);

// 이 아래 3줄이 있어서 등록 성공이 뜨는거였음. 왜냐 ? getFileSavedPath로 가기 때문
			productionRegisterPopupParam = getFileSavedPath(productionRegisterPopupParam, file);
			saveProductionSubFiles(subFiles, productionRegisterPopupParam.getObjectId(), productionRegisterPopupParam.getDistributeTypeCd());
			String reqPreparedByRaw = safeString(request.getParameter("registerUser"));
			if (reqPreparedByRaw.isEmpty()) {
				reqPreparedByRaw = safeString(request.getParameter("insertUserNm"));
			}
			if (reqPreparedByRaw.isEmpty()) {
				reqPreparedByRaw = safeString(request.getParameter("coPublisher"));
			}
			final String reqReviewerUsersRaw = safeString(request.getParameter("reviewerUser"));
			final String reqApproverUsersRaw = safeString(request.getParameter("swTypeCd"));
			convertMrbMainFileAtRegister(productionRegisterPopupParam);
			saveDistributionApprovalDetails(productionRegisterPopupParam.getObjectId(), MRB_OBJECT_TYPE, reqReviewerUsersRaw, reqApproverUsersRaw);
			Set<String> mailTargets = new LinkedHashSet<>();
			mailTargets.addAll(splitCsv(reqApproverUsersRaw));
			mailTargets.addAll(splitCsv(reqReviewerUsersRaw));
			sendRegistrationMail(String.join(",", mailTargets), DocsMailEnum.DISTRIBUTION_PRODUCT_STATUS, safeString(productionRegisterPopupParam.getProductionNo()), safeString(productionRegisterPopupParam.getFileNm()), safeString(request.getParameter("registerUser")), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
			resultVo.setSuccess(true);
			resultVo.setMessage("등록 완료. 변환/표지 병합은 백그라운드에서 처리됩니다.");
			return resultVo;
		}
	}

	private void sendRegistrationMail(
			String targetUsersCsv,
			DocsMailEnum mailEnum,
			String documentNo,
			String documentName,
			String registrant,
			String registeredAt
	) {
		try {
			System.out.println("[PRODUCT_REGISTER_MAIL] targetUsersCsv=" + targetUsersCsv);
			for (String token : splitCsv(targetUsersCsv)) {
				System.out.println("[PRODUCT_REGISTER_MAIL] token=" + token);
				MailInfoVO mailInfoVo = mailService.selectReceiveUser(token);
				if (mailInfoVo == null) {
					System.out.println("[PRODUCT_REGISTER_MAIL] selectReceiveUser is null. token=" + token);
					continue;
				}
				System.out.println("[PRODUCT_REGISTER_MAIL] toUserId=" + mailInfoVo.getToUserId() + ", toMail=" + mailInfoVo.getToMail());
				mailInfoVo.setMailEnum(mailEnum);
				mailInfoVo.setContent(buildRegistrationMailContent(mailInfoVo, "MRB", documentNo, documentName, registrant, registeredAt));
				ResultVO mailResult = mailService.sendDocsMail(mailInfoVo);
				System.out.println("[PRODUCT_REGISTER_MAIL] sendDocsMail success=" + (mailResult != null && mailResult.isSuccess()));
			}
		} catch (Exception e) {
			System.out.println("[PRODUCT_REGISTER_MAIL] skip: " + e.getMessage());
		}
	}

	private String loadRegistrationMailTemplate() {
		try {
			ClassPathResource resource = new ClassPathResource("templates/mail/mps8_kari_document_notification_email_template.html");
			return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			return "";
		}
	}

	private String buildRegistrationMailContent(
			MailInfoVO mailInfoVo,
			String menuName,
			String documentNo,
			String documentName,
			String registrant,
			String registeredAt
	) {
		String html = loadRegistrationMailTemplate();
		if (html == null || html.trim().isEmpty()) return html;
		String role = safeString(mailInfoVo.getApprovalGrade());
		return html
				.replace("{메뉴명}", safeString(menuName))
				.replace("{메일제목}", "문서 등록 및 검토/승인 요청 알림")
				.replace("{문서번호}", safeString(documentNo))
				.replace("{문서명}", safeString(documentName))
				.replace("{등록자}", safeString(registrant))
				.replace("{등록일시}", safeString(registeredAt))
				.replace("{요청업무}", "문서승인")
				.replace("{수신자역할}", role);
	}

	private void convertMrbMainFileAtRegister(ProductionRegisterPopupParam param) {
		String objectId = safeString(param.getObjectId());
		String sourceFilePath = safeString(param.getFilePath());
		if (objectId.isEmpty() || sourceFilePath.isEmpty()) return;
		String inputPdfPath = ensureConvertedPdfForCover(sourceFilePath, objectId, "MRB_CONVERT");
		if (inputPdfPath.isEmpty() || !inputPdfPath.toLowerCase().endsWith(".pdf")) {
			markMainFileProcessingFail(objectId, "convert failed: pdf output not found", "MRB_CONVERT");
			return;
		}
		File convertedMain = new File(inputPdfPath);
		if (!convertedMain.isFile()) {
			markMainFileProcessingFail(objectId, "convert failed: pdf file not found", "MRB_CONVERT");
			return;
		}
		Map<String, Object> convertedUpdateParam = new HashMap<>();
		convertedUpdateParam.put("objectId", objectId);
		convertedUpdateParam.put("filePath", inputPdfPath);
		convertedUpdateParam.put("orgFileNm", toPdfFileName(safeString(param.getOrgFileNm()), safeString(param.getFileNm())));
		convertedUpdateParam.put("fileSize", String.valueOf(convertedMain.length()));
		int updated = dao.updateMainFileAfterCoverMerge(convertedUpdateParam);
		System.out.println("[MRB_CONVERT] register main update rows=" + updated + ", filePath=" + inputPdfPath);
	}

	private void markMainFileProcessingFail(String objectId, String errorMessage, String logTag) {
		try {
			Map<String, Object> failParam = new HashMap<>();
			failParam.put("objectId", safeString(objectId));
			failParam.put("processingStatus", "FAIL");
			failParam.put("errorMessage", safeString(errorMessage));
			dao.updateMainFileProcessingFail(failParam);
			System.out.println("[" + logTag + "] processing status updated to FAIL: " + errorMessage);
		} catch (Exception e) {
			System.out.println("[" + logTag + "] fail status update exception: " + e.getMessage());
		}
	}

	private void mergeMrbCoverAfterApproval(String objectId) {
		Map<String, Object> info = dao.selectProductionApprovalInfo(objectId);
		List<Map<String, Object>> mainFiles = dao.selectMainFileInfo(objectId);
		if (info == null || mainFiles == null || mainFiles.isEmpty()) return;
		Map<String, Object> main = mainFiles.get(0);
		ProductionRegisterPopupParam param = new ProductionRegisterPopupParam();
		param.setObjectId(objectId);
		param.setFilePath(getInfoString(main, "filePath", "FILE_PATH_NM"));
		param.setOrgFileNm(getInfoString(main, "orgFileNm", "ORG_FILE_NM"));
		param.setFileNm(getInfoString(main, "orgFileNm", "ORG_FILE_NM"));
		param.setFileSize(getInfoString(main, "fileSize", "FILE_SIZE"));
		param.setProductionNo(getInfoString(main, "productionNo", "DOCUMENT_NO"));
		param.setRevNo(getInfoString(info, "revNo", "REV_NO"));
		mergeMrbCoverAtRegister(param, null, getInfoString(info, "insertUserNm", "INSERT_USER_NM"), getInfoString(info, "reviewerUser", "REVIEWERUSER"), getInfoString(info, "approver", "APPROVER"));
	}

	private void mergeMrbCoverAtRegister(ProductionRegisterPopupParam param, List<MultipartFile> subFiles, String preparedByRaw, String reviewerUsersRaw, String approverUsersRaw) {
		try {
			String sourceFilePath = safeString(param.getFilePath());
			String objectId = safeString(param.getObjectId());
			String inputPdfPath = ensureConvertedPdfForCover(sourceFilePath, objectId, "MRB_COVER");
			if (inputPdfPath.isEmpty()) {
				System.out.println("[MRB_COVER] skip: inputPdfPath is empty");
				return;
			}
			if (!inputPdfPath.equals(sourceFilePath) && inputPdfPath.toLowerCase().endsWith(".pdf")) {
				File convertedMain = new File(inputPdfPath);
				if (convertedMain.isFile()) {
					Map<String, Object> convertedUpdateParam = new HashMap<>();
					convertedUpdateParam.put("objectId", objectId);
					convertedUpdateParam.put("filePath", inputPdfPath);
					convertedUpdateParam.put("orgFileNm", toPdfFileName(safeString(param.getOrgFileNm()), safeString(param.getFileNm())));
					convertedUpdateParam.put("fileSize", String.valueOf(convertedMain.length()));
					int updated = dao.updateMainFileAfterCoverMerge(convertedUpdateParam);
					System.out.println("[MRB_COVER] pre-merge main update rows=" + updated + ", filePath=" + inputPdfPath);
				}
			}

			String outputFileName = "cover_" + objectId + ".pdf";
			Map<String, Object> mainFileInfo = dao.selectProductionFileDownloadInfo(new HashMap<String, Object>() {{
				put("objectId", objectId);
				put("fileNo", "1");
			}});
			String dbFilePath = safeString(getInfoString(mainFileInfo, "filePath", "FILE_PATH_NM", "filepath"));
			System.out.println("[MRB_COVER] objectId=" + objectId);
			System.out.println("[MRB_COVER] param.filePath=" + inputPdfPath + ", exists=" + new File(inputPdfPath).exists());
			System.out.println("[MRB_COVER] db.filePath=" + dbFilePath + ", exists=" + (!dbFilePath.isEmpty() && new File(dbFilePath).exists()));

			Map<String, Object> info = dao.selectProductionApprovalInfo(objectId);
			List<String> preparedBy = splitCsv(getInfoString(info, "registerUser", "REGISTER_USER", "insertUserNm", "INSERT_USER_NM"));
			List<String> reviewerUsers = splitCsv(reviewerUsersRaw);
			List<String> approverUsers = splitCsv(approverUsersRaw);
			List<String> subFileNames = extractSubFileNamesFromDb(objectId);

			Map<String, String> positionMap = getUserPositionMapByNames(preparedBy, reviewerUsers, approverUsers);
			List<String> preparedPositions = buildPositionsByNames(preparedBy, positionMap);
			List<String> reviewerPositions = buildPositionsByNames(reviewerUsers, positionMap);
			List<String> approverPositions = buildPositionsByNames(approverUsers, positionMap);
			List<String> preparedByWithPosition = combineUsersWithPositions(preparedBy, preparedPositions);
			List<String> reviewerUsersWithPosition = combineUsersWithPositions(reviewerUsers, reviewerPositions);
			List<String> approverUsersWithPosition = combineUsersWithPositions(approverUsers, approverPositions);

			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("inputPdfPath", inputPdfPath);
			requestBody.put("outputFileName", outputFileName);
			requestBody.put("cover_doc_type", "MRB");
			requestBody.put("cover_request_no", "-");
			requestBody.put("cover_object_id", objectId);
			requestBody.put("cover_user_id", "-");
			requestBody.put("cover_user_name", String.join(",", preparedBy));
			requestBody.put("cover_viewed_at", safeString(param.getInsertDt()));
			requestBody.put("cover_title", safeString(param.getFileNm()));
			requestBody.put("cover_note", "");
			requestBody.put("cover_document_no", safeString(param.getProductionNo()));
			requestBody.put("cover_issue", safeString(param.getRevNo()));
			requestBody.put("revision", safeString(param.getRevNo()));
			requestBody.put("fileName", safeString(param.getFileNm()));
			requestBody.put("cover_prepared_by", preparedByWithPosition);
			requestBody.put("cover_prepared_positions", preparedPositions);
			requestBody.put("cover_reviewer_users", reviewerUsersWithPosition);
			requestBody.put("cover_reviewer_positions", reviewerPositions);
			requestBody.put("cover_approver_users", approverUsersWithPosition);
			requestBody.put("cover_approver_positions", approverPositions);
			List<Map<String, Object>> approvalDetails = selectDistributionApprovalDetails(objectId, MRB_OBJECT_TYPE);
			requestBody.put("cover_reviewer_dates", buildApprovalDatesByNames(reviewerUsers, "REVIEWER", approvalDetails));
			requestBody.put("cover_approver_dates", buildApprovalDatesByNames(approverUsers, "APPROVER", approvalDetails));
			requestBody.put("cover_sub_file_names", subFileNames);

			String templatePdfPath = safeString(SystemConfig.getSystemConfigValue("FC_COVER_TMPL_PATH"));
			if (!templatePdfPath.isEmpty()) {
				requestBody.put("templatePdfPath", templatePdfPath);
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

			String endpoint = safeString(SystemConfig.getSystemConfigValue("FC_COVER_ENDPOINT"));
			if (endpoint.isEmpty()) {
				endpoint = "http://localhost:7442/cover_merge_pdf";
			}
			System.out.println("[MRB_COVER] endpoint=" + endpoint);
			System.out.println("[MRB_COVER] templatePdfPath=" + templatePdfPath);
			System.out.println("[MRB_COVER] inputPdfPath=" + inputPdfPath + ", outputFileName=" + outputFileName);

			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
			String responseBody = response.getBody();
			System.out.println("[MRB_COVER] responseStatus=" + response.getStatusCodeValue() + ", body=" + responseBody);

			if (responseBody != null && !responseBody.trim().isEmpty()) {
				Map<String, Object> responseMap = new Gson().fromJson(responseBody, new TypeToken<Map<String, Object>>() {}.getType());
				String outputPdfPath = safeString(responseMap.get("outputPdfPath"));
				if (!outputPdfPath.isEmpty()) {
					File mergedFile = new File(outputPdfPath);
					if (mergedFile.exists()) {
						Files.copy(Paths.get(outputPdfPath), Paths.get(inputPdfPath), StandardCopyOption.REPLACE_EXISTING);
						Map<String, Object> updateParam = new HashMap<>();
						updateParam.put("objectId", objectId);
						updateParam.put("filePath", inputPdfPath);
						updateParam.put("orgFileNm", toPdfFileName(safeString(param.getOrgFileNm()), safeString(param.getFileNm())));
						updateParam.put("fileSize", String.valueOf(mergedFile.length()));
						int updated = dao.updateMainFileAfterCoverMerge(updateParam);
						copyMergedPdfToViewerCache(outputPdfPath, objectId, "MRB_COVER");
						System.out.println("[MRB_COVER] post-merge main update rows=" + updated + ", filePath=" + inputPdfPath);
						System.out.println("[MRB_COVER] overwrite source file with merged cover: " + inputPdfPath);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("[MRB_COVER] exception: " + e.getMessage());
			e.printStackTrace();
			try {
				Map<String, Object> failParam = new HashMap<>();
				failParam.put("objectId", safeString(param.getObjectId()));
				String errorMessage = safeString(e.getMessage());
				if (errorMessage.length() > 1000) {
					errorMessage = errorMessage.substring(0, 1000);
				}
				failParam.put("errorMessage", errorMessage);
				dao.updateMainFileProcessingFail(failParam);
			} catch (Exception ignore) {
				System.out.println("[MRB_COVER] fail status update exception: " + ignore.getMessage());
			}
		}
	}

	private String ensureConvertedPdfForCover(String sourceFilePath, String objectId, String logTag) {
		String normalizedPath = safeString(sourceFilePath);
		if (normalizedPath.isEmpty()) return "";
		File sourceFile = new File(normalizedPath);
		if (!sourceFile.isFile()) return "";
		if (normalizedPath.toLowerCase().endsWith(".pdf")) return normalizedPath;
		try {
			String endpoint = safeString(SystemConfig.getSystemConfigValue("CONVERT_SERVER_URL"));
			if (endpoint.isEmpty()) endpoint = "http://localhost:9001/api/internal/convert";

			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("outputDir", sourceFile.getParentFile().getAbsolutePath());
			body.add("files", new FileSystemResource(sourceFile));

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setConnectTimeout(5000);
			requestFactory.setReadTimeout(60000);
			RestTemplate restTemplate = new RestTemplate(requestFactory);
			ResponseEntity<String> response = restTemplate.postForEntity(endpoint, new HttpEntity<>(body, headers), String.class);
			System.out.println("[" + logTag + "] convert status=" + response.getStatusCodeValue() + ", body=" + response.getBody());
			if (!response.getStatusCode().is2xxSuccessful()) {
				return "";
			}

			File dir = sourceFile.getParentFile();
			File[] candidates = dir.listFiles();
			if (candidates != null) {
				File latest = null;
				String prefix = safeString(objectId).toLowerCase();
				for (File candidate : candidates) {
					String name = candidate.getName().toLowerCase();
					if (candidate.isFile() && name.startsWith(prefix) && name.endsWith(".pdf")) {
						if (latest == null || candidate.lastModified() > latest.lastModified()) {
							latest = candidate;
						}
					}
				}
				if (latest != null) {
					System.out.println("[" + logTag + "] convertedPdfPath=" + latest.getAbsolutePath());
					return latest.getAbsolutePath();
				}
			}
		} catch (Exception e) {
			System.out.println("[" + logTag + "] convert exception: " + e.getMessage());
		}
		return "";
	}

	private List<String> extractSubFileNames(List<MultipartFile> subFiles) {
		List<String> names = new ArrayList<>();
		if (subFiles == null) {
			return names;
		}
		for (MultipartFile subFile : subFiles) {
			if (subFile == null || subFile.isEmpty()) {
				continue;
			}
			String fileName = safeString(subFile.getOriginalFilename());
			if (!fileName.isEmpty()) {
				names.add(fileName);
			}
		}
		return names;
	}

	private void saveProductionSubFiles(List<MultipartFile> subFiles, String objectId, String distributeTypeCd) {
		if (subFiles == null || subFiles.isEmpty()) {
			return;
		}

		for (MultipartFile subFile : subFiles) {
			if (subFile == null || subFile.isEmpty()) {
				continue;
			}

			String originalName = subFile.getOriginalFilename();
			if (originalName == null) {
				originalName = "";
			}
			if (originalName.contains(File.separator)) {
				originalName = originalName.substring(originalName.lastIndexOf(File.separator) + 1);
			}
			originalName = StringUtil.replaceLfiPath(originalName);

			String subObjectId = RandomStringGenerator.generateRandomString(32);
			String extension = FilenameUtils.getExtension(originalName);
			String targetPath = SystemConfig.getSystemConfigValue("PRODUCTION_PATH") + "\\" + subObjectId
					+ (extension == null || extension.trim().isEmpty() ? "" : "." + extension);
			targetPath = StringUtil.replaceLfiPath(targetPath);

			try {
				File outFile = new File(targetPath);
				outFile.createNewFile();
				Files.write(Paths.get(targetPath), subFile.getBytes());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			String convertedSubPdfPath = ensureConvertedPdfForCover(targetPath, subObjectId, "MRB_SUB_COVER");
			String savedPath = targetPath;
			String savedOrgFileNm = originalName;
			long savedSize = subFile.getSize();
			if (!safeString(convertedSubPdfPath).isEmpty() && convertedSubPdfPath.toLowerCase().endsWith(".pdf")) {
				File convertedFile = new File(convertedSubPdfPath);
				if (convertedFile.isFile()) {
					savedPath = convertedSubPdfPath;
					savedSize = convertedFile.length();
				}
			}
			ProductionSubFileParam subFileParam = ProductionSubFileParam.builder()
					.objectId(subObjectId)
					.parentObjectId(objectId)
					.orgFileNm(savedOrgFileNm)
					.filePath(savedPath)
					.fileSize(String.valueOf(savedSize))
					.useYn("Y")
					.build();
			dao.insertProductionSubFile(subFileParam);
		}
	}

	private ProductionRegisterPopupParam getFileSavedPath(ProductionRegisterPopupParam param, MultipartFile mf){

		String filePathNm = param.getFilePath();

		File filePath = new File(filePathNm);
		param.setFilePath(filePathNm);
		String orgName = mf.getOriginalFilename();

		if(orgName.contains(File.separator)) {
			orgName = orgName.substring(orgName.lastIndexOf(File.separator)+1, orgName.length());
		}
		orgName = StringUtil.replaceLfiPath(orgName);
		String path = filePathNm;
		path = StringUtil.replaceLfiPath(path);
		File file = new File(path);
		param.setFilePath(path);
		System.out.println("파일 경로 : " + path);

		param.setFileNm(orgName);
		param.setFileSize(String.valueOf(mf.getSize()));

		try {
			file.createNewFile();

			// 0703 (yskim)
			byte[] bytes = mf.getBytes();
			Files.write(Paths.get(path), bytes);
			//

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return param;
	}

    public int deletePrd(String objectId) {
		return dao.deletePrd(objectId);
    }

	private static final String STATUS_APPROVING = "승인진행중";
	private static final String STATUS_APPROVED = "승인완료";
	private static final String APPROVER_EMPTY_VALUE = "__NONE__";

	public ResultVO validateDeletePrd(String objectId, UserVO userVo) {
		ResultVO result = new ResultVO();
		if (objectId == null || objectId.trim().isEmpty()) {
			result.setSuccess(false);
			result.setMessage("철회 대상이 없습니다.");
			return result;
		}
		if (userVo == null) {
			result.setSuccess(false);
			result.setMessage("로그인 사용자 정보를 확인할 수 없습니다.");
			return result;
		}

		Map<String, Object> info = dao.selectProductionApprovalInfo(objectId.trim());
		if (info == null || info.isEmpty()) {
			result.setSuccess(false);
			result.setMessage("대상 문서를 찾을 수 없습니다.");
			return result;
		}

		String deletedYn = getInfoString(info, "deletedYn", "DELETED_YN");
		String ownerNm = getInfoString(info, "insertUserNm", "INSERT_USER_NM", "INSERTUSERNM");
		String ownerUid = getInfoString(info, "insertUid", "INSERT_UID", "INSERTUID");
		String loginUserNm = safeString(userVo.getUserNm());
		String loginUserCd = safeString(userVo.getUserCd());
		String loginUserId = safeString(userVo.getUserId());
		String ownerNmNorm = normalizeCompareKey(ownerNm);
		String ownerUidNorm = normalizeCompareKey(ownerUid);
		String loginUserNmNorm = normalizeCompareKey(loginUserNm);
		String loginUserCdNorm = normalizeCompareKey(loginUserCd);
		String loginUserIdNorm = normalizeCompareKey(loginUserId);

		if ("Y".equalsIgnoreCase(deletedYn)) {
			result.setSuccess(false);
			result.setMessage("이미 철회된 문서입니다.");
			return result;
		}

		boolean ownerMatched =
				(!ownerNmNorm.isEmpty() && ownerNmNorm.equals(loginUserNmNorm))
				|| (!ownerNmNorm.isEmpty() && ownerNmNorm.equals(loginUserCdNorm))
				|| (!ownerNmNorm.isEmpty() && ownerNmNorm.equals(loginUserIdNorm))
				|| (!ownerUidNorm.isEmpty() && ownerUidNorm.equals(loginUserCdNorm))
				|| (!ownerUidNorm.isEmpty() && ownerUidNorm.equals(loginUserIdNorm))
				|| (!ownerUidNorm.isEmpty() && ownerUidNorm.equals(loginUserNmNorm));

		if (!ownerMatched && ownerNmNorm.isEmpty() && ownerUidNorm.isEmpty()) {
			boolean isAdmin = "admin".equalsIgnoreCase(loginUserId) || "admin".equalsIgnoreCase(loginUserCd);
			if (isAdmin) {
				ownerMatched = true;
			}
		}

		if (!ownerMatched) {
			result.setSuccess(false);
			result.setMessage("등록자 본인만 철회할 수 있습니다.");
			return result;
		}

		if (isApprovalCompleted(info)) {
			result.setSuccess(false);
			result.setMessage("승인완료 문서는 철회할 수 없습니다.");
			return result;
		}

		result.setSuccess(true);
		result.setMessage("OK");
		return result;
	}

	public ResultVO approveProduction(String objectId, UserVO userVo) {
		ResultVO result = new ResultVO();
		Map<String, Object> info = dao.selectProductionApprovalInfo(objectId);

		if (info == null || info.isEmpty()) {
			result.setSuccess(false);
			result.setMessage("승인 대상을 찾을 수 없습니다.");
			return result;
		}

		if ("Y".equalsIgnoreCase(getInfoString(info, "deletedYn", "DELETED_YN"))) {
			result.setSuccess(false);
			result.setMessage("삭제된 문서는 승인할 수 없습니다.");
			return result;
		}

		List<String> requiredUsers = getRequiredApprovalUsers(info);
		List<String> reviewerUsers = splitCsv(getInfoString(info, "reviewerUser", "reviewer_user", "REVIEWER_USER", "REVIEWERUSER"));
		if (requiredUsers.isEmpty()) {
			result.setSuccess(false);
			result.setMessage("승인자가 지정되지 않은 문서입니다.");
			return result;
		}

		String userCd = safeString(userVo.getUserCd());
		String userNm = safeString(userVo.getUserNm());
		String approverToken = "";

		if (requiredUsers.contains(userCd)) {
			approverToken = userCd;
		} else if (requiredUsers.contains(userNm)) {
			approverToken = userNm;
		} else {
			result.setSuccess(false);
			result.setMessage("승인 대상자만 승인할 수 있습니다.");
			return result;
		}

		String currentStatus = getInfoString(info, "status", "STATUS");
		if (STATUS_APPROVED.equals(currentStatus)) {
			result.setSuccess(false);
			result.setMessage("이미 승인완료된 건입니다.");
			return result;
		}

		String currentApprovedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = currentApprovedUsers.isEmpty() ? currentStatus : currentApprovedUsers;
		Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
		String normalizedApprover = splitApprovedUser(approverToken);
		if (approvedSet.contains(normalizedApprover)) {
			result.setSuccess(false);
			result.setMessage("이미 승인한 건입니다.");
			return result;
		}

		approvedSet.add(normalizedApprover);
		boolean isFinalApprove = approvedSet.containsAll(requiredUsers);

		Map<String, Object> param = new HashMap<>();
		param.put("objectId", objectId);
		param.put("approvedUser", approverToken);
		param.put("isFinal", isFinalApprove ? "Y" : "N");

		int updated = dao.approveProduction(param);
		if (updated > 0) {
			updateDistributionApprovalDetail(objectId, MRB_OBJECT_TYPE, userCd, userNm);
			boolean detailFinalApprove = isDistributionApprovalDetailCompleted(objectId, MRB_OBJECT_TYPE);
			System.out.println("[MRB_APPROVAL] objectId=" + objectId + ", stringFinal=" + isFinalApprove + ", detailFinal=" + detailFinalApprove);
			if (isFinalApprove || detailFinalApprove) {
				mergeMrbCoverAfterApproval(objectId);
			}
		}
		result.setSuccess(updated > 0);
		result.setMessage(updated > 0 ? "승인되었습니다." : "승인 처리에 실패했습니다.");
		return result;
	}

	public ResultVO validateApproveProduction(String objectId, UserVO userVo) {
		ResultVO result = new ResultVO();

		if (objectId == null || objectId.trim().isEmpty()) {
			result.setSuccess(false);
			result.setMessage("승인 대상을 확인할 수 없습니다.");
			return result;
		}

		Map<String, Object> info = dao.selectProductionApprovalInfo(objectId.trim());
		if (info == null || info.isEmpty()) {
			result.setSuccess(false);
			result.setMessage("승인 대상을 확인할 수 없습니다.");
			return result;
		}

		if ("Y".equalsIgnoreCase(getInfoString(info, "deletedYn", "DELETED_YN"))) {
			result.setSuccess(false);
			result.setMessage("삭제된 문서는 승인할 수 없습니다.");
			return result;
		}

		List<String> requiredUsers = getRequiredApprovalUsers(info);
		if (requiredUsers.isEmpty()) {
			result.setSuccess(false);
			result.setMessage("승인자가 지정되지 않은 문서입니다.");
			return result;
		}

		String userCd = safeString(userVo.getUserCd());
		String userNm = safeString(userVo.getUserNm());
		String approverToken = "";

		if (requiredUsers.contains(userCd)) {
			approverToken = userCd;
		} else if (requiredUsers.contains(userNm)) {
			approverToken = userNm;
		} else {
			result.setSuccess(false);
			result.setMessage("승인 대상자만 승인할 수 있습니다.");	
					return result;
		}

		String currentStatus = getInfoString(info, "status", "STATUS");
		if (STATUS_APPROVED.equals(currentStatus)) {
			result.setSuccess(false);
			result.setMessage("이미 승인완료된 건입니다.");
						return result;
		}

		String currentApprovedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = currentApprovedUsers.isEmpty() ? currentStatus : currentApprovedUsers;
		Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
		if (approvedSet.contains(splitApprovedUser(approverToken))) {
			result.setSuccess(false);
			result.setMessage("이미 승인한 건입니다.");
			return result;
		}

		result.setSuccess(true);
		result.setMessage("OK");
		return result;
	}

	public String getApprovalStatusMessage(String objectId) {
		Map<String, Object> info = dao.selectProductionApprovalInfo(objectId);
		if (info == null || info.isEmpty()) {
			return "상태 정보를 찾을 수 없습니다.";
		}

		String status = getInfoString(info, "status", "STATUS");
		boolean forceApproved = STATUS_APPROVED.equals(status);

		List<String> requiredUsers = getRequiredApprovalUsers(info);
		if (requiredUsers.isEmpty()) {
			return forceApproved ? STATUS_APPROVED : "승인자가 지정되지 않았습니다.";
		}

		String approvedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = approvedUsers.isEmpty() ? status : approvedUsers;
		Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
		if (forceApproved) {
			approvedSet.addAll(requiredUsers);
		}

		List<String> rows = new ArrayList<>();
		for (String approver : requiredUsers) {
			boolean isApproved = approvedSet.contains(splitApprovedUser(approver));
			rows.add(approver + " : " + (isApproved ? "승인" : "미승인"));
		}
		return String.join("\n", rows);
	}

	public List<Map<String, Object>> getApprovalStatusRows(String objectId, UserVO userVo) {
		List<Map<String, Object>> rows = new ArrayList<>();
		Map<String, Object> info = dao.selectProductionApprovalInfo(objectId);
		if (info == null || info.isEmpty()) {
			return rows;
		}
		String status = getInfoString(info, "status", "STATUS");
		boolean forceApproved = STATUS_APPROVED.equals(status);
		List<String> requiredUsers = getRequiredApprovalUsers(info);
		String approvedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = approvedUsers.isEmpty() ? status : approvedUsers;
		Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
		if (forceApproved) approvedSet.addAll(requiredUsers);
		Map<String, String> commentMap = getApprovalCommentMap(objectId);
		List<String> reviewerUsers = splitCsv(getInfoString(info, "reviewerUser", "reviewer_user", "REVIEWER_USER", "REVIEWERUSER"));

		String userCd = safeString(userVo.getUserCd());
		String userNm = safeString(userVo.getUserNm());
		for (String approver : requiredUsers) {
			Map<String, Object> row = new HashMap<>();
			String normalized = splitApprovedUser(approver);
			row.put("approver", approver);
			row.put("status", approvedSet.contains(normalized) ? "승인" : "대기");
			row.put("comment", safeString(commentMap.get(normalized)));
			row.put("editable", (approver.equals(userCd) || approver.equals(userNm)) ? "Y" : "N");
			row.put("approvalType", reviewerUsers.contains(approver) ? "REVIEWER" : "APPROVER");
			rows.add(row);
		}
		return rows;
	}

	public ResultVO saveApprovalComment(String objectId, String comment, UserVO userVo) {
		ResultVO result = new ResultVO();
		Map<String, Object> info = dao.selectProductionApprovalInfo(objectId);
		if (info == null || info.isEmpty()) {
			result.setSuccess(false);
			result.setMessage("승인 대상 문서를 찾을 수 없습니다.");
			return result;
		}
		List<String> requiredUsers = getRequiredApprovalUsers(info);
		String userCd = safeString(userVo.getUserCd());
		String userNm = safeString(userVo.getUserNm());
		String actor = "";
		if (requiredUsers.contains(userCd)) {
			actor = userCd;
		} else if (requiredUsers.contains(userNm)) {
			actor = userNm;
		}
		if (actor.isEmpty()) {
			result.setSuccess(false);
			result.setMessage("본인 행에만 코멘트를 입력할 수 있습니다.");
			return result;
		}
		String currentStatus = getInfoString(info, "status", "STATUS");
		String currentApprovedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = currentApprovedUsers.isEmpty() ? currentStatus : currentApprovedUsers;
		Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
		if (!approvedSet.contains(splitApprovedUser(actor)) && !STATUS_APPROVED.equals(currentStatus)) {
			result.setSuccess(false);
			result.setMessage("승인 후 코멘트 입력이 가능합니다.");
			return result;
		}
		String normalizedComment = safeString(comment);
		if (normalizedComment.isEmpty()) normalizedComment = "승인하였습니다";
		Map<String, Object> param = new HashMap<>();
		param.put("objectId", objectId);
		param.put("approverId", splitApprovedUser(actor));
		param.put("comment", normalizedComment);
		int updated = dao.upsertApprovalComment(param);
		result.setSuccess(updated > 0);
		result.setMessage(updated > 0 ? "코멘트가 저장되었습니다." : "코멘트 저장에 실패했습니다.");
		return result;
	}

	private Map<String, String> getApprovalCommentMap(String objectId) {
		Map<String, String> map = new HashMap<>();
		List<Map<String, Object>> commentRows = dao.selectApprovalComments(objectId);
		if (commentRows == null) return map;
		for (Map<String, Object> row : commentRows) {
			String approverId = splitApprovedUser(getInfoString(row, "approverId", "APPROVER_ID"));
			if (!approverId.isEmpty()) map.put(approverId, safeString(getInfoString(row, "comment", "COMMENT_TXT")));
		}
		return map;
	}

	public List<Map<String, Object>> selectMainFileInfo(String objectId) {
		return appendFileExists(dao.selectMainFileInfo(objectId));
	}

	public List<Map<String, Object>> selectSubFileInfo(String objectId) {
		return appendFileExists(dao.selectSubFileInfo(objectId));
	}

	private List<Map<String, Object>> appendFileExists(List<Map<String, Object>> rows) {
		if (rows == null) {
			return rows;
		}
		for (Map<String, Object> row : rows) {
			String filePath = getInfoString(row, "filePath", "filePathNm", "FILE_PATH_NM", "file_path_nm", "filepath");
			boolean fileExists = false;
			if (!filePath.isEmpty()) {
				try {
					fileExists = Files.isRegularFile(Paths.get(StringUtil.replaceLfiPath(filePath)));
				} catch (Exception ignored) {
					fileExists = false;
				}
			}
			row.put("fileExists", fileExists);
		}
		return rows;
	}

	public Map<String, Object> selectProductionApprovalInfo(String objectId) {
		if (objectId == null || objectId.trim().isEmpty()) {
			return new HashMap<>();
		}
		Map<String, Object> info = dao.selectProductionApprovalInfo(objectId.trim());
		return info == null ? new HashMap<>() : info;
	}

	public Map<String, Object> getProductionFileDownloadInfo(String objectId, String fileNo) {
		if (objectId == null || objectId.trim().isEmpty()) return null;
		Map<String, Object> param = new HashMap<>();
		param.put("objectId", objectId.trim());
		param.put("fileNo", fileNo == null ? "" : fileNo.trim());
		Map<String, Object> fileInfo = dao.selectProductionFileDownloadInfo(param);
		String filePath = fileInfo == null || fileInfo.get("filePath") == null ? "" : String.valueOf(fileInfo.get("filePath"));
		System.out.println("[MRB_VIEWER] getProductionFileDownloadInfo objectId=" + objectId
				+ ", fileNo=" + (fileNo == null ? "" : fileNo)
				+ ", db.filePath=" + filePath
				+ ", exists=" + (!filePath.isEmpty() && new File(filePath).exists()));
		return fileInfo;
	}

	private String safeString(Object value) {
		return value == null ? "" : String.valueOf(value).trim();
	}

	private String getInfoString(Map<String, Object> info, String... keys) {
		if (info == null || info.isEmpty() || keys == null) {
			return "";
		}
		for (String key : keys) {
			if (key == null || key.isEmpty()) {
				continue;
			}
			Object value = info.get(key);
			if (value == null) {
				value = info.get(key.toUpperCase());
			}
			if (value == null) {
				value = info.get(key.toLowerCase());
			}
			if (value != null) {
				String text = safeString(value);
				if (!text.isEmpty()) {
					return text;
				}
			}
		}
		return "";
	}

	private List<String> splitCsv(String value) {
		List<String> list = new ArrayList<>();
		if (value == null || value.trim().isEmpty()) {
			return list;
		}
		String[] tokens = value.split(",");
		for (String token : tokens) {
			String trimmed = token == null ? "" : token.trim();
			if (!trimmed.isEmpty()) {
				list.add(trimmed);
			}
		}
		return list;
	}

	private List<String> parseApprovedUsers(String status) {
		if (status == null || status.trim().isEmpty()) {
			return new ArrayList<>();
		}
		if (STATUS_APPROVING.equals(status) || STATUS_APPROVED.equals(status)) {
			return new ArrayList<>();
		}
		List<String> rows = splitCsv(status.replace("\r", ",").replace("\n", ","));
		List<String> normalized = new ArrayList<>();
		for (String row : rows) {
			String token = splitApprovedUser(row);
			if (!token.isEmpty()) {
				normalized.add(token);
			}
		}
		return normalized;
	}

	private String splitApprovedUser(String entry) {
		String value = safeString(entry);
		if (value.isEmpty()) {
			return "";
		}
		String[] parts = value.split("\\|");
		return parts.length > 0 ? safeString(parts[0]) : value;
	}

	private void saveDistributionApprovalDetails(String objectId, String objectType, String reviewerRaw, String approverRaw) {
		insertDistributionApprovalDetails(objectId, objectType, "REVIEWER", splitCsv(reviewerRaw));
		insertDistributionApprovalDetails(objectId, objectType, "APPROVER", splitCsv(approverRaw));
	}

	private void insertDistributionApprovalDetails(String objectId, String objectType, String approvalRole, List<String> users) {
		if (users == null || users.isEmpty()) return;
		for (int i = 0; i < users.size(); i++) {
			String userNm = safeString(users.get(i));
			if (userNm.isEmpty()) continue;
			Map<String, Object> detail = new HashMap<>();
			detail.put("objectId", objectId);
			detail.put("objectType", objectType);
			detail.put("approvalRole", approvalRole);
			detail.put("approvalOrder", i + 1);
			detail.put("userNm", userNm);
			detail.put("userId", splitApprovedUser(userNm));
			approvalDetailDao.insertApprovalDetail(detail);
		}
	}

	private void updateDistributionApprovalDetail(String objectId, String objectType, String userCd, String userNm) {
		Map<String, Object> detail = new HashMap<>();
		detail.put("objectId", objectId);
		detail.put("objectType", objectType);
		detail.put("userId", safeString(userCd));
		detail.put("userNm", safeString(userNm));
		int updated = approvalDetailDao.updateApprovalDetailApproved(detail);
		System.out.println("[" + objectType + "_APPROVAL_DETAIL] approved rows=" + updated + ", objectId=" + objectId);
	}

	private boolean isDistributionApprovalDetailCompleted(String objectId, String objectType) {
		List<Map<String, Object>> details = selectDistributionApprovalDetails(objectId, objectType);
		if (details == null || details.isEmpty()) return false;
		for (Map<String, Object> detail : details) {
			if (!"APPROVAL".equals(getInfoString(detail, "approvalStatus", "APPROVAL_STATUS"))) return false;
		}
		return true;
	}

	private List<Map<String, Object>> selectDistributionApprovalDetails(String objectId, String objectType) {
		Map<String, Object> param = new HashMap<>();
		param.put("objectId", objectId);
		param.put("objectType", objectType);
		return approvalDetailDao.selectApprovalDetails(param);
	}

	private List<String> buildApprovalDatesByNames(List<String> users, String approvalRole, List<Map<String, Object>> details) {
		List<String> dates = new ArrayList<>();
		List<Map<String, Object>> roleDetails = new ArrayList<>();
		if (details != null) {
			for (Map<String, Object> detail : details) {
				if (approvalRole.equals(getInfoString(detail, "approvalRole", "APPROVAL_ROLE"))) roleDetails.add(detail);
			}
		}
		for (int i = 0; i < users.size(); i++) {
			String normalizedUser = splitApprovedUser(users.get(i));
			String approvalDt = "";
			for (Map<String, Object> detail : roleDetails) {
				String detailUserNm = splitApprovedUser(getInfoString(detail, "userNm", "USER_NM"));
				String detailUserId = splitApprovedUser(getInfoString(detail, "userId", "USER_ID"));
				if (normalizedUser.equals(detailUserNm) || normalizedUser.equals(detailUserId)) {
					approvalDt = getInfoString(detail, "approvalDt", "APPROVAL_DT");
					break;
				}
			}
			if (approvalDt.isEmpty() && i < roleDetails.size()) approvalDt = getInfoString(roleDetails.get(i), "approvalDt", "APPROVAL_DT");
			dates.add(approvalDt);
		}
		return dates;
	}

	private String toPdfFileName(String orgFileNm, String fallbackName) {
		String source = safeString(orgFileNm);
		if (source.isEmpty()) {
			source = safeString(fallbackName);
		}
		if (source.isEmpty()) {
			return "converted.pdf";
		}
		int dotIndex = source.lastIndexOf('.');
		if (dotIndex > 0) {
			return source.substring(0, dotIndex) + ".pdf";
		}
		return source + ".pdf";
	}

	private void copyMergedPdfToViewerCache(String mergedPdfPath, String objectId, String logTag) {
		try {
			String adapPdfPath = safeString(SystemConfig.getSystemConfigValue("ADAP_PDF_PATH"));
			if (adapPdfPath.isEmpty()) {
				System.out.println("[" + logTag + "] viewer cache skip: ADAP_PDF_PATH is empty");
				return;
			}
			File source = new File(safeString(mergedPdfPath));
			if (!source.isFile()) {
				System.out.println("[" + logTag + "] viewer cache skip: merged file not found: " + mergedPdfPath);
				return;
			}
			File targetDir = new File(adapPdfPath);
			if (!targetDir.exists()) {
				targetDir.mkdirs();
			}
			File target = new File(targetDir, safeString(objectId) + ".pdf");
			Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			System.out.println("[" + logTag + "] viewer cache updated: " + target.getAbsolutePath());
		} catch (Exception e) {
			System.out.println("[" + logTag + "] viewer cache update failed: " + e.getMessage());
		}
	}

	private Map<String, String> getUserPositionMapByNames(List<String>... userNameLists) {
		Set<String> allNames = new LinkedHashSet<>();
		if (userNameLists != null) {
			for (List<String> userNameList : userNameLists) {
				if (userNameList == null) {
					continue;
				}
				for (String userName : userNameList) {
					String normalized = splitApprovedUser(userName);
					if (!normalized.isEmpty()) {
						allNames.add(normalized);
					}
				}
			}
		}

		Map<String, String> result = new HashMap<>();
		if (allNames.isEmpty()) {
			return result;
		}

		Map<String, Object> param = new HashMap<>();
		param.put("names", new ArrayList<>(allNames));
		List<Map<String, Object>> rows;
		try {
			rows = dao.selectUserPositionByNames(param);
		} catch (Exception e) {
			System.out.println("[MRB_COVER] position lookup skipped: " + e.getMessage());
			return new HashMap<>();
		}
		if (rows == null) {
			return result;
		}

		for (Map<String, Object> row : rows) {
			String userName = safeString(row.get("userName"));
			String userId = safeString(row.get("userId"));
			String positionName = safeString(row.get("positionName"));
			if (!userName.isEmpty()) {
				result.put(userName, positionName);
			}
			if (!userId.isEmpty()) {
				result.put(userId, positionName);
			}
		}
		return result;
	}

	private List<String> buildPositionsByNames(List<String> userNames, Map<String, String> positionMap) {
		List<String> positions = new ArrayList<>();
		if (userNames == null) {
			return positions;
		}
		for (String userName : userNames) {
			String normalized = splitApprovedUser(userName);
			if (normalized.isEmpty()) {
				positions.add("");
				continue;
			}
			positions.add(positionMap.getOrDefault(normalized, ""));
		}
		return positions;
	}

	private List<String> combineUsersWithPositions(List<String> users, List<String> positions) {
		List<String> combined = new ArrayList<>();
		if (users == null) {
			return combined;
		}
		for (int i = 0; i < users.size(); i++) {
			String user = safeString(users.get(i));
			String position = "";
			if (positions != null && i < positions.size()) {
				position = safeString(positions.get(i));
			}
			combined.add(position.isEmpty() ? user : (user + "|" + position));
		}
		return combined;
	}

	private List<String> extractSubFileNamesFromDb(String objectId) {
		List<String> names = new ArrayList<>();
		if (safeString(objectId).isEmpty()) {
			return names;
		}
		List<Map<String, Object>> rows = dao.selectSubFileInfo(objectId);
		if (rows == null) {
			return names;
		}
		for (Map<String, Object> row : rows) {
			String name = getInfoString(row, "orgFileNm", "ORG_FILE_NM");
			if (!name.isEmpty()) {
				names.add(name);
			}
		}
		return names;
	}

	private List<String> getRequiredApprovalUsers(Map<String, Object> info) {
		List<String> result = new ArrayList<>();
		result.addAll(splitCsv(getInfoString(info, "approver", "APPROVER")));
		for (String reviewer : splitCsv(getInfoString(info, "reviewerUser", "reviewer_user", "REVIEWER_USER", "REVIEWERUSER"))) {
			if (!result.contains(reviewer)) {
				result.add(reviewer);
			}
		}
		return result;
	}

	private String trimCsv(String value) {
		return String.join(",", splitCsv(value));
	}

	private boolean isApprovalCompleted(Map<String, Object> info) {
		if (info == null || info.isEmpty()) {
			return false;
		}
		String status = getInfoString(info, "status", "STATUS");
		if (STATUS_APPROVED.equals(status)) {
			return true;
		}
		List<String> requiredUsers = getRequiredApprovalUsers(info);
		if (requiredUsers.isEmpty()) {
			return false;
		}
		String approvedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = approvedUsers.isEmpty() ? status : approvedUsers;
		Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
		return approvedSet.containsAll(requiredUsers);
	}

	private String normalizeCompareKey(String value) {
		if (value == null) {
			return "";
		}
		return value.replaceAll("\\s+", "").toLowerCase();
	}

	public int selectNextProductionRegisterNo(String levelNo) {
		Map<String, Object> param = new HashMap<>();
		param.put("levelNo", levelNo == null ? "" : levelNo.trim());
		Integer nextNo = dao.selectNextProductionRegisterNo(param);
		return nextNo == null ? 1 : nextNo;
	}
}




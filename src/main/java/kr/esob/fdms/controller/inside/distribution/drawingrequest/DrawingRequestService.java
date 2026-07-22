package kr.esob.fdms.controller.inside.distribution.drawingrequest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.convert.ConvertLogDao;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.commonrequest.RequestParam;
import kr.esob.fdms.controller.outside.drawing.request.DrawingInfoVO;
import kr.esob.fdms.util.RandomStringGenerator;
import kr.esob.fdms.util.StringUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
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
import java.io.UnsupportedEncodingException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Log4j2
public class DrawingRequestService implements CommonService{
	private static final String FUNCTION_CODE_OBJECT_TYPE = "FUNCTIONCODE";

	@Inject
	DrawingRequestDao dao;
	@Inject
	DocsMailService mailService;

	@Autowired
	DocPdfLinkRequestDao pdfDao;

	@Autowired
	ConvertLogDao convertLogDao;

	@Override
	public List selectList(Object param) {
		List<DrawingRequestVO> rtnList = dao.selectList(param);
//		for(int i=0; i<rtnList.size(); i++){
//			String filePath = "";
//			// 파일 PATH 구분자가 다른 경우 통일 처리
//			if(rtnList.get(i).getFilePath().contains("/")) {
//				rtnList.get(i).setFilePath(rtnList.get(i).getFilePath().replaceAll("/", "\\\\"));
//			}
//			if("".equals(filePath)) {
//				filePath = "" + rtnList.get(i).getFilePath().substring(0,rtnList.get(i).getFilePath().lastIndexOf("\\")+1) + rtnList.get(i).getOrgFileNm();
//			}else {
//				filePath += "" + rtnList.get(i).getFilePath().substring(0,rtnList.get(i).getFilePath().lastIndexOf("\\")+1) + rtnList.get(i).getOrgFileNm();
//			}
//			rtnList.get(i).setFilePath(filePath.replaceAll("\\\\", "\\\\\\\\\\\\\\\\"));
//		}
		return rtnList;
	}

	public List<DrawingRequestTreeVO> selectTree(Object param) {
		return dao.selectTree(param);
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

	public void setSearchAllParam(DrawingRequestParam param) {
		if(!"".equals(param.getSearchAllParam()) && param.getSearchAllParam() != null){
			Gson gson = new Gson();
			param.setDrawingList(gson.fromJson(param.getSearchAllParam().replace("&quot;","'"), new TypeToken<List<DrawingInfoVO>>() {}.getType()));
		}
	}
	// 2020.07.10 기범추가( 등록 )
	public ResultVO saveDrawingRegisterFileX2(MultipartHttpServletRequest request) throws Exception {
		String isNewRevision = request.getParameter("isNewRevision");

		ResultVO resultVo = new ResultVO();
		System.out.println("파라미터 맵 확인 = " + request.getParameterMap());
		System.out.println("파일명 파라미터 확인 = " + request.getParameter("fileName"));
		System.out.println("objectType -> " + request.getParameter("objectType"));
		MultipartFile file = request.getFile("file");
		List<MultipartFile> subFiles = request.getFiles("subFiles");

		System.out.println("protectYn 값 확인 = " + request.getParameter("protectYn"));

		String drawingType = request.getParameter("drawingType");
		String distributionPoint = request.getParameter("distributionPoint");
		String treeCd = request.getParameter("treeCd");
		treeCd = (treeCd == null) ? "" : treeCd.trim();
		String drawingNoParam = request.getParameter("drawingNo");
		String fileTypeParam = request.getParameter("fileType");
		String approvalDate = request.getParameter("approvalDate");
		String approver = request.getParameter("approver");
		String statusParam = request.getParameter("status");
		String coPublisher = request.getParameter("coPublisher");
		String reviewerUser = request.getParameter("reviewerUser");
		String objectId = RandomStringGenerator.generateRandomString(32);
		DrawingRegisterPopupParam drawingRegisterPopupParam = new DrawingRegisterPopupParam();
		boolean hasPreviousRevision = false; // 동일 문서번호의 기존 등록본 존재 여부

		if (isNewRevision != null && isNewRevision.equals("true")) {

			String orgFileNm = request.getParameter("orgFileNm");
			String prevObjectId = request.getParameter("objectId");
			String currentPageNo = "1";
			String statudCd = "승인진행중";
			// obejctId로 조회 DB에서 조회
			DrawingRequestVO prevRevisionData = dao.getPrevRevisionData(prevObjectId);

			drawingType = prevRevisionData.getDrawingType();
			// 새로운 objectId 생성
			String filePathNm;

			if (drawingType.equals("2D")) {
				filePathNm = SystemConfig.getSystemConfigValue("2D_FILE_PATH") + "\\" + objectId + "." + FilenameUtils.getExtension(request.getParameter("orgFileNm"));
			} else if (drawingType.equals("3D")) {
				filePathNm = SystemConfig.getSystemConfigValue("3D_FILE_PATH") + "\\" + objectId + "." + FilenameUtils.getExtension(request.getParameter("orgFileNm"));
			} else {
				log.info("Unexpected drawing type: {}. Expected 2D or 3D.", drawingType);
				filePathNm = SystemConfig.getSystemConfigValue("2D_FILE_PATH") + "\\" + objectId + "." + FilenameUtils.getExtension(request.getParameter("orgFileNm"));
			}
			// 업데이트된 리비전 (기존 리비전 + 1)
			String updatedRevNo = String.valueOf(Integer.parseInt(prevRevisionData.getRevNo()) + 1);

			drawingRegisterPopupParam = DrawingRegisterPopupParam.builder()
					.drawingNo(request.getParameter("prevDrawingNo"))
					.treeCd(treeCd)
					.approvalDate(approvalDate)
					.approver(approver)
					.status((statusParam != null && !"".equals(statusParam.trim())) ? statusParam : statudCd)
					.coPublisher((coPublisher != null && !"".equals(coPublisher.trim())) ? coPublisher : distributionPoint)
					.reviewerUser((reviewerUser != null && !"".equals(reviewerUser.trim())) ? reviewerUser : "")
					.orgFileNm(orgFileNm)
					.objectType(request.getParameter("objectType"))
					.totalPageNo(Objects.toString(prevRevisionData.getTotalPageNo(), ""))
					.currentPageNo(currentPageNo)
					.businessTypeCd(prevRevisionData.getBusinessTypeCd())
					.distributeTypeCd(prevRevisionData.getDistributeTypeCd())
					.fileType((fileTypeParam != null && !"".equals(fileTypeParam.trim())) ? fileTypeParam : prevRevisionData.getDistributeTypeCd())
					.objectId(objectId)
					.fileSize(String.valueOf(Objects.requireNonNull(file).getSize()))
					.fileNm(request.getParameter("fileName"))
					.revNo(updatedRevNo)
					.statusCd(statudCd)
					.drawingType(drawingType)
					.protectYn(prevRevisionData.getProtectYn())
					.filePath(filePathNm)
					.distributionPoint(distributionPoint)
					.modelCode(prevRevisionData.getModelCode())
//					.customerRevision(request.getParameter("customerRevision"))
					.build();
		}
		else{
			String currentPageNo = "1";
			String statudCd = "승인진행중";
			String revNo = resolveRevisionNo(request.getParameter("versionNo"), "0");
			if (drawingNoParam != null && !"".equals(drawingNoParam.trim())) {
				String latestRevNo = dao.selectLatestRevisionNoByDrawingNo(drawingNoParam.trim());
				if (latestRevNo != null && !"".equals(latestRevNo.trim())) {
					hasPreviousRevision = true;
					revNo = increaseRevisionNo(latestRevNo.trim());
				}
			}

			String filePathNm;
			// 2D인지 3D인지
			if (drawingType.equals("2D")) {
				filePathNm = SystemConfig.getSystemConfigValue("2D_FILE_PATH") + "\\" + objectId + "." + FilenameUtils.getExtension(request.getParameter("orgFileNm"));
			} else {
				filePathNm = SystemConfig.getSystemConfigValue("3D_FILE_PATH") + "\\" + objectId + "." + FilenameUtils.getExtension(request.getParameter("orgFileNm"));
			}

			// DrawingRegisterPopupParam 객체 만들어서 저장
			drawingRegisterPopupParam = DrawingRegisterPopupParam.builder()
					.drawingNo((drawingNoParam != null && !"".equals(drawingNoParam.trim())) ? drawingNoParam : "DRAWING_" + objectId.substring(0, 8))
					.treeCd(treeCd)
					.approvalDate(approvalDate)
					.approver(approver)
					.status((statusParam != null && !"".equals(statusParam.trim())) ? statusParam : statudCd)
					.coPublisher((coPublisher != null && !"".equals(coPublisher.trim())) ? coPublisher : distributionPoint)
					.reviewerUser((reviewerUser != null && !"".equals(reviewerUser.trim())) ? reviewerUser : "")
					.orgFileNm(request.getParameter("orgFileNm"))
					.objectType(request.getParameter("objectType"))
					.totalPageNo(request.getParameter("totalPageNo"))
					.currentPageNo(currentPageNo)
					.businessTypeCd(request.getParameter("businessTypeCd"))
					.distributeTypeCd(request.getParameter("distributeTypeCd"))
					.fileType((fileTypeParam != null && !"".equals(fileTypeParam.trim())) ? fileTypeParam : request.getParameter("distributeTypeCd"))
					.objectId(objectId)
					.fileSize(String.valueOf(Objects.requireNonNull(file).getSize()))
					.fileNm(request.getParameter("fileName"))
					.revNo(revNo)
					.statusCd(statudCd)
					.drawingType(drawingType)
					.protectYn(request.getParameter("protectYn"))
					.filePath(filePathNm)
					.distributionPoint(distributionPoint)
					.modelCode(request.getParameter("modelCode"))
//					.customerRevision(request.getParameter("customerRevision"))
					.build();
		}
		String existingFileName = dao.getDrawingRegisterByOrgFileNm(objectId);
		if(existingFileName != null){
			resultVo.setSuccess(false);
			resultVo.setMessage("이미 존재하는 파일입니다.");
			return resultVo;

		}else {

			System.out.println("drawingRegisterPopupParam 확인 = " + drawingRegisterPopupParam);

			if (isNewRevision != null && isNewRevision.equals("true")) {
				// 도면 리비전 업데이트 시
				dao.insertDrawingRevisionUpdate(drawingRegisterPopupParam);
			} else {
				// 도면 등록 시
				dao.insertDrawingRegisterInfo(drawingRegisterPopupParam);
			}

			if(drawingType.equals("2D")){
				drawingRegisterPopupParam = getFileSavedPath2D(drawingRegisterPopupParam, file);
			}else{
				drawingRegisterPopupParam = getFileSavedPath3D(drawingRegisterPopupParam, file);
			}
			saveDrawingSubFiles(subFiles, drawingRegisterPopupParam.getObjectId(), drawingType);
			convertFunctionCodeMainFileAtRegister(drawingRegisterPopupParam);
			saveFunctionCodeApprovalDetails(drawingRegisterPopupParam);
			String mailDocumentName = safeString(drawingRegisterPopupParam.getDrawingNm());
			if (mailDocumentName.isEmpty()) {
				mailDocumentName = safeString(drawingRegisterPopupParam.getFileNm());
			}
			Map<String, Object> mailDrawingInfo = dao.selectDrawingApprovalInfo(drawingRegisterPopupParam.getObjectId());
			String mailRegistrant = getInfoString(mailDrawingInfo, "insertUserNm", "INSERT_USER_NM", "INSERTUSERNM");
			if (mailRegistrant.isEmpty()) {
				mailRegistrant = safeString(request.getParameter("insertUserNm"));
			}
			if (mailRegistrant.isEmpty()) {
				mailRegistrant = safeString(request.getParameter("registerUser"));
			}
			Set<String> mailTargets = new LinkedHashSet<>();
			mailTargets.addAll(splitCsv(safeString(request.getParameter("approver"))));
			mailTargets.addAll(splitCsv(safeString(request.getParameter("reviewerUser"))));
			sendRegistrationMail(
					String.join(",", mailTargets),
					DocsMailEnum.DISTRIBUTION_DRAWING_STATUS,
					safeString(drawingRegisterPopupParam.getDrawingNo()),
					mailDocumentName,
					mailRegistrant,
					LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
			);

			resultVo.setSuccess(true);
			return resultVo;
		}
	}


	private void saveFunctionCodeApprovalDetails(DrawingRegisterPopupParam param) {
		String objectId = safeString(param.getObjectId());
		if (objectId.isEmpty()) {
			return;
		}
		insertApprovalDetails(objectId, "REVIEWER", splitCsv(safeString(param.getReviewerUser())));
		insertApprovalDetails(objectId, "APPROVER", splitCsv(safeString(param.getApprover())));
	}

	private void insertApprovalDetails(String objectId, String approvalRole, List<String> users) {
		if (users == null || users.isEmpty()) {
			return;
		}
		for (int i = 0; i < users.size(); i++) {
			String userNm = safeString(users.get(i));
			if (userNm.isEmpty()) {
				continue;
			}
			Map<String, Object> detail = new HashMap<>();
			detail.put("objectId", objectId);
			detail.put("objectType", FUNCTION_CODE_OBJECT_TYPE);
			detail.put("approvalRole", approvalRole);
			detail.put("approvalOrder", i + 1);
			detail.put("userNm", userNm);
			detail.put("userId", splitApprovedUser(userNm));
			dao.insertApprovalDetail(detail);
		}
	}

	private String resolveRevisionNo(String versionNo, String defaultRevNo) {
		if (versionNo == null) {
			return defaultRevNo;
		}
		String normalized = versionNo.trim();
		if (normalized.isEmpty()) {
			return defaultRevNo;
		}
		return normalized;
	}
	// 버전 번호 자동 증가
	private String increaseRevisionNo(String currentRevNo) {
		if (currentRevNo == null || currentRevNo.trim().isEmpty()) {
			return "1";
		}

		String value = currentRevNo.trim();
		Matcher matcher = Pattern.compile("^(.*?)(\\d+)$").matcher(value);
		if (matcher.matches()) {
			String prefix = matcher.group(1);
			String numberPart = matcher.group(2);
			int next = Integer.parseInt(numberPart) + 1;
			return prefix + String.format("%0" + numberPart.length() + "d", next);
		}

		try {
			return String.valueOf(Integer.parseInt(value) + 1);
		} catch (NumberFormatException e) {
			return value + "1";
		}
	}
	// 버전 타입별 다음 버전 번호 계산
	public String getNextVersionNo(String drawingNo, String versionType) {
		String prefix = "P";
		if ("Draft".equalsIgnoreCase(versionType)) {
			prefix = "D";
		} else if ("Final".equalsIgnoreCase(versionType)) {
			prefix = "F";
		}

		if (drawingNo == null || drawingNo.trim().isEmpty()) {
			return prefix + "00";
		}

		String latestRevNo = dao.selectLatestRevisionNoByDrawingNo(drawingNo.trim());
		if (latestRevNo == null || latestRevNo.trim().isEmpty()) {
			return prefix + "00";
		}
		return increaseRevisionNo(latestRevNo.trim());
	}

	private DrawingRegisterPopupParam getFileSavedPath2D(DrawingRegisterPopupParam param, MultipartFile mf){
//		String filePathNm = SystemConfig.getSystemConfigValue("2D_FILE_PATH")+"\\" + param.getObjectId() +"." + FilenameUtils.getExtension(param.getOrgFileNm());
		String filePathNm = param.getFilePath();

		String orgName = mf.getOriginalFilename();

		if(orgName.contains(File.separator)) {
			orgName = orgName.substring(orgName.lastIndexOf(File.separator)+1, orgName.length());
		}
		orgName = StringUtil.replaceLfiPath(orgName);
		String path = filePathNm;
		path = StringUtil.replaceLfiPath(path);
		File file = new File(path);
		param.setFilePath(path);
		System.out.println("파일 경로: " + path);

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

	private DrawingRegisterPopupParam getFileSavedPath3D(DrawingRegisterPopupParam param, MultipartFile mf){
//		String filePathNm = SystemConfig.getSystemConfigValue("3D_FILE_PATH")+"\\" + param.getObjectId() +"." + FilenameUtils.getExtension(param.getOrgFileNm());
		String filePathNm = param.getFilePath();
		File filePath = new File(filePathNm);

		String orgName = mf.getOriginalFilename();

		if(orgName.contains(File.separator)) {
			orgName = orgName.substring(orgName.lastIndexOf(File.separator)+1, orgName.length());
		}
		orgName = StringUtil.replaceLfiPath(orgName);
		String path = filePathNm;
		path = StringUtil.replaceLfiPath(path);
		File file = new File(path);
		param.setFilePath(path);
		System.out.println("파일 경로: " + path);

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

	private void saveDrawingSubFiles(List<MultipartFile> subFiles, String parentObjectId, String drawingType) {
		if (subFiles == null || subFiles.isEmpty()) {
			return;
		}

		for (MultipartFile subFile : subFiles) {
			if (subFile == null || subFile.isEmpty()) {
				continue;
			}

			String subObjectId = RandomStringGenerator.generateRandomString(32);
			String originalName = subFile.getOriginalFilename();
			if (originalName == null) {
				originalName = "";
			}
			if (originalName.contains(File.separator)) {
				originalName = originalName.substring(originalName.lastIndexOf(File.separator) + 1);
			}
			originalName = StringUtil.replaceLfiPath(originalName);

			String extension = FilenameUtils.getExtension(originalName);
			String targetPath = buildDrawingSavedPath(drawingType, subObjectId, extension);
			targetPath = StringUtil.replaceLfiPath(targetPath);

			File outFile = new File(targetPath);
			try {
				outFile.createNewFile();
				Files.write(Paths.get(targetPath), subFile.getBytes());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			String convertedSubPdfPath = ensureConvertedPdfForCover(targetPath, subObjectId, "FC_SUB_COVER");
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

			DrawingSubFileParam subFileParam = DrawingSubFileParam.builder()
					.objectId(subObjectId)
					.parentObjectId(parentObjectId)
					.orgFileNm(savedOrgFileNm)
					.filePath(savedPath)
					.fileSize(String.valueOf(savedSize))
					.useYn("Y")
					.build();

			dao.insertDrawingSubFile(subFileParam);
		}
	}

	private String buildDrawingSavedPath(String drawingType, String objectId, String extension) {
		String basePath;
		if ("3D".equals(drawingType)) {
			basePath = SystemConfig.getSystemConfigValue("3D_FILE_PATH");
		} else {
			basePath = SystemConfig.getSystemConfigValue("2D_FILE_PATH");
		}

		if (extension == null || extension.trim().isEmpty()) {
			return basePath + "\\" + objectId;
		}
		return basePath + "\\" + objectId + "." + extension;
	}

	private void convertFunctionCodeMainFileAtRegister(DrawingRegisterPopupParam param) {
		String objectId = safeString(param.getObjectId());
		String sourceFilePath = safeString(param.getFilePath());
		if (objectId.isEmpty() || sourceFilePath.isEmpty()) {
			return;
		}
		String inputPdfPath = ensureConvertedPdfForCover(sourceFilePath, objectId, "FC_CONVERT");
		if (inputPdfPath.isEmpty() || !inputPdfPath.toLowerCase().endsWith(".pdf")) {
			markMainFileProcessingFail(objectId, "convert failed: pdf output not found", "FC_CONVERT");
			return;
		}
		File convertedMain = new File(inputPdfPath);
		if (!convertedMain.isFile()) {
			markMainFileProcessingFail(objectId, "convert failed: pdf file not found", "FC_CONVERT");
			return;
		}
		Map<String, Object> convertedUpdateParam = new HashMap<>();
		convertedUpdateParam.put("objectId", objectId);
		convertedUpdateParam.put("filePath", inputPdfPath);
		convertedUpdateParam.put("orgFileNm", toPdfFileName(safeString(param.getOrgFileNm()), safeString(param.getFileNm())));
		convertedUpdateParam.put("fileSize", String.valueOf(convertedMain.length()));
		int updated = dao.updateMainFileAfterCoverMerge(convertedUpdateParam);
		System.out.println("[FC_CONVERT] register main update rows=" + updated + ", filePath=" + inputPdfPath);
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

	private String mergeFunctionCodeCoverAfterApproval(String objectId) {
		try {
			Map<String, Object> drawingInfo = dao.selectDrawingApprovalInfo(objectId);
			if (drawingInfo == null || drawingInfo.isEmpty()) {
				System.out.println("[FC_COVER] skip: drawingInfo is empty. objectId=" + objectId);
				return "drawingInfo is empty";
			}
			String sourceFilePath = getInfoString(drawingInfo, "filePath", "FILE_PATH_NM", "file_path_nm");
			String inputPdfPath = ensureConvertedPdfForCover(sourceFilePath, objectId, "FC_COVER");
			if (inputPdfPath.isEmpty()) {
				System.out.println("[FC_COVER] skip: inputPdfPath is empty");
				return "inputPdfPath is empty";
			}
			if (!inputPdfPath.equals(sourceFilePath) && inputPdfPath.toLowerCase().endsWith(".pdf")) {
				File convertedMain = new File(inputPdfPath);
				if (convertedMain.isFile()) {
					Map<String, Object> convertedUpdateParam = new HashMap<>();
					convertedUpdateParam.put("objectId", objectId);
					convertedUpdateParam.put("filePath", inputPdfPath);
					convertedUpdateParam.put("orgFileNm", toPdfFileName(getInfoString(drawingInfo, "orgFileNm", "ORG_FILE_NM"), getInfoString(drawingInfo, "fileNm", "FILE_NM")));
					convertedUpdateParam.put("fileSize", String.valueOf(convertedMain.length()));
					int updated = dao.updateMainFileAfterCoverMerge(convertedUpdateParam);
					System.out.println("[FC_COVER] pre-merge main update rows=" + updated + ", filePath=" + inputPdfPath);
				}
			}
			String outputFileName = "cover_" + objectId + ".pdf";

			String ownerNm = getInfoString(drawingInfo, "insertUserNm", "INSERT_USER_NM", "INSERTUSERNM");
			List<String> preparedBy = splitCsv(ownerNm);
			if (preparedBy.isEmpty()) {
				preparedBy = splitCsv(getInfoString(drawingInfo, "coPublisher", "COPUBLISHER"));
			}
			List<String> reviewerUsers = splitCsv(getInfoString(drawingInfo, "reviewerUser", "REVIEWERUSER", "reviewer_user"));
			List<String> approverUsers = splitCsv(getInfoString(drawingInfo, "approver", "APPROVER"));
			List<String> subFileNames = getSavedSubFileNames(objectId);
			Map<String, Object> detailParam = new HashMap<>();
			detailParam.put("objectId", objectId);
			detailParam.put("objectType", FUNCTION_CODE_OBJECT_TYPE);
			List<Map<String, Object>> approvalDetails = dao.selectApprovalDetails(detailParam);
			List<String> reviewerDates = buildApprovalDatesByNames(reviewerUsers, "REVIEWER", approvalDetails);
			List<String> approverDates = buildApprovalDatesByNames(approverUsers, "APPROVER", approvalDetails);

			Map<String, String> positionMap = getUserPositionMapByNames(preparedBy, reviewerUsers, approverUsers);
			List<String> preparedPositions = buildPositionsByNames(preparedBy, positionMap);
			List<String> reviewerPositions = buildPositionsByNames(reviewerUsers, positionMap);
			List<String> approverPositions = buildPositionsByNames(approverUsers, positionMap);

			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("inputPdfPath", inputPdfPath);
			requestBody.put("outputFileName", outputFileName);
			requestBody.put("cover_doc_type", "Function Code");
			requestBody.put("cover_request_no", "-");
			requestBody.put("cover_object_id", objectId);
			requestBody.put("cover_user_id", "-");
			requestBody.put("cover_user_name", getInfoString(drawingInfo, "coPublisher", "COPUBLISHER"));
			requestBody.put("cover_viewed_at", getInfoString(drawingInfo, "insertDt", "INSERT_DT"));
			requestBody.put("cover_title", getInfoString(drawingInfo, "orgFileNm", "ORG_FILE_NM", "fileNm", "FILE_NM"));
			requestBody.put("cover_note", "");
			requestBody.put("cover_document_no", getInfoString(drawingInfo, "drawingNo", "DRAWING_NO"));
			requestBody.put("cover_issue", getInfoString(drawingInfo, "revNo", "REV_NO"));
			requestBody.put("revision", getInfoString(drawingInfo, "revNo", "REV_NO"));
			requestBody.put("fileName", getInfoString(drawingInfo, "orgFileNm", "ORG_FILE_NM", "fileNm", "FILE_NM"));
			requestBody.put("cover_prepared_by", preparedBy);
			requestBody.put("cover_prepared_positions", preparedPositions);
			requestBody.put("cover_reviewer_users", reviewerUsers);
			requestBody.put("cover_reviewer_positions", reviewerPositions);
			requestBody.put("cover_reviewer_dates", reviewerDates);
			requestBody.put("cover_approver_users", approverUsers);
			requestBody.put("cover_approver_positions", approverPositions);
			requestBody.put("cover_approver_dates", approverDates);
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
			System.out.println("[FC_COVER] distributeTypeCd=" + getInfoString(drawingInfo, "distributeTypeCd", "DISTRIBUTE_TYPE_CD") + ", fileType=" + getInfoString(drawingInfo, "fileType", "FILE_TYPE"));
			System.out.println("[FC_COVER] templatePdfPath=" + templatePdfPath);
			System.out.println("[FC_COVER] endpoint=" + endpoint);
			System.out.println("[FC_COVER] inputPdfPath=" + inputPdfPath + ", outputFileName=" + outputFileName);
			System.out.println("[FC_COVER] approverCount=" + approverUsers.size() + ", reviewerCount=" + reviewerUsers.size() + ", preparedCount=" + preparedBy.size());

			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
			String responseBody = response.getBody();
			System.out.println("[FC_COVER] responseStatus=" + response.getStatusCodeValue() + ", body=" + responseBody);

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
						updateParam.put("orgFileNm", toPdfFileName(getInfoString(drawingInfo, "orgFileNm", "ORG_FILE_NM"), getInfoString(drawingInfo, "fileNm", "FILE_NM")));
						updateParam.put("fileSize", String.valueOf(mergedFile.length()));
						int updated = dao.updateMainFileAfterCoverMerge(updateParam);
						copyMergedPdfToViewerCache(outputPdfPath, objectId, "FC_COVER");
						System.out.println("[FC_COVER] post-merge main update rows=" + updated + ", filePath=" + inputPdfPath);
						System.out.println("[FC_COVER] overwrite source file with merged cover: " + inputPdfPath);
					} else {
						System.out.println("[FC_COVER] merged output file not found: " + outputPdfPath);
					}
				}
			}
			return null;
		} catch (Exception e) {
			System.out.println("[FC_COVER] exception: " + e.getMessage());
			log.warn("Function Code cover merge skipped after approval", e);
			return e.getMessage();
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
			System.out.println("[DRAWING_REGISTER_MAIL] targetUsersCsv=" + targetUsersCsv);
			for (String token : splitCsv(targetUsersCsv)) {
				System.out.println("[DRAWING_REGISTER_MAIL] token=" + token);
				MailInfoVO mailInfoVo = mailService.selectReceiveUser(token);
				if (mailInfoVo == null) {
					System.out.println("[DRAWING_REGISTER_MAIL] selectReceiveUser is null. token=" + token);
					continue;
				}
				System.out.println("[DRAWING_REGISTER_MAIL] toUserId=" + mailInfoVo.getToUserId() + ", toMail=" + mailInfoVo.getToMail());
				mailInfoVo.setMailEnum(mailEnum);
				mailInfoVo.setContent(buildRegistrationMailContent(
						mailInfoVo,
						"Document",
						documentNo,
						documentName,
						registrant,
						registeredAt
				));
				ResultVO mailResult = mailService.sendDocsMail(mailInfoVo);
				System.out.println("[DRAWING_REGISTER_MAIL] sendDocsMail success=" + (mailResult != null && mailResult.isSuccess()));
			}
		} catch (Exception e) {
			System.out.println("[DRAWING_REGISTER_MAIL] skip: " + e.getMessage());
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
			requestFactory.setReadTimeout(300000); //5분동안 변환 안되면 실패
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

	private String toPdfFileName(String orgFileNm, String fallbackName) {
		String source = safeString(orgFileNm);
		if (source.isEmpty()) {
			source = safeString(fallbackName);
		}
		if (source.isEmpty()) {
			return "document.pdf";
		}
		int dot = source.lastIndexOf('.');
		if (dot > 0) {
			return source.substring(0, dot) + ".pdf";
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

	private List<String> getSavedSubFileNames(String objectId) {
		List<String> names = new ArrayList<>();
		for (Map<String, Object> subFile : dao.selectSubFileInfo(objectId)) {
			String name = getInfoString(subFile, "orgFileNm", "ORG_FILE_NM");
			if (!name.isEmpty()) {
				names.add(name);
			}
		}
		return names;
	}

	private List<String> buildApprovalDatesByNames(List<String> users, String approvalRole, List<Map<String, Object>> details) {
		List<String> dates = new ArrayList<>();
		List<Map<String, Object>> roleDetails = new ArrayList<>();
		if (details != null) {
			for (Map<String, Object> detail : details) {
				if (approvalRole.equals(getInfoString(detail, "approvalRole", "APPROVAL_ROLE"))) {
					roleDetails.add(detail);
				}
			}
		}
		for (int i = 0; i < users.size(); i++) {
			String user = users.get(i);
			String normalizedUser = splitApprovedUser(user);
			String approvalDt = "";
			for (Map<String, Object> detail : roleDetails) {
				String detailUserNm = splitApprovedUser(getInfoString(detail, "userNm", "USER_NM"));
				String detailUserId = splitApprovedUser(getInfoString(detail, "userId", "USER_ID"));
				if (normalizedUser.equals(detailUserNm) || normalizedUser.equals(detailUserId)) {
					approvalDt = getInfoString(detail, "approvalDt", "APPROVAL_DT");
					break;
				}
			}
			if (approvalDt.isEmpty() && i < roleDetails.size()) {
				approvalDt = getInfoString(roleDetails.get(i), "approvalDt", "APPROVAL_DT");
			}
			dates.add(approvalDt);
		}
		System.out.println("[FC_COVER] " + approvalRole + " dates=" + dates);
		return dates;
	}

	private void updateFunctionCodeApprovalDetail(String objectId, String userCd, String userNm) {
		Map<String, Object> detail = new HashMap<>();
		detail.put("objectId", objectId);
		detail.put("objectType", FUNCTION_CODE_OBJECT_TYPE);
		detail.put("userId", safeString(userCd));
		detail.put("userNm", safeString(userNm));
		int updated = dao.updateApprovalDetailApproved(detail);
		System.out.println("[FC_APPROVAL_DETAIL] approved rows=" + updated + ", objectId=" + objectId + ", userCd=" + userCd + ", userNm=" + userNm);
	}

	private boolean isFunctionCodeApprovalDetailCompleted(String objectId) {
		Map<String, Object> detailParam = new HashMap<>();
		detailParam.put("objectId", objectId);
		detailParam.put("objectType", FUNCTION_CODE_OBJECT_TYPE);
		List<Map<String, Object>> details = dao.selectApprovalDetails(detailParam);
		if (details == null || details.isEmpty()) {
			return false;
		}
		for (Map<String, Object> detail : details) {
			if (!"APPROVAL".equals(getInfoString(detail, "approvalStatus", "APPROVAL_STATUS"))) {
				return false;
			}
		}
		return true;
	}

	private Map<String, String> getUserPositionMapByNames(List<String>... userNameLists) {
		Set<String> allNames = new LinkedHashSet<>();
		if (userNameLists != null) {
			for (List<String> userNameList : userNameLists) {
				if (userNameList == null) {
					continue;
				}
				for (String userName : userNameList) {
					String normalizedName = safeString(userName);
					if (!normalizedName.isEmpty()) {
						allNames.add(normalizedName);
					}
				}
			}
		}

		if (allNames.isEmpty()) {
			return new HashMap<>();
		}

		Map<String, Object> queryParam = new HashMap<>();
		queryParam.put("names", new ArrayList<>(allNames));
		List<Map<String, Object>> rows = dao.selectUserPositionByNames(queryParam);

		Map<String, String> positionMap = new HashMap<>();
		if (rows == null) {
			return positionMap;
		}

		for (Map<String, Object> row : rows) {
			String userName = safeString(row.get("userName"));
			String positionName = safeString(row.get("positionName"));
			if (!userName.isEmpty()) {
				positionMap.put(userName, positionName);
			}
		}
		return positionMap;
	}

	private List<String> buildPositionsByNames(List<String> names, Map<String, String> positionMap) {
		List<String> positions = new ArrayList<>();
		if (names == null) {
			return positions;
		}
		for (String name : names) {
			String normalizedName = safeString(name);
			if (normalizedName.isEmpty()) {
				continue;
			}
			String position = positionMap == null ? "" : safeString(positionMap.get(normalizedName));
			positions.add(position);
		}
		return positions;
	}

	public String compareImageRequest(RequestParam param, Authentication authentication) throws UnsupportedEncodingException {
		List<Map<String,Object>> dbConfig = pdfDao.selectDbConfig();
		String baseUrl="";
		String imgCompareUrl="";

		for(Map<String,Object> config : dbConfig) {
			System.out.println("Config keys and values: ");
			for (String key : config.keySet()) {
				System.out.println(key + " = " + config.get(key));
			}
			if(config.get("SYSTEM_CONFIG_CD").equals("BASE_URL")){
				baseUrl = config.get("SYSTEM_CONFIG_VALUE").toString();
			}
			if(config.get("SYSTEM_CONFIG_CD").equals("ADAP_IMG_COMPARE_URL")){
				imgCompareUrl = config.get("SYSTEM_CONFIG_VALUE").toString();
			}
		}

		String prevObjectId = param.getPrevObjectId();
		String curObjectId = param.getCurObjectId();
		String objectType = param.getObjectType();

		UserVO userVo = (UserVO) authentication.getPrincipal();
		String userId = userVo.getUserCd();

		String urlPrev = baseUrl + "/inside/distribution/docPdfLinkRequest/pdfConvert"
				+ "?file=" + prevObjectId + "&objectType=" + objectType + "&requestNo=" + "null" + "&fileNo=" + "null" + "&userId=" + userId + "&filePath=" + param.getPrevFilePath();

		String urlCur = baseUrl + "/inside/distribution/docPdfLinkRequest/pdfConvert"
				+ "?file=" + curObjectId + "&objectType=" + objectType + "&requestNo=" + "null" + "&fileNo=" + "null" + "&userId=" + userId + "&filePath=" + param.getCurFilePath();

		RestTemplate restTemplate = new RestTemplate();

		try {
			// GET 요청 실행
			ResponseEntity<Map> responsePrev = restTemplate.getForEntity(urlPrev, Map.class);
			ResponseEntity<Map> responseCur = restTemplate.getForEntity(urlCur, Map.class);

			Map<String, Object> bodyPrev = responsePrev.getBody();
			Map<String, Object> bodyCur = responseCur.getBody();

			// 두 pdfConvert 요청 모두 success인 경우
			if ("success".equals(bodyPrev.get("status")) && "success".equals(bodyCur.get("status"))) {
				String prevFilePath = (String) bodyPrev.get("cvrtFilePathNm");
				String curFilePath = (String) bodyCur.get("cvrtFilePathNm");
				String prevRevNo = param.getPrevRevNo();
				String curRevNo = param.getCurRevNo();

				String convertedPrevFilePath = prevFilePath.substring(0, prevFilePath.lastIndexOf(".pdf")) + ".esob";
				String convertedCurFilePath = curFilePath.substring(0, curFilePath.lastIndexOf(".pdf")) + ".esob";

				// 이미지 비교 리다이렉트 URL 구성
				String redirectUrl = imgCompareUrl
						+ "?Prev_Path=" + convertedPrevFilePath
						+ "&Cur_Path=" + convertedCurFilePath
						+ "&Prev_DisplayLabel=" + prevRevNo
						+ "&Cur_DisplayLabel=" + curRevNo
						+ "&Request_Flag=collabhub";

				log.info("redirectUrl: {}", redirectUrl);
				return "redirect:" + redirectUrl;
			} else {
				// 두 요청 중 하나라도 success가 아닌 경우
				if (!"success".equals(bodyPrev.get("status")) && !"success".equals(bodyCur.get("status"))) {
					// 양쪽 모두 변환 실패한 경우
					return "both_failed";
				} else if (!"success".equals(bodyPrev.get("status"))) {
					// 이전 리비전 파일 변환 실패한 경우
					return "prev_failed";
				} else {
					// 현재 리비전 파일 변환 실패한 경우
					return "cur_failed";
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			return "http_exception: " + e.getMessage();
		}
	}

	public int deleteDrawing(String objectId) {
		Map<String, String> param = new HashMap<>();
		param.put("objectId", objectId);
		return dao.deleteDrawing(param);
	}

	public ResultVO validateDeleteDrawing(String objectId, UserVO userVo) {
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

		Map<String, Object> info = dao.selectDrawingApprovalInfo(objectId.trim());
		if (info == null || info.isEmpty()) {
			result.setSuccess(false);
			result.setMessage("대상 문서를 찾을 수 없습니다.");
			return result;
		}

		String deletedYn = getInfoString(info, "deletedYn", "DELETED_YN");
		String ownerNm = getInfoString(info, "insertUserNm", "INSERT_USER_NM", "INSERTUSERNM");
		String ownerUid = getInfoString(info, "insertUid", "INSERT_UID", "INSERTUID");
		String coPublisher = getInfoString(info, "coPublisher", "CO_PUBLISHER", "COPUBLISHER");
		String loginUserNm = safeString(userVo.getUserNm());
		String loginUserCd = safeString(userVo.getUserCd());
		String loginUserId = safeString(userVo.getUserId());
		String ownerNmNorm = normalizeCompareKey(ownerNm);
		String ownerUidNorm = normalizeCompareKey(ownerUid);
		String coPublisherNorm = normalizeCompareKey(coPublisher);
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
			// Co-publisher withdrawal permission disabled by customer request.
//			else if (!coPublisherNorm.isEmpty()) {
//				List<String> coPublisherList = splitCsv(coPublisher);
//				for (String coPublisherToken : coPublisherList) {
//					String tokenNorm = normalizeCompareKey(coPublisherToken);
//					if (!tokenNorm.isEmpty() && (
//							tokenNorm.equals(loginUserNmNorm)
//							|| tokenNorm.equals(loginUserCdNorm)
//							|| tokenNorm.equals(loginUserIdNorm))) {
//						ownerMatched = true;
//						break;
//					}
//				}
//			}
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

	public boolean canDeleteDrawing(String objectId, UserVO userVo) {
		return validateDeleteDrawing(objectId, userVo).isSuccess();
	}

	private boolean isApprovalCompleted(Map<String, Object> info) {
		if (info == null || info.isEmpty()) {
			return false;
		}

		String status = getInfoString(info, "status", "STATUS");
		if (STATUS_APPROVED.equals(status)) {
			return true;
		}

		List<String> approverList = splitCsv(getInfoString(info, "approver", "APPROVER"));
		if (approverList.isEmpty()) {
			return false;
		}

		String approvedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = approvedUsers.isEmpty() ? status : approvedUsers;
		Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
		return approvedSet.containsAll(approverList);
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

	public Map<String, Object> getDrawingFileDownloadInfo(String objectId, String fileNo) {
		if (objectId == null || objectId.trim().isEmpty()) {
			return null;
		}
		Map<String, Object> param = new HashMap<>();
		param.put("objectId", objectId.trim());
		param.put("fileNo", fileNo == null ? "" : fileNo.trim());
		return dao.selectDrawingFileDownloadInfo(param);
	}

	private static final String STATUS_APPROVING = "승인진행중";
	private static final String STATUS_APPROVED = "승인완료";

	// 승인 처리 로직
	public ResultVO approveDrawing(String objectId, UserVO userVo) {
		ResultVO result = new ResultVO();
		Map<String, Object> info = dao.selectDrawingApprovalInfo(objectId);

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
			if (false && isFunctionCodeApprovalDetailCompleted(objectId)) {
				String coverMergeError = mergeFunctionCodeCoverAfterApproval(objectId);
				if (coverMergeError != null && !coverMergeError.trim().isEmpty()) {
					result.setSuccess(false);
					result.setMessage("이미 승인완료 상태이나 표지 병합 실패: " + coverMergeError);
					return result;
				}
				result.setSuccess(true);
				result.setMessage("이미 승인완료된 문서의 표지 병합을 완료했습니다.");
				return result;
			}
			result.setSuccess(false);
			result.setMessage("이미 승인완료된 건입니다.");
			return result;
		}

		String currentApprovedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = currentApprovedUsers.isEmpty() ? currentStatus : currentApprovedUsers;
		Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
		if (approvedSet.contains(approverToken)) {
			if (false && isFunctionCodeApprovalDetailCompleted(objectId)) {
				String coverMergeError = mergeFunctionCodeCoverAfterApproval(objectId);
				if (coverMergeError != null && !coverMergeError.trim().isEmpty()) {
					result.setSuccess(false);
					result.setMessage("이미 승인된 문서이나 표지 병합 실패: " + coverMergeError);
					return result;
				}
				result.setSuccess(true);
				result.setMessage("이미 승인된 문서의 표지 병합을 완료했습니다.");
				return result;
			}
			result.setSuccess(false);
			result.setMessage("이미 승인한 건입니다.");
			return result;
		}

		approvedSet.add(approverToken);
		boolean isFinalApprove = approvedSet.containsAll(requiredUsers);

		Map<String, Object> param = new HashMap<>();
		param.put("objectId", objectId);
		param.put("approvedUser", approverToken);
		param.put("isFinal", isFinalApprove ? "Y" : "N");

		int updated = dao.approveDrawing(param);
		if (updated > 0) {
			updateFunctionCodeApprovalDetail(objectId, userCd, userNm);
			boolean detailFinalApprove = isFunctionCodeApprovalDetailCompleted(objectId);
			System.out.println("[FC_APPROVAL] objectId=" + objectId + ", stringFinal=" + isFinalApprove + ", detailFinal=" + detailFinalApprove);
			if (isFinalApprove || detailFinalApprove) {
				String coverMergeError = mergeFunctionCodeCoverAfterApproval(objectId);
				if (coverMergeError != null && !coverMergeError.trim().isEmpty()) {
					result.setMessage("승인되었으나 표지 병합 실패: " + coverMergeError);
				}
			}
		}
		result.setSuccess(updated > 0);
		if (result.getMessage() == null || result.getMessage().trim().isEmpty()) {
			result.setMessage(updated > 0 ? "승인되었습니다." : "승인 처리에 실패했습니다.");
		}
		return result;
	}
	// 상태 메시지
	public ResultVO validateApproveDrawing(String objectId, UserVO userVo) {
		ResultVO result = new ResultVO();

		if (objectId == null || objectId.trim().isEmpty()) {
			result.setSuccess(false);
			result.setMessage("승인 대상을 확인할 수 없습니다.");
			return result;
		}

		Map<String, Object> info = dao.selectDrawingApprovalInfo(objectId.trim());
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
			if (false && isFunctionCodeApprovalDetailCompleted(objectId.trim())) {
				result.setSuccess(true);
				result.setMessage("OK");
				return result;
			}
			result.setSuccess(false);
			result.setMessage("이미 승인완료된 건입니다.");
			return result;
		}

		String currentApprovedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = currentApprovedUsers.isEmpty() ? currentStatus : currentApprovedUsers;
		Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
		if (approvedSet.contains(approverToken)) {
			if (false && isFunctionCodeApprovalDetailCompleted(objectId.trim())) {
				result.setSuccess(true);
				result.setMessage("OK");
				return result;
			}
			result.setSuccess(false);
			result.setMessage("이미 승인한 건입니다.");
			return result;
		}

		result.setSuccess(true);
		result.setMessage("OK");
		return result;
	}

	public String getApprovalStatusMessage(String objectId) {
		Map<String, Object> info = dao.selectDrawingApprovalInfo(objectId);
		if (info == null || info.isEmpty()) {
			return "상태 정보를 찾을 수 없습니다.";
		}

		String status = getInfoString(info, "status", "STATUS");
		boolean forceApproved = STATUS_APPROVED.equals(status);

		List<String> approverList = splitCsv(getInfoString(info, "approver", "APPROVER"));
		if (approverList.isEmpty()) {
			return forceApproved ? STATUS_APPROVED : "승인자가 지정되지 않았습니다.";
		}

		String approvedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = approvedUsers.isEmpty() ? status : approvedUsers;
		Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
		if (forceApproved) {
			approvedSet.addAll(approverList);
		}

		List<String> rows = new ArrayList<>();
		// rows.add("승인자 : 상태");
		for (String approver : approverList) {
			boolean isApproved = approvedSet.contains(approver);
			rows.add(approver + " : " + (isApproved ? "승인" : "미승인"));
		}
		return String.join("\n", rows);
	}

	public List<Map<String, Object>> getApprovalStatusRows(String objectId, UserVO userVo) {
		List<Map<String, Object>> rows = new ArrayList<>();
		Map<String, Object> info = dao.selectDrawingApprovalInfo(objectId);
		if (info == null || info.isEmpty()) return rows;

		String status = getInfoString(info, "status", "STATUS");
		boolean forceApproved = STATUS_APPROVED.equals(status);
		List<String> approverList = splitCsv(getInfoString(info, "approver", "APPROVER"));
		List<String> reviewerUsers = splitCsv(getInfoString(info, "reviewerUser", "reviewer_user", "REVIEWER_USER", "REVIEWERUSER"));
		List<String> targetUsers = new ArrayList<>();
		targetUsers.addAll(reviewerUsers);
		for (String approver : approverList) {
			if (!targetUsers.contains(approver)) {
				targetUsers.add(approver);
			}
		}

		String approvedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = approvedUsers.isEmpty() ? status : approvedUsers;
		Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
		if (forceApproved) approvedSet.addAll(targetUsers);

		Map<String, String> commentMap = getApprovalCommentMap(objectId);
		String userCd = safeString(userVo.getUserCd());
		String userNm = safeString(userVo.getUserNm());

		for (String targetUser : targetUsers) {
			Map<String, Object> row = new HashMap<>();
			String normalized = splitApprovedUser(targetUser);
			row.put("approver", targetUser);
			row.put("status", approvedSet.contains(normalized) ? "승인" : "대기");
			row.put("comment", safeString(commentMap.get(normalized)));
			row.put("editable", (targetUser.equals(userCd) || targetUser.equals(userNm)) ? "Y" : "N");
			row.put("approvalType", reviewerUsers.contains(targetUser) ? "REVIEWER" : "APPROVER");
			rows.add(row);
		}
		return rows;
	}

	public ResultVO saveApprovalComment(String objectId, String comment, UserVO userVo) {
		ResultVO result = new ResultVO();
		Map<String, Object> info = dao.selectDrawingApprovalInfo(objectId);
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
			result.setMessage("본인 결재에만 코멘트를 입력할 수 있습니다.");
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
		if (normalizedComment.isEmpty()) normalizedComment = "승인했습니다.";

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

	private String splitApprovedUser(String value) {
		String normalized = safeString(value);
		if (normalized.isEmpty()) {
			return "";
		}
		int pipeIndex = normalized.indexOf('|');
		if (pipeIndex >= 0) {
			normalized = normalized.substring(0, pipeIndex);
		}
		return safeString(normalized);
	}

	private List<String> getRequiredApprovalUsers(Map<String, Object> info) {
		List<String> requiredUsers = new ArrayList<>();
		List<String> reviewerUsers = splitCsv(getInfoString(info, "reviewerUser", "reviewer_user", "REVIEWER_USER", "REVIEWERUSER"));
		List<String> approverUsers = splitCsv(getInfoString(info, "approver", "APPROVER"));
		requiredUsers.addAll(reviewerUsers);
		for (String approver : approverUsers) {
			if (!requiredUsers.contains(approver)) {
				requiredUsers.add(approver);
			}
		}
		return requiredUsers;
	}
	// null 방지 및 공백 제거
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
	// CSV 분리
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
	// DB 상태값을 줄바꿈/콤마 기준으로 분리
	private List<String> parseApprovedUsers(String status) {
		if (status == null || status.trim().isEmpty()) {
			return new ArrayList<>();
		}
		if (STATUS_APPROVING.equals(status) || STATUS_APPROVED.equals(status)) {
			return new ArrayList<>();
		}
		return splitCsv(status.replace("\r", ",").replace("\n", ","));
	}

	private String normalizeCompareKey(String value) {
		if (value == null) {
			return "";
		}
		return value.replaceAll("\\s+", "").toLowerCase();
	}

}

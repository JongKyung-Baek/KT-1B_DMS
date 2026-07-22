package kr.esob.fdms.controller.inside.distribution.docrequest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.controller.inside.distribution.approvaldetail.DistributionApprovalDetailDao;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.doc.request.DocInfoVO;
import kr.esob.fdms.util.RandomStringGenerator;
import kr.esob.fdms.util.StringUtil;

@Service
public class DocRequestService implements CommonService {
	private static final String IOC_OBJECT_TYPE = "IOC";

	@Inject
	DocRequestDao dao;
	@Inject
	DistributionApprovalDetailDao approvalDetailDao;
	@Inject
	DocsMailService mailService;

	@Override
	public List selectList(Object param) {
		List<DocRequestVO> rtnList = dao.selectList(param);
//		for(int i=0; i<rtnList.size(); i++){
//			String filePath = "";
//			// 파일 PATH 구분자가 다른경우 통일을 위한 작업
//			if( !(null==rtnList.get(i).getFilePath()) && !("".equals(rtnList.get(i).getFilePath()))) {
//				if(rtnList.get(i).getFilePath().contains("/")) {
//					rtnList.get(i).setFilePath(rtnList.get(i).getFilePath().replaceAll("/", "\\\\"));
//				}
//				if("".equals(filePath)) {
//					filePath = "" + rtnList.get(i).getFilePath().substring(0,rtnList.get(i).getFilePath().lastIndexOf("\\")+1) + rtnList.get(i).getOrgFileNm();
//				}else {
//					filePath += "" + rtnList.get(i).getFilePath().substring(0,rtnList.get(i).getFilePath().lastIndexOf("\\")+1) + rtnList.get(i).getOrgFileNm();
//				}
//				rtnList.get(i).setFilePath(filePath.replaceAll("\\\\", "\\\\\\\\\\\\\\\\"));
//			}
//		}
		return rtnList;
	}

	public List<DocRequestTreeVO> selectTree(Object param) {
		return dao.selectTree(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public void deleteList(Object param) {
	}

	public void setSearchAllParam(DocRequestParam param) {
		if (!"".equals(param.getSearchAllParam()) && param.getSearchAllParam() != null) {
			Gson gson = new Gson();
			param.setDocumentList(
					gson.fromJson(param.getSearchAllParam().replace("&quot;", "'"), new TypeToken<List<DocInfoVO>>() {
					}.getType()));
		}
	}

	// 2020.07.24 기범추가( 등록 )
	public ResultVO saveDocRegisterFileX2(MultipartHttpServletRequest request) throws Exception {
		ResultVO resultVo = new ResultVO();
		System.out.println("[IOC_COVER] enter saveDocRegisterFileX2");
		System.out.println("들어오는지 체크 = " + request.getParameterMap());
		System.out.println("들어오는지 체크 = " + request.getParameter("fileName"));
		System.out.println("objectType -> " + request.getParameter("objectType"));
		MultipartFile file = request.getFile("file");
		List<MultipartFile> subFiles = request.getFiles("subFiles");

		System.out.println("protectYn 잘 들어오나 " + request.getParameter("protectYn"));

		String objectId = RandomStringGenerator.generateRandomString(32);
		String treeCd = request.getParameter("treeCd");
		treeCd = (treeCd == null) ? "" : treeCd.trim();
		String docNoParam = request.getParameter("docNo");
		String fileTypeParam = request.getParameter("fileType");

		String revNo = resolveRevisionNo(request.getParameter("versionNo"), "0");
		String filePathNm = SystemConfig.getSystemConfigValue("DOCUMENT_PATH") + "\\" + objectId + "."
				+ FilenameUtils.getExtension(request.getParameter("orgFileNm"));

		// DocRegisterPopupParam 객체 만들어서 저장
		DocRegisterPopupParam docRegisterPopupParam = DocRegisterPopupParam.builder()
				.fileSize(String.valueOf(Objects.requireNonNull(file).getSize()))
				.filePath(filePathNm)
				.orgFileNm(request.getParameter("orgFileNm"))
				.businessTypeCd(request.getParameter("businessTypeCd"))
				.distributeTypeCd(request.getParameter("distributeTypeCd"))
				.fileType((fileTypeParam != null && !"".equals(fileTypeParam.trim())) ? fileTypeParam : "")
				.objectId(objectId)
				.docNo((docNoParam != null && !"".equals(docNoParam.trim())) ? docNoParam : "DOC_" + objectId.substring(0, 8))
				.fileNm(request.getParameter("fileName"))
				.revNo(revNo)
				.protectYn(request.getParameter("protectYn"))
				.docClassCd1(request.getParameter("docClassCd1"))
				.approvalDate(request.getParameter("approvalDate"))
				.approver(request.getParameter("approver"))
				.coPublisher(request.getParameter("coPublisher"))
				.reviewerUser(request.getParameter("reviewerUser"))
				.treeCd(treeCd)
				.build();

		String existingFileName = dao.getDocRegisterByOrgFileNm(objectId);
		if (existingFileName != null) {
			resultVo.setSuccess(false);
			resultVo.setMessage("이미 존재하는 파일입니다.");
			return resultVo;
		}

		dao.insertDocRegisterInfo(docRegisterPopupParam);
		dao.insertDocReigsterInfoFile(docRegisterPopupParam); /* 한번 더 해서 DOCUMENT_FILE에 날리면 됨 */

		// 이 아래 3줄이 있어서 등록 성공이 뜨는거였음. 왜냐 ? getFileSavedPath로 가기 때문
		docRegisterPopupParam = getFileSavedPath(docRegisterPopupParam, file);
		saveDocSubFiles(subFiles, docRegisterPopupParam.getObjectId());
		convertIocMainFileAtRegister(docRegisterPopupParam);
		saveDistributionApprovalDetails(docRegisterPopupParam.getObjectId(), IOC_OBJECT_TYPE, safeString(request.getParameter("approver")));
		Set<String> mailTargets = new LinkedHashSet<>();
		mailTargets.addAll(splitCsv(safeString(request.getParameter("approver"))));
		mailTargets.addAll(splitCsv(safeString(request.getParameter("reviewerUser"))));
		sendRegistrationMail(
				String.join(",", mailTargets),
				DocsMailEnum.DISTRIBUTION_DOC_STATUS,
				safeString(docRegisterPopupParam.getDocNo()),
				safeString(docRegisterPopupParam.getFileNm()),
				safeString(request.getParameter("registerUser")),
				LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
		);
		resultVo.setSuccess(true);
		return resultVo;
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
			System.out.println("[DOC_REGISTER_MAIL] targetUsersCsv=" + targetUsersCsv);
			for (String token : splitCsv(targetUsersCsv)) {
				System.out.println("[DOC_REGISTER_MAIL] token=" + token);
				MailInfoVO mailInfoVo = mailService.selectReceiveUser(token);
				if (mailInfoVo == null) {
					System.out.println("[DOC_REGISTER_MAIL] selectReceiveUser is null. token=" + token);
					continue;
				}
				System.out.println("[DOC_REGISTER_MAIL] toUserId=" + mailInfoVo.getToUserId() + ", toMail=" + mailInfoVo.getToMail());
				mailInfoVo.setMailEnum(mailEnum);
				mailInfoVo.setContent(buildRegistrationMailContent(
						mailInfoVo,
						"IOC",
						documentNo,
						documentName,
						registrant,
						registeredAt
				));
				ResultVO mailResult = mailService.sendDocsMail(mailInfoVo);
				System.out.println("[DOC_REGISTER_MAIL] sendDocsMail success=" + (mailResult != null && mailResult.isSuccess()));
			}
		} catch (Exception e) {
			System.out.println("[DOC_REGISTER_MAIL] skip: " + e.getMessage());
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

	private DocRegisterPopupParam getFileSavedPath(DocRegisterPopupParam param, MultipartFile mf) {
		String filePathNm = param.getFilePath();
		param.setFilePath(filePathNm);
		String orgName = mf.getOriginalFilename();

		if (orgName.contains(File.separator)) {
			orgName = orgName.substring(orgName.lastIndexOf(File.separator) + 1, orgName.length());
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
			byte[] bytes = mf.getBytes();
			Files.write(Paths.get(path), bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return param;
	}

	public int deleteDoc(String objectId) {
		return dao.deleteDoc(objectId);
	}

	public ResultVO validateDeleteDoc(String objectId, UserVO userVo) {
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

		Map<String, Object> info = dao.selectDocApprovalInfo(objectId.trim());
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

	public ResultVO approveDocument(String objectId, UserVO userVo) {
		ResultVO result = new ResultVO();
		Map<String, Object> info = dao.selectDocApprovalInfo(objectId);

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
			result.setMessage("승인 대상자가 지정되지 않은 문서입니다.");
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
		java.util.Set<String> approvedSet = new java.util.LinkedHashSet<>(parseApprovedUsers(approvalSource));
		if (approvedSet.contains(approverToken)) {
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

		int updated = dao.approveDocument(param);
		if (updated > 0) {
			updateDistributionApprovalDetail(objectId, IOC_OBJECT_TYPE, userCd, userNm);
			boolean detailFinalApprove = isDistributionApprovalDetailCompleted(objectId, IOC_OBJECT_TYPE);
			System.out.println("[IOC_APPROVAL] objectId=" + objectId + ", stringFinal=" + isFinalApprove + ", detailFinal=" + detailFinalApprove);
			if (isFinalApprove || detailFinalApprove) {
				mergeIocCoverAfterApproval(objectId);
			}
		}
		result.setSuccess(updated > 0);
		result.setMessage(updated > 0 ? "승인되었습니다." : "승인 처리에 실패했습니다.");
		return result;
	}

	public ResultVO validateApproveDocument(String objectId, UserVO userVo) {
		ResultVO result = new ResultVO();

		if (objectId == null || objectId.trim().isEmpty()) {
			result.setSuccess(false);
			result.setMessage("승인 대상을 확인할 수 없습니다.");
			return result;
		}

		Map<String, Object> info = dao.selectDocApprovalInfo(objectId.trim());
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
			result.setMessage("승인 대상자가 지정되지 않은 문서입니다.");
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
		java.util.Set<String> approvedSet = new java.util.LinkedHashSet<>(parseApprovedUsers(approvalSource));
		if (approvedSet.contains(approverToken)) {
			result.setSuccess(false);
			result.setMessage("이미 승인한 건입니다.");
			return result;
		}

		result.setSuccess(true);
		result.setMessage("OK");
		return result;
	}

	public String getApprovalStatusMessage(String objectId) {
		Map<String, Object> info = dao.selectDocApprovalInfo(objectId);
		if (info == null || info.isEmpty()) {
			return "\uC0C1\uD0DC \uC815\uBCF4\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4.";
		}
		String status = getInfoString(info, "status", "STATUS");
		boolean forceApproved = STATUS_APPROVED.equals(status);
		List<String> requiredUsers = getRequiredApprovalUsers(info);
		if (requiredUsers.isEmpty()) {
			return forceApproved ? STATUS_APPROVED : "승인자/배포자가 지정되지 않았습니다.";
		}
		String approvedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = approvedUsers.isEmpty() ? status : approvedUsers;
		java.util.Set<String> approvedSet = new java.util.LinkedHashSet<>(parseApprovedUsers(approvalSource));
		if (forceApproved) { approvedSet.addAll(requiredUsers); }
		Map<String, String> commentMap = getApprovalCommentMap(objectId);
		java.util.List<String> rows = new java.util.ArrayList<>();
		for (String approver : requiredUsers) {
			String key = splitApprovedUser(approver);
			boolean isApproved = approvedSet.contains(key);
			String comment = safeString(commentMap.get(key));
			rows.add(approver + " : " + (isApproved ? "승인인" : "대기") + " : " + (comment.isEmpty() ? "-" : comment));
		}
		return String.join("\n", rows);
	}

	public List<Map<String, Object>> getApprovalStatusRows(String objectId, UserVO userVo) {
		List<Map<String, Object>> rows = new java.util.ArrayList<>();
		Map<String, Object> info = dao.selectDocApprovalInfo(objectId);
		if (info == null || info.isEmpty()) { return rows; }
		String status = getInfoString(info, "status", "STATUS");
		boolean forceApproved = STATUS_APPROVED.equals(status);
		List<String> approverUsers = getApproverUsers(info);
		List<String> requiredUsers = getRequiredApprovalUsers(info);
		String approvedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = approvedUsers.isEmpty() ? status : approvedUsers;
		java.util.Set<String> approvedSet = new java.util.LinkedHashSet<>(parseApprovedUsers(approvalSource));
		if (forceApproved) { approvedSet.addAll(requiredUsers); }
		Map<String, String> commentMap = getApprovalCommentMap(objectId);
		String userCd = safeString(userVo.getUserCd());
		String userNm = safeString(userVo.getUserNm());
		for (String user : approverUsers) {
			String normalized = splitApprovedUser(user);
			boolean approved = approvedSet.contains(normalized);
			boolean editable = normalized.equals(userCd) || normalized.equals(userNm);
			Map<String, Object> row = new HashMap<>();
			row.put("approver", user); row.put("status", approved ? "\uC2B9\uC778" : "\uB300\uAE30");
			row.put("comment", safeString(commentMap.get(normalized))); row.put("editable", editable ? "Y" : "N");
			row.put("approvalType", "APPROVER"); rows.add(row);
		}
		return rows;
	}

	public ResultVO saveApprovalComment(String objectId, String comment, UserVO userVo) {
		ResultVO result = new ResultVO();
		Map<String, Object> info = dao.selectDocApprovalInfo(objectId);
		if (info == null || info.isEmpty()) { result.setSuccess(false); result.setMessage("\uC2B9\uC778 \uB300\uC0C1 \uBB38\uC11C\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4."); return result; }
		List<String> requiredUsers = getRequiredApprovalUsers(info);
		String userCd = safeString(userVo.getUserCd()); String userNm = safeString(userVo.getUserNm());
		String actor = requiredUsers.contains(userCd) ? userCd : (requiredUsers.contains(userNm) ? userNm : "");
		if (actor.isEmpty()) { result.setSuccess(false); result.setMessage("\uBCF8\uC778 \uD589\uC5D0\uB9CC \uCF54\uBA58\uD2B8\uB97C \uC785\uB825\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4."); return result; }
		String currentStatus = getInfoString(info, "status", "STATUS");
		String currentApprovedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users");
		String approvalSource = currentApprovedUsers.isEmpty() ? currentStatus : currentApprovedUsers;
		java.util.Set<String> approvedSet = new java.util.LinkedHashSet<>(parseApprovedUsers(approvalSource));
		if (!approvedSet.contains(splitApprovedUser(actor))) { result.setSuccess(false); result.setMessage("\uC2B9\uC778 \uD6C4 \uCF54\uBA58\uD2B8 \uC785\uB825\uC774 \uAC00\uB2A5\uD569\uB2C8\uB2E4."); return result; }
		String normalizedComment = safeString(comment);
		if (normalizedComment.isEmpty()) { normalizedComment = "\uC2B9\uC778\uD558\uC600\uC2B5\uB2C8\uB2E4"; }
		Map<String, Object> param = new HashMap<>(); param.put("objectId", objectId); param.put("approverId", splitApprovedUser(actor)); param.put("comment", normalizedComment);
		int updated = dao.upsertApprovalComment(param); result.setSuccess(updated > 0);
		result.setMessage(updated > 0 ? "\uCF54\uBA58\uD2B8\uAC00 \uC800\uC7A5\uB418\uC5C8\uC2B5\uB2C8\uB2E4." : "\uCF54\uBA58\uD2B8 \uC800\uC7A5\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4.");
		return result;
	}

	private String splitApprovedUser(String entry) {
		String normalized = safeString(entry);
		if (normalized.isEmpty()) { return ""; }
		int idx = normalized.indexOf('|');
		return idx >= 0 ? normalized.substring(0, idx).trim() : normalized;
	}

	private void saveDistributionApprovalDetails(String objectId, String objectType, String approverRaw) {
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
		List<String> dates = new java.util.ArrayList<>();
		List<Map<String, Object>> roleDetails = new java.util.ArrayList<>();
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

	private List<String> getRequiredApprovalUsers(Map<String, Object> info) {
		return getApproverUsers(info);
	}

	private List<String> getApproverUsers(Map<String, Object> info) {
		return splitCsv(getInfoString(info, "approver", "APPROVER"));
	}

	private List<String> getReviewerUsers(Map<String, Object> info) {
		return splitCsv(getInfoString(info, "reviewerUser", "reviewer_user", "REVIEWER_USER", "REVIEWERUSER"));
	}

	private Map<String, String> getApprovalCommentMap(String objectId) {
		Map<String, String> map = new HashMap<>();
		if (objectId == null || objectId.trim().isEmpty()) { return map; }
		List<Map<String, Object>> commentRows = dao.selectApprovalComments(objectId.trim());
		if (commentRows == null) { return map; }
		for (Map<String, Object> row : commentRows) {
			String approverId = splitApprovedUser(getInfoString(row, "approverId", "APPROVER_ID", "approver", "APPROVER"));
			if (!approverId.isEmpty()) { map.put(approverId, safeString(getInfoString(row, "comment", "COMMENT", "COMMENT_TXT"))); }
		}
		return map;
	}

	public int selectNextDocRegisterNo(String treeCd, String functionCode2No) {
		Map<String, Object> param = new HashMap<>();
		param.put("treeCd", treeCd == null ? "" : treeCd.trim());
		param.put("functionCode2No", functionCode2No == null ? "" : functionCode2No.trim());
		Integer nextNo = dao.selectNextDocRegisterNo(param);
		return nextNo == null ? 1 : nextNo;
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

	public Map<String, Object> getDocFileDownloadInfo(String objectId, String fileNo) {
		if (objectId == null || objectId.trim().isEmpty()) {
			return null;
		}
		Map<String, Object> param = new HashMap<>();
		param.put("objectId", objectId.trim());
		param.put("fileNo", fileNo == null ? "" : fileNo.trim());
		return dao.selectDocFileDownloadInfo(param);
	}

	private void convertIocMainFileAtRegister(DocRegisterPopupParam param) {
		String objectId = safeString(param.getObjectId());
		String sourceFilePath = safeString(param.getFilePath());
		if (objectId.isEmpty() || sourceFilePath.isEmpty()) return;
		String inputPdfPath = ensureConvertedPdfForCover(sourceFilePath, objectId, "IOC_CONVERT");
		if (inputPdfPath.isEmpty() || !inputPdfPath.toLowerCase().endsWith(".pdf")) {
			markMainFileProcessingFail(objectId, "convert failed: pdf output not found", "IOC_CONVERT");
			return;
		}
		File convertedMain = new File(inputPdfPath);
		if (!convertedMain.isFile()) {
			markMainFileProcessingFail(objectId, "convert failed: pdf file not found", "IOC_CONVERT");
			return;
		}
		Map<String, Object> convertedUpdateParam = new HashMap<>();
		convertedUpdateParam.put("objectId", objectId);
		convertedUpdateParam.put("filePath", inputPdfPath);
		convertedUpdateParam.put("orgFileNm", toPdfFileName(safeString(param.getOrgFileNm()), safeString(param.getFileNm())));
		convertedUpdateParam.put("fileSize", String.valueOf(convertedMain.length()));
		int updated = dao.updateMainFileAfterCoverMerge(convertedUpdateParam);
		System.out.println("[IOC_CONVERT] register main update rows=" + updated + ", filePath=" + inputPdfPath);
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

	private void mergeIocCoverAfterApproval(String objectId) {
		Map<String, Object> info = dao.selectDocApprovalInfo(objectId);
		List<Map<String, Object>> mainFiles = dao.selectMainFileInfo(objectId);
		if (info == null || mainFiles == null || mainFiles.isEmpty()) return;
		Map<String, Object> main = mainFiles.get(0);
		DocRegisterPopupParam param = new DocRegisterPopupParam();
		param.setObjectId(objectId);
		param.setFilePath(getInfoString(main, "filePath", "FILE_PATH_NM"));
		param.setOrgFileNm(getInfoString(main, "orgFileNm", "ORG_FILE_NM"));
		param.setFileNm(getInfoString(main, "orgFileNm", "ORG_FILE_NM"));
		param.setFileSize(getInfoString(main, "fileSize", "FILE_SIZE"));
		param.setDocNo(getInfoString(main, "documentNo", "DOCUMENT_NO"));
		param.setRevNo(getInfoString(info, "revNo", "REV_NO"));
		param.setApprovalDate(getInfoString(info, "approvalDate", "APPROVALDATE", "insertDt", "INSERT_DT"));
		param.setCoPublisher(getInfoString(info, "coPublisher", "COPUBLISHER"));
		param.setReviewerUser(getInfoString(info, "reviewerUser", "REVIEWERUSER"));
		param.setApprover(getInfoString(info, "approver", "APPROVER"));
		mergeIocCoverAtRegister(null, param, null);
	}

	private void mergeIocCoverAtRegister(MultipartHttpServletRequest request, DocRegisterPopupParam param, List<MultipartFile> subFiles) {
		try {
			String sourceFilePath = safeString(param.getFilePath());
			String objectId = safeString(param.getObjectId());
			String inputPdfPath = ensureConvertedPdfForCover(sourceFilePath, objectId, "IOC_COVER");
			if (inputPdfPath.isEmpty()) {
				System.out.println("[IOC_COVER] skip: inputPdfPath is empty");
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
					System.out.println("[IOC_COVER] pre-merge main update rows=" + updated + ", filePath=" + inputPdfPath);
				}
			}
			String outputFileName = "cover_" + objectId + ".pdf";
			Map<String, Object> docInfo = dao.selectDocApprovalInfo(objectId);
			String fromUserName = getInfoString(docInfo, "registerUser", "REGISTER_USER", "insertUserNm", "INSERT_USER_NM", "INSERTUSERNM");
			if (safeString(fromUserName).isEmpty() && param.getSessionUser() != null) {
				fromUserName = safeString(param.getSessionUser().getUserNm());
				if (fromUserName.isEmpty()) {
					fromUserName = safeString(param.getSessionUser().getUsername());
				}
			}

			List<String> preparedBy = splitCsv(request == null ? safeString(param.getCoPublisher()) : safeString(request.getParameter("coPublisher")));
			List<String> reviewerUsers = splitCsv(request == null ? safeString(param.getReviewerUser()) : safeString(request.getParameter("reviewerUser")));
			List<String> approverUsers = splitCsv(request == null ? safeString(param.getApprover()) : safeString(request.getParameter("approver")));
			List<String> subFileNames = request == null ? extractSubFileNamesFromDb(objectId) : extractSubFileNames(subFiles);
			List<String> fromUserList = new java.util.ArrayList<>();
			if (!safeString(fromUserName).isEmpty()) {
				fromUserList.add(fromUserName);
			}
			Map<String, String> positionMap = getUserPositionMapByNames(fromUserList, preparedBy, reviewerUsers, approverUsers);
			List<String> preparedPositions = buildPositionsByNames(preparedBy, positionMap);
			List<String> reviewerPositions = buildPositionsByNames(reviewerUsers, positionMap);
			List<String> approverPositions = buildPositionsByNames(approverUsers, positionMap);
			List<String> fromPositions = buildPositionsByNames(fromUserList, positionMap);

			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("inputPdfPath", inputPdfPath);
			requestBody.put("outputFileName", outputFileName);
			requestBody.put("cover_doc_type", "IOC");
			requestBody.put("cover_request_no", "-");
			requestBody.put("cover_object_id", objectId);
			requestBody.put("cover_user_id", "-");
			requestBody.put("cover_user_name", safeString(param.getCoPublisher()));
			requestBody.put("cover_viewed_at", safeString(param.getApprovalDate()));
			requestBody.put("cover_title", safeString(param.getFileNm()));
			requestBody.put("cover_note", "");
			requestBody.put("cover_document_no", safeString(param.getDocNo()));
			requestBody.put("cover_issue", safeString(param.getRevNo()));
			requestBody.put("revision", safeString(param.getRevNo()));
			requestBody.put("fileName", safeString(param.getFileNm()));
			requestBody.put("cover_from_user", safeString(fromUserName));
			requestBody.put("cover_from_position", fromPositions.isEmpty() ? "" : safeString(fromPositions.get(0)));
			requestBody.put("cover_prepared_by", preparedBy);
			requestBody.put("cover_prepared_positions", preparedPositions);
			requestBody.put("cover_reviewer_users", reviewerUsers);
			requestBody.put("cover_reviewer_positions", reviewerPositions);
			requestBody.put("cover_approver_users", approverUsers);
			requestBody.put("cover_approver_positions", approverPositions);
			List<Map<String, Object>> approvalDetails = selectDistributionApprovalDetails(objectId, IOC_OBJECT_TYPE);
			requestBody.put("cover_reviewer_dates", buildApprovalDatesByNames(reviewerUsers, "REVIEWER", approvalDetails));
			requestBody.put("cover_approver_dates", buildApprovalDatesByNames(approverUsers, "APPROVER", approvalDetails));
			requestBody.put("cover_sub_file_names", subFileNames);

			String templatePdfPath = safeString(SystemConfig.getSystemConfigValue("IOC_COVER_TMPL_PATH"));
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
			System.out.println("[IOC_COVER] endpoint=" + endpoint);
			System.out.println("[IOC_COVER] templatePdfPath=" + templatePdfPath);
			System.out.println("[IOC_COVER] inputPdfPath=" + inputPdfPath + ", outputFileName=" + outputFileName);
			System.out.println("[IOC_COVER] cover_from_user=" + safeString(fromUserName) + ", cover_from_position=" + (fromPositions.isEmpty() ? "" : safeString(fromPositions.get(0))));

			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
			String responseBody = response.getBody();
			System.out.println("[IOC_COVER] responseStatus=" + response.getStatusCodeValue() + ", body=" + responseBody);

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
						copyMergedPdfToViewerCache(outputPdfPath, objectId, "IOC_COVER");
						System.out.println("[IOC_COVER] post-merge main update rows=" + updated + ", filePath=" + inputPdfPath);
						System.out.println("[IOC_COVER] overwrite source file with merged cover: " + inputPdfPath);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("[IOC_COVER] exception: " + e.getMessage());
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
				System.out.println("[IOC_COVER] fail status update exception: " + ignore.getMessage());
			}
		}
	}

	private List<String> extractSubFileNames(List<MultipartFile> subFiles) {
		List<String> names = new java.util.ArrayList<>();
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

	private List<String> extractSubFileNamesFromDb(String objectId) {
		List<String> names = new java.util.ArrayList<>();
		for (Map<String, Object> subFile : dao.selectSubFileInfo(objectId)) {
			String name = getInfoString(subFile, "orgFileNm", "ORG_FILE_NM");
			if (!name.isEmpty()) {
				names.add(name);
			}
		}
		return names;
	}

	private Map<String, String> getUserPositionMapByNames(List<String>... userNameLists) {
		java.util.Set<String> allNames = new java.util.LinkedHashSet<>();
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
		queryParam.put("names", new java.util.ArrayList<>(allNames));
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
		List<String> positions = new java.util.ArrayList<>();
		if (names == null) {
			return positions;
		}
		for (String name : names) {
			String normalizedName = safeString(name);
			if (normalizedName.isEmpty()) {
				positions.add("");
				continue;
			}
			positions.add(safeString(positionMap.get(normalizedName)));
		}
		return positions;
	}

	private void saveDocSubFiles(List<MultipartFile> subFiles, String objectId) {
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
			String targetPath = buildDocumentSavedPath(subObjectId, extension);
			targetPath = StringUtil.replaceLfiPath(targetPath);

			File outFile = new File(targetPath);
			try {
				outFile.createNewFile();
				Files.write(Paths.get(targetPath), subFile.getBytes());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			String convertedSubPdfPath = ensureConvertedPdfForCover(targetPath, subObjectId, "IOC_SUB_COVER");
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

			DocSubFileParam subFileParam = DocSubFileParam.builder()
					.objectId(subObjectId)
					.parentObjectId(objectId)
					.orgFileNm(savedOrgFileNm)
					.filePath(savedPath)
					.fileSize(String.valueOf(savedSize))
					.useYn("Y")
					.build();

			dao.insertDocSubFile(subFileParam);
		}
	}

	private String buildDocumentSavedPath(String objectId, String extension) {
		String basePath = SystemConfig.getSystemConfigValue("DOCUMENT_PATH");
		if (extension == null || extension.trim().isEmpty()) {
			return basePath + "\\" + objectId;
		}
		return basePath + "\\" + objectId + "." + extension;
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

	private static final String STATUS_APPROVING = "승인진행중";
	private static final String STATUS_APPROVED = "승인완료";

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
		java.util.Set<String> approvedSet = new java.util.LinkedHashSet<>(parseApprovedUsers(approvalSource));
		return approvedSet.containsAll(requiredUsers);
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

	private String normalizeCompareKey(String value) {
		if (value == null) {
			return "";
		}
		return value.replaceAll("\\s+", "").toLowerCase();
	}

	private List<String> splitCsv(String value) {
		List<String> list = new java.util.ArrayList<>();
		if (value == null || value.trim().isEmpty()) {
			return list;
		}
		String[] tokens = value.split(",");
		for (String token : tokens) {
			String t = token == null ? "" : token.trim();
			if (!t.isEmpty()) {
				list.add(t);
			}
		}
		return list;
	}

	private List<String> parseApprovedUsers(String status) {
		if (status == null || status.trim().isEmpty()) {
			return new java.util.ArrayList<>();
		}
		if (STATUS_APPROVING.equals(status) || STATUS_APPROVED.equals(status)) {
			return new java.util.ArrayList<>();
		}
		return splitCsv(status.replace("\r", ",").replace("\n", ","));
	}
}

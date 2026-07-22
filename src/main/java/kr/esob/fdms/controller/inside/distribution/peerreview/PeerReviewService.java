package kr.esob.fdms.controller.inside.distribution.peerreview;

import java.io.File;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.util.RandomStringGenerator;
import kr.esob.fdms.util.StringUtil;
import kr.esob.fdms.controller.login.UserVO;

@Service
public class PeerReviewService implements CommonService {
    private static final String STATUS_APPROVING = "승인진행중";
    private static final String STATUS_APPROVED = "승인완료";

    @Inject
    PeerReviewDao dao;
    @Inject
    DocsMailService mailService;

    @SuppressWarnings("rawtypes")
    @Override
    public List selectList(Object obj) {
        return dao.selectList(obj);
    }

    @Override
    public int selectListCount(Object obj) {
        return dao.selectListCount(obj);
    }

    public String getNextPeerReviewNo() {
        String nextNo = dao.selectNextPeerReviewNo();
        return (nextNo == null || nextNo.trim().isEmpty()) ? "K8-PeerReview-001" : nextNo;
    }

    public ResultVO savePeerReview(PeerReviewSaveParam param) {
        ResultVO result = new ResultVO();

        String no = param.getPeerreviewNo() == null ? "" : param.getPeerreviewNo().trim();
        String nm = param.getPeerreviewNm() == null ? "" : param.getPeerreviewNm().trim();
        if (no.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("PeerReview 번호를 입력해 주세요.");
            return result;
        }
        if (nm.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("PeerReview 제목을 입력해 주세요.");
            return result;
        }

        Integer nextCnSerial = dao.selectNextCnSerial();
        param.setCnSerial(nextCnSerial == null ? "1" : String.valueOf(nextCnSerial));
        param.setObjectId(RandomStringGenerator.generateRandomString(32));
        param.setStatus(param.getStatus() == null || param.getStatus().trim().isEmpty() ? "승인진행중" : param.getStatus().trim());
        param.setApprover(param.getApprover() == null ? "" : param.getApprover().trim());
        param.setRevieweruser(param.getRevieweruser() == null ? "" : param.getRevieweruser().trim());
        param.setFileNm(nm);
        dao.insertPeerReview(param);
        Set<String> mailTargets = new LinkedHashSet<>();
        mailTargets.addAll(splitCsv(param.getApprover()));
        mailTargets.addAll(splitCsv(param.getRevieweruser()));
        sendRegistrationMail(
                String.join(",", mailTargets),
                DocsMailEnum.DISTRIBUTION_DOC_STATUS,
                safeString(param.getPeerreviewNo()),
                safeString(param.getOrgFileNm()),
                "-",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                safeString(param.getReviewdate())
        );

        result.setSuccess(true);
        result.setMessage("등록되었습니다.");
        return result;
    }

    public ResultVO savePeerReviewRegisterFile(MultipartHttpServletRequest request) throws Exception {
        ResultVO result = new ResultVO();
        MultipartFile file = request.getFile("file");
        if (file == null || file.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("주파일을 선택해 주세요.");
            return result;
        }

        String peerreviewNo = trim(request.getParameter("peerreviewNo"));
        String peerreviewNm = trim(request.getParameter("peerreviewNm"));
        String reviewdate = trim(request.getParameter("reviewdate"));
        String revNo = trim(request.getParameter("revNo"));
        if (peerreviewNo.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("PeerReview 번호를 입력해 주세요.");
            return result;
        }
        if (peerreviewNm.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("PeerReview 제목을 입력해 주세요.");
            return result;
        }

        String objectId = RandomStringGenerator.generateRandomString(32);
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        String basePath = resolvePeerReviewBasePath();
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePathNm = basePath + "\\" + objectId + (extension == null || extension.isEmpty() ? "" : "." + extension);
        filePathNm = StringUtil.replaceLfiPath(filePathNm);
        Files.write(Paths.get(filePathNm), file.getBytes());

        String storedPathNm = filePathNm;
        String storedOrgFileNm = trim(file.getOriginalFilename());
        if (storedOrgFileNm.isEmpty()) {
            storedOrgFileNm = toPdfFileName(peerreviewNm);
        }

        PeerReviewSaveParam param = new PeerReviewSaveParam();
        Integer nextCnSerial = dao.selectNextCnSerial();
        param.setCnSerial(nextCnSerial == null ? "1" : String.valueOf(nextCnSerial));
        param.setObjectId(objectId);
        param.setPeerreviewNo(peerreviewNo);
        param.setPeerreviewNm(peerreviewNm);
        param.setReviewdate(reviewdate);
        param.setRevNo(revNo.isEmpty() ? "00" : revNo);
        param.setApprover(trim(request.getParameter("approver")));
        param.setRevieweruser(trim(request.getParameter("revieweruser")));
        param.setStatus("승인진행중");
        param.setFileNm(peerreviewNm);
        param.setOrgFileNm(storedOrgFileNm);
        param.setFilePathNm(storedPathNm);
        param.setFileSize(String.valueOf(file.getSize()));
        param.setCurrentPageNo("1");
        param.setTotalPageNo("1");
        dao.insertPeerReview(param);

        convertPeerReviewMainFileAtRegister(
                objectId,
                filePathNm,
                basePath,
                extension == null ? "" : extension
        );

        Set<String> mailTargets = new LinkedHashSet<>();
        mailTargets.addAll(splitCsv(param.getApprover()));
        mailTargets.addAll(splitCsv(param.getRevieweruser()));
        sendRegistrationMail(
                String.join(",", mailTargets),
                DocsMailEnum.DISTRIBUTION_DOC_STATUS,
                safeString(param.getPeerreviewNo()),
                safeString(param.getOrgFileNm()),
                trim(request.getParameter("registerUser")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                safeString(reviewdate)
        );

        result.setSuccess(true);
        result.setMessage("등록되었습니다.");
        return result;
    }

    private String resolvePeerReviewBasePath() {
        String path = SystemConfig.getSystemConfigValue("PEERREVIEW_PATH");
        if (path == null || path.trim().isEmpty()) {
            path = SystemConfig.getSystemConfigValue("2D_FILE_PATH");
        }
        if (path == null || path.trim().isEmpty()) {
            path = SystemConfig.getSystemConfigValue("DOCUMENT_PATH");
        }
        if (path == null || path.trim().isEmpty()) {
            path = System.getProperty("java.io.tmpdir");
        }
        return StringUtil.replaceLfiPath(path);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String requestPathConvertApiAndGetPdf(String inputFilePath, String outputDir, String objectId) {
        HttpURLConnection connection = null;
        DataOutputStream requestStream = null;
        try {
            String endpoint = SystemConfig.getSystemConfigValue("CONVERT_SERVER_URL");
            if (endpoint == null || endpoint.trim().isEmpty()) {
                endpoint = "http://localhost:9001/api/internal/convert";
            }

            File inputFile = new File(inputFilePath);
            if (!inputFile.isFile()) {
                return null;
            }

            String boundary = "----FDMS-PR-" + System.currentTimeMillis();
            URL url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(60000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            requestStream = new DataOutputStream(connection.getOutputStream());
            writeFormField(requestStream, boundary, "outputDir", outputDir);

            String fileName = inputFile.getName();
            int dot = fileName.lastIndexOf('.');
            String ext = dot > -1 ? fileName.substring(dot) : "";
            String uploadName = objectId + ext;
            writeFilePart(requestStream, boundary, "files", uploadName, inputFile);

            requestStream.writeBytes("--" + boundary + "--\r\n");
            requestStream.flush();

            int status = connection.getResponseCode();
            if (status >= 200 && status < 300) {
                return findLatestConvertedPdf(outputDir, objectId);
            }
        } catch (Exception ignored) {
        } finally {
            try {
                if (requestStream != null) requestStream.close();
            } catch (Exception ignored) {}
            if (connection != null) connection.disconnect();
        }
        return null;
    }

    private String findLatestConvertedPdf(String outputDir, String objectId) {
        try {
            File outDir = new File(outputDir);
            File[] candidates = outDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    String lower = name.toLowerCase(Locale.ROOT);
                    return lower.startsWith(objectId.toLowerCase(Locale.ROOT) + "_") && lower.endsWith(".pdf");
                }
            });
            if (candidates == null || candidates.length == 0) {
                return null;
            }
            Arrays.sort(candidates, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return Long.compare(o2.lastModified(), o1.lastModified());
                }
            });
            return candidates[0].getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private String toPdfFileName(String originalName) {
        String base = originalName == null ? "" : originalName.trim();
        if (base.isEmpty()) {
            return "peerreview.pdf";
        }
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);
        }
        return base + ".pdf";
    }

    private void convertPeerReviewMainFileAtRegister(
            String objectId,
            String inputFilePath,
            String outputDir,
            String extension
    ) {
        try {
            String normalizedExt = extension == null ? "" : extension.trim();
            if ("pdf".equalsIgnoreCase(normalizedExt)) {
                updatePeerReviewProcessingStatus(objectId, "DONE");
                return;
            }
            String convertedPdfPath = requestPathConvertApiAndGetPdf(inputFilePath, outputDir, objectId);
            if (convertedPdfPath == null || convertedPdfPath.trim().isEmpty()) {
                updatePeerReviewProcessingStatus(objectId, "FAIL");
                System.out.println("[PEERREVIEW_CONVERT] failed: pdf output not found. objectId=" + objectId);
                return;
            }
            File convertedFile = new File(convertedPdfPath);
            if (!convertedFile.isFile()) {
                updatePeerReviewProcessingStatus(objectId, "FAIL");
                System.out.println("[PEERREVIEW_CONVERT] failed: pdf file not found. objectId=" + objectId + ", filePath=" + convertedPdfPath);
                return;
            }
            Map<String, Object> updateParam = new java.util.HashMap<>();
            updateParam.put("objectId", objectId);
            updateParam.put("filePathNm", StringUtil.replaceLfiPath(convertedPdfPath));
            updateParam.put("fileSize", String.valueOf(convertedFile.length()));
            dao.updateConvertedMainFile(updateParam);
            System.out.println("[PEERREVIEW_CONVERT] updated objectId=" + objectId + ", filePath=" + convertedPdfPath);
        } catch (Exception e) {
            updatePeerReviewProcessingStatus(objectId, "FAIL");
            System.out.println("[PEERREVIEW_CONVERT] failed: " + e.getMessage());
        }
    }

    private void updatePeerReviewProcessingStatus(String objectId, String processingStatus) {
        try {
            Map<String, Object> statusParam = new java.util.HashMap<>();
            statusParam.put("objectId", objectId);
            statusParam.put("processingStatus", processingStatus);
            dao.updateProcessingStatus(statusParam);
        } catch (Exception e) {
            System.out.println("[PEERREVIEW_CONVERT] status update failed: " + e.getMessage());
        }
    }

    private void writeFormField(DataOutputStream out, String boundary, String name, String value) throws Exception {
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        out.write(value.getBytes(StandardCharsets.UTF_8));
        out.writeBytes("\r\n");
    }

    private void writeFilePart(DataOutputStream out, String boundary, String partName, String fileName, File file) throws Exception {
        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"" + partName + "\"; filename=\"" + fileName + "\"\r\n");
        out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            if (fis != null) fis.close();
        }
        out.writeBytes("\r\n");
    }

    private void sendRegistrationMail(
            String targetUsersCsv,
            DocsMailEnum mailEnum,
            String documentNo,
            String documentName,
            String registrant,
            String registeredAt,
            String reviewdate
    ) {
        try {
            System.out.println("[PEERREVIEW_REGISTER_MAIL] targetUsersCsv=" + targetUsersCsv);
            for (String token : splitCsv(targetUsersCsv)) {
                System.out.println("[PEERREVIEW_REGISTER_MAIL] token=" + token);
                MailInfoVO mailInfoVo = mailService.selectReceiveUser(token);
                if (mailInfoVo == null) {
                    System.out.println("[PEERREVIEW_REGISTER_MAIL] selectReceiveUser is null. token=" + token);
                    continue;
                }
                System.out.println("[PEERREVIEW_REGISTER_MAIL] toUserId=" + mailInfoVo.getToUserId() + ", toMail=" + mailInfoVo.getToMail());
                mailInfoVo.setMailEnum(mailEnum);
                mailInfoVo.setContent(buildRegistrationMailContent(
                        mailInfoVo,
                        "PeerReview",
                        documentNo,
                        documentName,
                        registrant,
                        registeredAt,
                        reviewdate
                ));
                ResultVO mailResult = mailService.sendDocsMail(mailInfoVo);
                System.out.println("[PEERREVIEW_REGISTER_MAIL] sendDocsMail success=" + (mailResult != null && mailResult.isSuccess()));
            }
        } catch (Exception e) {
            System.out.println("[PEERREVIEW_REGISTER_MAIL] skip: " + e.getMessage());
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
            String registeredAt,
            String reviewdate
    ) {
        String html = loadRegistrationMailTemplate();
        if (html == null || html.trim().isEmpty()) return html;
        String role = safeString(mailInfoVo.getApprovalGrade());
        String content = html
                .replace("{메뉴명}", safeString(menuName))
                .replace("{메일제목}", "문서 등록 및 검토 요청 알림")
                .replace("{문서번호}", safeString(documentNo))
                .replace("{문서명}", safeString(documentName))
                .replace("{등록자}", safeString(registrant))
                .replace("{등록일시}", safeString(registeredAt))
                .replace("{요청업무}", "문서검토")
                .replace("{수신자역할}", role);
        return appendPeerReviewReviewDateMessage(content, reviewdate);
    }

    private String appendPeerReviewReviewDateMessage(String html, String reviewdate) {
        String normalizedReviewdate = normalizeMailDate(reviewdate);
        if (html == null || html.trim().isEmpty() || normalizedReviewdate.isEmpty()) {
            return html;
        }

        String reviewDateRow =
                "          <tr>\r\n" +
                "            <td style=\"padding:8px 38px 4px 38px; font-size:15px; color:#222222; line-height:1.7;\">\r\n" +
                "              " + escapeHtml(normalizedReviewdate) + "\uC77C \uC774\uB0B4\uB85C \uAC80\uD1A0 \uBD80\uD0C1\uB4DC\uB9BD\uB2C8\uB2E4.\r\n" +
                "            </td>\r\n" +
                "          </tr>";
        String buttonCellAnchor = "<td align=\"center\" style=\"padding:24px 38px 34px 38px;\">";
        int buttonCellIndex = html.indexOf(buttonCellAnchor);
        if (buttonCellIndex > -1) {
            int buttonRowIndex = html.lastIndexOf("<tr>", buttonCellIndex);
            if (buttonRowIndex > -1) {
                return html.substring(0, buttonRowIndex) + reviewDateRow + "\r\n\r\n" + html.substring(buttonRowIndex);
            }
        }
        return html.replace("</body>", reviewDateRow + "\r\n</body>");
    }

    private String normalizeMailDate(String date) {
        String value = safeString(date);
        if (value.length() >= 10) {
            return value.substring(0, 10);
        }
        return value;
    }

    private String escapeHtml(String value) {
        return safeString(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public int deletePeerReview(String objectId) {
        Map<String, String> param = new java.util.HashMap<>();
        param.put("objectId", objectId);
        return dao.deletePeerReview(param);
    }

    public ResultVO validateDeletePeerReview(String objectId, UserVO userVo) {
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

        Map<String, Object> info = dao.selectPeerReviewApprovalInfo(objectId.trim());
        if (info == null || info.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("대상 문서를 찾을 수 없습니다.");
            return result;
        }

        String deletedYn = getInfoString(info, "deletedYn", "DELETED_YN", "deleted_yn");
        if ("Y".equalsIgnoreCase(deletedYn)) {
            result.setSuccess(false);
            result.setMessage("이미 철회된 문서입니다.");
            return result;
        }

        String ownerUid = getInfoString(info, "insertUid", "INSERT_UID", "insert_uid");
        String ownerNm = getInfoString(info, "insertUserNm", "INSERT_USER_NM", "insert_user_nm");
        String loginId = trim(userVo.getUserId());
        String loginCd = trim(userVo.getUserCd());
        String loginNm = trim(userVo.getUserNm());

        boolean ownerMatched =
                (!ownerUid.isEmpty() && (ownerUid.equals(loginId) || ownerUid.equals(loginCd) || ownerUid.equals(loginNm)))
                        || (!ownerNm.isEmpty() && (ownerNm.equals(loginNm) || ownerNm.equals(loginId) || ownerNm.equals(loginCd)));

        if (!ownerMatched && ownerUid.isEmpty() && ownerNm.isEmpty()) {
            boolean isAdmin = "admin".equalsIgnoreCase(loginId) || "admin".equalsIgnoreCase(loginCd);
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

    private String trimObj(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    public ResultVO validateApprovePeerReview(String objectId, UserVO userVo) {
        ResultVO result = new ResultVO();

        if (objectId == null || objectId.trim().isEmpty()) {
            result.setSuccess(false);
            result.setMessage("검토 대상을 확인할 수 없습니다.");
            return result;
        }

        Map<String, Object> info = dao.selectPeerReviewApprovalInfo(objectId.trim());
        if (info == null || info.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("검토 대상을 확인할 수 없습니다.");
            return result;
        }

        if ("Y".equalsIgnoreCase(getInfoString(info, "deletedYn", "DELETED_YN", "deleted_yn"))) {
            result.setSuccess(false);
            result.setMessage("철회된 문서는 검토할 수 없습니다.");
            return result;
        }

        List<String> approverListRaw = splitCsv(getInfoString(info, "approver", "APPROVER"));
        if (approverListRaw.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("검토자가 지정되지 않은 문서입니다.");
            return result;
        }
        List<String> approverList = normalizeApproverList(approverListRaw);

        String userCd = trim(userVo.getUserCd());
        String userNm = trim(userVo.getUserNm());
        String approverToken = resolveApproverToken(approverListRaw, userCd, userNm);
        if (approverToken.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("검토 대상자만 검토할 수 있습니다.");
            return result;
        }

        String currentStatus = getInfoString(info, "status", "STATUS");
        if (STATUS_APPROVED.equals(currentStatus)) {
            result.setSuccess(false);
            result.setMessage("이미 검토완료된 건입니다.");
            return result;
        }

        String currentApprovedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users", "approvedusers");
        String approvalSource = currentApprovedUsers.isEmpty() ? currentStatus : currentApprovedUsers;
        Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
        if (approvedSet.contains(approverToken)) {
            result.setSuccess(false);
            result.setMessage("이미 검토한 건입니다.");
            return result;
        }

        result.setSuccess(true);
        result.setMessage("OK");
        return result;
    }


    public ResultVO approvePeerReview(String objectId, UserVO userVo) {
        ResultVO result = new ResultVO();
        Map<String, Object> info = dao.selectPeerReviewApprovalInfo(objectId);

        if (info == null || info.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("검토 대상을 찾을 수 없습니다.");
            return result;
        }

        List<String> approverListRaw = splitCsv(getInfoString(info, "approver", "APPROVER"));
        List<String> approverList = normalizeApproverList(approverListRaw);
        String userCd = trim(userVo.getUserCd());
        String userNm = trim(userVo.getUserNm());
        String approverToken = resolveApproverToken(approverListRaw, userCd, userNm);
        if (approverToken.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("검토 대상자만 검토할 수 있습니다.");
            return result;
        }

        String currentStatus = getInfoString(info, "status", "STATUS");
        String currentApprovedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users", "approvedusers");
        String approvalSource = currentApprovedUsers.isEmpty() ? currentStatus : currentApprovedUsers;
        Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
        approvedSet.add(approverToken);

        boolean isFinalApprove = approvedSet.containsAll(approverList);
        String nextStatus = isFinalApprove ? STATUS_APPROVED : STATUS_APPROVING;
        String approvedUsersValue = String.join(",", approvedSet);

        Map<String, Object> param = new java.util.HashMap<>();
        param.put("objectId", objectId);
        param.put("approvedUsers", approvedUsersValue);
        param.put("status", nextStatus);

        int updated = dao.approvePeerReview(param);
        result.setSuccess(updated > 0);
        result.setMessage(updated > 0 ? "검토되었습니다." : "검토 처리에 실패했습니다.");
        return result;
    }


    private List<String> splitCsv(String value) {
        List<String> result = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return result;
        }
        String[] tokens = value.split(",");
        for (String token : tokens) {
            String normalized = trim(token);
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return result;
    }

    private List<String> parseApprovedUsers(String value) {
        List<String> result = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return result;
        }
        if (STATUS_APPROVING.equals(value) || STATUS_APPROVED.equals(value)) {
            return result;
        }
        String[] tokens = value.split(",");
        for (String token : tokens) {
            String normalized = splitApprovedUser(trim(token));
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return result;
    }

    private boolean isApprovalCompleted(Map<String, Object> info) {
        if (info == null || info.isEmpty()) {
            return false;
        }

        String status = getInfoString(info, "status", "STATUS");
        if (STATUS_APPROVED.equals(status)) {
            return true;
        }

        List<String> approverList = normalizeApproverList(splitCsv(getInfoString(info, "approver", "APPROVER")));
        if (approverList.isEmpty()) {
            return false;
        }

        String approvedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users", "approvedusers");
        String approvalSource = approvedUsers.isEmpty() ? status : approvedUsers;
        Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
        return approvedSet.containsAll(approverList);
    }

    public List<Map<String, Object>> selectMainFileInfo(String objectId) {
        return appendFileExists(dao.selectMainFileInfo(objectId));
    }

    public List<Map<String, Object>> selectMainFileInfo(String objectId, String peerreviewNo) {
        List<Map<String, Object>> rows = dao.selectMainFileInfo(objectId);
        if (rows != null && !rows.isEmpty()) {
            return appendFileExists(rows);
        }
        String no = trim(peerreviewNo);
        if (no.isEmpty()) {
            return appendFileExists(rows);
        }
        return appendFileExists(dao.selectMainFileInfoByPeerReviewNo(no));
    }

    private List<Map<String, Object>> appendFileExists(List<Map<String, Object>> rows) {
        if (rows == null) {
            return rows;
        }
        for (Map<String, Object> row : rows) {
            String filePathNm = getInfoString(row, "filePathNm", "FILE_PATH_NM", "file_path_nm", "filepathnm");
            boolean fileExists = false;
            if (!filePathNm.isEmpty()) {
                try {
                    fileExists = Files.isRegularFile(Paths.get(StringUtil.replaceLfiPath(filePathNm)));
                } catch (Exception ignored) {
                    fileExists = false;
                }
            }
            row.put("fileExists", fileExists);
        }
        return rows;
    }

    public Map<String, Object> getPeerReviewFileDownloadInfo(String objectId, String fileNo) {
        if (objectId == null || objectId.trim().isEmpty()) {
            return null;
        }
        Map<String, Object> param = new java.util.HashMap<>();
        param.put("objectId", objectId.trim());
        param.put("fileNo", fileNo == null ? "" : fileNo.trim());
        return dao.selectPeerReviewFileDownloadInfo(param);
    }

    public Map<String, Object> getPeerReviewFileDownloadInfoByPeerReviewNo(String peerreviewNo) {
        String no = trim(peerreviewNo);
        if (no.isEmpty()) {
            return null;
        }
        return dao.selectPeerReviewFileDownloadInfoByPeerReviewNo(no);
    }

    public String resolveObjectId(String objectId, String peerreviewNo) {
        String resolved = trim(objectId);
        if (!resolved.isEmpty()) {
            return resolved;
        }
        String no = trim(peerreviewNo);
        if (no.isEmpty()) {
            return "";
        }
        String found = dao.selectObjectIdByPeerReviewNo(no);
        return found == null ? "" : found.trim();
    }

    public String resolvePeerReviewNoByObjectId(String objectId) {
        String id = trim(objectId);
        if (id.isEmpty()) {
            return "";
        }
        String no = dao.selectPeerReviewNoByObjectId(id);
        return no == null ? "" : no.trim();
    }

    public List<Map<String, Object>> getApprovalStatusRows(String objectId, UserVO userVo) {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> info = dao.selectPeerReviewApprovalInfo(objectId);
        if (info == null || info.isEmpty()) {
            return rows;
        }

        List<String> approverUsers = splitCsv(getInfoString(info, "approver", "APPROVER"));
        String status = getInfoString(info, "status", "STATUS");
        String approvedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users", "approvedusers");
        String approvalSource = approvedUsers.isEmpty() ? status : approvedUsers;
        Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
        Map<String, String> commentMap = getApprovalCommentMap(objectId);

        String userCd = trim(userVo.getUserCd());
        String userNm = trim(userVo.getUserNm());
        for (String user : approverUsers) {
            String normalized = splitApprovedUser(user);
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("approver", user);
            row.put("status", approvedSet.contains(normalized) ? "승인" : "대기");
            row.put("comment", commentMap.getOrDefault(normalized, "-"));
            row.put("editable", (normalized.equals(userCd) || normalized.equals(userNm)) ? "Y" : "N");
            row.put("approvalType", "APPROVER");
            rows.add(row);
        }
        return rows;
    }

    public ResultVO saveApprovalComment(String objectId, String comment, UserVO userVo) {
        ResultVO result = new ResultVO();
        Map<String, Object> info = dao.selectPeerReviewApprovalInfo(objectId);
        if (info == null || info.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("승인 대상 문서를 찾을 수 없습니다.");
            return result;
        }

        List<String> users = new ArrayList<>();
        users.addAll(splitCsv(getInfoString(info, "approver", "APPROVER")));

        String userCd = trim(userVo.getUserCd());
        String userNm = trim(userVo.getUserNm());
        String actor = users.contains(userCd) ? userCd : (users.contains(userNm) ? userNm : "");
        if (actor.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("본인 결재에만 코멘트를 입력할 수 있습니다.");
            return result;
        }

        String currentStatus = getInfoString(info, "status", "STATUS");
        String currentApprovedUsers = getInfoString(info, "approvedUsers", "APPROVED_USERS", "approved_users", "approvedusers");
        String approvalSource = currentApprovedUsers.isEmpty() ? currentStatus : currentApprovedUsers;
        Set<String> approvedSet = new LinkedHashSet<>(parseApprovedUsers(approvalSource));
        if (!approvedSet.contains(splitApprovedUser(actor))) {
            result.setSuccess(false);
            result.setMessage("승인 후 코멘트 입력이 가능합니다.");
            return result;
        }

        String normalizedComment = trim(comment);
        if (normalizedComment.isEmpty()) {
            normalizedComment = "승인하였습니다.";
        }
        Map<String, Object> param = new java.util.HashMap<>();
        param.put("objectId", objectId);
        param.put("approverId", splitApprovedUser(actor));
        param.put("comment", normalizedComment);
        int updated = dao.upsertApprovalComment(param);
        result.setSuccess(updated > 0);
        result.setMessage(updated > 0 ? "코멘트가 저장되었습니다." : "코멘트 저장에 실패했습니다.");
        return result;
    }

    private String splitApprovedUser(String entry) {
        String normalized = trim(entry);
        if (normalized.isEmpty()) {
            return "";
        }
        int idx = normalized.indexOf('|');
        return idx >= 0 ? normalized.substring(0, idx).trim() : normalized;
    }

    private List<String> normalizeApproverList(List<String> approverListRaw) {
        List<String> normalized = new ArrayList<>();
        for (String raw : approverListRaw) {
            String token = splitApprovedUser(raw);
            if (!token.isEmpty() && !normalized.contains(token)) {
                normalized.add(token);
            }
        }
        return normalized;
    }

    private String resolveApproverToken(List<String> approverListRaw, String userCd, String userNm) {
        for (String raw : approverListRaw) {
            String normalized = splitApprovedUser(raw);
            String displayName = extractDisplayName(raw);
            if (normalized.equals(userCd) || normalized.equals(userNm)
                    || displayName.equals(userCd) || displayName.equals(userNm)) {
                return normalized;
            }
        }
        return "";
    }

    private String extractDisplayName(String entry) {
        String normalized = trim(entry);
        if (normalized.isEmpty()) {
            return "";
        }
        int idx = normalized.indexOf('|');
        return idx >= 0 ? normalized.substring(idx + 1).trim() : "";
    }

    private Map<String, String> getApprovalCommentMap(String objectId) {
        Map<String, String> map = new java.util.HashMap<>();
        if (objectId == null || objectId.trim().isEmpty()) {
            return map;
        }
        List<Map<String, Object>> list = dao.selectApprovalComments(objectId.trim());
        for (Map<String, Object> row : list) {
            String approver = splitApprovedUser(getInfoString(row, "approverId", "APPROVER_ID", "approverid"));
            String c = getInfoString(row, "comment", "COMMENT", "comment_txt", "COMMENT_TXT");
            if (!approver.isEmpty()) {
                map.put(approver, c);
            }
        }
        return map;
    }

    private String getInfoString(Map<String, Object> map, String... keys) {
        if (map == null || keys == null) {
            return "";
        }
        for (String key : keys) {
            if (key == null || key.isEmpty()) {
                continue;
            }
            if (map.containsKey(key)) {
                return trimObj(map.get(key));
            }
        }
        return "";
    }
}


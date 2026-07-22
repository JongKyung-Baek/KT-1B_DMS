package kr.esob.fdms.controller.inside.distribution.peerreview;

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

import javax.inject.Inject;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.controller.login.UserVO;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/inside/distribution/peerReview")
public class PeerReviewController extends AbstractController {

    @Inject
    PeerReviewService peerReviewService;

    @Inject
    ComboService comboService;

    @RequestMapping(value = "/")
    public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
        setHomeParam(model, param);
        model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formPeerReviewRequest")));
        model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarPeerReviewRequest")));
        model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridPeerReviewRequestList")));
        return "inside/distribution/peerReview/peerReviewList";
    }

    @RequestMapping(value = "/selectList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody GridResultVO selectList(PeerReviewListParam param) throws Exception {
        return commonSelectList(param, peerReviewService);
    }

    @RequestMapping(value = "/registerPopup")
    public String registerPopup(PeerReviewSaveParam param, Model model) {
        UserVO user = param.getSessionUser();
        String registerUser = user == null ? "" : user.getUsername();
        String date = LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        model.addAttribute("registerUser", registerUser);
        model.addAttribute("date", date);
        model.addAttribute("coPublisherUsers", comboService.selectActiveUserList());
        return "inside/distribution/peerReview/peerReviewRegisterPopup";
    }

  /*   @RequestMapping(value = "/saveRegister", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody ResultVO saveRegister(@RequestBody PeerReviewSaveParam param) {
        try {
            return peerReviewService.savePeerReview(param);
        } catch (Exception e) {
            ResultVO result = new ResultVO();
            result.setSuccess(false);
            result.setMessage("등록 중 오류가 발생했습니다.");
            return result;
        }
    } */

    @RequestMapping(value = "/nextPeerReviewNo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody Map<String, Object> nextPeerReviewNo() {
        Map<String, Object> result = new HashMap<>();
        result.put("peerreviewNo", peerReviewService.getNextPeerReviewNo());
        return result;
    }

    @RequestMapping("/peerReviewFilePopup")
    public String peerReviewFilePopup(PeerReviewSaveParam param, Model model) {
        String peerReviewNo = param.getPeerreviewNo();
        if (peerReviewNo == null || peerReviewNo.trim().isEmpty()) {
            peerReviewNo = param.getRequestNo();
        }

        String objectId = peerReviewService.resolveObjectId(param.getObjectId(), peerReviewNo);
        List<Map<String, Object>> mainFileList = peerReviewService.selectMainFileInfo(objectId, peerReviewNo);
        model.addAttribute("objectId", objectId);
        model.addAttribute("peerReviewNo", peerReviewNo);
        model.addAttribute("mainFileList", mainFileList);
        model.addAttribute("mainFileJson", JSONArray.fromObject(mainFileList));
        return "inside/distribution/peerReview/peerReviewFilePopup";
    }

    @PostMapping(value = "/uploadPeerReviewRegisFile", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody ResultVO uploadPeerReviewRegisFile(MultipartHttpServletRequest multipartHttpServletRequest) {
        try {
            return peerReviewService.savePeerReviewRegisterFile(multipartHttpServletRequest);
        } catch (Exception e) {
            ResultVO result = new ResultVO();
            result.setSuccess(false);
            result.setMessage("등록 중 오류가 발생했습니다.");
            return result;
        }
    }

    @PostMapping("/approveStatusRows")
    @ResponseBody
    public Map<String, Object> approveStatusRows(@RequestBody Map<String, String> param, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        UserVO userVo = (UserVO) authentication.getPrincipal();
        result.put("rows", peerReviewService.getApprovalStatusRows(param.get("objectId"), userVo));
        return result;
    }

    @PostMapping("/saveApprovalComment")
    @ResponseBody
    public ResultVO saveApprovalComment(@RequestBody Map<String, String> param, Authentication authentication) {
        UserVO userVo = (UserVO) authentication.getPrincipal();
        return peerReviewService.saveApprovalComment(param.get("objectId"), param.get("comment"), userVo);
    }

    @GetMapping("/downloadFile")
    public ResponseEntity<byte[]> downloadFile(
            @RequestParam("objectId") String objectId,
            @RequestParam(value = "fileNo", required = false) String fileNo,
            @RequestParam(value = "peerReviewNo", required = false) String peerReviewNo,
            @RequestParam(value = "watermarkYn", required = false, defaultValue = "Y") String watermarkYn,
            Authentication authentication) throws Exception {
        String effectivePeerReviewNo = (peerReviewNo == null ? "" : peerReviewNo.trim());
        if (effectivePeerReviewNo.isEmpty()) {
            effectivePeerReviewNo = peerReviewService.resolvePeerReviewNoByObjectId(objectId);
        }

        Map<String, Object> fileInfo = peerReviewService.getPeerReviewFileDownloadInfo(objectId, fileNo);
        if ((fileInfo == null || fileInfo.isEmpty()) && !effectivePeerReviewNo.isEmpty()) {
            String resolvedObjectId = peerReviewService.resolveObjectId("", effectivePeerReviewNo);
            if (resolvedObjectId != null && !resolvedObjectId.trim().isEmpty()) {
                fileInfo = peerReviewService.getPeerReviewFileDownloadInfo(resolvedObjectId, fileNo);
            }
        }
        if (fileInfo == null || fileInfo.isEmpty()) {
            System.out.println("[PEERREVIEW_DOWNLOAD] not found: fileInfo empty, objectId=" + objectId + ", fileNo=" + fileNo + ", peerReviewNo=" + effectivePeerReviewNo);
            return ResponseEntity.notFound().build();
        }
        String rawFilePath = getMapString(fileInfo, "filePath", "filepath", "FILEPATH", "file_path_nm", "FILE_PATH_NM");
        String filePath = normalizePath(rawFilePath);
        String orgFileNm = getMapString(fileInfo, "orgFileNm", "orgfilenm", "ORGFILENM", "org_file_nm", "ORG_FILE_NM");
        if (orgFileNm.isEmpty()) {
            orgFileNm = "download.bin";
        }
        System.out.println("[PEERREVIEW_DOWNLOAD] db rawFilePath=[" + rawFilePath + "], normalizedFilePath=[" + filePath + "], rawLen=" + rawFilePath.length() + ", normalizedLen=" + filePath.length() + ", orgFileNm=" + orgFileNm);

        if ((filePath.isEmpty() || !Files.exists(Paths.get(filePath))) && !effectivePeerReviewNo.isEmpty()) {
            Map<String, Object> fallbackByNo = peerReviewService.getPeerReviewFileDownloadInfoByPeerReviewNo(effectivePeerReviewNo);
            if (fallbackByNo != null && !fallbackByNo.isEmpty()) {
                String fallbackRawPathByNo = getMapString(fallbackByNo, "filePath", "filepath", "FILEPATH", "file_path_nm", "FILE_PATH_NM");
                String fallbackPathByNo = normalizePath(fallbackRawPathByNo);
                System.out.println("[PEERREVIEW_DOWNLOAD] fallbackByNo rawPath=[" + fallbackRawPathByNo + "], normalizedPath=[" + fallbackPathByNo + "], len=" + fallbackPathByNo.length());
                if (!fallbackPathByNo.isEmpty()) {
                    fileInfo = fallbackByNo;
                    filePath = fallbackPathByNo;
                    String fallbackOrgFileNmByNo = getMapString(fallbackByNo, "orgFileNm", "orgfilenm", "ORGFILENM", "org_file_nm", "ORG_FILE_NM");
                    if (!fallbackOrgFileNmByNo.isEmpty()) {
                        orgFileNm = fallbackOrgFileNmByNo;
                    }
                }
                if (!fallbackPathByNo.isEmpty() && Files.exists(Paths.get(fallbackPathByNo))) {
                    System.out.println("[PEERREVIEW_DOWNLOAD] fallback success by peerReviewNo direct query. filePath=" + filePath);
                }
            }
        }

        if ((filePath.isEmpty() || !Files.exists(Paths.get(filePath))) && !effectivePeerReviewNo.isEmpty()) {
            String resolvedObjectId = peerReviewService.resolveObjectId("", effectivePeerReviewNo);
            if (resolvedObjectId != null && !resolvedObjectId.trim().isEmpty()) {
                Map<String, Object> fallbackFileInfo = peerReviewService.getPeerReviewFileDownloadInfo(resolvedObjectId, fileNo);
                if (fallbackFileInfo != null && !fallbackFileInfo.isEmpty()) {
                    String fallbackRawPath = getMapString(fallbackFileInfo, "filePath", "filepath", "FILEPATH", "file_path_nm", "FILE_PATH_NM");
                    String fallbackPath = normalizePath(fallbackRawPath);
                    System.out.println("[PEERREVIEW_DOWNLOAD] fallbackByResolvedObject rawPath=[" + fallbackRawPath + "], normalizedPath=[" + fallbackPath + "], len=" + fallbackPath.length());
                    if (!fallbackPath.isEmpty()) {
                        fileInfo = fallbackFileInfo;
                        filePath = fallbackPath;
                        String fallbackOrgFileNm = getMapString(fallbackFileInfo, "orgFileNm", "orgfilenm", "ORGFILENM", "org_file_nm", "ORG_FILE_NM");
                        if (!fallbackOrgFileNm.isEmpty()) {
                            orgFileNm = fallbackOrgFileNm;
                        }
                    }
                    if (!fallbackPath.isEmpty() && Files.exists(Paths.get(fallbackPath))) {
                        System.out.println("[PEERREVIEW_DOWNLOAD] fallback success by peerReviewNo. objectId=" + resolvedObjectId + ", filePath=" + filePath);
                    }
                }
            }
        }
        if (filePath.isEmpty() || !Files.exists(Paths.get(filePath))) {
            System.out.println("[PEERREVIEW_DOWNLOAD] not found on disk: " + filePath);
            return ResponseEntity.notFound().build();
        }
        if (!isPdfFilePath(filePath) && !isAdminRole(authentication)) {
            return ResponseEntity.status(403).build();
        }
        byte[] bytes = null;
        if ("Y".equalsIgnoreCase(watermarkYn)) {
            bytes = requestWatermarkPdf(filePath, orgFileNm, authentication);
        }
        if (bytes == null || bytes.length == 0) {
            bytes = Files.readAllBytes(Paths.get(filePath));
        }
        String downloadFileName = buildDownloadFileName(filePath, orgFileNm);
        String encodedFileName = URLEncoder.encode(downloadFileName, "UTF-8").replaceAll("\\+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName)
                .contentLength(bytes.length)
                .body(bytes);
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

    @RequestMapping("/delete")
    @ResponseBody
    public Map<String, Object> deletePeerReview(@RequestBody Map<String, List<Map<String, String>>> param, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        UserVO userVo = (UserVO) authentication.getPrincipal();

        List<Map<String, String>> list = param.get("list");
        if (list == null || list.isEmpty()) {
            result.put("successCount", 0);
            result.put("error", "철회할 대상을 선택하세요");
            return result;
        }

        for (Map<String, String> item : list) {
            String objectId = resolveObjectId(item);
            ResultVO validateResult = peerReviewService.validateDeletePeerReview(objectId, userVo);
            if (!validateResult.isSuccess()) {
                result.put("successCount", 0);
                result.put("error", validateResult.getMessage());
                return result;
            }
        }

        int success = 0;
        for (Map<String, String> item : list) {
            String objectId = resolveObjectId(item);
            if (objectId != null && !objectId.isEmpty()) {
                success += peerReviewService.deletePeerReview(objectId);
            }
        }

        if (success == 0) {
            result.put("error", "철회 처리에 실패했습니다.");
        }
        result.put("successCount", success);
        return result;
    }

    @RequestMapping("/approve")
    @ResponseBody
    public Map<String, Object> approvePeerReview(@RequestBody Map<String, List<Map<String, String>>> param, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        UserVO userVo = (UserVO) authentication.getPrincipal();

        List<Map<String, String>> list = param.get("list");
        int successCount = 0;
        int failCount = 0;
        String message = "";

        if (list == null || list.isEmpty()) {
            result.put("successCount", 0);
            result.put("failCount", 0);
            result.put("message", "검토할 대상을 선택하세요");
            return result;
        }

        for (Map<String, String> item : list) {
            String objectId = resolveObjectId(item);
            ResultVO validateResult = peerReviewService.validateApprovePeerReview(objectId, userVo);
            if (!validateResult.isSuccess()) {
                result.put("successCount", 0);
                result.put("failCount", list.size());
                result.put("message", validateResult.getMessage());
                return result;
            }
        }

        for (Map<String, String> item : list) {
            String objectId = resolveObjectId(item);
            ResultVO approveResult = peerReviewService.approvePeerReview(objectId, userVo);
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

    private String resolveObjectId(Map<String, String> item) {
        if (item == null) {
            return "";
        }
        String objectId = trim(item.get("objectId"));
        if (!objectId.isEmpty()) {
            return objectId;
        }
        objectId = trim(item.get("hiddenObjectId"));
        if (!objectId.isEmpty()) {
            return objectId;
        }

        String no = trim(item.get("peerReviewNo"));
        if (no.isEmpty()) no = trim(item.get("peerreviewNo"));
        if (no.isEmpty()) no = trim(item.get("requestNo"));
        if (no.isEmpty()) return "";
        return peerReviewService.resolveObjectId("", no);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizePath(String value) {
        if (value == null) {
            return "";
        }
        String path = value.trim();
        if (path.startsWith("\"") && path.endsWith("\"") && path.length() > 1) {
            path = path.substring(1, path.length() - 1).trim();
        }
        path = path.replace('/', '\\');
        return path;
    }

    private String getMapString(Map<String, Object> map, String... keys) {
        if (map == null || keys == null) {
            return "";
        }
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                String text = String.valueOf(value).trim();
                if (!text.isEmpty()) {
                    return text;
                }
            }
        }
        return "";
    }

}



package kr.esob.fdms.commonlogic.updown;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

/* import kr.esob.fdms.commonlogic.distactlog.DistributionActLogDao;
import kr.esob.fdms.commonlogic.distactlog.DistributionActLogParam; */
import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeState;
import kr.esob.fdms.commonlogic.filecache.ExternalFileApiClient;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
/* import kr.esob.fdms.controller.outside.secp.request.SecpRequestService;
import kr.esob.fdms.controller.outside.sw.request.RequestService; */
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CommonUpdownV2Service {
    @Inject private CommonUpdownDao commonUpdownDao;
    @Inject private ExternalFileApiClient apiClient;
    /* @Inject private RequestService swRequestService; */
/*     @Inject private SecpRequestService secpRequestService;
    @Inject private DistributionActLogDao distActLogDao; */
    @Inject private OutsideDistributionDeliveryConfirmService outsideDistributionDeliveryConfirmService;

    public RestSeqResolveResult resolveRestRequestSeq(CommonUpdownV2StartParam param) {
        if (param == null) {throw new IllegalArgumentException("param is null"); }

        String objectType = nvl(param.getObjectType()).toUpperCase(); // DOC | DRAWING
        String requestNo  = nvl(param.getRequestNo());                 // SERIAL_NO
        String dataNo     = nvl(param.getDataNo());                    // 문서는 DATA_OFFER_DOC_SEQ, 일부 구기능은 DATA_NO
        String docSeqParam = nvl(param.getDocSeq());                   // *_DOC_SEQ
        String fileSeqParam = nvl(param.getFileSeq());                 // FILE_SEQ

        if (objectType.isEmpty()) {
            throw new IllegalArgumentException("objectType is required");
        }

        if (!isBlank(fileSeqParam)) {
            RestSeqResolveResult result = new RestSeqResolveResult();
            result.setRestSeq(fileSeqParam);
            result.setRestSeqType("FILE_SEQ");
            return result;
        }

        if (requestNo.isEmpty()) {
            throw new IllegalArgumentException("requestNo is required when fileSeq is empty");
        }

        String docSeq;
        String fileSeq;
        if ("DOC".equals(objectType)) {
            if (!isBlank(docSeqParam)) {
                docSeq = docSeqParam;
            } else {
//            } else if (!isBlank(dataNo)) {
//                docSeq = commonUpdownDao.selectDocSeqByDataNo(requestNo, dataNo); // 기존 DATA_NO 기반 해석
//                if (isBlank(docSeq)) { throw new IllegalStateException("DATA_OFFER_DOC_SEQ not found by requestNo+docSeq"); }
//            } else {
                throw new IllegalArgumentException("docSeq is required");
            }
            fileSeq = commonUpdownDao.selectDocFileSeqByDocSeq(requestNo, docSeq);
        } else if ("DRAWING".equals(objectType)) {
            if (!isBlank(dataNo)) {
                docSeq = commonUpdownDao.selectDrawingDocSeqByDataNo(requestNo, dataNo);
                if (isBlank(docSeq)) { throw new IllegalStateException("DELVY_CNFIRM_DOC_SEQ not found by requestNo+dataNo"); }
            } else if (!isBlank(docSeqParam)) {
                docSeq = docSeqParam;
            } else {
                throw new IllegalArgumentException("dataNo or docSeq is required");
            }
            fileSeq = commonUpdownDao.selectDrawingFileSeqByDocSeq(requestNo, docSeq);
        } else {
            throw new IllegalArgumentException("unsupported objectType: " + objectType);
        }

        if (fileSeq == null || fileSeq.trim().isEmpty()) {throw new IllegalStateException("FILE_SEQ not found"); }

        RestSeqResolveResult result = new RestSeqResolveResult();
        result.setRestSeq(fileSeq);         // REST API로 보낼 실제 seq = FILE_SEQ
        result.setRestSeqType("FILE_SEQ");  // 명시적으로 타입 고정
        return result;
    }

    public TempDownloadResult fetchAndSaveTempFile(String objectType, String restSeq, String originalFileName, String fileExt) throws Exception {
        File localSource = resolveLocalSourceFile(objectType, restSeq);
        if (localSource != null && localSource.exists() && localSource.isFile()) {
            log.info("[V2-LOCAL][HIT] objectType={}, restSeq={}, sourcePath={}, sourceLength={}",
                objectType, restSeq, localSource.getAbsolutePath(), localSource.length());
            return copyLocalFileToTemp(objectType, restSeq, originalFileName, fileExt, localSource);
        }

        log.info("[V2-LOCAL][MISS] objectType={}, restSeq={}, fallback=REST", objectType, restSeq);
        String apiUrl = SystemConfig.getSystemConfigValue("KAI_DOWNLOAD");
        if (isBlank(apiUrl)) { apiUrl = SystemConfig.getSystemConfigValue("REST_DELIVERY_FILE_DOWNLOAD_URL"); }
        if (isBlank(apiUrl)) { throw new IllegalStateException("KAI_DOWNLOAD API URL is empty."); }

        byte[] bytes = apiClient.requestOriginalBySeq(apiUrl, restSeq);
        if (bytes == null || bytes.length == 0) { throw new IllegalStateException("REST API returned empty file bytes."); }

        // 파일 저장 루트
        String rootPath = SystemConfig.getSystemConfigValue("UPDOWN_PATH");
        if (isBlank(rootPath)) { rootPath = System.getProperty("java.io.tmpdir"); }

        String ext = extractExt(originalFileName);
        if (isBlank(ext)) {
            ext = normalizeExt(fileExt);
        }
        if (isBlank(ext)) {
            ext = "bin";
        }
        String storedBaseName = UUID.randomUUID().toString().replace("-", "");
        String storedFileName = storedBaseName + "." + ext;

        File dir = new File(rootPath);
        if (!dir.exists() && !dir.mkdirs()) { throw new IllegalStateException("Failed to create temp directory: " + rootPath); }

        File target = new File(dir, storedFileName);
        try (FileOutputStream fos = new FileOutputStream(target)) {
            fos.write(bytes);
            fos.flush();
        }

        TempDownloadResult out = new TempDownloadResult();
        out.setObjectType(objectType);
        out.setRestSeq(restSeq);
        out.setTempFilePath(target.getAbsolutePath());
        out.setSavedFileName(storedFileName);
        out.setFileSize(bytes.length);
        return out;
    }

    private File resolveLocalSourceFile(String objectType, String restSeq) {
        String normalized = nvl(objectType).toUpperCase();
        if (isBlank(restSeq)) {
            return null;
        }
        /* if ("SW".equals(normalized)) {
            return swRequestService.resolveDownloadSourceFileByItemFileSeq(restSeq);
        }
        if ("SECP".equals(normalized)) {
            return secpRequestService.resolveDownloadSourceFileByFileSeq(restSeq);
        } */
        return null;
    }

    private TempDownloadResult copyLocalFileToTemp(String objectType, String restSeq, String originalFileName, String fileExt, File sourceFile) throws Exception {
        String rootPath = SystemConfig.getSystemConfigValue("UPDOWN_PATH");
        if (isBlank(rootPath)) { rootPath = System.getProperty("java.io.tmpdir"); }

        String ext = extractExt(originalFileName);
        if (isBlank(ext)) {
            ext = normalizeExt(fileExt);
        }
        if (isBlank(ext)) {
            ext = extractExt(sourceFile.getName());
        }
        if (isBlank(ext)) {
            ext = "bin";
        }

        String storedBaseName = UUID.randomUUID().toString().replace("-", "");
        String storedFileName = storedBaseName + "." + ext;

        File dir = new File(rootPath);
        if (!dir.exists() && !dir.mkdirs()) { throw new IllegalStateException("Failed to create temp directory: " + rootPath); }

        File target = new File(dir, storedFileName);
        Files.copy(sourceFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

        TempDownloadResult out = new TempDownloadResult();
        out.setObjectType(objectType);
        out.setRestSeq(restSeq);
        out.setTempFilePath(target.getAbsolutePath());
        out.setSavedFileName(storedFileName);
        out.setFileSize(target.length());
        return out;
    }

    private String extractExt(String fileName) {
        if (isBlank(fileName)) return "";
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) return "";
        return normalizeExt(fileName.substring(idx + 1));
    }

    private String normalizeExt(String ext) {
        if (isBlank(ext)) return "";
        String normalized = ext.trim();
        while (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        return normalized.trim().toLowerCase();
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    public void saveOutsideDistributionDownloadActLog(Object userObj, DownloadRuntimeState state, String status, String errorMessage) {
        try {
            if (!(userObj instanceof kr.esob.fdms.controller.login.UserVO)) {
                return;
            }
            kr.esob.fdms.controller.login.UserVO userVo = (kr.esob.fdms.controller.login.UserVO) userObj;
            if (userVo == null || state == null) {
                return;
            }

            String requestType = nvl(state.getRequestType()).toUpperCase();
            String objectType = nvl(state.getObjectType()).toUpperCase();
            if (!"DISTRIBUTION".equals(requestType)) {
                return;
            }
            if (!"DOC".equals(objectType) && !"DRAWING".equals(objectType)) {
                return;
            }
            if (state.isActLogSaved()) {
                return;
            }

            Map<String, Object> base = commonUpdownDao.selectOutsideDistributionActLogBase(
                state.getRequestNo(),
                state.getDocSeq(),
                objectType,
                isBlank(state.getFileSeq()) ? state.getFileNo() : state.getFileSeq()
            );
            if (base == null) {
                return;
            }

           /*  DistributionActLogParam logParam = new DistributionActLogParam();
            logParam.setRequestNo(readMapString(base, "requestNo"));
            logParam.setRequestType(readMapString(base, "requestType"));
            logParam.setObjectId(readMapString(base, "objectId"));
            logParam.setFileNo(readMapString(base, "fileNo"));
            logParam.setObjectType(readMapString(base, "objectType"));
            logParam.setProtectYn(readMapString(base, "protectYn"));
            logParam.setActType("DOWNLOAD");
            logParam.setActResultCd("COMPLETED".equalsIgnoreCase(status) ? "SUCCESS" : "FAIL");
            logParam.setActResultMsg(trimToNull(errorMessage));
            logParam.setOrgFileNm(readMapString(base, "orgFileNm"));
            logParam.setActionUserCd(userVo.getUserCd());
            logParam.setActionUserNm(userVo.getUserNm());
            logParam.setCompanyCd(userVo.getCompanyCd());
            logParam.setCompanyNm(userVo.getCompanyNm());
            logParam.setProjectNm(readMapString(base, "projectNm"));
            logParam.setSourceType(readMapString(base, "sourceType"));
            logParam.setSourceKey(readMapString(base, "sourceKey"));
            logParam.setInsertUid(userVo.getUserCd());
            distActLogDao.insertActLog(logParam); */
        } catch (Exception e) {
            log.warn("[DOWNLOAD-ACT-LOG][SKIP] requestNo={}, docSeq={}, objectType={}, status={}, reason={}",
                state == null ? null : state.getRequestNo(),
                state == null ? null : state.getDocSeq(),
                state == null ? null : state.getObjectType(),
                status,
                e.getMessage());
        }
    }

    public void updateOutsideDistributionDeliveryConfirm(DownloadRuntimeState state, String status) {
        try {
            if (state == null) { return; }

            String requestType = nvl(state.getRequestType()).toUpperCase();
            String objectType = nvl(state.getObjectType()).toUpperCase();
            if (!"DISTRIBUTION".equals(requestType)) { return; }
            if (!"DOC".equals(objectType) && !"DRAWING".equals(objectType)) { return; }
            if (!"COMPLETED".equalsIgnoreCase(status)) { return; }

            outsideDistributionDeliveryConfirmService.markConfirmed(
                state.getRequestNo(),
                state.getDocSeq(),
                objectType,
                isBlank(state.getFileSeq()) ? state.getFileNo() : state.getFileSeq()
            );
        } catch (Exception e) {
            log.warn("[DELIVERY-CONFIRM][DOWNLOAD][SKIP] requestNo={}, docSeq={}, objectType={}, status={}, reason={}",
                state == null ? null : state.getRequestNo(),
                state == null ? null : state.getDocSeq(),
                state == null ? null : state.getObjectType(),
                status,
                e.getMessage());
        }
    }

    private String trimToNull(String value) {
        if (value == null) { return null; }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String readMapString(Map<String, Object> source, String key) {
        if (source == null || key == null) { return null; }
        Object value = source.get(key);
        if (value == null) { value = source.get(key.toLowerCase()); }
        if (value == null) { value = source.get(key.toUpperCase()); }
        return value == null ? null : String.valueOf(value);
    }

    private String toNumericString(String value) {
        if (value == null) { return null; }
        String trimmed = value.trim();
        return trimmed.matches("\\d+") ? trimmed : null;
    }

    public static class TempDownloadResult {
        private String objectType;
        private String restSeq;
        private String tempFilePath;
        private String savedFileName;
        private long fileSize;

        public String getObjectType() { return objectType; }
        public void setObjectType(String objectType) { this.objectType = objectType; }

        public String getRestSeq() { return restSeq; }
        public void setRestSeq(String restSeq) { this.restSeq = restSeq; }

        public String getTempFilePath() { return tempFilePath; }
        public void setTempFilePath(String tempFilePath) { this.tempFilePath = tempFilePath; }

        public String getSavedFileName() { return savedFileName; }
        public void setSavedFileName(String savedFileName) { this.savedFileName = savedFileName; }

        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    }    


    private String nvl(String s) { return s == null ? "" : s.trim(); }

    public static class RestSeqResolveResult {
        private String restSeq;
        private String restSeqType;

        public String getRestSeq() { return restSeq; }
        public void setRestSeq(String restSeq) { this.restSeq = restSeq; }

        public String getRestSeqType() { return restSeqType; }
        public void setRestSeqType(String restSeqType) { this.restSeqType = restSeqType; }
    }
}

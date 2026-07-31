package kr.esob.fdms.commonlogic.updown;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

/* import kr.esob.fdms.commonlogic.distactlog.DistributionActLogDao;
import kr.esob.fdms.commonlogic.distactlog.DistributionActLogParam; */
import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeState;
import kr.esob.fdms.commonlogic.filecache.ExternalFileApiClient;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.controller.login.UserVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CommonUpdownV2Service {
    @Inject private CommonUpdownDao commonUpdownDao;
    @Inject private ExternalFileApiClient apiClient;
    @Inject private SecurityAclService securityAclService;

    public ResolvedDownloadResource resolveAuthorizedResource(CommonUpdownV2StartParam param, UserVO actor) {
        if (param == null || actor == null) {
            throw new IllegalArgumentException("다운로드 요청자와 자료정보는 필수입니다.");
        }
        String requestNo = nvl(param.getRequestNo());
        String objectId = nvl(param.getDocSeq());
        if (isBlank(objectId) && !isBlank(param.getDataNo())) {
            objectId = commonUpdownDao.selectObjectIdByDataNo(requestNo, nvl(param.getDataNo()));
        }
        String fileKey = isBlank(param.getFileNo()) ? nvl(param.getFileSeq()) : nvl(param.getFileNo());
        if (isBlank(requestNo) || isBlank(objectId) || isBlank(fileKey)) {
            throw new IllegalArgumentException("requestNo, 자료 식별자와 파일 번호는 필수입니다.");
        }

        CommonUpdownFileVO dbResource = commonUpdownDao.selectDownloadResource(requestNo, objectId, fileKey);
        if (dbResource == null) {
            throw new IllegalArgumentException("요청에 포함된 다운로드 파일을 찾을 수 없습니다.");
        }

        String resolvedObjectType = normalizeDbObjectType(dbResource.getObjectType());
        String resolvedObjectId = nvl(dbResource.getObjectId());
        String resolvedFileNo = nvl(dbResource.getFileNo());
        String resolvedRequestNo = nvl(dbResource.getRequestNo());
        if (isBlank(resolvedObjectId) || isBlank(resolvedFileNo) || isBlank(resolvedRequestNo)) {
            throw new IllegalArgumentException("다운로드 자료 메타정보가 올바르지 않습니다.");
        }
        if (commonUpdownDao.countDownloadBusinessAccess(
            resolvedRequestNo, resolvedObjectId, resolvedFileNo, actor.getUserCd()) == 0) {
            throw new org.springframework.security.access.AccessDeniedException("배포 대상 또는 요청자가 아닌 자료입니다.");
        }
        ResolvedDownloadResource resource = new ResolvedDownloadResource();
        resource.setObjectType(resolvedObjectType);
        resource.setObjectId(resolvedObjectId);
        resource.setFileNo(resolvedFileNo);
        resource.setRequestNo(resolvedRequestNo);
        return resource;
    }

    public RestSeqResolveResult resolveRestRequestSeq(ResolvedDownloadResource resource) {
        if (resource == null || isBlank(resource.getRequestNo())
            || isBlank(resource.getObjectId()) || isBlank(resource.getFileNo())) {
            throw new IllegalArgumentException("검증된 다운로드 자료 식별자가 필요합니다.");
        }

        // PostgreSQL dump의 DOCS_REQUEST_FILE.FILE_NO가 원격 파일 API의 FILE_SEQ다.
        // 요청 본문의 fileSeq는 신뢰하지 않고, 앞 단계에서 requestNo/objectId/fileNo로
        // 조회한 DB 값만 사용해야 다른 자료의 파일을 대리 조회하는 것을 막을 수 있다.
        RestSeqResolveResult result = new RestSeqResolveResult();
        result.setRestSeq(resource.getFileNo());
        result.setRestSeqType("FILE_SEQ");
        return result;
    }

    public TempDownloadResult fetchAndSaveTempFile(
            String objectType, String restSeq, String originalFileName,
            String fileExt, TempPathRegistrar tempPathRegistrar) throws Exception {
        if (tempPathRegistrar == null) {
            throw new IllegalArgumentException(
                "Durable temp path registrar is required.");
        }
        File localSource = resolveLocalSourceFile(objectType, restSeq);
        if (localSource != null && localSource.exists() && localSource.isFile()) {
            log.info("[V2-LOCAL][HIT] objectType={}, sourceLength={}",
                objectType, localSource.length());
            return copyLocalFileToTemp(
                objectType, restSeq, originalFileName, fileExt,
                localSource, tempPathRegistrar);
        }

        log.info("[V2-LOCAL][MISS] objectType={}, fallback=REST", objectType);
        String apiUrl = SystemConfig.getSystemConfigValue("FILE_DOWNLOAD_URL");
        if (isBlank(apiUrl)) { apiUrl = SystemConfig.getSystemConfigValue("REST_DELIVERY_FILE_DOWNLOAD_URL"); }
        if (isBlank(apiUrl)) { throw new IllegalStateException("FILE_DOWNLOAD_URL is empty."); }

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
        tempPathRegistrar.register(target.getAbsolutePath(), storedFileName);
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

    private TempDownloadResult copyLocalFileToTemp(
            String objectType, String restSeq, String originalFileName,
            String fileExt, File sourceFile,
            TempPathRegistrar tempPathRegistrar) throws Exception {
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
        tempPathRegistrar.register(target.getAbsolutePath(), storedFileName);
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
        normalized = normalized.trim().toLowerCase(Locale.ROOT);
        return normalized.length() <= 16 && normalized.matches("[a-z0-9]+")
            ? normalized : "";
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    public boolean saveDownloadAudit(Object userObj, DownloadRuntimeState state, String status, String errorMessage) {
        try {
            if (!(userObj instanceof kr.esob.fdms.controller.login.UserVO)) {
                return false;
            }
            kr.esob.fdms.controller.login.UserVO userVo = (kr.esob.fdms.controller.login.UserVO) userObj;
            if (userVo == null || state == null) {
                return false;
            }
            if (state.isActLogSaved()) {
                return true;
            }
            boolean success = "COMPLETED".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status);
            String normalizedStatus = nvl(status).toUpperCase(Locale.ROOT);
            securityAclService.recordDownloadResult(userVo, success ? "SUCCESS" : "FAIL",
                success ? null : "DOWNLOAD_" + normalizedStatus,
                state.getObjectType(), state.getDocSeq(), state.getFileNo(), state.getRequestNo(),
                success ? "Download completed." : "Download failed. status=" + normalizedStatus);
            return true;
        } catch (Exception e) {
            log.warn("[DOWNLOAD-ACT-LOG][SKIP] objectType={}, status={}, cause={}",
                state == null ? null : state.getObjectType(),
                status,
                e.getClass().getSimpleName());
            return false;
        }
    }

    private String normalizeDbObjectType(String objectType) {
        String normalized = nvl(objectType).toUpperCase(Locale.ROOT);
        if ("PRODUCT_DOC".equals(normalized)) {
            normalized = "PRODUCT_DOCUMENT";
        } else if ("PEERREVIEW".equals(normalized)) {
            normalized = "PEER_REVIEW";
        }
        return securityAclService.normalizeObjectType(normalized);
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

    @FunctionalInterface
    public interface TempPathRegistrar {
        void register(String tempFilePath, String savedFileName) throws Exception;
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

    public static class ResolvedDownloadResource {
        private String objectType;
        private String objectId;
        private String fileNo;
        private String requestNo;

        public String getObjectType() { return objectType; }
        public void setObjectType(String objectType) { this.objectType = objectType; }
        public String getObjectId() { return objectId; }
        public void setObjectId(String objectId) { this.objectId = objectId; }
        public String getFileNo() { return fileNo; }
        public void setFileNo(String fileNo) { this.fileNo = fileNo; }
        public String getRequestNo() { return requestNo; }
        public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    }
}

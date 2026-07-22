package kr.esob.fdms.commonlogic.filecache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.springframework.stereotype.Service;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;

@Service
public class FileCacheService {
    
    @Inject
    private FileCacheDao dao;

    @Inject
    private ExternalFileApiClient apiClient;

    public Map<String, Object> getOrFetchOnetimeOfferOriginal(String dataOfferDocSeq) throws Exception {
        return getOrFetchOnetimeOfferFile(dataOfferDocSeq, "ORIGINAL");
    }

    public Map<String, Object> getOrFetchOnetimeOfferPdf(String dataOfferDocSeq) throws Exception {
        return getOrFetchOnetimeOfferFile(dataOfferDocSeq, "PDF");
    }

    public Map<String, Object> getOrFetchOnetimeOfferPdfByFileSeq(String fileSeq) throws Exception {
        return getOrFetchOnetimeOfferFileByFileSeq(fileSeq, "PDF");
    }

    public Map<String, Object> getOrFetchDeliveryConfirmOriginal(String delvyCnfirmDocSeq) throws Exception {
        return getOrFetchDeliveryConfirmFile(delvyCnfirmDocSeq, "ORIGINAL");
    }

    public Map<String, Object> getOrFetchDeliveryConfirmPdf(String delvyCnfirmDocSeq) throws Exception {
        return getOrFetchDeliveryConfirmFile(delvyCnfirmDocSeq, "PDF");
    }

    public Map<String, Object> getOrFetchDeliveryConfirmPdfByFileSeq(String fileSeq) throws Exception {
        return getOrFetchDeliveryConfirmFileByFileSeq(fileSeq, "PDF");
    }

    private Map<String, Object> getOrFetchOnetimeOfferFile(String dataOfferDocSeq, String filePurpose) throws Exception {
        Map<String, Object> cached = normalizeCacheMap(dao.selectLatestOnetimeOfferFile(dataOfferDocSeq, filePurpose));
        String rootPath = resolveCacheRootPath();
        if (isReusableCachedFile(cached, rootPath)) {
            return cached;
        }

        // 시스템설정에서 REST API URL 설정
        String originalApiUrl = SystemConfig.getSystemConfigValue("KAI_DOWNLOAD");
        String pdfApiUrl = SystemConfig.getSystemConfigValue("KAI_VIEW");
        // backward compatibility
        if (originalApiUrl == null || "".equals(originalApiUrl)) {
            originalApiUrl = SystemConfig.getSystemConfigValue("REST_DELIVERY_FILE_DOWNLOAD_URL");
        }
        if (pdfApiUrl == null || "".equals(pdfApiUrl)) {
            pdfApiUrl = SystemConfig.getSystemConfigValue("REST_DELIVERY_FILE_VIEW_URL");
        }
        if (originalApiUrl == null || "".equals(originalApiUrl)) {
            originalApiUrl = SystemConfig.getSystemConfigValue("REST_ORIGINAL_BY_SEQ_URL");
        }
        if (pdfApiUrl == null || "".equals(pdfApiUrl)) {
            pdfApiUrl = SystemConfig.getSystemConfigValue("REST_PDF_BY_SEQ_URL");
        }

        String fileSeq = cached == null ? null : getMapString(cached, "fileSeq", "fileseq", "FILE_SEQ");
        if (fileSeq == null || fileSeq.trim().isEmpty()) {
            throw new IllegalStateException("FILE_SEQ not found for DATA_OFFER_DOC_SEQ=" + dataOfferDocSeq + ", filePurpose=" + filePurpose);
        }

        byte[] bytes;
        if ("PDF".equals(filePurpose)) { bytes = apiClient.requestPdfBySeq(pdfApiUrl, fileSeq);
        } else {bytes = apiClient.requestOriginalBySeq(originalApiUrl, fileSeq);}

        if (bytes == null || bytes.length == 0) {throw new IllegalStateException("REST API returned empty file bytes");}

        String ext = "PDF".equals(filePurpose) ? "pdf" : "bin";
        String pdfFullPath = rootPath + File.separator + buildViewerFileName("DOC", dataOfferDocSeq, fileSeq, ext);
        String esobRootPath = resolveViewerEsobRootPath();
        String esobFullPath = esobRootPath + File.separator + buildViewerEsobFileName(dataOfferDocSeq);

        File pdfTarget = new File(pdfFullPath);
        File pdfParent = pdfTarget.getParentFile();
        if (pdfParent != null && !pdfParent.exists()) {pdfParent.mkdirs();}

        File esobTarget = new File(esobFullPath);
        File esobParent = esobTarget.getParentFile();
        if (esobParent != null && !esobParent.exists()) {esobParent.mkdirs();}

        try (FileOutputStream fos = new FileOutputStream(pdfTarget)) {
            fos.write(bytes);
            fos.flush();
        }

        try (FileOutputStream fos = new FileOutputStream(esobTarget)) {
            fos.write(bytes);
            fos.flush();
        }

        if (pdfTarget.exists() && !pdfTarget.delete()) {
            // 실패해도 서비스 동작은 유지하되 로그로만 남김
            System.err.println("[FILE-CACHE] temp pdf delete failed: " + pdfTarget.getAbsolutePath());
        }

        Map<String, Object> update = new HashMap<>();
        update.put("fileSeq", fileSeq);
        update.put("viewerEsobPathNm", esobFullPath);
        update.put("updateUserCd", "SYSTEM");
        dao.updateOnetimeOfferViewerPaths(update);

        return normalizeCacheMap(dao.selectLatestOnetimeOfferFile(dataOfferDocSeq, filePurpose));
    }

    private Map<String, Object> getOrFetchDeliveryConfirmFile(String delvyCnfirmDocSeq, String filePurpose) throws Exception {
        Map<String, Object> cached = normalizeCacheMap(dao.selectLatestDeliveryConfirmFile(delvyCnfirmDocSeq, filePurpose));
        String rootPath = resolveCacheRootPath();
        if (isReusableCachedFile(cached, rootPath)) {
            return cached;
        }

        String originalApiUrl = SystemConfig.getSystemConfigValue("KAI_DOWNLOAD");
        String pdfApiUrl = SystemConfig.getSystemConfigValue("KAI_VIEW");
        if (originalApiUrl == null || "".equals(originalApiUrl)) {
            originalApiUrl = SystemConfig.getSystemConfigValue("REST_DELIVERY_FILE_DOWNLOAD_URL");
        }
        if (pdfApiUrl == null || "".equals(pdfApiUrl)) {
            pdfApiUrl = SystemConfig.getSystemConfigValue("REST_DELIVERY_FILE_VIEW_URL");
        }
        if (originalApiUrl == null || "".equals(originalApiUrl)) {
            originalApiUrl = SystemConfig.getSystemConfigValue("REST_ORIGINAL_BY_SEQ_URL");
        }
        if (pdfApiUrl == null || "".equals(pdfApiUrl)) {
            pdfApiUrl = SystemConfig.getSystemConfigValue("REST_PDF_BY_SEQ_URL");
        }

        String fileSeq = cached == null ? null : getMapString(cached, "fileSeq", "fileseq", "FILE_SEQ");
        if (fileSeq == null || fileSeq.trim().isEmpty()) {
            throw new IllegalStateException("FILE_SEQ not found for DELVY_CNFIRM_DOC_SEQ=" + delvyCnfirmDocSeq + ", filePurpose=" + filePurpose);
        }

        byte[] bytes;
        if ("PDF".equals(filePurpose)) { bytes = apiClient.requestPdfBySeq(pdfApiUrl, fileSeq);
        } else {bytes = apiClient.requestOriginalBySeq(originalApiUrl, fileSeq);}

        if (bytes == null || bytes.length == 0) {throw new IllegalStateException("REST API returned empty file bytes");}

        String ext = "PDF".equals(filePurpose) ? "pdf" : "bin";
        String pdfFullPath = rootPath + File.separator + buildViewerFileName("DRAWING", delvyCnfirmDocSeq, fileSeq, ext);
        String esobRootPath = resolveViewerEsobRootPath();
        String esobFullPath = esobRootPath + File.separator + buildViewerEsobFileName(delvyCnfirmDocSeq);

        File pdfTarget = new File(pdfFullPath);
        File pdfParent = pdfTarget.getParentFile();
        if (pdfParent != null && !pdfParent.exists()) {pdfParent.mkdirs();}

        File esobTarget = new File(esobFullPath);
        File esobParent = esobTarget.getParentFile();
        if (esobParent != null && !esobParent.exists()) {esobParent.mkdirs();}

        try (FileOutputStream fos = new FileOutputStream(pdfTarget)) {
            fos.write(bytes);
            fos.flush();
        }

        try (FileOutputStream fos = new FileOutputStream(esobTarget)) {
            fos.write(bytes);
            fos.flush();
        }

        if (pdfTarget.exists() && !pdfTarget.delete()) {
            // 실패해도 서비스 동작은 유지하되 로그로만 남김
            System.err.println("[FILE-CACHE] temp pdf delete failed: " + pdfTarget.getAbsolutePath());
        }

        Map<String, Object> update = new HashMap<>();
        update.put("fileSeq", fileSeq);
        update.put("viewerEsobPathNm", esobFullPath);
        update.put("updateUserCd", "SYSTEM");
        dao.updateDeliveryConfirmViewerPaths(update);

        return normalizeCacheMap(dao.selectLatestDeliveryConfirmFile(delvyCnfirmDocSeq, filePurpose));
    }

    private Map<String, Object> getOrFetchOnetimeOfferFileByFileSeq(String fileSeq, String filePurpose) throws Exception {
        Map<String, Object> cached = normalizeCacheMap(dao.selectOnetimeOfferFileByFileSeq(fileSeq));
        return getOrFetchCachedFileByFileSeq(cached, fileSeq, filePurpose, "DOC");
    }

    private Map<String, Object> getOrFetchDeliveryConfirmFileByFileSeq(String fileSeq, String filePurpose) throws Exception {
        Map<String, Object> cached = normalizeCacheMap(dao.selectDeliveryConfirmFileByFileSeq(fileSeq));
        return getOrFetchCachedFileByFileSeq(cached, fileSeq, filePurpose, "DRAWING");
    }

    private Map<String, Object> getOrFetchCachedFileByFileSeq(Map<String, Object> cached, String fileSeq, String filePurpose, String objectType) throws Exception {
        String rootPath = resolveCacheRootPath();
        String resolvedFileSeq = cached == null ? null : getMapString(cached, "fileSeq", "fileseq", "FILE_SEQ");
        if (resolvedFileSeq == null || resolvedFileSeq.trim().isEmpty()) {
            throw new IllegalStateException("FILE_SEQ not found. fileSeq=" + fileSeq + ", objectType=" + objectType);
        }
        if (isReusableCachedFileByFileSeq(cached, rootPath, resolvedFileSeq)) {
            return cached;
        }

        String docSeqKey = "DOC".equals(objectType) ? "dataOfferDocSeq" : "delvyCnfirmDocSeq";
        String docSeq = cached == null ? null : getMapString(cached, docSeqKey);
        if (docSeq == null || docSeq.trim().isEmpty()) {
            throw new IllegalStateException("DOC_SEQ not found. fileSeq=" + fileSeq + ", objectType=" + objectType);
        }

        String originalApiUrl = SystemConfig.getSystemConfigValue("KAI_DOWNLOAD");
        String pdfApiUrl = SystemConfig.getSystemConfigValue("KAI_VIEW");
        if (originalApiUrl == null || "".equals(originalApiUrl)) {
            originalApiUrl = SystemConfig.getSystemConfigValue("REST_DELIVERY_FILE_DOWNLOAD_URL");
        }
        if (pdfApiUrl == null || "".equals(pdfApiUrl)) {
            pdfApiUrl = SystemConfig.getSystemConfigValue("REST_DELIVERY_FILE_VIEW_URL");
        }
        if (originalApiUrl == null || "".equals(originalApiUrl)) {
            originalApiUrl = SystemConfig.getSystemConfigValue("REST_ORIGINAL_BY_SEQ_URL");
        }
        if (pdfApiUrl == null || "".equals(pdfApiUrl)) {
            pdfApiUrl = SystemConfig.getSystemConfigValue("REST_PDF_BY_SEQ_URL");
        }

        byte[] bytes;
        if ("PDF".equals(filePurpose)) {
            bytes = apiClient.requestPdfBySeq(pdfApiUrl, resolvedFileSeq);
        } else {
            bytes = apiClient.requestOriginalBySeq(originalApiUrl, resolvedFileSeq);
        }
        if (bytes == null || bytes.length == 0) {
            throw new IllegalStateException("REST API returned empty file bytes");
        }

        String ext = "PDF".equals(filePurpose) ? "pdf" : "bin";
        String pdfFullPath = rootPath + File.separator + buildViewerFileName(objectType, docSeq, resolvedFileSeq, ext);
        String esobRootPath = resolveViewerEsobRootPath();
        String esobFullPath = esobRootPath + File.separator + buildViewerEsobFileName(resolvedFileSeq);

        File pdfTarget = new File(pdfFullPath);
        File pdfParent = pdfTarget.getParentFile();
        if (pdfParent != null && !pdfParent.exists()) {pdfParent.mkdirs();}

        File esobTarget = new File(esobFullPath);
        File esobParent = esobTarget.getParentFile();
        if (esobParent != null && !esobParent.exists()) {esobParent.mkdirs();}

        try (FileOutputStream fos = new FileOutputStream(pdfTarget)) {
            fos.write(bytes);
            fos.flush();
        }

        try (FileOutputStream fos = new FileOutputStream(esobTarget)) {
            fos.write(bytes);
            fos.flush();
        }

        if (pdfTarget.exists() && !pdfTarget.delete()) {
            System.err.println("[FILE-CACHE] temp pdf delete failed: " + pdfTarget.getAbsolutePath());
        }

        Map<String, Object> update = new HashMap<>();
        update.put("fileSeq", resolvedFileSeq);
        update.put("viewerEsobPathNm", esobFullPath);
        update.put("updateUserCd", "SYSTEM");
        if ("DOC".equals(objectType)) {
            dao.updateOnetimeOfferViewerPaths(update);
            return normalizeCacheMap(dao.selectOnetimeOfferFileByFileSeq(resolvedFileSeq));
        }

        dao.updateDeliveryConfirmViewerPaths(update);
        return normalizeCacheMap(dao.selectDeliveryConfirmFileByFileSeq(resolvedFileSeq));
    }

    private String getMapString(Map<String, Object> map, String... keys) {
        if (map == null || keys == null) {return null;}
        for (String key : keys) {
            if (key == null) {continue;}
            Object value = map.get(key);
            if (value != null) {return String.valueOf(value);}
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {continue;}
            String entryNorm = normalizeKey(entry.getKey());
            for (String key : keys) {
                if (key == null) {continue;}
                if (entryNorm.equals(normalizeKey(key))) {
                    return String.valueOf(entry.getValue());
                }
            }
        }
        return null;
    }

    private Map<String, Object> normalizeCacheMap(Map<String, Object> map) {
        if (map == null) {return null;}
        Map<String, Object> normalized = new LinkedHashMap<>(map);
        putAliasIfPresent(normalized, "fileSeq", "fileseq", "FILE_SEQ", "FILESEQ");
        putAliasIfPresent(normalized, "filePathNm", "filepathnm", "FILE_PATH_NM", "FILEPATHNM");
        putAliasIfPresent(normalized, "orgFileNm", "orgfilenm", "ORG_FILE_NM", "ORGFILENM");
        putAliasIfPresent(normalized, "storedFileNm", "storedfilenm", "STORED_FILE_NM", "STOREDFILENM");
        // putAliasIfPresent(normalized, "filePurpose", "filepurpose", "FILE_PURPOSE", "FILEPURPOSE");
        return normalized;
    }

    private void putAliasIfPresent(Map<String, Object> map, String targetKey, String... candidateKeys) {
        if (map == null || targetKey == null) {return;}
        if (map.get(targetKey) != null) {return;}
        String value = getMapString(map, candidateKeys);
        if (value != null) {
            map.put(targetKey, value);
        }
    }

    private String normalizeKey(String key) {
        if (key == null) {return "";}
        return key.replace("_", "").toLowerCase();
    }

    private String resolveCacheRootPath() {
        String rootPath = SystemConfig.getSystemConfigValue("VIEWER_CACHE_PATH");
        if (rootPath == null || rootPath.trim().isEmpty()) {
            rootPath = SystemConfig.getSystemConfigValue("UPDOWN_PATH");
        }
        if (rootPath == null || rootPath.trim().isEmpty()) {
            rootPath = "C:/workspaceStartupHub/OUT/Viewer";
        }
        return rootPath;
    }

    private boolean isReusableCachedFile(Map<String, Object> cached, String rootPath) {
        String esobPath = getMapString(cached, "viewerEsobPathNm", "VIEWER_ESOB_PATH_NM");
        if (esobPath == null || esobPath.trim().isEmpty()) return false;

        File target = new File(esobPath);
        if (!target.exists() || !target.isFile() || target.length() == 0L) return false;

        return isPathUnderRoot(target, resolveViewerEsobRootPath());
    }

    private boolean isPathUnderRoot(File target, String rootPath) {
        if (rootPath == null || rootPath.trim().isEmpty()) {
            return true;
        }
        try {
            File root = new File(rootPath);
            String rootCanonical = root.getCanonicalPath();
            String targetCanonical = target.getCanonicalPath();
            if (targetCanonical.equals(rootCanonical)) {
                return true;
            }
            String rootWithSep = rootCanonical.endsWith(File.separator) ? rootCanonical : rootCanonical + File.separator;
            return targetCanonical.startsWith(rootWithSep);
        } catch (IOException e) {
            return false;
        }
    }

    private String buildViewerFileName(String objectType, String docSeq, String fileSeq, String ext) {
        String safeObjectType = objectType == null ? "OBJ" : objectType;
        String safeDocSeq = sanitizePathSegment(docSeq);
        String safeFileSeq = sanitizePathSegment(fileSeq);
        String safeExt = sanitizePathSegment(ext);
        return safeObjectType + "_" + safeDocSeq + "_" + safeFileSeq + "." + safeExt;
    }

    private String sanitizePathSegment(String src) {
        if (src == null || src.trim().isEmpty()) {
            return "UNKNOWN";
        }
        return src.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String resolveViewerEsobRootPath() {
        String root = SystemConfig.getSystemConfigValue("ADAP_PDF_PATH");
        if (root == null || root.trim().isEmpty()) {
            // 뷰어 public/out/destfile 실제 경로로 맞춰주세요
            root = "C:/workspaceStartupHub/vServer/pdfview_express/public/OUT/destFile";
        }
        return root;
    }

    private String buildViewerEsobFileName(String objectId) {
        return sanitizePathSegment(objectId) + ".esob";
    }

    private boolean isReusableCachedFileByFileSeq(Map<String, Object> cached, String rootPath, String fileSeq) {
        if (!isReusableCachedFile(cached, rootPath)) {
            return false;
        }
        String esobPath = getMapString(cached, "viewerEsobPathNm", "VIEWER_ESOB_PATH_NM");
        if (esobPath == null || esobPath.trim().isEmpty()) {
            return false;
        }
        return buildViewerEsobFileName(fileSeq).equalsIgnoreCase(new File(esobPath).getName());
    }
}

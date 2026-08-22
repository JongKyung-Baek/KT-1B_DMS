package kr.esob.tdms.commonlogic.pdfconversion;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import kr.esob.tdms.controller.general.distribution.swrequest.TechnicalFileTypePolicy;

@Service
public class PdfConversionQueueService {
    private final PdfConversionDao conversionDao;
    private final PdfConversionProjectionDao projectionDao;
    private final PdfConversionSourceStore sourceStore;
    private final PdfConversionProperties properties;

    public PdfConversionQueueService(PdfConversionDao conversionDao,
                                     PdfConversionProjectionDao projectionDao,
                                     PdfConversionSourceStore sourceStore,
                                     PdfConversionProperties properties) {
        this.conversionDao = conversionDao;
        this.projectionDao = projectionDao;
        this.sourceStore = sourceStore;
        this.properties = properties;
    }

    public PdfConversionJob enqueueUpload(String objectType,
                                          String objectId,
                                          String fileNo,
                                          String originalFileName,
                                          String storedPath,
                                          MultipartFile upload) {
        requireViewerProcessable(originalFileName);
        if (upload == null || upload.isEmpty()) {
            throw new IllegalArgumentException("Conversion upload is empty.");
        }
        try (InputStream input = upload.getInputStream()) {
            String sourceHash = PdfConversionCrypto.sha256(input);
            return enqueue(objectType, objectId, fileNo, originalFileName,
                    storedPath, Long.valueOf(upload.getSize()), sourceHash);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to queue the PDF conversion upload.", exception);
        }
    }

    public PdfConversionJob enqueueStored(String objectType,
                                          String objectId,
                                          String fileNo,
                                          String storedPath,
                                          String originalFileName) {
        requireViewerProcessable(originalFileName);
        java.nio.file.Path source = sourceStore.materialize(
                storedPath, PdfConversionSourceStore.extensionSuffix(originalFileName));
        try {
            return enqueue(objectType, objectId, fileNo, originalFileName,
                    storedPath, Long.valueOf(java.nio.file.Files.size(source)),
                    PdfConversionCrypto.sha256(source));
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to queue the stored PDF conversion.", exception);
        } finally {
            PdfConversionSourceStore.deleteQuietly(source);
        }
    }

    public PdfConversionJob findCurrent(String objectType, String objectId, String fileNo) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("objectType", requireText(objectType, "object type"));
        params.put("objectId", requireText(objectId, "object identifier"));
        params.put("fileNo", requireText(fileNo, "file number"));
        return conversionDao.selectCurrent(params);
    }

    private PdfConversionJob enqueue(String objectType,
                                     String objectId,
                                     String fileNo,
                                     String originalFileName,
                                     String storedPath,
                                     Long sourceSize,
                                     String sourceHash) {
        PdfConversionJob requested = new PdfConversionJob();
        requested.setConversionId(UUID.randomUUID().toString());
        requested.setObjectType(requireText(objectType, "object type").toUpperCase());
        requested.setObjectId(requireText(objectId, "object identifier"));
        requested.setFileNo(requireText(fileNo, "file number"));
        requested.setSourceFileName(requireText(originalFileName, "source file name"));
        requested.setSourceFilePath(requireText(storedPath, "source file path"));
        requested.setSourceSizeBytes(sourceSize);
        requested.setSourceSha256(requireSha256(sourceHash));
        requested.setMaxAttempts(Math.max(1, properties.getMaxAttempts()));

        PdfConversionJob queued = conversionDao.enqueue(requested);
        if (queued == null) {
            throw new IllegalStateException("PDF conversion queue did not return a job.");
        }
        if (TechnicalFileTypePolicy.isPdf(originalFileName)
                || TechnicalFileTypePolicy.isStep(originalFileName)) {
            Map<String, Object> ready = identity(queued);
            ready.put("outputFileName", originalFileName);
            ready.put("outputFilePath", storedPath);
            ready.put("outputSizeBytes", sourceSize);
            ready.put("outputSha256", sourceHash);
            conversionDao.markNotRequired(ready);
            queued = conversionDao.selectById(queued.getConversionId());
        }
        project(queued);
        return queued;
    }

    void project(PdfConversionJob job) {
        if (job == null) {
            return;
        }
        Map<String, Object> projection = new HashMap<String, Object>();
        projection.put("conversionId", job.getConversionId());
        projection.put("objectType", job.getObjectType());
        projection.put("objectId", job.getObjectId());
        projection.put("fileNo", job.getFileNo());
        projection.put("status", projectionStatus(job.getStatus()));
        projection.put("lastError", safeError(job.getLastError()));
        projectionDao.updateStatus(projection);
    }

    void reconcileCurrentProjections() {
        projectionDao.reconcileCurrentSw();
        projectionDao.reconcileCurrentSwSub();
    }

    private String projectionStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if ("SUCCEEDED".equals(normalized) || "NOT_REQUIRED".equals(normalized)) {
            return "DONE";
        }
        if ("FAILED".equals(normalized)) {
            return "FAIL";
        }
        if ("PROCESSING".equals(normalized)) {
            return "PROCESSING";
        }
        return "PENDING";
    }

    private Map<String, Object> identity(PdfConversionJob job) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("conversionId", job.getConversionId());
        values.put("claimToken", UUID.randomUUID().toString());
        return values;
    }

    private String requireText(String value, String label) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("PDF conversion " + label + " is required.");
        }
        return normalized;
    }

    private String requireSha256(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase();
        if (!normalized.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("PDF conversion source SHA-256 is invalid.");
        }
        return normalized;
    }

    private void requireViewerProcessable(String originalFileName) {
        if (!TechnicalFileTypePolicy.isViewerProcessable(originalFileName)) {
            throw new IllegalArgumentException(
                    "PDF conversion is not available for this file extension.");
        }
    }

    private String safeError(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replaceAll("[\\r\\n\\p{Cntrl}]", " ").trim();
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500);
    }
}

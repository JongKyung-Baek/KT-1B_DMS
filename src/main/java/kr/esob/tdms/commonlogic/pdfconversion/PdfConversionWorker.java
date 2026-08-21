package kr.esob.tdms.commonlogic.pdfconversion;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PdfConversionWorker {
    private static final Logger log = LoggerFactory.getLogger(PdfConversionWorker.class);

    private final PdfConversionDao conversionDao;
    private final PdfConversionQueueService queueService;
    private final PdfConversionSourceStore sourceStore;
    private final PdfConversionClient client;
    private final PdfConversionProperties properties;
    private ExecutorService executor;
    private Semaphore workerSlots;

    public PdfConversionWorker(PdfConversionDao conversionDao,
                               PdfConversionQueueService queueService,
                               PdfConversionSourceStore sourceStore,
                               PdfConversionClient client,
                               PdfConversionProperties properties) {
        this.conversionDao = conversionDao;
        this.queueService = queueService;
        this.sourceStore = sourceStore;
        this.client = client;
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() {
        int threads = Math.max(1, Math.min(properties.getWorkerThreads(), 8));
        workerSlots = new Semaphore(threads);
        executor = Executors.newFixedThreadPool(threads, new ThreadFactory() {
            private int number;

            @Override
            public synchronized Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "tdms-pdf-conversion-" + (++number));
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    @Scheduled(fixedDelayString = "${tdms.pdf-conversion.poll-interval-ms:3000}")
    public void poll() {
        if (!properties.isEnabled() || executor == null) {
            return;
        }
        properties.requireOutboundConfiguration();
        List<PdfConversionJob> exhausted = conversionDao.failExpiredExhausted();
        if (exhausted != null) {
            for (PdfConversionJob failed : exhausted) {
                projectSafely(failed, "expired exhausted job");
            }
        }
        try {
            queueService.reconcileCurrentProjections();
        } catch (RuntimeException exception) {
            log.warn("PDF conversion projection reconciliation failed.", exception);
        }
        List<String> dueIds = conversionDao.selectDueIds(
                Math.max(1, Math.min(properties.getBatchSize(), 20)));
        for (String conversionId : dueIds) {
            if (workerSlots == null || !workerSlots.tryAcquire()) {
                break;
            }
            PdfConversionJob claimed = null;
            try {
                claimed = claim(conversionId);
                if (claimed == null) {
                    workerSlots.release();
                    continue;
                }
                projectSafely(claimed, "claimed job");
                PdfConversionJob submitted = claimed;
                executor.execute(() -> {
                    try {
                        process(submitted);
                    } finally {
                        workerSlots.release();
                    }
                });
            } catch (RuntimeException exception) {
                workerSlots.release();
                log.warn("PDF conversion job submission failed. conversionId={}",
                        conversionId, exception);
            }
        }
    }

    private PdfConversionJob claim(String conversionId) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("conversionId", conversionId);
        values.put("claimToken", UUID.randomUUID().toString());
        int configuredLease = Math.max(60, properties.getStaleProcessingMinutes() * 60);
        int requestLease = Math.max(60, properties.getReadTimeoutMs() / 1000 + 60);
        values.put("leaseSeconds", Integer.valueOf(Math.max(configuredLease, requestLease)));
        return conversionDao.claim(values);
    }

    private void process(PdfConversionJob job) {
        Path source = null;
        Path pdf = null;
        try {
            source = sourceStore.materialize(job.getSourceFilePath(),
                    PdfConversionSourceStore.extensionSuffix(job.getSourceFileName()));
            String actualSourceHash = PdfConversionCrypto.sha256(source);
            if (!actualSourceHash.equalsIgnoreCase(job.getSourceSha256())) {
                throw new PermanentConversionException("Stored source hash no longer matches the queued file.");
            }

            PdfConversionJob reusable = findReusable(job);
            if (reusable != null) {
                pdf = materializeReusable(reusable);
                if (pdf != null) {
                    // The cached bytes belong to the current source hash, but the
                    // display name belongs to the current document.  Reusing the
                    // first job's name would leak unrelated document metadata.
                    complete(job, baseName(job.getSourceFileName()) + ".pdf",
                            reusable.getOutputFilePath(), Long.valueOf(Files.size(pdf)),
                            PdfConversionCrypto.sha256(pdf));
                    return;
                }
            }

            pdf = sourceStore.createWorkFile(".pdf");
            // A content-stable key lets the converter coalesce the same source
            // even when two different TDMS document rows are queued together.
            String idempotencyKey = "tdms:sha256:" + job.getSourceSha256();
            PdfConversionClientResult result = client.convert(
                    source, job.getSourceFileName(), job.getSourceSha256(), idempotencyKey, pdf);
            PdfConversionFiles.requirePdf(pdf);
            String outputHash = PdfConversionCrypto.sha256(pdf);
            String outputPath = sourceStore.saveConvertedPdf(pdf, job.getSourceSha256());
            String outputName = baseName(job.getSourceFileName()) + ".pdf";
            complete(job, outputName, outputPath, Long.valueOf(Files.size(pdf)), outputHash);
            log.info("PDF conversion completed. conversionId={}, converterReused={}",
                    job.getConversionId(), result.isReused());
        } catch (PermanentConversionException exception) {
            fail(job, exception.getMessage());
        } catch (Exception exception) {
            retry(job, safeError(exception));
        } finally {
            PdfConversionSourceStore.deleteQuietly(source);
            PdfConversionSourceStore.deleteQuietly(pdf);
        }
    }

    private PdfConversionJob findReusable(PdfConversionJob job) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceSha256", job.getSourceSha256());
        PdfConversionJob reusable = conversionDao.selectReusableByHash(params);
        return reusable != null && !job.getConversionId().equals(reusable.getConversionId())
                ? reusable : null;
    }

    private Path materializeReusable(PdfConversionJob reusable) {
        Path candidate = null;
        try {
            candidate = sourceStore.materialize(reusable.getOutputFilePath(), ".pdf");
            PdfConversionFiles.requirePdf(candidate);
            String hash = PdfConversionCrypto.sha256(candidate);
            if (!hash.equalsIgnoreCase(reusable.getOutputSha256())) {
                PdfConversionSourceStore.deleteQuietly(candidate);
                return null;
            }
            return candidate;
        } catch (Exception exception) {
            PdfConversionSourceStore.deleteQuietly(candidate);
            log.warn("Reusable PDF was unavailable. conversionId={}", reusable.getConversionId());
            return null;
        }
    }

    private void complete(PdfConversionJob job,
                          String outputFileName,
                          String outputFilePath,
                          Long outputSizeBytes,
                          String outputSha256) {
        Map<String, Object> values = identity(job);
        values.put("outputFileName", outputFileName);
        values.put("outputFilePath", outputFilePath);
        values.put("outputSizeBytes", outputSizeBytes);
        values.put("outputSha256", outputSha256);
        int updated = conversionDao.markSucceeded(values);
        projectAfterFencedUpdate(job, updated, "successful job");
    }

    private void retry(PdfConversionJob job, String error) {
        Map<String, Object> values = identity(job);
        values.put("retryDelaySeconds", Integer.valueOf(
                Math.max(0, properties.getRetryDelaySeconds())));
        values.put("lastError", error);
        int affected = conversionDao.markRetry(values);
        PdfConversionJob updated = conversionDao.selectById(job.getConversionId());
        if (affected == 1) {
            projectSafely(updated, "retry job");
        } else {
            projectCurrentSafely(job, "stale retry job");
        }
        log.warn("PDF conversion attempt failed. conversionId={}, status={}, attempt={}",
                job.getConversionId(), updated == null ? "UNKNOWN" : updated.getStatus(),
                job.getAttemptCount());
    }

    private void fail(PdfConversionJob job, String error) {
        Map<String, Object> values = identity(job);
        values.put("lastError", error);
        int updated = conversionDao.markFailed(values);
        projectAfterFencedUpdate(job, updated, "failed job");
        log.warn("PDF conversion permanently failed. conversionId={}", job.getConversionId());
    }

    private void projectAfterFencedUpdate(PdfConversionJob job, int affected, String context) {
        if (affected == 1) {
            projectSafely(conversionDao.selectById(job.getConversionId()), context);
        } else {
            projectCurrentSafely(job, "stale " + context);
        }
    }

    private void projectCurrentSafely(PdfConversionJob job, String context) {
        try {
            PdfConversionJob current = queueService.findCurrent(
                    job.getObjectType(), job.getObjectId(), job.getFileNo());
            projectSafely(current, context);
        } catch (RuntimeException exception) {
            log.warn("PDF conversion current projection lookup failed. conversionId={}, context={}",
                    job.getConversionId(), context, exception);
        }
    }

    private void projectSafely(PdfConversionJob job, String context) {
        if (job == null) {
            return;
        }
        try {
            queueService.project(job);
        } catch (RuntimeException exception) {
            log.warn("PDF conversion status projection failed. conversionId={}, context={}",
                    job.getConversionId(), context, exception);
        }
    }

    private Map<String, Object> identity(PdfConversionJob job) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("conversionId", job.getConversionId());
        values.put("claimToken", job.getClaimToken());
        return values;
    }

    private String baseName(String fileName) {
        String value = fileName == null ? "document" : fileName.trim();
        int dot = value.lastIndexOf('.');
        return dot > 0 ? value.substring(0, dot) : value;
    }

    private String safeError(Exception exception) {
        String message = exception.getMessage();
        String value = exception.getClass().getSimpleName()
                + (message == null || message.trim().isEmpty() ? "" : ": " + message.trim());
        value = value.replaceAll("[\\r\\n\\p{Cntrl}]", " ");
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    private static final class PermanentConversionException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private PermanentConversionException(String message) {
            super(message);
        }
    }
}

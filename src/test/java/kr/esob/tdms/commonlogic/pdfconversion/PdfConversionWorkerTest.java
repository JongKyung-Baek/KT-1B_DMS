package kr.esob.tdms.commonlogic.pdfconversion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class PdfConversionWorkerTest {
    private static final byte[] VALID_PDF =
            "%PDF-1.4\n1 0 obj\n<<>>\nendobj\n%%EOF\n"
                    .getBytes(StandardCharsets.US_ASCII);

    @TempDir
    Path tempDir;

    @Test
    void validReusablePdfAndMatchingHashCompleteWithoutCallingConverter() throws Exception {
        Fixture fixture = fixture();
        byte[] sourceBytes = "same source document".getBytes(StandardCharsets.UTF_8);
        Path source = write("current.docx", sourceBytes);
        Path reusablePdf = write("reusable.pdf", VALID_PDF);
        PdfConversionJob job = processingJob(
                "JOB-CURRENT", "current-copy.docx", PdfConversionCrypto.sha256(source));
        PdfConversionJob reusable = new PdfConversionJob();
        reusable.setConversionId("JOB-REUSABLE");
        reusable.setOutputFileName("unrelated-first-name.pdf");
        reusable.setOutputFilePath("CONVERTED_PDF/reusable.pdf");
        reusable.setOutputSizeBytes(Long.valueOf(999L));
        reusable.setOutputSha256(PdfConversionCrypto.sha256(reusablePdf));
        PdfConversionJob completed = completed(job, "SUCCEEDED");

        when(fixture.sourceStore.materialize(job.getSourceFilePath(), ".docx"))
                .thenReturn(source);
        when(fixture.dao.selectReusableByHash(anyMap())).thenReturn(reusable);
        when(fixture.sourceStore.materialize(reusable.getOutputFilePath(), ".pdf"))
                .thenReturn(reusablePdf);
        when(fixture.dao.markSucceeded(anyMap())).thenReturn(1);
        when(fixture.dao.selectById(job.getConversionId())).thenReturn(completed);

        process(fixture.worker, job);

        verify(fixture.client, never()).convert(
                any(Path.class), anyString(), anyString(), anyString(), any(Path.class));
        verify(fixture.sourceStore, never()).saveConvertedPdf(any(Path.class), anyString());
        ArgumentCaptor<Map<String, Object>> success = mapCaptor();
        verify(fixture.dao).markSucceeded(success.capture());
        assertThat(success.getValue())
                .containsEntry("conversionId", job.getConversionId())
                .containsEntry("claimToken", job.getClaimToken())
                .containsEntry("outputFileName", "current-copy.pdf")
                .containsEntry("outputFilePath", reusable.getOutputFilePath())
                .containsEntry("outputSizeBytes", Long.valueOf(VALID_PDF.length))
                .containsEntry("outputSha256", PdfConversionCrypto.sha256(
                        new java.io.ByteArrayInputStream(VALID_PDF)));
        verify(fixture.queueService).project(completed);
    }

    @Test
    void successfulConversionStoresPdfAndProjectsSucceededStatus() throws Exception {
        Fixture fixture = fixture();
        Path source = write("drawing.docx", "office source".getBytes(StandardCharsets.UTF_8));
        Path target = tempDir.resolve("worker-output.pdf");
        PdfConversionJob job = processingJob(
                "JOB-SUCCESS", "drawing.docx", PdfConversionCrypto.sha256(source));
        PdfConversionJob completed = completed(job, "SUCCEEDED");

        when(fixture.sourceStore.materialize(job.getSourceFilePath(), ".docx"))
                .thenReturn(source);
        when(fixture.dao.selectReusableByHash(anyMap())).thenReturn(null);
        when(fixture.sourceStore.createWorkFile(".pdf")).thenReturn(target);
        when(fixture.client.convert(
                eq(source), eq("drawing.docx"), eq(job.getSourceSha256()),
                eq("tdms:sha256:" + job.getSourceSha256()), eq(target)))
                .thenAnswer(invocation -> {
                    Files.write(target, VALID_PDF);
                    return new PdfConversionClientResult(false);
                });
        when(fixture.sourceStore.saveConvertedPdf(target, job.getSourceSha256()))
                .thenReturn("CONVERTED_PDF/" + job.getSourceSha256() + ".pdf");
        when(fixture.dao.markSucceeded(anyMap())).thenReturn(1);
        when(fixture.dao.selectById(job.getConversionId())).thenReturn(completed);

        process(fixture.worker, job);

        verify(fixture.client).convert(
                eq(source), eq("drawing.docx"), eq(job.getSourceSha256()),
                eq("tdms:sha256:" + job.getSourceSha256()), eq(target));
        ArgumentCaptor<Map<String, Object>> success = mapCaptor();
        verify(fixture.dao).markSucceeded(success.capture());
        assertThat(success.getValue())
                .containsEntry("outputFileName", "drawing.pdf")
                .containsEntry("outputFilePath",
                        "CONVERTED_PDF/" + job.getSourceSha256() + ".pdf")
                .containsEntry("outputSizeBytes", Long.valueOf(VALID_PDF.length))
                .containsEntry("outputSha256", PdfConversionCrypto.sha256(
                        new java.io.ByteArrayInputStream(VALID_PDF)));
        verify(fixture.queueService).project(completed);
    }

    @Test
    void transientFailureIsReturnedToPendingWithRetryDelay() throws Exception {
        assertConverterFailureProjection("PENDING");
    }

    @Test
    void exhaustedFailureIsProjectedAsTerminalFailed() throws Exception {
        assertConverterFailureProjection("FAILED");
    }

    @Test
    void changedStoredSourceHashFailsPermanentlyWithoutCallingConverter() throws Exception {
        Fixture fixture = fixture();
        Path source = write("changed.docx", "changed bytes".getBytes(StandardCharsets.UTF_8));
        PdfConversionJob job = processingJob(
                "JOB-HASH-MISMATCH", "changed.docx", repeat('a', 64));
        PdfConversionJob failed = completed(job, "FAILED");

        when(fixture.sourceStore.materialize(job.getSourceFilePath(), ".docx"))
                .thenReturn(source);
        when(fixture.dao.markFailed(anyMap())).thenReturn(1);
        when(fixture.dao.selectById(job.getConversionId())).thenReturn(failed);

        process(fixture.worker, job);

        verify(fixture.client, never()).convert(
                any(Path.class), anyString(), anyString(), anyString(), any(Path.class));
        verify(fixture.dao, never()).markRetry(anyMap());
        ArgumentCaptor<Map<String, Object>> terminal = mapCaptor();
        verify(fixture.dao).markFailed(terminal.capture());
        assertThat(terminal.getValue().get("lastError").toString())
                .contains("source hash no longer matches");
        verify(fixture.queueService).project(failed);
    }

    @Test
    void pollClaimsOnlyAsManyJobsAsThereAreWorkerSlots() throws Exception {
        Fixture fixture = fixture();
        fixture.properties.setEnabled(true);
        fixture.properties.setBaseUrl("https://converter.example");
        fixture.properties.setClientId("tdms-test");
        fixture.properties.setSharedSecret("0123456789abcdef0123456789abcdef");
        fixture.properties.setWorkerThreads(1);
        fixture.properties.setBatchSize(20);
        PdfConversionJob claimed = processingJob(
                "JOB-ONE", "one.docx", repeat('a', 64));
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        when(fixture.dao.failExpiredExhausted()).thenReturn(Collections.emptyList());
        when(fixture.dao.selectDueIds(20)).thenReturn(List.of("JOB-ONE", "JOB-TWO"));
        when(fixture.dao.claim(anyMap())).thenReturn(claimed);
        when(fixture.sourceStore.materialize(anyString(), anyString())).thenAnswer(invocation -> {
            started.countDown();
            release.await(5, TimeUnit.SECONDS);
            throw new IllegalStateException("test worker release");
        });
        when(fixture.dao.markRetry(anyMap())).thenReturn(1);

        fixture.worker.initialize();
        try {
            fixture.worker.poll();
            assertThat(started.await(2, TimeUnit.SECONDS)).isTrue();
            verify(fixture.dao, times(1)).claim(anyMap());
            verify(fixture.queueService).reconcileCurrentProjections();
        } finally {
            release.countDown();
            fixture.worker.shutdown();
        }
    }

    @Test
    void staleFencedFailureProjectsTheCurrentJobInsteadOfSupersededJob() throws Exception {
        Fixture fixture = fixture();
        Path source = write("stale.docx", "changed bytes".getBytes(StandardCharsets.UTF_8));
        PdfConversionJob stale = processingJob(
                "JOB-STALE", "stale.docx", repeat('a', 64));
        PdfConversionJob current = completed(stale, "SUCCEEDED");
        current.setConversionId("JOB-CURRENT");

        when(fixture.sourceStore.materialize(stale.getSourceFilePath(), ".docx"))
                .thenReturn(source);
        when(fixture.dao.markFailed(anyMap())).thenReturn(0);
        when(fixture.queueService.findCurrent("SW", "OBJ-1", "1"))
                .thenReturn(current);

        process(fixture.worker, stale);

        verify(fixture.dao, never()).selectById(stale.getConversionId());
        verify(fixture.queueService).project(current);
    }

    @Test
    void terminalProjectionFailureDoesNotTurnTerminalStateIntoRetry() throws Exception {
        Fixture fixture = fixture();
        Path source = write("projection.docx", "changed bytes".getBytes(StandardCharsets.UTF_8));
        PdfConversionJob job = processingJob(
                "JOB-PROJECTION", "projection.docx", repeat('a', 64));
        PdfConversionJob failed = completed(job, "FAILED");

        when(fixture.sourceStore.materialize(job.getSourceFilePath(), ".docx"))
                .thenReturn(source);
        when(fixture.dao.markFailed(anyMap())).thenReturn(1);
        when(fixture.dao.selectById(job.getConversionId())).thenReturn(failed);
        doThrow(new IllegalStateException("projection unavailable"))
                .when(fixture.queueService).project(failed);

        process(fixture.worker, job);

        verify(fixture.dao).markFailed(anyMap());
        verify(fixture.dao, never()).markRetry(anyMap());
    }

    private void assertConverterFailureProjection(String persistedStatus) throws Exception {
        Fixture fixture = fixture();
        Path source = write(
                "failure-" + persistedStatus + ".docx",
                ("failure source " + persistedStatus).getBytes(StandardCharsets.UTF_8));
        Path target = tempDir.resolve("failure-" + persistedStatus + ".pdf");
        PdfConversionJob job = processingJob(
                "JOB-" + persistedStatus, "failure.docx", PdfConversionCrypto.sha256(source));
        PdfConversionJob updated = completed(job, persistedStatus);

        when(fixture.sourceStore.materialize(job.getSourceFilePath(), ".docx"))
                .thenReturn(source);
        when(fixture.dao.selectReusableByHash(anyMap())).thenReturn(null);
        when(fixture.sourceStore.createWorkFile(".pdf")).thenReturn(target);
        when(fixture.client.convert(
                any(Path.class), anyString(), anyString(), anyString(), any(Path.class)))
                .thenThrow(new IllegalStateException("temporary converter outage"));
        when(fixture.dao.markRetry(anyMap())).thenReturn(1);
        when(fixture.dao.selectById(job.getConversionId())).thenReturn(updated);

        process(fixture.worker, job);

        verify(fixture.dao, never()).markFailed(anyMap());
        ArgumentCaptor<Map<String, Object>> retry = mapCaptor();
        verify(fixture.dao).markRetry(retry.capture());
        assertThat(retry.getValue())
                .containsEntry("conversionId", job.getConversionId())
                .containsEntry("claimToken", job.getClaimToken())
                .containsEntry("retryDelaySeconds", Integer.valueOf(17));
        assertThat(retry.getValue().get("lastError").toString())
                .contains("IllegalStateException")
                .contains("temporary converter outage");
        verify(fixture.queueService).project(updated);
    }

    private Fixture fixture() {
        Fixture fixture = new Fixture();
        fixture.dao = mock(PdfConversionDao.class);
        fixture.queueService = mock(PdfConversionQueueService.class);
        fixture.sourceStore = mock(PdfConversionSourceStore.class);
        fixture.client = mock(PdfConversionClient.class);
        fixture.properties = new PdfConversionProperties();
        fixture.properties.setRetryDelaySeconds(17);
        fixture.worker = new PdfConversionWorker(
                fixture.dao, fixture.queueService, fixture.sourceStore,
                fixture.client, fixture.properties);
        return fixture;
    }

    private PdfConversionJob processingJob(String conversionId,
                                           String sourceFileName,
                                           String sourceSha256) {
        PdfConversionJob job = new PdfConversionJob();
        job.setConversionId(conversionId);
        job.setObjectType("SW");
        job.setObjectId("OBJ-1");
        job.setFileNo("1");
        job.setSourceFileName(sourceFileName);
        job.setSourceFilePath("SW/" + sourceFileName);
        job.setSourceSha256(sourceSha256);
        job.setStatus("PROCESSING");
        job.setClaimToken("CLAIM-" + conversionId);
        job.setAttemptCount(1);
        job.setMaxAttempts(3);
        return job;
    }

    private PdfConversionJob completed(PdfConversionJob source, String status) {
        PdfConversionJob result = new PdfConversionJob();
        result.setConversionId(source.getConversionId());
        result.setObjectType(source.getObjectType());
        result.setObjectId(source.getObjectId());
        result.setFileNo(source.getFileNo());
        result.setStatus(status);
        return result;
    }

    private Path write(String name, byte[] bytes) throws Exception {
        return Files.write(tempDir.resolve(name), bytes);
    }

    private void process(PdfConversionWorker worker, PdfConversionJob job) {
        ReflectionTestUtils.invokeMethod(worker, "process", job);
    }

    private String repeat(char value, int count) {
        return String.valueOf(value).repeat(count);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<Map<String, Object>> mapCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(Map.class);
    }

    private static final class Fixture {
        PdfConversionDao dao;
        PdfConversionQueueService queueService;
        PdfConversionSourceStore sourceStore;
        PdfConversionClient client;
        PdfConversionProperties properties;
        PdfConversionWorker worker;
    }
}

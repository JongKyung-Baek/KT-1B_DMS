package kr.esob.tdms.commonlogic.pdfconversion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

class PdfConversionQueueServiceTest {

    @Test
    void sameObjectFileAndSourceBytesReturnTheExistingConversionJob() {
        PdfConversionDao dao = mock(PdfConversionDao.class);
        PdfConversionProjectionDao projectionDao = mock(PdfConversionProjectionDao.class);
        PdfConversionProperties properties = properties();
        PdfConversionQueueService service = new PdfConversionQueueService(
                dao, projectionDao, mock(PdfConversionSourceStore.class), properties);
        AtomicReference<PdfConversionJob> persisted = new AtomicReference<PdfConversionJob>();
        when(dao.enqueue(any(PdfConversionJob.class))).thenAnswer(invocation -> {
            PdfConversionJob request = invocation.getArgument(0);
            PdfConversionJob existing = persisted.get();
            if (existing == null) {
                request.setStatus("PENDING");
                request.setCurrent(true);
                persisted.set(request);
                return request;
            }
            return existing;
        });
        byte[] source = "same office document".getBytes(StandardCharsets.UTF_8);

        PdfConversionJob first = service.enqueueUpload(
                "sw", "OBJ-1", "1", "report.docx", "SW/report.docx",
                new MockMultipartFile("file", "report.docx", null, source));
        PdfConversionJob second = service.enqueueUpload(
                "sw", "OBJ-1", "1", "report.docx", "SW/report.docx",
                new MockMultipartFile("file", "report.docx", null, source));

        assertThat(second.getConversionId()).isEqualTo(first.getConversionId());
        ArgumentCaptor<PdfConversionJob> queued = ArgumentCaptor.forClass(PdfConversionJob.class);
        verify(dao, times(2)).enqueue(queued.capture());
        assertThat(queued.getAllValues())
                .extracting(PdfConversionJob::getSourceSha256)
                .containsOnly(PdfConversionCrypto.sha256(
                        new java.io.ByteArrayInputStream(source)));
        verify(dao, never()).markNotRequired(anyMap());
    }

    @Test
    void pdfAndStepFilesAreImmediatelyMarkedNotRequiredAndProjectedDone() {
        for (String fileName : new String[] {"manual.PDF", "assembly.StP"}) {
            PdfConversionDao dao = mock(PdfConversionDao.class);
            PdfConversionProjectionDao projectionDao = mock(PdfConversionProjectionDao.class);
            PdfConversionQueueService service = new PdfConversionQueueService(
                    dao, projectionDao, mock(PdfConversionSourceStore.class), properties());
            AtomicReference<PdfConversionJob> queued = new AtomicReference<PdfConversionJob>();
            when(dao.enqueue(any(PdfConversionJob.class))).thenAnswer(invocation -> {
                PdfConversionJob request = invocation.getArgument(0);
                request.setStatus("PENDING");
                queued.set(request);
                return request;
            });
            when(dao.markNotRequired(anyMap())).thenReturn(1);
            when(dao.selectById(anyString())).thenAnswer(invocation -> {
                PdfConversionJob job = queued.get();
                job.setStatus("NOT_REQUIRED");
                return job;
            });
            byte[] source = ("source-" + fileName).getBytes(StandardCharsets.UTF_8);

            PdfConversionJob result = service.enqueueUpload(
                    "SW", "OBJ-2", "2", fileName, "SW/" + fileName,
                    new MockMultipartFile("file", fileName, null, source));

            assertThat(result.getStatus()).isEqualTo("NOT_REQUIRED");
            ArgumentCaptor<Map<String, Object>> ready = mapCaptor();
            verify(dao).markNotRequired(ready.capture());
            assertThat(ready.getValue())
                    .containsEntry("outputFileName", fileName)
                    .containsEntry("outputFilePath", "SW/" + fileName)
                    .containsEntry("outputSizeBytes", Long.valueOf(source.length))
                    .containsEntry("outputSha256", PdfConversionCrypto.sha256(
                            new java.io.ByteArrayInputStream(source)));
            ArgumentCaptor<Map<String, Object>> projection = mapCaptor();
            verify(projectionDao).updateStatus(projection.capture());
            assertThat(projection.getValue())
                    .containsEntry("objectType", "SW")
                    .containsEntry("status", "DONE");
        }
    }

    @Test
    void convertibleNonPdfFileRemainsPendingForTheWorker() {
        PdfConversionDao dao = mock(PdfConversionDao.class);
        PdfConversionProjectionDao projectionDao = mock(PdfConversionProjectionDao.class);
        PdfConversionQueueService service = new PdfConversionQueueService(
                dao, projectionDao, mock(PdfConversionSourceStore.class), properties());
        when(dao.enqueue(any(PdfConversionJob.class))).thenAnswer(invocation -> {
            PdfConversionJob request = invocation.getArgument(0);
            request.setStatus("PENDING");
            return request;
        });

        PdfConversionJob result = service.enqueueUpload(
                "sw", "OBJ-3", "3", "specification.docx", "SW/specification.docx",
                new MockMultipartFile("file", "specification.docx", null,
                        "office bytes".getBytes(StandardCharsets.UTF_8)));

        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getObjectType()).isEqualTo("SW");
        assertThat(result.getMaxAttempts()).isEqualTo(3);
        verify(dao, never()).markNotRequired(anyMap());
        ArgumentCaptor<Map<String, Object>> projection = mapCaptor();
        verify(projectionDao).updateStatus(projection.capture());
        assertThat(projection.getValue())
                .containsEntry("conversionId", result.getConversionId())
                .containsEntry("status", "PENDING");
    }

    @Test
    void unsupportedViewerExtensionNeverCreatesOrMaterializesAConversionJob() {
        PdfConversionDao dao = mock(PdfConversionDao.class);
        PdfConversionProjectionDao projectionDao = mock(PdfConversionProjectionDao.class);
        PdfConversionSourceStore sourceStore = mock(PdfConversionSourceStore.class);
        PdfConversionQueueService service = new PdfConversionQueueService(
                dao, projectionDao, sourceStore, properties());

        assertThatThrownBy(() -> service.enqueueUpload(
                "SW", "OBJ-4", "1", "archive.zip", "SW/archive.zip",
                new MockMultipartFile("file", "archive.zip", null, new byte[] {1, 2, 3})))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available");
        assertThatThrownBy(() -> service.enqueueStored(
                "SW", "OBJ-4", "1", "SW/archive.zip", "archive.zip"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not available");

        verify(dao, never()).enqueue(any(PdfConversionJob.class));
        verify(sourceStore, never()).materialize(anyString(), anyString());
        verify(projectionDao, never()).updateStatus(anyMap());
    }

    @Test
    void reconciliationRepairsBothMainAndSupportingFileProjections() {
        PdfConversionProjectionDao projectionDao = mock(PdfConversionProjectionDao.class);
        PdfConversionQueueService service = new PdfConversionQueueService(
                mock(PdfConversionDao.class), projectionDao,
                mock(PdfConversionSourceStore.class), properties());

        service.reconcileCurrentProjections();

        verify(projectionDao).reconcileCurrentSw();
        verify(projectionDao).reconcileCurrentSwSub();
    }

    private PdfConversionProperties properties() {
        PdfConversionProperties properties = new PdfConversionProperties();
        properties.setMaxAttempts(3);
        return properties;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<Map<String, Object>> mapCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(Map.class);
    }
}

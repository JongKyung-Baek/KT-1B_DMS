package kr.esob.tdms.controller.general.distribution.swrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import kr.esob.tdms.commonlogic.mail.DocsMailService;
import kr.esob.tdms.commonlogic.message.Prop;
import kr.esob.tdms.commonlogic.pdfconversion.PdfConversionQueueService;
import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.tdms.commonlogic.value.Constant;
import kr.esob.tdms.controller.general.distribution.approvaldetail.DistributionApprovalDetailDao;

class SwRequestMultipleSubFileRegistrationTest {

    private HttpServer fileApi;
    private Map<String, String> previousSystemConfig;
    private final List<String> uploadedPaths = new ArrayList<String>();

    @BeforeEach
    void startFileApi() throws IOException {
        previousSystemConfig = SystemConfig.snapshot();
        fileApi = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        fileApi.createContext("/api/v1/files/", this::acceptUpload);
        fileApi.start();

        Map<String, String> config = new HashMap<String, String>();
        config.put(Constant.SYSTEM_CONFIG + "|FILE_API_BASE_URL",
                "http://127.0.0.1:" + fileApi.getAddress().getPort());
        config.put(Constant.SYSTEM_CONFIG + "|FILE_API_KEY", "test-file-api-key");
        config.put(Constant.SYSTEM_CONFIG + "|FILE_API_SW_FOLDER", "UPLOAD");
        SystemConfig.replaceSystemConfig(config);
    }

    @AfterEach
    void stopFileApi() {
        if (fileApi != null) {
            fileApi.stop(0);
        }
        SystemConfig.replaceSystemConfig(previousSystemConfig);
    }

    @Test
    void repeatedSubFilePartsAreStoredAndOnlyViewerSupportedFilesAreQueued() throws Exception {
        SwRequestDao dao = mock(SwRequestDao.class);
        PdfConversionQueueService conversionQueue = mock(PdfConversionQueueService.class);
        AtomicInteger nextSubFileNo = new AtomicInteger();
        doAnswer(invocation -> {
            SwSubFileParam subFile = invocation.getArgument(0);
            subFile.setFileNo(Integer.valueOf(nextSubFileNo.incrementAndGet()));
            return null;
        }).when(dao).insertSwSubFile(any(SwSubFileParam.class));

        SwRequestService service = service(dao, conversionQueue);
        MockMultipartHttpServletRequest request = registrationRequest();
        request.addFile(file("subFiles", "attachment-a.docx", "attachment A"));
        request.addFile(file("subFiles", "attachment-b.pdf", "attachment B"));
        request.addFile(file("subFiles", "model.step", "attachment C"));
        request.addFile(file("subFiles", "source-archive.zip", "attachment D"));

        assertThat(service.saveSwRegisterFileX2(request).isSuccess()).isTrue();

        ArgumentCaptor<SwSubFileParam> inserted = ArgumentCaptor.forClass(SwSubFileParam.class);
        verify(dao, times(4)).insertSwSubFile(inserted.capture());
        assertThat(inserted.getAllValues())
                .extracting(SwSubFileParam::getFileNo)
                .containsExactly(1, 2, 3, 4);
        assertThat(inserted.getAllValues())
                .extracting(SwSubFileParam::getOrgFileNm)
                .containsExactly("attachment-a.docx", "attachment-b.pdf", "model.step", "source-archive.zip");
        assertThat(inserted.getAllValues())
                .extracting(SwSubFileParam::getProcessingStatus)
                .containsExactly("PENDING", "DONE", "DONE", "NOT_VIEWABLE");
        assertThat(inserted.getAllValues())
                .extracting(SwSubFileParam::getObjectId)
                .doesNotHaveDuplicates();

        ArgumentCaptor<String> objectType = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> objectId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> fileNo = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> originalName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> storedPath = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MultipartFile> upload = ArgumentCaptor.forClass(MultipartFile.class);
        verify(conversionQueue, times(4)).enqueueUpload(
                objectType.capture(), objectId.capture(), fileNo.capture(),
                originalName.capture(), storedPath.capture(), upload.capture());

        assertThat(objectType.getAllValues())
                .containsExactly("SW", "SW_SUB", "SW_SUB", "SW_SUB");
        assertThat(fileNo.getAllValues()).containsExactly("1", "1", "2", "3");
        assertThat(originalName.getAllValues()).containsExactly(
                "main.docx", "attachment-a.docx", "attachment-b.pdf", "model.step");
        assertThat(objectId.getAllValues().subList(1, 4)).doesNotHaveDuplicates();
        assertThat(originalName.getAllValues()).doesNotContain("source-archive.zip");
        assertThat(uploadedPaths).hasSize(5);
        assertThat(uploadedPaths).allMatch(path -> path.contains("folder=UPLOAD"));
    }

    @Test
    void queueFailurePropagatesSoTheTransactionalRegistrationRollsBack() throws Exception {
        SwRequestDao dao = mock(SwRequestDao.class);
        PdfConversionQueueService conversionQueue = mock(PdfConversionQueueService.class);
        AtomicInteger nextSubFileNo = new AtomicInteger();
        doAnswer(invocation -> {
            SwSubFileParam subFile = invocation.getArgument(0);
            subFile.setFileNo(Integer.valueOf(nextSubFileNo.incrementAndGet()));
            return null;
        }).when(dao).insertSwSubFile(any(SwSubFileParam.class));
        doAnswer(invocation -> {
            if ("attachment-b.docx".equals(invocation.getArgument(3))) {
                throw new IllegalStateException("queue unavailable");
            }
            return null;
        }).when(conversionQueue).enqueueUpload(
                anyString(), anyString(), anyString(), anyString(), anyString(), any(MultipartFile.class));

        SwRequestService service = service(dao, conversionQueue);
        MockMultipartHttpServletRequest request = registrationRequest();
        request.addFile(file("subFiles", "attachment-a.docx", "attachment A"));
        request.addFile(file("subFiles", "attachment-b.docx", "attachment B"));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> service.saveSwRegisterFileX2(request));

        assertThat(SwRequestService.class
                .getMethod("saveSwRegisterFileX2", org.springframework.web.multipart.MultipartHttpServletRequest.class)
                .getAnnotation(org.springframework.transaction.annotation.Transactional.class)
                .rollbackFor())
                .contains(Exception.class);
    }

    private MockMultipartHttpServletRequest registrationRequest() {
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.addFile(file("file", "main.docx", "main content"));
        request.setParameter("fileName", "Main technical data");
        request.setParameter("swNo", "TD-TEST-001");
        request.setParameter("swTypeCd", "");
        request.setParameter("reviewerUser", "");
        return request;
    }

    private MockMultipartFile file(String fieldName, String originalName, String content) {
        return new MockMultipartFile(
                fieldName,
                originalName,
                "application/octet-stream",
                content.getBytes(StandardCharsets.UTF_8));
    }

    private SwRequestService service(SwRequestDao dao, PdfConversionQueueService conversionQueue) {
        StaticMessageSource messages = new StaticMessageSource();
        messages.addMessage("feature.techRegister.result.complete", Locale.KOREAN, "등록 완료");
        messages.addMessage("feature.techRegister.result.complete", Locale.KOREA, "등록 완료");
        Prop prop = new Prop();
        prop.setMessageSource(messages);

        SwRequestService service = new SwRequestService();
        ReflectionTestUtils.setField(service, "dao", dao);
        ReflectionTestUtils.setField(service, "approvalDetailDao", mock(DistributionApprovalDetailDao.class));
        ReflectionTestUtils.setField(service, "mailService", mock(DocsMailService.class));
        ReflectionTestUtils.setField(service, "pdfConversionQueueService", conversionQueue);
        ReflectionTestUtils.setField(service, "prop", prop);
        ReflectionTestUtils.setField(service, "securityAclService", mock(SecurityAclService.class));
        return service;
    }

    private void acceptUpload(HttpExchange exchange) throws IOException {
        try {
            assertThat(exchange.getRequestMethod()).isEqualTo("PUT");
            assertThat(exchange.getRequestHeaders().getFirst("X-TDDS-API-Key"))
                    .isEqualTo("test-file-api-key");
            assertThat(exchange.getRequestBody().readAllBytes()).isNotEmpty();
            uploadedPaths.add(exchange.getRequestURI().toString());
            exchange.sendResponseHeaders(201, -1);
        } finally {
            exchange.close();
        }
    }
}

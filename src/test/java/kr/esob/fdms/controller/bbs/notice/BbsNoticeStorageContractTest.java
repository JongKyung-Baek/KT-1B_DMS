package kr.esob.fdms.controller.bbs.notice;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import kr.esob.fdms.controller.login.UserVO;

class BbsNoticeStorageContractTest {

    @TempDir
    Path tempDir;

    @Test
    void downloadAlwaysUsesTheSingleStoredFilePath() throws Exception {
        Path storedFile = tempDir.resolve("notice.txt");
        byte[] contents = "notice attachment".getBytes(StandardCharsets.UTF_8);
        Files.write(storedFile, contents);

        BbsNoticeDao dao = mock(BbsNoticeDao.class);
        BbsNoticeService service = new BbsNoticeService();
        ReflectionTestUtils.setField(service, "dao", dao);

        BbsNoticeFileVO requestParam = new BbsNoticeFileVO();
        requestParam.setNoticeCd(10L);
        requestParam.setFileNo("1");
        requestParam.setSessionUser(new UserVO());

        BbsNoticeFileVO stored = new BbsNoticeFileVO();
        stored.setFilePath(storedFile.toString());
        stored.setFileNm("notice.txt");
        when(dao.selectFilePath(requestParam)).thenReturn(stored);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Chrome");
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.fileDownload(requestParam, response, request);

        assertArrayEquals(contents, response.getContentAsByteArray());
        verify(dao).selectFilePath(requestParam);
    }

    @Test
    void noticeModelsAndMapperHaveNoOutsideStorageContract() throws Exception {
        assertFalse(hasField(BbsNoticeAddParam.class, "filePathOutside"));
        assertFalse(hasField(BbsNoticeFileVO.class, "filePathOutside"));
        assertFalse(hasField(BbsNoticePopupParam.class, "filePathOutside"));

        String mapper = Files.readString(Path.of(
                "src", "main", "resources", "sqlMaps", "oracle", "its",
                "controller", "bbs", "Notice.xml"), StandardCharsets.UTF_8);
        assertFalse(mapper.contains("FILE_PATH_OUTSIDE"));
        assertFalse(mapper.contains("filePathOutside"));

        String service = Files.readString(Path.of(
                "src", "main", "java", "kr", "esob", "fdms", "controller",
                "bbs", "notice", "BbsNoticeService.java"), StandardCharsets.UTF_8);
        assertFalse(service.contains("SERVER_URL_INSIDE"));
        assertFalse(service.contains("SERVER_URL_OUTSIDE"));
        assertFalse(service.contains("FileUtil.callSender"));
        assertFalse(service.contains("getAuthSite()"));
    }

    private boolean hasField(Class<?> type, String fieldName) {
        return Arrays.stream(type.getDeclaredFields())
                .anyMatch(field -> fieldName.equals(field.getName()));
    }
}

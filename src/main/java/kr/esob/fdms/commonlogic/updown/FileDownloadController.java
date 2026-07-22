package kr.esob.fdms.commonlogic.updown;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeState;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeStore;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

// 2023. 07. 13 기범추가
// 파일 다운로드 Class. FileDeletingInputStream 이랑 세트

@RestController
@Slf4j
public class FileDownloadController {

    @Autowired
    private DownloadRuntimeStore runtimeStore;

    private static final String DIRECTORY = "C:\\workspaceStartupHub\\OUT\\DownTemp\\";

    //테스트용 임시
    // private static final Map<String, String> TEST_ORIGINAL_NAME_MAP = new HashMap<>();
    // static { TEST_ORIGINAL_NAME_MAP.put("12345678910.pdf", "DownTEST.pdf"); }

    /*다운로드 테스트용*/
    private static final String TEST_DOWNLOAD_KEY = "test";
    private static final String TEST_FILE_PATH = "C:\\workspaceStartupHub\\OUT\\DownTemp\\sample.pptx";
    private static final String TEST_ORIGINAL_FILE_NAME = "샘플파일.pptx";
    /*다운로드 테스트용*/

    // 웹소켓 get endPoint
        @GetMapping("/download/{uuid}")
        public ResponseEntity<?> downloadFile(@PathVariable("uuid") String uuid) throws IOException {

            String filePath = uuid;
            File file = new File(Paths.get("C:\\workspaceStartupHub\\OUT\\DownTemp\\", filePath).toString());
            String responseFileName = uuid;
            String decodedRequestName = decodeRequestName(uuid);
            DownloadRuntimeState state = runtimeStore.findLatestByDownloadRequestKey(uuid);
            
            /*다운로드 테스트용*/
            if (TEST_DOWNLOAD_KEY.equals(uuid)) {
                File testFile = new File(TEST_FILE_PATH);
                if (!testFile.exists() || !testFile.isFile()) {
                    log.warn("[DOWNLOAD][TEST-404] uuid={}, testFilePath={}, exists={}, isFile={}",
                            uuid, TEST_FILE_PATH, testFile.exists(), testFile.isFile());
                    return ResponseEntity.notFound().build();
                }

                String filename = TEST_ORIGINAL_FILE_NAME;
                String asciiFallbackFilename = buildAsciiFallbackFileName(filename, "sample");
                String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.name())
                        .replace("+", "%20");

                HttpHeaders headers = new HttpHeaders();
                headers.add(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + asciiFallbackFilename + "\"; filename*=UTF-8''" + encodedFilename
                );
                headers.add("X-Original-Filename", encodedFilename);
                headers.add("X-Download-Key", uuid);

                log.info("[DOWNLOAD][TEST-RESP] uuid={}, contentDisposition={}, xOriginalFilename={}, xDownloadKey={}, fileSize={}",
                        uuid,
                        headers.getFirst(HttpHeaders.CONTENT_DISPOSITION),
                        headers.getFirst("X-Original-Filename"),
                        headers.getFirst("X-Download-Key"),
                        testFile.length());

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentLength(testFile.length())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(new InputStreamResource(new FileInputStream(testFile)));
            }
            /*다운로드 테스트용*/

            if (state == null && decodedRequestName != null && !decodedRequestName.equals(uuid)) {
                state = runtimeStore.findLatestByDownloadRequestKey(decodedRequestName);
            }
            if (state == null) { state = runtimeStore.findLatestByFileNm(uuid); }
            if (state == null && decodedRequestName != null && !decodedRequestName.equals(uuid)) { 
                state = runtimeStore.findLatestByFileNm(decodedRequestName); 
            }
            log.info("[DOWNLOAD][REQ] uuid={}, decodedRequestName={}, directPath={}, directExists={}, directIsFile={}, storeSize={}",
                    uuid, decodedRequestName, file.getAbsolutePath(), file.exists(), file.isFile(), runtimeStore.size());
            log.info("[DOWNLOAD][STATE] uuid={}, stateFound={}, downloadRequestKey={}, savedFileName={}, originalFileName={}, tempFilePath={}, restSequence={}, status={}, resultCode={}",
                    uuid,
                    state != null,
                    state == null ? null : state.getDownloadRequestKey(),
                    state == null ? null : state.getSavedFileName(),
                    state == null ? null : state.getOriginalFileName(),
                    state == null ? null : state.getTempFilePath(),
                    state == null ? null : state.getRestSequence(),
                    state == null ? null : state.getStatus(),
                    state == null ? null : state.getResultCode());

            //테스트용 임시
            // String responseFileName = TEST_ORIGINAL_NAME_MAP.getOrDefault(uuid, uuid);
            // if (state != null && state.getOriginalFileName() != null && !state.getOriginalFileName().trim().isEmpty()) { responseFileName = state.getOriginalFileName(); }
            //테스트용 임시

            if (state != null && state.getOriginalFileName() != null && !state.getOriginalFileName().trim().isEmpty()) { responseFileName = state.getOriginalFileName(); }

            if (!file.exists() || file.isDirectory()){
                if (state == null || state.getTempFilePath() == null || state.getTempFilePath().trim().isEmpty()) {
                    log.warn("[DOWNLOAD][404][NO-MAPPING] uuid={}, directExists={}, directIsDirectory={}, stateFound={}",
                            uuid, file.exists(), file.isDirectory(), state != null);
                    return ResponseEntity.notFound().build();
                }
                File mapped = new File(state.getTempFilePath());
                if (!mapped.exists() || mapped.isDirectory()) {
                    log.warn("[DOWNLOAD][404][MAPPED-NOT-FOUND] uuid={}, mappedTempPath={}, mappedExists={}, mappedIsDirectory={}",
                            uuid, state.getTempFilePath(), mapped.exists(), mapped.isDirectory());
                    return ResponseEntity.notFound().build();
                }
                file = mapped;
                responseFileName = state.getOriginalFileName() == null || state.getOriginalFileName().trim().isEmpty()
                        ? uuid
                        : state.getOriginalFileName();
                log.info("[DOWNLOAD][MAP] requestName={}, mappedTempPath={}, mappedExists={}, mappedIsFile={}, mappedLength={}, responseFileName={}",
                        uuid, state.getTempFilePath(), mapped.exists(), mapped.isFile(), mapped.length(), responseFileName);
            }

            // String filename = responseFileName;
            // String asciiFallbackFilename = buildAsciiFallbackFileName(filename, uuid);
            // String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");

            // HttpHeaders headers = new HttpHeaders();
            // headers.add(
            //     HttpHeaders.CONTENT_DISPOSITION,
            //     "attachment; filename=\"" + asciiFallbackFilename + "\"; filename*=UTF-8''" + encodedFilename
            // );
            // headers.add("X-Original-Filename", encodedFilename);
            // headers.add("X-Download-Key", uuid);      

            // headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + asciiFallbackFilename + "\"; filename*=UTF-8''" + encodedFilename);
            // headers.add("filename", filename);

            String filename = responseFileName;
            String asciiFallbackFilename = buildAsciiFallbackFileName(filename, uuid);
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.name())
                    .replace("+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.add(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + asciiFallbackFilename + "\"; filename*=UTF-8''" + encodedFilename
            );            
            /* [DOWNLOAD-DEBUG-START] 최종 다운로드 응답 헤더 fileNm 확인용 로그 */
            log.info("[DOWNLOAD-DEBUG][RESP-HEADER] uuid={}, responseFileName={}, asciiFallbackFilename={}, encodedFilename={}, stateDownloadRequestKey={}, stateSavedFileName={}, stateOriginalFileName={}",
                    uuid,
                    filename,
                    asciiFallbackFilename,
                    encodedFilename,
                    state == null ? null : state.getDownloadRequestKey(),
                    state == null ? null : state.getSavedFileName(),
                    state == null ? null : state.getOriginalFileName());
            /* [DOWNLOAD-DEBUG-END] 최종 다운로드 응답 헤더 fileNm 확인용 로그 */

            log.info("[DOWNLOAD][RESP] uuid={}, contentDisposition={}, filenameHeader={}, fileSize={}",
                    uuid,
                    headers.getFirst(HttpHeaders.CONTENT_DISPOSITION),
                    headers.getFirst("filename"),
                    file.length());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(new InputStreamResource(new FileDeletingInputStream(file))); // 여기서 DeletableResource를 사용합니다.
        }

        private String buildAsciiFallbackFileName(String originalFileName, String defaultBaseName) {
            String fileName = originalFileName == null ? "" : originalFileName.trim();
            String baseName = defaultBaseName == null ? "" : defaultBaseName.trim();
            if (baseName.isEmpty()) { baseName = "download"; }

            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < fileName.length() - 1) { extension = fileName.substring(dotIndex + 1).replaceAll("[^A-Za-z0-9]", ""); }

            String safeBaseName = fileName;
            if (dotIndex > 0) { safeBaseName = fileName.substring(0, dotIndex); }

            safeBaseName = safeBaseName.replaceAll("[^A-Za-z0-9._-]", "_");
            safeBaseName = safeBaseName.replaceAll("_+", "_");
            safeBaseName = safeBaseName.replaceAll("^[._-]+|[._-]+$", "");
            if (safeBaseName.isEmpty()) {
                safeBaseName = baseName.replaceAll("[^A-Za-z0-9._-]", "_");
                safeBaseName = safeBaseName.replaceAll("_+", "_");
                safeBaseName = safeBaseName.replaceAll("^[._-]+|[._-]+$", "");
            }
            if (safeBaseName.isEmpty()) { safeBaseName = "download"; }

            if (!extension.isEmpty()) { return safeBaseName + "." + extension; }
            return safeBaseName;
        }

        private String decodeRequestName(String value) {
            if (value == null || value.trim().isEmpty()) { return value; }
            try { return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                log.warn("[DOWNLOAD][DECODE-FAIL] value={}", value, e);
                return value;
            }
        }


}

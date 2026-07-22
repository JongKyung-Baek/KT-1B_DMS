package kr.esob.fdms.commonlogic.updown;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeState;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeStore;
import kr.esob.fdms.controller.login.UserVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/common/updown/v2")
public class CommonUpdownV2Controller {
    @Inject private DownloadRuntimeStore runtimeStore;
    @Inject private CommonUpdownV2Service v2Service;
    private static final long WS_RESULT_TIMEOUT_SECONDS = 30L;

      /**
       * JS -> 서버: 다운로드 시작 등록 (wsSeq 기반)
       * payload 예:
       * {
       *   "wsSeq":"1234567890abcdef1234567890abcdef",
       *   "reqType":"DISTRIBUTION",
       *   "requestNo":"REQ20260001",
       *   "docSeq":"U77A23248933971",
       *   "objectType":"DOC",
       *   "fileNo":"12345",
       *   "fileNm":"265491-b9f8-4c5f-9da9-a398422b0a42.svg"
       * }
       */
    @RequestMapping("/start")
    public @ResponseBody Map<String, Object> start(@RequestBody CommonUpdownV2StartParam param, Authentication authentication) {
        Map<String, Object> result = new HashMap<String, Object>();
        UserVO userVo = authentication != null && authentication.getPrincipal() instanceof UserVO
            ? (UserVO) authentication.getPrincipal() : null;
        log.info("[V2-START][REQ] wsSeq={}, reqType={}, objectType={}, requestNo={}, dataNo={}, docSeq={}, fileNo={}, fileSeq={}, fileNm={}",
            param == null ? null : param.getWsSeq(),
            param == null ? null : param.getReqType(),
            param == null ? null : param.getObjectType(),
            param == null ? null : param.getRequestNo(),
            param == null ? null : param.getDataNo(),
            param == null ? null : param.getDocSeq(),
            param == null ? null : param.getFileNo(),
            param == null ? null : param.getFileSeq(),
            param == null ? null : param.getFileNm()
        );
        /* [DOWNLOAD-DEBUG-START] 웹소켓 패킷 생성용 JAVA 수신값 및 downloadRequestKey 확인용 로그 */
        log.info("[DOWNLOAD-DEBUG][V2-START][REQ] wsSeq={}, reqType={}, objectType={}, requestNo={}, dataNo={}, docSeq={}, fileNo={}, fileSeq={}, fileNm={}, orgFileNm={}, resolvedDownloadRequestKey={}",
            param == null ? null : param.getWsSeq(),
            param == null ? null : param.getReqType(),
            param == null ? null : param.getObjectType(),
            param == null ? null : param.getRequestNo(),
            param == null ? null : param.getDataNo(),
            param == null ? null : param.getDocSeq(),
            param == null ? null : param.getFileNo(),
            param == null ? null : param.getFileSeq(),
            param == null ? null : param.getFileNm(),
            param == null ? null : param.getOrgFileNm(),
            resolveDownloadRequestKey(param)
        );
        /* [DOWNLOAD-DEBUG-END] 웹소켓 패킷 생성용 JAVA 수신값 및 downloadRequestKey 확인용 로그 */

        if (param == null || isBlank(param.getWsSeq())) {
            result.put("success", false);
            result.put("message", "wsSeq is required.");
            log.warn("[V2-START][FAIL] {}", result.get("message"));
            return result;
        }

        if (!param.getWsSeq().matches("[0-9a-fA-F]{32}")) {
            result.put("success", false);
            result.put("message", "wsSeq must be 32-char hex string.");
            log.warn("[V2-START][FAIL] wsSeq={} message={}", param.getWsSeq(), result.get("message"));
            return result;
        }

        try {
            // 1) runtime 등록
            DownloadRuntimeState state = runtimeStore.registerQueued(
                param.getWsSeq(),
                nvl(param.getRequestNo()),
                nvl(param.getDocSeq()),
                nvl(param.getFileNo()),
                nvl(param.getFileSeq()),
                resolveDownloadRequestKey(param),
                nvl(param.getReqType()),
                nvl(param.getObjectType()),
                isBlank(param.getOrgFileNm()) ? nvl(param.getFileNm()) : nvl(param.getOrgFileNm())
            );

            // 2) REST 요청용 seq 결정
            CommonUpdownV2Service.RestSeqResolveResult seqInfo = v2Service.resolveRestRequestSeq(param);

            // 3) REST API 호출 + 임시파일 저장
            CommonUpdownV2Service.TempDownloadResult temp = v2Service.fetchAndSaveTempFile(
                nvl(param.getObjectType()).toUpperCase(),
                seqInfo.getRestSeq(),
                isBlank(param.getOrgFileNm()) ? nvl(param.getFileNm()) : nvl(param.getOrgFileNm()),
                nvl(param.getFileExt())
            );

            // 4) 상태 갱신
            runtimeStore.update(param.getWsSeq(), s -> s.markDownloading(seqInfo.getRestSeq(), temp.getTempFilePath(), temp.getSavedFileName()));
            runtimeStore.update(param.getWsSeq(), s -> s.markSentToWs());
            DownloadRuntimeState updatedState = runtimeStore.get(param.getWsSeq());

            result.put("success", true);
            result.put("wsSeq", state.getWsSeq());
            result.put("status", updatedState == null ? null : updatedState.getStatus().name());
            result.put("restSeq", seqInfo.getRestSeq());
            result.put("restSeqType", seqInfo.getRestSeqType());
            result.put("downloadRequestKey", updatedState == null ? null : updatedState.getDownloadRequestKey());
            result.put("tempFilePath", temp.getTempFilePath());
            result.put("savedFileName", temp.getSavedFileName());
            result.put("fileSize", temp.getFileSize());
            java.io.File tempFile = new java.io.File(temp.getTempFilePath());
            /* [DOWNLOAD-DEBUG-START] 웹소켓 전달 전 JAVA 처리 결과 확인용 로그 */
            log.info("[DOWNLOAD-DEBUG][V2-START][READY] wsSeq={}, restSeq={}, restSeqType={}, downloadRequestKey={}, savedFileName={}, originalFileName={}, tempFilePath={}, fileSize={}",
                state.getWsSeq(),
                seqInfo.getRestSeq(),
                seqInfo.getRestSeqType(),
                updatedState == null ? null : updatedState.getDownloadRequestKey(),
                temp.getSavedFileName(),
                updatedState == null ? null : updatedState.getOriginalFileName(),
                temp.getTempFilePath(),
                temp.getFileSize()
            );
            /* [DOWNLOAD-DEBUG-END] 웹소켓 전달 전 JAVA 처리 결과 확인용 로그 */
            log.info("[V2-START][OK] wsSeq={}, restSeq={}, restSeqType={}, downloadRequestKey={}, savedFileName={}, originalFileName={}, tempFilePath={}, tempExists={}, tempIsFile={}, tempLength={}, fileSize={}, state={}",
                state.getWsSeq(),
                seqInfo.getRestSeq(),
                seqInfo.getRestSeqType(),
                updatedState == null ? null : updatedState.getDownloadRequestKey(),
                temp.getSavedFileName(),
                updatedState == null ? null : updatedState.getOriginalFileName(),
                temp.getTempFilePath(),
                tempFile.exists(),
                tempFile.isFile(),
                tempFile.exists() && tempFile.isFile() ? tempFile.length() : -1L,
                temp.getFileSize(),
                summarizeState(updatedState));
            return result;
        } catch (Exception e) {
            runtimeStore.update(param.getWsSeq(), s -> s.markFailed(e.getMessage()));
            DownloadRuntimeState failedState = runtimeStore.get(param.getWsSeq());
            if (failedState != null && userVo != null) {
                v2Service.saveOutsideDistributionDownloadActLog(userVo, failedState, "FAILED", e.getMessage());
                runtimeStore.update(param.getWsSeq(), DownloadRuntimeState::markActLogSaved);
            }
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("[V2-START][EXCEPTION] wsSeq={} message={}", param.getWsSeq(), e.getMessage(), e);
            return result;
        }
    }

      /**
       * 프론트 폴링용 상태 조회
       * payload 예: {"wsSeq":"1234567890abcdef1234567890abcdef"}
       */
    @RequestMapping("/status")
    public @ResponseBody Map<String, Object> status(@RequestBody CommonUpdownV2SeqParam param) {
        Map<String, Object> result = new HashMap<String, Object>();

        if (param == null || isBlank(param.getWsSeq())) {
            result.put("success", false);
            result.put("message", "wsSeq is required.");
            return result;
        }

        DownloadRuntimeState state = runtimeStore.get(param.getWsSeq());
        if (state == null) {
            log.warn("[V2-STATUS][NOT-FOUND] wsSeq={}, storeSize={}", param.getWsSeq(), runtimeStore.size());
            result.put("success", false);
            result.put("message", "not found");
            return result;
        }

        runtimeStore.update(param.getWsSeq(), s -> {
            if (s.markFailedIfWsTimedOut(WS_RESULT_TIMEOUT_SECONDS)) {
                log.warn("[V2-STATUS][WS-TIMEOUT] wsSeq={}, requestNo={}, docSeq={}, objectType={}, timeoutSeconds={}",
                    s.getWsSeq(), s.getRequestNo(), s.getDocSeq(), s.getObjectType(), WS_RESULT_TIMEOUT_SECONDS);
            }
        });
        state = runtimeStore.get(param.getWsSeq());

        result.put("success", true);
        result.put("wsSeq", state.getWsSeq());
        result.put("status", state.getStatus().name());
        result.put("resultCode", state.getResultCode());
        result.put("optionalData", state.getOptionalData());
        result.put("errorMessage", state.getErrorMessage());
        result.put("updatedAt", state.getUpdatedAt());
        return result;
    }

      /**
       * 웹소켓 처리 결과 반영
       * payload 예:
       * {
       *   "message":"1234567890abcdef1234567890abcdef00C:/temp/a.svg"
       * }
       * 포맷: SEQ(32) + RESULT_CODE(2) + OPTIONAL_DATA
       */
    @RequestMapping("/ws-result")
    public @ResponseBody Map<String, Object> wsResult(@RequestBody CommonUpdownV2WsResultParam param, Authentication authentication) {
        Map<String, Object> result = new HashMap<String, Object>();

        if (param == null || isBlank(param.getMessage())) {
            result.put("success", false);
            result.put("message", "message is required.");
            return result;
        }

        String msg = param.getMessage();
        if (msg.length() < 34) {
            log.warn("[WS-CHECK][WS-RESULT][INVALID-LENGTH] length={}, rawMessage={}", msg.length(), msg);
            result.put("success", false);
            result.put("message", "invalid message length.");
            return result;
        }

        String wsSeq = msg.substring(0, 32);
        String code = msg.substring(32, 34);
        String data = msg.substring(34);
        // 웹소켓 송수신 확인용: ws-result 수신값 파싱 로그
        log.info("[WS-CHECK][WS-RESULT] wsSeq={}, resultCode={}, optionalData={}, rawLength={}", wsSeq, code, data, msg.length());

        // 00만 성공, 나머지 실패
        runtimeStore.update(wsSeq, state -> {
            if ("00".equals(code)) { state.markResult("00", data);
            } else { state.markResult(code, data); } // markResult 내부에서 FAILED 처리됨
        });

        DownloadRuntimeState state = runtimeStore.get(wsSeq);
        if (state == null) {
            log.warn("[WS-CHECK][WS-RESULT][SEQ-NOT-FOUND] wsSeq={}, storeSize={}", wsSeq, runtimeStore.size());
            result.put("success", false);
            result.put("message", "seq not found.");
            return result;
        }
        log.info("[WS-CHECK][WS-RESULT][STATE] wsSeq={}, state={}", wsSeq, summarizeState(state));

        if (!state.isActLogSaved()) {
            UserVO userVo = authentication != null && authentication.getPrincipal() instanceof UserVO
                ? (UserVO) authentication.getPrincipal() : null;
            if (userVo != null) {
                v2Service.saveOutsideDistributionDownloadActLog(
                    userVo,
                    state,
                    state.getStatus().name(),
                    isBlank(state.getErrorMessage()) ? state.getOptionalData() : state.getErrorMessage()
                );
                v2Service.updateOutsideDistributionDeliveryConfirm(state, state.getStatus().name());
                runtimeStore.update(wsSeq, DownloadRuntimeState::markActLogSaved);
            }
        }

        result.put("success", true);
        result.put("wsSeq", wsSeq);
        result.put("status", state.getStatus().name());
        result.put("resultCode", state.getResultCode());
        result.put("optionalData", state.getOptionalData());
        return result;
    }

    @RequestMapping("/finalize")
    public @ResponseBody Map<String, Object> finalizeResult(@RequestBody CommonUpdownV2FinalizeParam param, Authentication authentication) {
        Map<String, Object> result = new HashMap<String, Object>();

        if (param == null || isBlank(param.getWsSeq())) {
            result.put("success", false);
            result.put("message", "wsSeq is required.");
            return result;
        }

        DownloadRuntimeState state = runtimeStore.get(param.getWsSeq());
        if (state == null) {
            result.put("success", false);
            result.put("message", "not found");
            return result;
        }

        if (state.isActLogSaved()) {
            result.put("success", true);
            result.put("saved", false);
            result.put("reason", "already saved");
            return result;
        }

        UserVO userVo = authentication != null && authentication.getPrincipal() instanceof UserVO
            ? (UserVO) authentication.getPrincipal() : null;
        if (userVo == null) {
            result.put("success", false);
            result.put("message", "user not found");
            return result;
        }

        String status = isBlank(param.getStatus()) ? state.getStatus().name() : param.getStatus();
        String errorMessage = isBlank(param.getErrorMessage()) ? state.getErrorMessage() : param.getErrorMessage();
        v2Service.saveOutsideDistributionDownloadActLog(userVo, state, status, errorMessage);
        v2Service.updateOutsideDistributionDeliveryConfirm(state, status);
        runtimeStore.update(param.getWsSeq(), DownloadRuntimeState::markActLogSaved);

        result.put("success", true);
        result.put("saved", true);
        result.put("status", status);
        return result;
    }

      /**
       * 팝업 닫을 때 정리
       * payload 예: {"wsSeq":"1234567890abcdef1234567890abcdef"}
       */
    @RequestMapping("/cleanup")
    public @ResponseBody Map<String, Object> cleanup(@RequestBody CommonUpdownV2SeqParam param) {
        Map<String, Object> result = new HashMap<String, Object>();

        if (param == null || isBlank(param.getWsSeq())) {
            result.put("success", false);
            result.put("message", "wsSeq is required.");
            return result;
        }

        DownloadRuntimeState target = runtimeStore.get(param.getWsSeq());
        if (target != null) { deleteIfExists(target.getTempFilePath()); }

        DownloadRuntimeState removed = runtimeStore.remove(param.getWsSeq());
        result.put("success", true);
        result.put("removed", removed != null);
        return result;
    }

    private void deleteIfExists(String path) {
        if (path == null || path.trim().isEmpty()) return;
        try {
            java.io.File f = new java.io.File(path);
            if (f.exists() && f.isFile()) { f.delete(); }
        } catch (Exception ignore) {}
    }

      /**
       * 운영 확인용
       */
    @RequestMapping("/cleanup-expired")
    public @ResponseBody Map<String, Object> cleanupExpired() {
        Map<String, Object> result = new HashMap<String, Object>();
        int removed = runtimeStore.cleanupExpired();
        result.put("success", true);
        result.put("removedCount", removed);
        result.put("currentSize", runtimeStore.size());
        return result;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private String nvl(String s) { return s == null ? "" : s; }
    private String resolveDownloadRequestKey(CommonUpdownV2StartParam param) {
        if (param == null) { return ""; }
        String objectType = nvl(param.getObjectType()).trim().toUpperCase();
        if ("SW".equals(objectType) || "SECP".equals(objectType) || "SECP_PARTDOC".equals(objectType)) {
            return nvl(param.getFileSeq()).trim();
        }
        if ("DOC".equals(objectType) || "DRAWING".equals(objectType)) {
            return nvl(param.getFileSeq()).trim();
        }
        return nvl(param.getFileNm()).trim();
    }
    private String summarizeState(DownloadRuntimeState state) {
        if (state == null) { return "null"; }
        return String.format(
            "status=%s, downloadRequestKey=%s, savedFileName=%s, originalFileName=%s, tempFilePath=%s, restSequence=%s, resultCode=%s, optionalData=%s, errorMessage=%s",
            state.getStatus(),
            state.getDownloadRequestKey(),
            state.getSavedFileName(),
            state.getOriginalFileName(),
            state.getTempFilePath(),
            state.getRestSequence(),
            state.getResultCode(),
            state.getOptionalData(),
            state.getErrorMessage()
        );
    }

}

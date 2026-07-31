package kr.esob.fdms.commonlogic.updown;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeState;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeCleanupScheduler;
import kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeStore;
import kr.esob.fdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.controller.login.UserVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/common/updown/v2")
public class CommonUpdownV2Controller {
    @Inject private DownloadRuntimeStore runtimeStore;
    @Inject private CommonUpdownV2Service v2Service;
    @Inject private SecurityAclService securityAclService;
    @Inject private DownloadRuntimeCleanupScheduler cleanupScheduler;
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
    @PostMapping("/start")
    public @ResponseBody Map<String, Object> start(@RequestBody CommonUpdownV2StartParam param, Authentication authentication) {
        Map<String, Object> result = new HashMap<String, Object>();
        UserVO userVo = authentication != null && authentication.getPrincipal() instanceof UserVO
            ? (UserVO) authentication.getPrincipal() : null;
        CommonUpdownV2Service.TempDownloadResult preparedTemp = null;
        AtomicBoolean tempPathPersisted = new AtomicBoolean(false);
        boolean runtimeRegistered = false;
        log.info("[V2-START][REQ] received");
        /* [DOWNLOAD-DEBUG-START] 웹소켓 패킷 생성용 JAVA 수신값 및 downloadRequestKey 확인용 로그 */
        /* [DOWNLOAD-DEBUG-END] 웹소켓 패킷 생성용 JAVA 수신값 및 downloadRequestKey 확인용 로그 */

        if (param == null || isBlank(param.getWsSeq())) {
            result.put("success", false);
            result.put("message", "wsSeq is required.");
            log.warn("[V2-START][FAIL] request rejected");
            return result;
        }

        if (!param.getWsSeq().matches("[0-9a-fA-F]{32}")) {
            result.put("success", false);
            result.put("message", "wsSeq must be 32-char hex string.");
            log.warn("[V2-START][FAIL] invalid sequence");
            return result;
        }

        try {
            userVo = securityAclService.requireCurrentUser();
            String ownerSessionId = currentSessionId();
            CommonUpdownV2Service.ResolvedDownloadResource resource =
                v2Service.resolveAuthorizedResource(param, userVo);

            FileAccessRequest access = new FileAccessRequest();
            access.setActionCd(SecurityAclService.DOWNLOAD_ORIGINAL);
            access.setObjectType(resource.getObjectType());
            access.setObjectId(resource.getObjectId());
            access.setFileNo(resource.getFileNo());
            access.setRequestNo(resource.getRequestNo());
            securityAclService.requireAccess(access);

            String downloadTicket = UUID.randomUUID().toString().replace("-", "");
            // 1) runtime 등록
            DownloadRuntimeState state = runtimeStore.registerQueued(
                param.getWsSeq(),
                nvl(resource.getRequestNo()),
                nvl(resource.getObjectId()),
                nvl(resource.getFileNo()),
                nvl(resource.getFileNo()),
                downloadTicket,
                nvl(param.getReqType()),
                nvl(resource.getObjectType()),
                isBlank(param.getOrgFileNm()) ? nvl(param.getFileNm()) : nvl(param.getOrgFileNm()),
                userVo.getUserCd(),
                userVo.getUserId(),
                userVo.getUserNm(),
                ownerSessionId
            );
            runtimeRegistered = true;

            // 2) REST 요청용 seq 결정
            CommonUpdownV2Service.RestSeqResolveResult seqInfo = v2Service.resolveRestRequestSeq(resource);

            // 3) REST API 호출 + 임시파일 저장
            preparedTemp = v2Service.fetchAndSaveTempFile(
                resource.getObjectType(),
                seqInfo.getRestSeq(),
                isBlank(param.getOrgFileNm()) ? nvl(param.getFileNm()) : nvl(param.getOrgFileNm()),
                nvl(param.getFileExt()),
                (tempFilePath, savedFileName) -> {
                    runtimeStore.update(param.getWsSeq(), runtime -> runtime.markDownloading(
                        seqInfo.getRestSeq(), tempFilePath, savedFileName));
                    tempPathPersisted.set(true);
                }
            );

            // 4) 상태 갱신
            CommonUpdownV2Service.TempDownloadResult temp = preparedTemp;
            if (!tempPathPersisted.get()) {
                throw new IllegalStateException(
                    "Temporary download path was not persisted.");
            }
            runtimeStore.update(param.getWsSeq(), s -> s.markSentToWs());
            DownloadRuntimeState updatedState = runtimeStore.get(param.getWsSeq());
            if (updatedState == null
                    || updatedState.getStatus()
                    != kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeStatus.SENT_TO_WS
                    || isBlank(updatedState.getDownloadRequestKey())) {
                throw new IllegalStateException(
                    "Durable download capability is not ready.");
            }

            result.put("success", true);
            result.put("wsSeq", state.getWsSeq());
            result.put("status", updatedState.getStatus().name());
            result.put("restSeq", seqInfo.getRestSeq());
            result.put("restSeqType", seqInfo.getRestSeqType());
            result.put("downloadRequestKey", updatedState.getDownloadRequestKey());
            result.put("savedFileName", temp.getSavedFileName());
            result.put("fileSize", temp.getFileSize());
            /* [DOWNLOAD-DEBUG-START] 웹소켓 전달 전 JAVA 처리 결과 확인용 로그 */
            /* [DOWNLOAD-DEBUG-END] 웹소켓 전달 전 JAVA 처리 결과 확인용 로그 */
            log.info("[V2-START][OK] status={}",
                updatedState.getStatus().name());
            return result;
        } catch (Exception e) {
            String clientMessage = safeClientErrorMessage(e);
            if (preparedTemp != null && !tempPathPersisted.get()) {
                deleteIfExists(preparedTemp.getTempFilePath());
            }
            if (runtimeRegistered) {
                try {
                    runtimeStore.update(param.getWsSeq(), s -> s.markFailed(clientMessage));
                    DownloadRuntimeState failedState = runtimeStore.get(param.getWsSeq());
                    if (failedState != null && userVo != null) {
                        if (v2Service.saveDownloadAudit(
                                userVo, failedState, "FAILED", clientMessage)) {
                            runtimeStore.update(
                                param.getWsSeq(), DownloadRuntimeState::markActLogSaved);
                        }
                    }
                } catch (Exception persistenceException) {
                    // Most importantly, never return a capability when its
                    // QUEUED row could not be durably created or updated.
                    log.error("[V2-START][PERSISTENCE-FAIL] type={}",
                        persistenceException.getClass().getSimpleName());
                }
            }
            result.put("success", false);
            result.put("message", clientMessage);
            log.error("[V2-START][EXCEPTION] type={}", e.getClass().getSimpleName());
            return result;
        }
    }

      /**
       * 프론트 폴링용 상태 조회
       * payload 예: {"wsSeq":"1234567890abcdef1234567890abcdef"}
       */
    @PostMapping("/status")
    public @ResponseBody Map<String, Object> status(@RequestBody CommonUpdownV2SeqParam param,
                                                    Authentication authentication,
                                                    HttpServletRequest request) {
        Map<String, Object> result = new HashMap<String, Object>();

        if (param == null || isBlank(param.getWsSeq())) {
            result.put("success", false);
            result.put("message", "wsSeq is required.");
            return result;
        }

        DownloadRuntimeState state = runtimeStore.get(param.getWsSeq());
        if (state == null) {
            log.warn("[V2-STATUS][NOT-FOUND] storeSize={}", runtimeStore.size());
            result.put("success", false);
            result.put("message", "not found");
            return result;
        }
		if (!isOwner(state, authentication, request)) {
			result.put("success", false);
			result.put("message", "forbidden");
			return result;
		}

        if (state.getStatus()
                == kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeStatus.SENT_TO_WS) {
            runtimeStore.update(param.getWsSeq(), s -> {
                if (s.markFailedIfWsTimedOut(WS_RESULT_TIMEOUT_SECONDS)) {
                    log.warn("[V2-STATUS][WS-TIMEOUT] timeoutSeconds={}",
                        WS_RESULT_TIMEOUT_SECONDS);
                }
            });
            state = runtimeStore.get(param.getWsSeq());
        }

        boolean auditSaved = ensureTerminalAuditSaved(state, authentication, param.getWsSeq());
        if (isTerminal(state) && !auditSaved) {
            result.put("success", false);
            result.put("message", "download audit persistence failed");
            result.put("status", state.getStatus().name());
            return result;
        }
        result.put("success", true);
        result.put("wsSeq", state.getWsSeq());
        result.put("status", state.getStatus().name());
        result.put("resultCode", state.getResultCode());
        result.put("errorMessage", state.getStatus() == kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeStatus.FAILED
                ? "Download failed." : null);
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
    @PostMapping("/ws-result")
    public @ResponseBody Map<String, Object> wsResult(@RequestBody CommonUpdownV2WsResultParam param,
                                                      Authentication authentication,
                                                      HttpServletRequest request) {
        Map<String, Object> result = new HashMap<String, Object>();

        if (param == null || isBlank(param.getMessage())) {
            result.put("success", false);
            result.put("message", "message is required.");
            return result;
        }

        String msg = param.getMessage();
        if (msg.length() < 34) {
            log.warn("[WS-CHECK][WS-RESULT][INVALID-LENGTH] length={}", msg.length());
            result.put("success", false);
            result.put("message", "invalid message length.");
            return result;
        }

        String wsSeq = msg.substring(0, 32);
        String code = msg.substring(32, 34);
        String data = msg.substring(34);
        // 웹소켓 송수신 확인용: ws-result 수신값 파싱 로그
        log.info("[WS-CHECK][WS-RESULT] resultCode={}, payloadLength={}", code, data.length());

        DownloadRuntimeState state = runtimeStore.get(wsSeq);
        if (state == null) {
            log.warn("[WS-CHECK][WS-RESULT][SEQ-NOT-FOUND] storeSize={}", runtimeStore.size());
            result.put("success", false);
            result.put("message", "seq not found.");
            return result;
        }
		if (!isOwner(state, authentication, request)) {
			result.put("success", false);
			result.put("message", "forbidden");
			return result;
		}

        // 00만 성공, 나머지 실패. 소유권 확인 후 상태를 변경한다.
        runtimeStore.update(wsSeq, runtime -> runtime.markResult(code, ""));
        state = runtimeStore.get(wsSeq);
        log.info("[WS-CHECK][WS-RESULT][STATE] status={}, resultCode={}",
            state.getStatus().name(), state.getResultCode());

        boolean auditSaved = ensureTerminalAuditSaved(state, authentication, wsSeq);

        result.put("success", auditSaved);
        result.put("wsSeq", wsSeq);
        result.put("status", state.getStatus().name());
        result.put("resultCode", state.getResultCode());
        if (!auditSaved) {
            result.put("message", "download audit persistence failed");
        }
        return result;
    }

    @PostMapping("/finalize")
    public @ResponseBody Map<String, Object> finalizeResult(@RequestBody CommonUpdownV2FinalizeParam param,
                                                            Authentication authentication,
                                                            HttpServletRequest request) {
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
		if (!isOwner(state, authentication, request)) {
			result.put("success", false);
			result.put("message", "forbidden");
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

        // 감사 결과는 클라이언트가 제출한 문자열이 아니라 서버가 보유한
        // 웹소켓 처리 상태만 사용한다. 사용자가 finalize 요청을 변조해
        // 실패한 다운로드를 성공으로 기록할 수 없어야 한다.
        String status = state.getStatus().name();
        String errorMessage = state.getErrorMessage();
        boolean saved = v2Service.saveDownloadAudit(userVo, state, status, errorMessage);
        if (saved) {
            runtimeStore.update(param.getWsSeq(), DownloadRuntimeState::markActLogSaved);
        }

        result.put("success", saved);
        result.put("saved", saved);
        result.put("status", status);
        return result;
    }

      /**
       * 팝업 닫을 때 정리
       * payload 예: {"wsSeq":"1234567890abcdef1234567890abcdef"}
       */
    @PostMapping("/cleanup")
    public @ResponseBody Map<String, Object> cleanup(@RequestBody CommonUpdownV2SeqParam param,
                                                     Authentication authentication,
                                                     HttpServletRequest request) {
        Map<String, Object> result = new HashMap<String, Object>();

        if (param == null || isBlank(param.getWsSeq())) {
            result.put("success", false);
            result.put("message", "wsSeq is required.");
            return result;
        }

        DownloadRuntimeState target = runtimeStore.get(param.getWsSeq());
		if (target != null && !isOwner(target, authentication, request)) {
			result.put("success", false);
			result.put("message", "forbidden");
			return result;
		}
        if (target != null && !target.isActLogSaved()) {
            if (!isTerminal(target)) {
                runtimeStore.update(param.getWsSeq(),
                    state -> state.markFailed("Download cleanup before completion."));
                target = runtimeStore.get(param.getWsSeq());
            }
            if (!ensureTerminalAuditSaved(target, authentication, param.getWsSeq())) {
                result.put("success", false);
                result.put("removed", false);
                result.put("message", "download audit persistence failed");
                return result;
            }
        }
        if (target != null && !deleteIfExists(target.getTempFilePath())) {
            result.put("success", false);
            result.put("removed", false);
            result.put("message", "temporary file cleanup failed");
            return result;
        }

        DownloadRuntimeState removed = runtimeStore.remove(param.getWsSeq());
        result.put("success", true);
        result.put("removed", removed != null);
        return result;
    }

    private boolean deleteIfExists(String path) {
        if (path == null || path.trim().isEmpty()) return true;
        try {
            java.io.File f = new java.io.File(path);
            return !f.exists() || (f.isFile() && f.delete());
        } catch (Exception ignore) {
            return false;
        }
    }

      /**
       * 운영 확인용
       */
    @PostMapping("/cleanup-expired")
    public @ResponseBody Map<String, Object> cleanupExpired() {
        Map<String, Object> result = new HashMap<String, Object>();
		securityAclService.requireManageAcl();
        int removed = cleanupScheduler.cleanupExpiredNow();
        result.put("success", true);
        result.put("removedCount", removed);
        result.put("currentSize", runtimeStore.size());
        return result;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private String nvl(String s) { return s == null ? "" : s; }

    private String safeClientErrorMessage(Exception exception) {
        if (exception instanceof org.springframework.security.access.AccessDeniedException) {
            return "Download access denied.";
        }
        if (exception instanceof IllegalArgumentException) {
            return "Invalid download request.";
        }
        return "Download could not be prepared.";
    }

	private String currentSessionId() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes == null || attributes.getRequest().getSession(false) == null) {
			throw new org.springframework.security.access.AccessDeniedException("session not found");
		}
		return attributes.getRequest().getSession(false).getId();
	}

	private boolean isOwner(DownloadRuntimeState state, Authentication authentication, HttpServletRequest request) {
		if (state == null || authentication == null || !(authentication.getPrincipal() instanceof UserVO)
				|| request == null || request.getSession(false) == null) {
			return false;
		}
		UserVO actor = (UserVO) authentication.getPrincipal();
		return state.isOwnedBy(actor.getUserCd(), request.getSession(false).getId());
	}

    private boolean ensureTerminalAuditSaved(DownloadRuntimeState state, Authentication authentication, String wsSeq) {
        if (state == null || !isTerminal(state)) {
            return state != null && state.isActLogSaved();
        }
        if (state.isActLogSaved()) {
            return true;
        }
        UserVO actor = authentication != null && authentication.getPrincipal() instanceof UserVO
                ? (UserVO) authentication.getPrincipal() : null;
        if (actor == null) {
            return false;
        }
        boolean saved = v2Service.saveDownloadAudit(
                actor, state, state.getStatus().name(), state.getErrorMessage());
        if (saved) {
            runtimeStore.update(wsSeq, DownloadRuntimeState::markActLogSaved);
        }
        return saved;
    }

    private boolean isTerminal(DownloadRuntimeState state) {
        return state != null
                && (state.getStatus() == kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeStatus.COMPLETED
                || state.getStatus() == kr.esob.fdms.commonlogic.updown.runtime.DownloadRuntimeStatus.FAILED);
    }

}

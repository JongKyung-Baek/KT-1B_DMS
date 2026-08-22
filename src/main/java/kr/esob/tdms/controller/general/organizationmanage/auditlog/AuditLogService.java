package kr.esob.tdms.controller.general.organizationmanage.auditlog;

import kr.esob.tdms.commonlogic.abstractclass.CommonService;
import kr.esob.tdms.commonlogic.securityacl.SecurityAuditWriter;
import kr.esob.tdms.controller.login.UserVO;
import kr.esob.tdms.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AuditLogService implements CommonService {

    public static final String SESSION_AUDIT_USER_CD = "auditLogUserCd";
    public static final String SESSION_AUDIT_USER_ID = "auditLogUserId";
    public static final String SESSION_AUDIT_USER_NAME = "auditLogUserName";
    public static final String SESSION_AUDIT_ACCESS_IP = "auditLogAccessIp";
    public static final String SESSION_AUDIT_LOGOUT_RECORDED = "auditLogLogoutRecorded";

    @Inject
    AuditLogDao dao;

    @Inject
    RequestUtil requestUtil;

    @Inject
    SecurityAuditWriter securityAuditWriter;

    @SuppressWarnings("rawtypes")
    @Override
    public List selectList(Object param) {
        return dao.selectList(param);
    }

    @Override
    public int selectListCount(Object obj) {
        Integer count = dao.selectListCount(obj);
        return count == null ? 0 : count;
    }

    public void setSessionAuditInfo(HttpSession session, String userId, String userName, HttpServletRequest request) {
        setSessionAuditInfo(session, resolveAuthenticatedUserCd(userId), userId, userName, request);
    }

    public Map<String, Object> selectSummary() {
        Map<String, Object> summary = dao.selectSummary();
        return summary == null ? emptySummary() : summary;
    }

    private Map<String, Object> emptySummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalToday", 0);
        summary.put("successToday", 0);
        summary.put("deniedToday", 0);
        summary.put("failedToday", 0);
        summary.put("activeUsersToday", 0);
        return summary;
    }

    public void setSessionAuditInfo(HttpSession session, String userCd, String userId,
                                    String userName, HttpServletRequest request) {
        if (session == null) {
            return;
        }

        session.setAttribute(SESSION_AUDIT_USER_CD, normalizeBlank(userCd));
        session.setAttribute(SESSION_AUDIT_USER_ID, normalizeBlank(userId));
        session.setAttribute(SESSION_AUDIT_USER_NAME, normalizeBlank(userName));
        session.setAttribute(SESSION_AUDIT_ACCESS_IP, normalizeBlank(requestUtil.getClientIp(request)));
        session.setAttribute(SESSION_AUDIT_LOGOUT_RECORDED, Boolean.FALSE);
    }

    public void insertLogoutAuditLogIfNeeded(HttpSession session, HttpServletRequest request) {
        insertLogoutAuditLogIfNeeded(session, request == null ? null : requestUtil.getClientIp(request));
    }

    public void insertLogoutAuditLogIfNeeded(HttpSession session, String accessIp) {
        if (session == null || hasLogoutRecorded(session)) {
            return;
        }

        String userId = normalizeBlank(toStringValue(session.getAttribute(SESSION_AUDIT_USER_ID)));
        String userName = normalizeBlank(toStringValue(session.getAttribute(SESSION_AUDIT_USER_NAME)));
        String userCd = normalizeBlank(toStringValue(session.getAttribute(SESSION_AUDIT_USER_CD)));
        String sessionAccessIp = normalizeBlank(toStringValue(session.getAttribute(SESSION_AUDIT_ACCESS_IP)));
        String auditAccessIp = normalizeBlank(accessIp);

        if (auditAccessIp == null) {
            auditAccessIp = sessionAccessIp;
        }

        if (userCd == null && userId == null && userName == null && auditAccessIp == null) {
            return;
        }

        writeCanonicalAuthenticationAudit(
                "logOut", userCd, userId, userName, auditAccessIp,
                safeSessionId(session), null);
        session.setAttribute(SESSION_AUDIT_LOGOUT_RECORDED, Boolean.TRUE);
    }

    public void insertAuditLog(String actionType, String userId, String userName, HttpServletRequest request) {
        insertAuditLog(actionType, resolveAuthenticatedUserCd(userId), userId, userName, request);
    }

    public void insertAuditLog(String actionType, String userCd, String userId,
                               String userName, HttpServletRequest request) {
        String accessIp = request == null ? null : requestUtil.getClientIp(request);
        HttpSession session = request == null ? null : request.getSession(false);
        writeCanonicalAuthenticationAudit(
                actionType, userCd, userId, userName, accessIp,
                session == null ? null : session.getId(),
                resolveTargetUserIdentifier(actionType, request));
    }

    public void insertAuditLog(String actionType, String userId, String userName, String accessIp) {
        writeCanonicalAuthenticationAudit(
                actionType, resolveAuthenticatedUserCd(userId), userId, userName,
                accessIp, null, null);
    }

    private void writeCanonicalAuthenticationAudit(String legacyActionType, String userCd,
                                                    String userId, String userName,
                                                    String accessIp, String sessionId,
                                                    String targetUserIdentifier) {
        AuditAction action = AuditAction.from(legacyActionType);
        UserVO actor = new UserVO();
        actor.setUserCd(normalizeBlank(userCd));
        actor.setUserId(normalizeBlank(userId));
        actor.setUserNm(normalizeBlank(userName));
        try {
            securityAuditWriter.writeAuthentication(
                    actor,
                    action.actionType,
                    action.actionNm,
                    action.resultCd,
                    action.reasonCd,
                    null,
                    normalizeBlank(accessIp),
                    normalizeBlank(sessionId),
                    normalizeBlank(targetUserIdentifier));
        } catch (Exception e) {
            log.warn("Canonical authentication audit write failed; authentication flow is preserved. "
                            + "actionType={}, userId={}, cause={}",
                    action.actionType, normalizeBlank(userId), e.getClass().getSimpleName());
        }
    }

    private String resolveTargetUserIdentifier(String legacyActionType,
                                               HttpServletRequest request) {
        if (!"changePassword".equalsIgnoreCase(legacyActionType) || request == null) {
            return null;
        }
        return normalizeBlank(request.getParameter("userCd"));
    }

    private String resolveAuthenticatedUserCd(String expectedUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserVO)) {
            return null;
        }
        UserVO principal = (UserVO) authentication.getPrincipal();
        String normalizedExpected = normalizeBlank(expectedUserId);
        if (normalizedExpected != null && !normalizedExpected.equals(normalizeBlank(principal.getUserId()))) {
            return null;
        }
        return normalizeBlank(principal.getUserCd());
    }

    private String safeSessionId(HttpSession session) {
        try {
            return session.getId();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasLogoutRecorded(HttpSession session) {
        Object recorded = session.getAttribute(SESSION_AUDIT_LOGOUT_RECORDED);
        return recorded instanceof Boolean && (Boolean) recorded;
    }

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static final class AuditAction {
        private final String actionType;
        private final String actionNm;
        private final String resultCd;
        private final String reasonCd;

        private AuditAction(String actionType, String actionNm,
                            String resultCd, String reasonCd) {
            this.actionType = actionType;
            this.actionNm = actionNm;
            this.resultCd = resultCd;
            this.reasonCd = reasonCd;
        }

        private static AuditAction from(String legacyActionType) {
            if ("loginFail".equalsIgnoreCase(legacyActionType)) {
                return new AuditAction(
                        "LOGIN", "로그인", "FAILURE", "AUTHENTICATION_FAILED");
            }
            if ("logOut".equalsIgnoreCase(legacyActionType)) {
                return new AuditAction("LOGOUT", "로그아웃", "SUCCESS", null);
            }
            if ("changePassword".equalsIgnoreCase(legacyActionType)) {
                return new AuditAction("PASSWORD_CHANGE", "비밀번호 변경", "SUCCESS", null);
            }
            return new AuditAction("LOGIN", "로그인", "SUCCESS", null);
        }
    }
}

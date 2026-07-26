package kr.esob.fdms.commonlogic.securityacl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.fdms.controller.login.UserVO;

@Service
public class SecurityAclService {
    public static final String LIST = "LIST";
    public static final String DETAIL = "DETAIL";
    public static final String VIEW = "VIEW";
    public static final String DOWNLOAD_ORIGINAL = "DOWNLOAD_ORIGINAL";
    public static final String PRINT = "PRINT";
    public static final String MANAGE_ACL = "MANAGE_ACL";

    private static final Set<String> ACTIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        LIST, DETAIL, VIEW, DOWNLOAD_ORIGINAL, PRINT, MANAGE_ACL
    )));
    private static final Set<String> OBJECT_TYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        "DOCUMENT", "DRAWING", "SW", "PRODUCT_DOCUMENT", "PRODUCT_SW", "DXF", "PEER_REVIEW",
        "DOCUMENT_SUB", "DRAWING_SUB", "SW_SUB", "PRODUCT_DOCUMENT_SUB", "PRODUCT_SW_SUB", "DXF_SUB"
    )));

    private final SecurityAclDao dao;
    private final SecurityAuditWriter auditWriter;
    private final ObjectMapper objectMapper;

    public SecurityAclService(SecurityAclDao dao, SecurityAuditWriter auditWriter,
                              ObjectMapper objectMapper) {
        this.dao = dao;
        this.auditWriter = auditWriter;
        this.objectMapper = objectMapper;
    }

    public UserVO requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
            || !(authentication.getPrincipal() instanceof UserVO)) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        return (UserVO) authentication.getPrincipal();
    }

    public void requireManageAcl() {
        UserVO actor = requireCurrentUser();
        if (!dao.hasActionPermission(actor.getUserCd(), MANAGE_ACL)) {
            throw new AccessDeniedException("ACL 관리 권한이 없습니다.");
        }
    }

    public List<SecurityGradeVO> selectGradesForManagement() {
        requireManageAcl();
        return dao.selectGrades();
    }

    @Transactional
    public void saveGrade(SecurityGradeVO grade) {
        requireManageAcl();
        UserVO actor = requireCurrentUser();
        validateGrade(grade);
        if ("Y".equals(grade.getDefaultYn())) {
            dao.clearDefaultGrade(actor.getUserCd());
        }
        requireSingleRow(dao.upsertGrade(grade, actor.getUserCd()), "save security grade");
        recordAclChange(actor, "ACL_CHANGE", "MANAGE_GRADE", "SUCCESS", null,
            "보안등급 저장", null, grade.getGradeCd(), null, null, grade.getGradeCd(), "{}");
    }

    public List<UserClearanceVO> selectUsersForManagement(String keyword) {
        requireManageAcl();
        List<UserClearanceVO> users = dao.selectUsers(trim(keyword));
        for (UserClearanceVO user : users) {
            user.buildPermissions();
        }
        return users;
    }

    @Transactional
    public void saveUserClearance(UserClearanceVO clearance) {
        requireManageAcl();
        UserVO actor = requireCurrentUser();
        if (clearance == null || isBlank(clearance.getUserCd()) || isBlank(clearance.getGradeCd())) {
            throw new IllegalArgumentException("사용자와 보안등급은 필수입니다.");
        }
        clearance.setUserCd(clearance.getUserCd().trim());
        clearance.setGradeCd(normalizeCode(clearance.getGradeCd()));
        if (dao.countGrade(clearance.getGradeCd()) != 1) {
            throw new IllegalArgumentException("사용할 수 없는 보안등급입니다.");
        }
        requireSingleRow(dao.upsertClearance(clearance, actor.getUserCd()), "save user clearance");

        Map<String, String> permissions = clearance.getPermissions();
        for (String action : ACTIONS) {
            String value = permissions == null ? null : permissions.get(action);
            requireSingleRow(
                dao.upsertActionPermission(clearance.getUserCd(), action,
                    normalizeYn(value, false), actor.getUserCd()),
                "save user action permission");
        }
        recordAclChange(actor, "ACL_CHANGE", "MANAGE_CLEARANCE", "SUCCESS", null,
            "사용자 인가등급 저장", null, clearance.getUserCd(), null, null, clearance.getGradeCd(), "{}");
    }

    public List<FileSecurityLabelVO> selectFilesForManagement(String objectType, String keyword) {
        requireManageAcl();
        return dao.selectFiles(normalizeObjectType(objectType), trim(keyword));
    }

    public List<FileUserPermissionVO> selectFileUserPermissionsForManagement(
            String objectType, String objectId, String fileNo) {
        requireManageAcl();
        FileAccessRequest resource = normalizeManagedResource(objectType, objectId, fileNo);
        validateManagedResource(resource);
        requireEffectiveFileGrade(resource);
        return dao.selectFileUserPermissions(resource);
    }

    public List<AccessAuditEventVO> selectAuditForManagement(String keyword, String eventType, String resultCd) {
        requireManageAcl();
        return dao.selectAudit(trim(keyword), normalizeOptionalCode(eventType), normalizeOptionalCode(resultCd));
    }

    /**
     * 접근이력 메뉴 전용 조회.
     *
     * 메뉴 접근 여부는 SecurityConfig의 ROLE_MENU_206으로 통제한다. 이 조회에서
     * MANAGE_ACL을 다시 요구하면 기존 메뉴 권한 배정 화면에서 접근이력을 허용한
     * 사용자도 403이 되므로, 로그인 사용자 확인만 수행한다.
     */
    public List<AccessAuditEventVO> selectAccessHistoryForViewer(
            String keyword, String eventType, String resultCd) {
        requireCurrentUser();
        return dao.selectAudit(trim(keyword), normalizeOptionalCode(eventType), normalizeOptionalCode(resultCd));
    }

    @Transactional
    public void saveFileLabel(FileSecurityLabelVO label) {
        requireManageAcl();
        UserVO actor = requireCurrentUser();
        if (label == null || isBlank(label.getObjectId()) || isBlank(label.getGradeCd())) {
            throw new IllegalArgumentException("자료와 보안등급은 필수입니다.");
        }
        label.setObjectType(normalizeObjectType(label.getObjectType()));
        label.setObjectId(label.getObjectId().trim());
        label.setFileNo(isBlank(label.getFileNo()) ? "*" : label.getFileNo().trim());
        label.setGradeCd(normalizeCode(label.getGradeCd()));
        if (dao.countGrade(label.getGradeCd()) != 1) {
            throw new IllegalArgumentException("사용할 수 없는 보안등급입니다.");
        }
        if (dao.countResource(label) == 0) {
            throw new IllegalArgumentException("등급을 지정할 자료를 찾을 수 없습니다.");
        }
        requireSingleRow(dao.upsertFileLabel(label, actor.getUserCd()), "save file security label");
        recordAclChange(actor, "ACL_CHANGE", "MANAGE_FILE_LABEL", "SUCCESS", null,
            "파일 보안등급 저장", label.getObjectType(), label.getObjectId(), label.getFileNo(), null,
            label.getGradeCd(), "{}");
    }

    @Transactional
    public void saveFileUserPermissions(FileUserPermissionSaveRequest saveRequest) {
        requireManageAcl();
        UserVO actor = requireCurrentUser();
        if (saveRequest == null) {
            throw new IllegalArgumentException("Document permission request is required.");
        }

        FileAccessRequest resource = normalizeManagedResource(
            saveRequest.getObjectType(), saveRequest.getObjectId(), saveRequest.getFileNo());
        validateManagedResource(resource);
        String fileGradeCd = requireEffectiveFileGrade(resource);
        String changeReason = trim(saveRequest.getChangeReason());
        if (changeReason.isEmpty()) {
            throw new IllegalArgumentException("Document permission change reason is required.");
        }
        if (changeReason.length() > 500) {
            throw new IllegalArgumentException("Document permission change reason is too long.");
        }

        List<FileUserPermissionVO> permissions = saveRequest.getPermissions();
        if (permissions == null) {
            permissions = Collections.emptyList();
        }
        if (permissions.isEmpty()) {
            throw new IllegalArgumentException("Document permission entries are required.");
        }
        if (permissions.size() > 500) {
            throw new IllegalArgumentException("Too many document permission entries.");
        }

        Set<String> users = new HashSet<String>();
        for (FileUserPermissionVO permission : permissions) {
            if (permission == null || isBlank(permission.getUserCd())) {
                throw new IllegalArgumentException("Document permission user is required.");
            }
            permission.setUserCd(permission.getUserCd().trim());
            if (!users.add(permission.getUserCd())) {
                throw new IllegalArgumentException("Duplicate document permission user.");
            }
            boolean view = isAllowed(permission.getViewYn());
            if ((isAllowed(permission.getDownloadOriginalYn()) || isAllowed(permission.getPrintYn())) && !view) {
                throw new IllegalArgumentException("Download and print permissions require view permission.");
            }
        }

        List<FileUserPermissionVO> beforePermissions = dao.selectDocumentUserPermissionStates(
            resource, new ArrayList<String>(users));
        int grantedUsers = 0;
        int grantedActions = 0;
        for (FileUserPermissionVO permission : permissions) {
            dao.deleteDocumentUserPermissions(
                resource.getPermissionObjectType(), resource.getObjectId(), permission.getUserCd());
            boolean hasPermission = false;
            if (isAllowed(permission.getViewYn())) {
                upsertDocumentPermission(resource, permission.getUserCd(), LIST, changeReason, actor.getUserCd());
                upsertDocumentPermission(resource, permission.getUserCd(), DETAIL, changeReason, actor.getUserCd());
                upsertDocumentPermission(resource, permission.getUserCd(), VIEW, changeReason, actor.getUserCd());
                grantedActions += 3;
                hasPermission = true;
            }
            if (isAllowed(permission.getDownloadOriginalYn())) {
                upsertDocumentPermission(resource, permission.getUserCd(), DOWNLOAD_ORIGINAL,
                    changeReason, actor.getUserCd());
                grantedActions++;
                hasPermission = true;
            }
            if (isAllowed(permission.getPrintYn())) {
                upsertDocumentPermission(resource, permission.getUserCd(), PRINT,
                    changeReason, actor.getUserCd());
                grantedActions++;
                hasPermission = true;
            }
            if (hasPermission) {
                grantedUsers++;
            }
        }

        String detailJson = buildPermissionAuditDetail(
            beforePermissions, permissions, grantedUsers, grantedActions);
        recordAclChange(actor, "ACL_CHANGE", "MANAGE_DOCUMENT_PERMISSION", "SUCCESS", null,
            changeReason, resource.getObjectType(), resource.getObjectId(), resource.getFileNo(), null,
            fileGradeCd, detailJson);
    }

    public FileAccessDecisionVO checkAccess(FileAccessRequest request) {
        UserVO actor = requireCurrentUser();
        FileAccessRequest normalized = normalizeAccessRequest(request, actor.getUserCd());
        FileAccessDecisionVO decision = dao.selectDecision(normalized);
        if (decision == null) {
            decision = denied(normalized, "ACL_DECISION_ERROR");
        }
        decision.setActorUserCd(actor.getUserCd());
        decision.setActionCd(normalized.getActionCd());
        decision.setObjectType(normalized.getObjectType());
        decision.setObjectId(normalized.getObjectId());
        decision.setFileNo(normalized.getFileNo());
        recordEvent(actor, "FILE_ACCESS", normalized.getActionCd(), decision.isAllowed() ? "ALLOW" : "DENY",
            decision.getReasonCd(), null, normalized.getObjectType(), normalized.getObjectId(), normalized.getFileNo(),
            normalized.getRequestNo(), decision.getFileGradeCd(), "{}");
        return decision;
    }

    public FileAccessDecisionVO requireAccess(FileAccessRequest request) {
        FileAccessDecisionVO decision = checkAccess(request);
        if (!decision.isAllowed()) {
            throw new AccessDeniedException("자료 접근이 거부되었습니다: " + decision.getReasonCd());
        }
        return decision;
    }

    public void recordDownloadResult(UserVO actor, String resultCd, String reasonCd,
                                     String objectType, String objectId, String fileNo,
                                     String requestNo, String message) {
        if (actor == null) {
            actor = requireCurrentUser();
        }
        recordEvent(actor, "DOWNLOAD_RESULT", DOWNLOAD_ORIGINAL, resultCd, reasonCd, message,
            normalizeObjectType(objectType), objectId, fileNo, requestNo, null, "{}");
    }

    public void recordPrintResult(UserVO actor, String resultCd, String reasonCd,
                                  String objectType, String objectId, String fileNo,
                                  String requestNo, String message, String detailJson) {
        if (actor == null) {
            actor = requireCurrentUser();
        }
        auditWriter.writeInCurrentTransaction(actor, "PRINT_RESULT", PRINT, resultCd, reasonCd, message,
            normalizeAuditObjectType(objectType), objectId, fileNo, requestNo, null,
            isBlank(detailJson) ? "{}" : detailJson);
    }

    private String normalizeAuditObjectType(String objectType) {
        String normalized = normalizeCode(objectType);
        // A merged print job is an aggregate audit subject, not a file ACL subject.
        if ("MERGE".equals(normalized)) {
            return normalized;
        }
        return normalizeObjectType(normalized);
    }

    private FileAccessRequest normalizeAccessRequest(FileAccessRequest source, String actorUserCd) {
        if (source == null || isBlank(source.getObjectId())) {
            throw new IllegalArgumentException("자료 식별자는 필수입니다.");
        }
        FileAccessRequest request = normalizeManagedResource(
            source.getObjectType(), source.getObjectId(), source.getFileNo());
        request.setActorUserCd(actorUserCd);
        request.setActionCd(normalizeAction(source.getActionCd()));
        request.setRequestNo(trim(source.getRequestNo()));
        return request;
    }

    private FileAccessRequest normalizeManagedResource(String objectType, String objectId, String fileNo) {
        if (isBlank(objectId)) {
            throw new IllegalArgumentException("자료 식별자는 필수입니다.");
        }
        FileAccessRequest request = new FileAccessRequest();
        request.setObjectType(normalizeObjectType(objectType));
        request.setPermissionObjectType(normalizePermissionObjectType(request.getObjectType()));
        request.setObjectId(objectId.trim());
        request.setFileNo(isBlank(fileNo) ? "*" : fileNo.trim());
        return request;
    }

    private String normalizePermissionObjectType(String objectType) {
        if ("DOCUMENT_SUB".equals(objectType)) return "DOCUMENT";
        if ("DRAWING_SUB".equals(objectType)) return "DRAWING";
        if ("SW_SUB".equals(objectType)) return "SW";
        if ("PRODUCT_DOCUMENT_SUB".equals(objectType)) return "PRODUCT_DOCUMENT";
        if ("PRODUCT_SW_SUB".equals(objectType)) return "PRODUCT_SW";
        if ("DXF_SUB".equals(objectType)) return "DXF";
        return objectType;
    }

    private void validateManagedResource(FileAccessRequest resource) {
        FileSecurityLabelVO label = new FileSecurityLabelVO();
        label.setObjectType(resource.getObjectType());
        label.setObjectId(resource.getObjectId());
        label.setFileNo(resource.getFileNo());
        if (dao.countResource(label) != 1) {
            throw new IllegalArgumentException("자료를 찾을 수 없습니다.");
        }
    }

    private String requireEffectiveFileGrade(FileAccessRequest resource) {
        String gradeCd = dao.selectEffectiveFileGradeCd(resource);
        if (isBlank(gradeCd)) {
            throw new IllegalArgumentException("사용자 권한을 지정하기 전에 자료 보안등급을 먼저 지정하세요.");
        }
        return gradeCd;
    }

    private void upsertDocumentPermission(FileAccessRequest resource, String userCd, String actionCd,
                                          String changeReason, String actorUserCd) {
        requireSingleRow(
            dao.upsertDocumentUserPermission(resource, userCd, actionCd, changeReason, actorUserCd),
            "save document user permission");
    }

    private String normalizeAction(String action) {
        String normalized = normalizeCode(action);
        if (!ACTIONS.contains(normalized) || MANAGE_ACL.equals(normalized)) {
            throw new IllegalArgumentException("지원하지 않는 파일 행위입니다.");
        }
        return normalized;
    }

    public String normalizeObjectType(String objectType) {
        String normalized = normalizeCode(objectType);
        if ("DOC".equals(normalized)) normalized = "DOCUMENT";
        if ("PRODUCTION".equals(normalized) || "PRODUCT".equals(normalized)) normalized = "PRODUCT_DOCUMENT";
        if ("SECP".equals(normalized) || "SECP_PARTDOC".equals(normalized)) normalized = "PRODUCT_SW";
        if ("PEERREVIEW".equals(normalized)) normalized = "PEER_REVIEW";
        if (!OBJECT_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("지원하지 않는 자료 유형입니다: " + normalized);
        }
        return normalized;
    }

    private void validateGrade(SecurityGradeVO grade) {
        if (grade == null || isBlank(grade.getGradeCd()) || isBlank(grade.getGradeNm()) || grade.getGradeLevel() == null) {
            throw new IllegalArgumentException("등급코드, 등급명, 등급순위는 필수입니다.");
        }
        grade.setGradeCd(normalizeCode(grade.getGradeCd()));
        if (!grade.getGradeCd().matches("[A-Z][A-Z0-9_]{1,29}")) {
            throw new IllegalArgumentException("등급코드는 영문 대문자, 숫자, 밑줄만 사용할 수 있습니다.");
        }
        if (grade.getGradeLevel().intValue() < 0) {
            throw new IllegalArgumentException("등급순위는 0 이상이어야 합니다.");
        }
        grade.setDefaultYn(normalizeYn(grade.getDefaultYn(), false));
        grade.setUseYn(normalizeYn(grade.getUseYn(), true));
    }

    private FileAccessDecisionVO denied(FileAccessRequest request, String reasonCd) {
        FileAccessDecisionVO decision = new FileAccessDecisionVO();
        decision.setAllowed(false);
        decision.setReasonCd(reasonCd);
        decision.setActorUserCd(request.getActorUserCd());
        decision.setActionCd(request.getActionCd());
        decision.setObjectType(request.getObjectType());
        decision.setObjectId(request.getObjectId());
        decision.setFileNo(request.getFileNo());
        return decision;
    }

    private String buildPermissionAuditDetail(List<FileUserPermissionVO> beforePermissions,
                                              List<FileUserPermissionVO> afterPermissions,
                                              int grantedUsers, int grantedActions) {
        Map<String, FileUserPermissionVO> beforeByUser =
            new HashMap<String, FileUserPermissionVO>();
        if (beforePermissions != null) {
            for (FileUserPermissionVO permission : beforePermissions) {
                if (permission != null && !isBlank(permission.getUserCd())) {
                    beforeByUser.put(permission.getUserCd(), permission);
                }
            }
        }

        List<Map<String, Object>> changes = new ArrayList<Map<String, Object>>();
        for (FileUserPermissionVO after : afterPermissions) {
            FileUserPermissionVO before = beforeByUser.get(after.getUserCd());
            Map<String, String> beforeFlags = permissionFlags(before);
            Map<String, String> afterFlags = permissionFlags(after);
            if (!beforeFlags.equals(afterFlags)) {
                Map<String, Object> change = new LinkedHashMap<String, Object>();
                change.put("userCd", after.getUserCd());
                change.put("before", beforeFlags);
                change.put("after", afterFlags);
                changes.add(change);
            }
        }

        Map<String, Object> detail = new LinkedHashMap<String, Object>();
        detail.put("submittedUsers", afterPermissions.size());
        detail.put("grantedUsers", grantedUsers);
        detail.put("grantedActions", grantedActions);
        detail.put("changes", changes);
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize document permission audit.", exception);
        }
    }

    private Map<String, String> permissionFlags(FileUserPermissionVO permission) {
        Map<String, String> flags = new LinkedHashMap<String, String>();
        flags.put(VIEW, permission == null ? "N" : normalizeYn(permission.getViewYn(), false));
        flags.put(DOWNLOAD_ORIGINAL,
            permission == null ? "N" : normalizeYn(permission.getDownloadOriginalYn(), false));
        flags.put(PRINT, permission == null ? "N" : normalizeYn(permission.getPrintYn(), false));
        return flags;
    }

    private void recordAclChange(UserVO actor, String eventType, String actionType, String resultCd,
                                 String reasonCd, String message, String objectType, String objectId,
                                 String fileNo, String requestNo, String gradeCd, String detailJson) {
        auditWriter.writeInCurrentTransaction(actor, eventType, actionType, resultCd, reasonCd, message,
            objectType, objectId, fileNo, requestNo, gradeCd, detailJson);
    }

    private void recordEvent(UserVO actor, String eventType, String actionType, String resultCd,
                             String reasonCd, String message, String objectType, String objectId,
                             String fileNo, String requestNo, String gradeCd, String detailJson) {
        auditWriter.write(actor, eventType, actionType, resultCd, reasonCd, message,
                objectType, objectId, fileNo, requestNo, gradeCd, detailJson);
    }

    private void requireSingleRow(int affectedRows, String operation) {
        if (affectedRows != 1) {
            throw new IllegalStateException("Unable to " + operation + ".");
        }
    }

    private String normalizeCode(String value) {
        return trim(value).toUpperCase();
    }

    private String normalizeOptionalCode(String value) {
        return isBlank(value) ? "" : normalizeCode(value);
    }

    private String normalizeYn(String value, boolean defaultValue) {
        if (isBlank(value)) return defaultValue ? "Y" : "N";
        return ("Y".equalsIgnoreCase(value) || "TRUE".equalsIgnoreCase(value) || "1".equals(value)) ? "Y" : "N";
    }

    private boolean isAllowed(String value) {
        return "Y".equals(normalizeYn(value, false));
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

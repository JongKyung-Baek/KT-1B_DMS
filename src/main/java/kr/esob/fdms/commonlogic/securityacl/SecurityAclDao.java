package kr.esob.fdms.commonlogic.securityacl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class SecurityAclDao extends AbstractDao {
    private static final String PREFIX = "sql.SecurityAcl.";

    @SuppressWarnings("unchecked")
    public List<SecurityGradeVO> selectGrades() {
        return listNotUseSession(PREFIX + "selectGrades");
    }

    public int countGrade(String gradeCd) {
        return (Integer) objNotUseSession(PREFIX + "countGrade", gradeCd);
    }

    public int clearDefaultGrade(String actorUserCd) {
        return update(PREFIX + "clearDefaultGrade", actorUserCd);
    }

    public int upsertGrade(SecurityGradeVO grade, String actorUserCd) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("grade", grade);
        param.put("actorUserCd", actorUserCd);
        return update(PREFIX + "upsertGrade", param);
    }

    @SuppressWarnings("unchecked")
    public List<UserClearanceVO> selectUsers(String keyword) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("keyword", keyword);
        return listNotUseSession(PREFIX + "selectUsers", param);
    }

    public int upsertClearance(UserClearanceVO clearance, String actorUserCd) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("clearance", clearance);
        param.put("actorUserCd", actorUserCd);
        return update(PREFIX + "upsertClearance", param);
    }

    public int upsertActionPermission(String userCd, String actionCd, String allowYn, String actorUserCd) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("userCd", userCd);
        param.put("actionCd", actionCd);
        param.put("allowYn", allowYn);
        param.put("actorUserCd", actorUserCd);
        return update(PREFIX + "upsertActionPermission", param);
    }

    @SuppressWarnings("unchecked")
    public List<FileSecurityLabelVO> selectFiles(String objectType, String keyword) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("objectType", objectType);
        param.put("keyword", keyword);
        return listNotUseSession(PREFIX + "selectFiles", param);
    }

    public int countResource(FileSecurityLabelVO label) {
        return (Integer) objNotUseSession(PREFIX + "countResource", label);
    }

    public int upsertFileLabel(FileSecurityLabelVO label, String actorUserCd) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("label", label);
        param.put("actorUserCd", actorUserCd);
        return update(PREFIX + "upsertFileLabel", param);
    }

    public String selectEffectiveFileGradeCd(FileAccessRequest request) {
        return (String) objNotUseSession(PREFIX + "selectEffectiveFileGradeCd", request);
    }

    @SuppressWarnings("unchecked")
    public List<FileUserPermissionVO> selectFileUserPermissions(FileAccessRequest request) {
        return listNotUseSession(PREFIX + "selectFileUserPermissions", request);
    }

    @SuppressWarnings("unchecked")
    public List<FileUserPermissionVO> selectDocumentUserPermissionStates(
            FileAccessRequest request, List<String> userCds) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("request", request);
        param.put("userCds", userCds);
        return listNotUseSession(PREFIX + "selectDocumentUserPermissionStates", param);
    }

    public int deleteDocumentUserPermissions(String permissionObjectType, String objectId, String userCd) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("permissionObjectType", permissionObjectType);
        param.put("objectId", objectId);
        param.put("userCd", userCd);
        return delete(PREFIX + "deleteDocumentUserPermissions", param);
    }

    public int upsertDocumentUserPermission(FileAccessRequest request, String userCd,
                                            String actionCd, String grantReason,
                                            String actorUserCd) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("request", request);
        param.put("userCd", userCd);
        param.put("actionCd", actionCd);
        param.put("grantReason", grantReason);
        param.put("actorUserCd", actorUserCd);
        return update(PREFIX + "upsertDocumentUserPermission", param);
    }

    public FileAccessDecisionVO selectDecision(FileAccessRequest request) {
        return (FileAccessDecisionVO) objNotUseSession(PREFIX + "selectDecision", request);
    }

    public boolean hasActionPermission(String userCd, String actionCd) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("userCd", userCd);
        param.put("actionCd", actionCd);
        return ((Integer) objNotUseSession(PREFIX + "countActionPermission", param)) > 0;
    }

    public int insertAudit(AccessAuditEventVO event) {
        return (Integer) insert(PREFIX + "insertAudit", event);
    }

    @SuppressWarnings("unchecked")
    public List<AccessAuditEventVO> selectAudit(String keyword, String eventType, String resultCd) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("keyword", keyword);
        param.put("eventType", eventType);
        param.put("resultCd", resultCd);
        return listNotUseSession(PREFIX + "selectAudit", param);
    }

    @SuppressWarnings("unchecked")
    public List<AccessAuditEventVO> selectAccessHistory(String keyword, String eventType, String resultCd) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("keyword", keyword);
        param.put("eventType", eventType);
        param.put("resultCd", resultCd);
        return listNotUseSession(PREFIX + "selectAccessHistory", param);
    }
}

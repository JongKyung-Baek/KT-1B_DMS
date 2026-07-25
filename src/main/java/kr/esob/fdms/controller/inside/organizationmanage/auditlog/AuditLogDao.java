package kr.esob.fdms.controller.inside.organizationmanage.auditlog;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AuditLogDao extends AbstractDao {

    private final String prefix = "sql.OrganizationmanageAuditLog.";

    @SuppressWarnings("unchecked")
    public List<AuditLogListVO> selectList(Object param) {
        return list(prefix + "selectList", param);
    }

    public Integer selectListCount(Object param) {
        return (Integer) obj(prefix + "selectListCount", param);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> selectSummary() {
        return (Map<String, Object>) obj(prefix + "selectSummary", null);
    }

    public void insertAuditLog(Object param) {
        insert(prefix + "insertAuditLog", param);
    }
}

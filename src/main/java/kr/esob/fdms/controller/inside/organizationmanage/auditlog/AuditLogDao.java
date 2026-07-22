package kr.esob.fdms.controller.inside.organizationmanage.auditlog;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    public void insertAuditLog(Object param) {
        insert(prefix + "insertAuditLog", param);
    }
}

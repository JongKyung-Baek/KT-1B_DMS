package kr.esob.tdms.controller.general.organizationmanage.partner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class PartnerManagementDao extends AbstractDao {
    private static final String PREFIX = "sql.PartnerManagement.";

    public long nextCompanyId() {
        return ((Number) obj(PREFIX + "nextCompanyId")).longValue();
    }

    public long nextUserId() {
        return ((Number) obj(PREFIX + "nextUserId")).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<PartnerCompany> selectCompanies(String keyword) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("keyword", keyword);
        return list(PREFIX + "selectCompanies", values);
    }

    public PartnerCompany selectCompany(long companyId) {
        return (PartnerCompany) obj(PREFIX + "selectCompany", companyId);
    }

    public PartnerCompany selectCompanyForUpdate(long companyId) {
        return (PartnerCompany) obj(PREFIX + "selectCompanyForUpdate", companyId);
    }

    @SuppressWarnings("unchecked")
    public List<PartnerUser> selectUsers(long companyId, boolean includeInactive) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("partnerCompanyId", companyId);
        values.put("includeInactive", includeInactive);
        return list(PREFIX + "selectUsers", values);
    }

    public int countDuplicateBusinessNo(String businessNo, Long excludeCompanyId) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("businessNo", businessNo);
        values.put("excludeCompanyId", excludeCompanyId);
        return ((Number) obj(PREFIX + "countDuplicateBusinessNo", values)).intValue();
    }

    public int insertCompany(PartnerCompany company, String actorUserCd) {
        return update(PREFIX + "insertCompany", auditValues(company, actorUserCd));
    }

    public int updateCompany(PartnerCompany company, String actorUserCd) {
        return update(PREFIX + "updateCompany", auditValues(company, actorUserCd));
    }

    public int softDeleteCompany(long companyId, String actorUserCd) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("partnerCompanyId", companyId);
        values.put("actorUserCd", actorUserCd);
        return update(PREFIX + "softDeleteCompany", values);
    }

    public int softDeleteCompanyUsers(long companyId, String actorUserCd) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("partnerCompanyId", companyId);
        values.put("actorUserCd", actorUserCd);
        return update(PREFIX + "softDeleteCompanyUsers", values);
    }

    public int clearRepresentatives(long companyId, String actorUserCd) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("partnerCompanyId", companyId);
        values.put("actorUserCd", actorUserCd);
        return update(PREFIX + "clearRepresentatives", values);
    }

    public int insertUser(PartnerUser user, String actorUserCd) {
        return update(PREFIX + "insertUser", userValues(user, actorUserCd));
    }

    public int updateUser(PartnerUser user, String actorUserCd) {
        return update(PREFIX + "updateUser", userValues(user, actorUserCd));
    }

    public int softDeleteMissingUsers(long companyId, List<Long> retainedUserIds,
            String actorUserCd) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("partnerCompanyId", companyId);
        values.put("retainedUserIds", retainedUserIds);
        values.put("actorUserCd", actorUserCd);
        return update(PREFIX + "softDeleteMissingUsers", values);
    }

    @SuppressWarnings("unchecked")
    public List<PartnerRecipient> selectActiveRecipients(Long companyId) {
        return list(PREFIX + "selectActiveRecipients", companyId);
    }

    public PartnerRecipient selectActiveRecipient(long companyId, long userId) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("partnerCompanyId", companyId);
        values.put("partnerUserId", userId);
        return (PartnerRecipient) obj(PREFIX + "selectActiveRecipient", values);
    }

    private Map<String, Object> auditValues(PartnerCompany company, String actorUserCd) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("company", company);
        values.put("actorUserCd", actorUserCd);
        return values;
    }

    private Map<String, Object> userValues(PartnerUser user, String actorUserCd) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("user", user);
        values.put("actorUserCd", actorUserCd);
        return values;
    }
}

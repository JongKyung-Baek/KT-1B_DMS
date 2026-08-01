package kr.esob.tdms.controller.general.organizationmanage.partner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.controller.login.UserVO;

@Service
public class PartnerManagementService {
    private static final int MAX_USERS = 200;

    private final PartnerManagementDao dao;
    private final SecurityAclService aclService;

    public PartnerManagementService(PartnerManagementDao dao, SecurityAclService aclService) {
        this.dao = dao;
        this.aclService = aclService;
    }

    @Transactional(readOnly = true)
    public List<PartnerCompany> list(String keyword) {
        aclService.requireCurrentUser();
        String normalizedKeyword = trim(keyword);
        if (normalizedKeyword.length() > 100) {
            throw PartnerManagementException.badRequest(
                "PARTNER_SEARCH_TOO_LONG", "Search text must be 100 characters or fewer.");
        }
        return dao.selectCompanies(normalizedKeyword);
    }

    @Transactional(readOnly = true)
    public PartnerCompany detail(long partnerCompanyId) {
        aclService.requireCurrentUser();
        PartnerCompany company = requireCompany(partnerCompanyId, false);
        company.setUsers(dao.selectUsers(partnerCompanyId, true));
        return company;
    }

    @Transactional(rollbackFor = Exception.class)
    public PartnerCompany create(PartnerCompany input) {
        UserVO actor = aclService.requireCurrentUser();
        PartnerCompany company = normalize(input, null);
        validateUniqueBusinessNo(company.getBusinessNo(), null);

        long companyId = dao.nextCompanyId();
        company.setPartnerCompanyId(Long.valueOf(companyId));
        company.setCompanyCode(String.format(Locale.ROOT, "PARTNER-%06d", companyId));
        requireOne(dao.insertCompany(company, actor.getUserCd()), "create partner company");
        saveUsers(company, actor.getUserCd(), false);
        return detailWithoutAuthentication(companyId);
    }

    @Transactional(rollbackFor = Exception.class)
    public PartnerCompany update(long partnerCompanyId, PartnerCompany input) {
        UserVO actor = aclService.requireCurrentUser();
        PartnerCompany existing = requireCompany(partnerCompanyId, true);
        PartnerCompany company = normalize(input, Long.valueOf(partnerCompanyId));
        company.setCompanyCode(existing.getCompanyCode());
        validateUniqueBusinessNo(company.getBusinessNo(), Long.valueOf(partnerCompanyId));

        requireOne(dao.updateCompany(company, actor.getUserCd()), "update partner company");
        // Clearing first makes representative transfer safe under the database's
        // one-active-representative partial unique index.
        dao.clearRepresentatives(partnerCompanyId, actor.getUserCd());
        saveUsers(company, actor.getUserCd(), true);
        return detailWithoutAuthentication(partnerCompanyId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(long partnerCompanyId) {
        UserVO actor = aclService.requireCurrentUser();
        requireCompany(partnerCompanyId, true);
        requireOne(dao.softDeleteCompany(partnerCompanyId, actor.getUserCd()),
            "delete partner company");
        dao.softDeleteCompanyUsers(partnerCompanyId, actor.getUserCd());
    }

    private void saveUsers(PartnerCompany company, String actorUserCd, boolean update) {
        List<Long> retainedIds = new ArrayList<Long>();
        Set<Long> newUserIds = new HashSet<Long>();
        for (PartnerUser user : company.getUsers()) {
            user.setPartnerCompanyId(company.getPartnerCompanyId());
            if (user.getPartnerUserId() == null) {
                user.setPartnerUserId(Long.valueOf(dao.nextUserId()));
                newUserIds.add(user.getPartnerUserId());
            } else {
                if (!update || user.getPartnerUserId().longValue() <= 0) {
                    throw PartnerManagementException.badRequest(
                        "INVALID_PARTNER_USER_ID", "A partner user identifier is invalid.");
                }
            }
            retainedIds.add(user.getPartnerUserId());
        }
        if (update) {
            // Retire removed rows before inserting replacements so a recipient
            // may be recreated with the same email without violating the active
            // per-company email index.
            dao.softDeleteMissingUsers(company.getPartnerCompanyId().longValue(), retainedIds,
                actorUserCd);
        }
        for (PartnerUser user : company.getUsers()) {
            if (newUserIds.contains(user.getPartnerUserId())) {
                requireOne(dao.insertUser(user, actorUserCd), "create partner user");
            } else {
                requireOne(dao.updateUser(user, actorUserCd), "update partner user");
            }
        }
    }

    private PartnerCompany normalize(PartnerCompany source, Long companyId) {
        if (source == null) {
            throw PartnerManagementException.badRequest(
                "PARTNER_COMPANY_REQUIRED", "Partner company data is required.");
        }
        PartnerCompany result = new PartnerCompany();
        result.setPartnerCompanyId(companyId);
        result.setCompanyName(required(source.getCompanyName(), 200, "company name"));
        result.setBusinessNo(optional(source.getBusinessNo(), 30, "business number"));
        result.setContactEmail(email(source.getContactEmail(), false));
        result.setContactPhone(optional(source.getContactPhone(), 40, "company phone"));
        result.setAddress(optional(source.getAddress(), 500, "address"));
        result.setUseYn(yn(source.getUseYn(), "Y"));

        List<PartnerUser> sourceUsers = source.getUsers();
        if (sourceUsers == null || sourceUsers.isEmpty() || sourceUsers.size() > MAX_USERS) {
            throw PartnerManagementException.badRequest(
                "PARTNER_USERS_REQUIRED", "A partner needs between 1 and 200 users.");
        }

        int representativeCount = 0;
        Set<Long> identifiers = new HashSet<Long>();
        Set<String> emails = new HashSet<String>();
        List<PartnerUser> users = new ArrayList<PartnerUser>();
        for (PartnerUser sourceUser : sourceUsers) {
            if (sourceUser == null) {
                throw PartnerManagementException.badRequest(
                    "INVALID_PARTNER_USER", "Each partner user is required.");
            }
            PartnerUser user = new PartnerUser();
            user.setPartnerUserId(sourceUser.getPartnerUserId());
            if (user.getPartnerUserId() != null && !identifiers.add(user.getPartnerUserId())) {
                throw PartnerManagementException.badRequest(
                    "DUPLICATE_PARTNER_USER", "A partner user cannot be entered twice.");
            }
            user.setUserName(required(sourceUser.getUserName(), 100, "user name"));
            user.setEmail(email(sourceUser.getEmail(), true));
            user.setPhone(optional(sourceUser.getPhone(), 40, "user phone"));
            user.setPositionName(optional(sourceUser.getPositionName(), 100, "position"));
            user.setUseYn(yn(sourceUser.getUseYn(), "Y"));
            user.setRepresentativeYn(yn(sourceUser.getRepresentativeYn(), "N"));
            if ("Y".equals(user.getRepresentativeYn())) {
                if (!"Y".equals(user.getUseYn())) {
                    throw PartnerManagementException.badRequest(
                        "INACTIVE_REPRESENTATIVE", "The representative user must be active.");
                }
                representativeCount += 1;
            }
            String emailKey = user.getEmail().toLowerCase(Locale.ROOT);
            if (!emails.add(emailKey)) {
                throw PartnerManagementException.badRequest(
                    "DUPLICATE_PARTNER_USER_EMAIL", "Partner user email addresses must be unique within a company.");
            }
            users.add(user);
        }
        if (representativeCount != 1) {
            throw PartnerManagementException.badRequest(
                "ONE_REPRESENTATIVE_REQUIRED", "Exactly one active representative user is required.");
        }
        result.setUsers(users);
        return result;
    }

    private PartnerCompany requireCompany(long companyId, boolean forUpdate) {
        if (companyId <= 0) {
            throw PartnerManagementException.notFound();
        }
        PartnerCompany company = forUpdate
            ? dao.selectCompanyForUpdate(companyId) : dao.selectCompany(companyId);
        if (company == null) {
            throw PartnerManagementException.notFound();
        }
        return company;
    }

    private PartnerCompany detailWithoutAuthentication(long companyId) {
        PartnerCompany company = dao.selectCompany(companyId);
        if (company == null) {
            throw new IllegalStateException("Saved partner company cannot be reloaded.");
        }
        company.setUsers(dao.selectUsers(companyId, true));
        return company;
    }

    private void validateUniqueBusinessNo(String businessNo, Long excludeCompanyId) {
        if (!businessNo.isEmpty()
                && dao.countDuplicateBusinessNo(businessNo, excludeCompanyId) > 0) {
            throw PartnerManagementException.conflict(
                "DUPLICATE_PARTNER_BUSINESS_NO", "The business number is already registered.");
        }
    }

    private String required(String value, int maxLength, String label) {
        String normalized = trim(value);
        if (normalized.isEmpty() || normalized.length() > maxLength) {
            throw PartnerManagementException.badRequest(
                "INVALID_PARTNER_FIELD", label + " is required and must be " + maxLength + " characters or fewer.");
        }
        return normalized;
    }

    private String optional(String value, int maxLength, String label) {
        String normalized = trim(value);
        if (normalized.length() > maxLength) {
            throw PartnerManagementException.badRequest(
                "INVALID_PARTNER_FIELD", label + " must be " + maxLength + " characters or fewer.");
        }
        return normalized;
    }

    private String email(String value, boolean required) {
        String normalized = trim(value);
        if ((required && normalized.isEmpty()) || normalized.length() > 254
                || (!normalized.isEmpty() && (normalized.indexOf('@') <= 0
                    || normalized.lastIndexOf('.') < normalized.indexOf('@') + 2))) {
            throw PartnerManagementException.badRequest(
                "INVALID_PARTNER_EMAIL", "A valid email address is required.");
        }
        return normalized;
    }

    private String yn(String value, String defaultValue) {
        String normalized = trim(value).toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) return defaultValue;
        if (!"Y".equals(normalized) && !"N".equals(normalized)) {
            throw PartnerManagementException.badRequest(
                "INVALID_PARTNER_FLAG", "A Y/N value is invalid.");
        }
        return normalized;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void requireOne(int affectedRows, String operation) {
        if (affectedRows != 1) {
            throw PartnerManagementException.conflict(
                "PARTNER_CONCURRENT_CHANGE", "Unable to " + operation + ". Reload and try again.");
        }
    }
}

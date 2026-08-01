package kr.esob.tdms.controller.general.organizationmanage.partner;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;

/**
 * Distribution-facing directory boundary. Only active companies and active
 * recipient contacts are exposed. Management internals are intentionally not
 * part of this API.
 */
@Service
public class PartnerDirectoryService {
    private final PartnerManagementDao dao;
    private final SecurityAclService aclService;

    public PartnerDirectoryService(PartnerManagementDao dao, SecurityAclService aclService) {
        this.dao = dao;
        this.aclService = aclService;
    }

    @Transactional(readOnly = true)
    public List<PartnerRecipient> listActiveRecipients() {
        aclService.requireCurrentUser();
        return dao.selectActiveRecipients(null);
    }

    @Transactional(readOnly = true)
    public List<PartnerRecipient> listActiveRecipients(long partnerCompanyId) {
        aclService.requireCurrentUser();
        requirePositive(partnerCompanyId);
        return dao.selectActiveRecipients(Long.valueOf(partnerCompanyId));
    }

    @Transactional(readOnly = true)
    public PartnerRecipient requireActiveRecipient(long partnerCompanyId, long partnerUserId) {
        aclService.requireCurrentUser();
        requirePositive(partnerCompanyId);
        requirePositive(partnerUserId);
        PartnerRecipient recipient = dao.selectActiveRecipient(partnerCompanyId, partnerUserId);
        if (recipient == null) {
            throw PartnerManagementException.badRequest(
                "PARTNER_RECIPIENT_UNAVAILABLE", "The selected partner recipient is unavailable.");
        }
        return recipient;
    }

    private void requirePositive(long value) {
        if (value <= 0) {
            throw PartnerManagementException.badRequest(
                "INVALID_PARTNER_IDENTIFIER", "A valid partner identifier is required.");
        }
    }
}

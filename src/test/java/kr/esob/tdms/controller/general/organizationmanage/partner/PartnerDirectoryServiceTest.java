package kr.esob.tdms.controller.general.organizationmanage.partner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.controller.login.UserVO;

@ExtendWith(MockitoExtension.class)
class PartnerDirectoryServiceTest {
    @Mock private PartnerManagementDao dao;
    @Mock private SecurityAclService aclService;

    private PartnerDirectoryService service;

    @BeforeEach
    void setUp() {
        service = new PartnerDirectoryService(dao, aclService);
        when(aclService.requireCurrentUser()).thenReturn(new UserVO());
    }

    @Test
    void exposesAllActiveRecipientsForOneCompany() {
        PartnerRecipient representative = recipient(7, 71, "Y");
        PartnerRecipient user = recipient(7, 72, "N");
        when(dao.selectActiveRecipients(Long.valueOf(7)))
            .thenReturn(Arrays.asList(representative, user));

        assertEquals(2, service.listActiveRecipients(7).size());
        verify(aclService).requireCurrentUser();
    }

    @Test
    void validatesSelectedRecipientAgainstBothCompanyAndUser() {
        when(dao.selectActiveRecipient(7, 99)).thenReturn(null);

        PartnerManagementException error = assertThrows(
            PartnerManagementException.class,
            () -> service.requireActiveRecipient(7, 99));

        assertEquals("PARTNER_RECIPIENT_UNAVAILABLE", error.getCode());
    }

    private PartnerRecipient recipient(long companyId, long userId, String representativeYn) {
        PartnerRecipient recipient = new PartnerRecipient();
        recipient.setPartnerCompanyId(Long.valueOf(companyId));
        recipient.setPartnerUserId(Long.valueOf(userId));
        recipient.setRepresentativeYn(representativeYn);
        return recipient;
    }
}

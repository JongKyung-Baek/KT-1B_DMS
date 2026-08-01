package kr.esob.tdms.controller.general.organizationmanage.partner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.controller.login.UserVO;

@ExtendWith(MockitoExtension.class)
class PartnerManagementServiceTest {
    @Mock private PartnerManagementDao dao;
    @Mock private SecurityAclService aclService;

    private PartnerManagementService service;

    @BeforeEach
    void setUp() {
        service = new PartnerManagementService(dao, aclService);
        UserVO actor = new UserVO();
        actor.setUserCd("USER_ADMIN");
        when(aclService.requireCurrentUser()).thenReturn(actor);
    }

    @Test
    void createsSeparatePartnerRecipientsWithExactlyOneRepresentative() {
        PartnerCompany request = company(
            user("Representative", "rep@example.com", "Y"),
            user("Engineer", "engineer@example.com", "N"));
        PartnerCompany saved = new PartnerCompany();
        saved.setPartnerCompanyId(Long.valueOf(31));
        saved.setCompanyCode("PARTNER-000031");

        when(dao.nextCompanyId()).thenReturn(31L);
        when(dao.nextUserId()).thenReturn(101L, 102L);
        when(dao.insertCompany(any(PartnerCompany.class), eq("USER_ADMIN"))).thenReturn(1);
        when(dao.insertUser(any(PartnerUser.class), eq("USER_ADMIN"))).thenReturn(1);
        when(dao.selectCompany(31L)).thenReturn(saved);
        when(dao.selectUsers(31L, true)).thenReturn(request.getUsers());

        PartnerCompany result = service.create(request);

        assertEquals("PARTNER-000031", result.getCompanyCode());
        ArgumentCaptor<PartnerCompany> companyCaptor = ArgumentCaptor.forClass(PartnerCompany.class);
        verify(dao).insertCompany(companyCaptor.capture(), eq("USER_ADMIN"));
        assertEquals("PARTNER-000031", companyCaptor.getValue().getCompanyCode());
        assertFalse(Arrays.stream(PartnerUser.class.getDeclaredFields())
            .anyMatch(field -> field.getName().equals("password")
                || field.getName().equals("userPwd")
                || field.getName().equals("roleGroup")));
    }

    @Test
    void rejectsMultipleRepresentatives() {
        PartnerCompany request = company(
            user("First", "first@example.com", "Y"),
            user("Second", "second@example.com", "Y"));

        PartnerManagementException error = assertThrows(
            PartnerManagementException.class, () -> service.create(request));

        assertEquals("ONE_REPRESENTATIVE_REQUIRED", error.getCode());
    }

    @Test
    void rejectsInactiveRepresentative() {
        PartnerUser representative = user("Representative", "rep@example.com", "Y");
        representative.setUseYn("N");

        PartnerManagementException error = assertThrows(
            PartnerManagementException.class,
            () -> service.create(company(representative)));

        assertEquals("INACTIVE_REPRESENTATIVE", error.getCode());
    }

    private PartnerCompany company(PartnerUser... users) {
        PartnerCompany company = new PartnerCompany();
        company.setCompanyName("Test Partner");
        company.setBusinessNo("101-22-33333");
        company.setUseYn("Y");
        company.setUsers(users.length == 0
            ? Collections.<PartnerUser>emptyList() : Arrays.asList(users));
        return company;
    }

    private PartnerUser user(String name, String email, String representativeYn) {
        PartnerUser user = new PartnerUser();
        user.setUserName(name);
        user.setEmail(email);
        user.setRepresentativeYn(representativeYn);
        user.setUseYn("Y");
        return user;
    }
}

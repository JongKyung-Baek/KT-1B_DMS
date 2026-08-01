package kr.esob.tdms.controller.general.organizationmanage.partner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PartnerManagementControllerTest {
    @Mock private PartnerManagementService managementService;
    @Mock private PartnerDirectoryService directoryService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(
            new PartnerManagementController(managementService, directoryService)).build();
    }

    @Test
    void listsCompaniesForManagementScreen() throws Exception {
        PartnerCompany company = new PartnerCompany();
        company.setPartnerCompanyId(Long.valueOf(7));
        company.setCompanyName("Partner Seven");
        when(managementService.list("seven")).thenReturn(Arrays.asList(company));

        mvc.perform(get("/general/organizationmanage/partner/api/companies")
                .param("keyword", "seven"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].partnerCompanyId").value(7))
            .andExpect(jsonPath("$[0].companyName").value("Partner Seven"));
    }

    @Test
    void createsAggregateThroughJsonApi() throws Exception {
        PartnerCompany saved = new PartnerCompany();
        saved.setPartnerCompanyId(Long.valueOf(9));
        when(managementService.create(any(PartnerCompany.class))).thenReturn(saved);

        mvc.perform(post("/general/organizationmanage/partner/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"companyName\":\"New Partner\",\"users\":[]}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.partnerCompanyId").value(9));
    }

    @Test
    void deletesCompanyAsSoftDeleteServiceOperation() throws Exception {
        mvc.perform(delete("/general/organizationmanage/partner/api/companies/11"))
            .andExpect(status().isNoContent());

        verify(managementService).delete(11L);
    }

    @Test
    void malformedJsonUsesStableApiError() throws Exception {
        mvc.perform(post("/general/organizationmanage/partner/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"companyName\":"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("INVALID_PARTNER_REQUEST"));
    }
}

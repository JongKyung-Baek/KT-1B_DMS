package kr.esob.tdms.controller.general.distribution.accountrequest;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DistributionAccountRequestAdminControllerTest {
    private DistributionAccountRequestAdminService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(DistributionAccountRequestAdminService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
            new DistributionAccountRequestAdminController(service)).build();
    }

    @Test
    void approveReturnsTheExistingRecordForAnIdempotentServiceRetry()
            throws Exception {
        DistributionAccountRequestRecord approved = new DistributionAccountRequestRecord();
        approved.setRequestId(Long.valueOf(11L));
        approved.setStatus("APPROVED");
        approved.setDecisionComment("approved");
        when(service.approve(eq(11L), argThat(request ->
                request != null && "approved".equals(request.getDecisionComment()))))
            .thenReturn(approved);

        mockMvc.perform(post(
                "/general/distribution/account-requests/api/requests/11/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"decisionComment\":\"approved\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestId").value(11))
            .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(service).approve(eq(11L), argThat(request ->
            request != null && "approved".equals(request.getDecisionComment())));
    }

    @Test
    void changedRedecisionRemainsAnHttpConflict() throws Exception {
        when(service.approve(eq(11L), argThat(request -> request != null)))
            .thenThrow(DistributionAccountRequestException.conflict(
                "INVALID_DISTRIBUTION_ACCOUNT_STATUS_TRANSITION",
                "Only a pending account request may be decided."));

        mockMvc.perform(post(
                "/general/distribution/account-requests/api/requests/11/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"decisionComment\":\"changed\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(
                "INVALID_DISTRIBUTION_ACCOUNT_STATUS_TRANSITION"));
    }
}

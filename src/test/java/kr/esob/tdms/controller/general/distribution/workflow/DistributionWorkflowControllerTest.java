package kr.esob.tdms.controller.general.distribution.workflow;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DistributionWorkflowControllerTest {
    @Mock
    DistributionWorkflowService service;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new DistributionWorkflowController(service))
            .build();
    }

    @Test
    void createReturnsTheCompleteUiDetailContract() throws Exception {
        when(service.create(any())).thenReturn(detail());

        mockMvc.perform(post("/general/distribution/workflow/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Payload package\",\"purpose\":\"Delivery\","
                    + "\"partnerCompanyId\":1,\"recipientUserIds\":[11],"
                    + "\"approverUserCd\":\"USER-2\","
                    + "\"documents\":[{\"objectId\":\"OBJ-1\"}]}"))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.request.requestId").value(31))
            .andExpect(jsonPath("$.request.requestNo").value("DREQ-20260801-00000031"))
            .andExpect(jsonPath("$.request.status").value("DRAFT"))
            .andExpect(jsonPath("$.request.itemCount").value(1))
            .andExpect(jsonPath("$.items", hasSize(1)))
            .andExpect(jsonPath("$.items[0].originalFileName").value("drawing.pdf"))
            .andExpect(jsonPath("$.items[0].gradeCd").value("GENERAL"))
            .andExpect(jsonPath("$.events", hasSize(1)))
            .andExpect(jsonPath("$.events[0].eventType").value("CREATE"))
            .andExpect(jsonPath("$.events[0].actorUserNm").value("Requester"));
    }

    @Test
    void catalogReturnsCategoryAndDisplayMetadataWithoutChangingPayloadIdentifiers() throws Exception {
        DistributionRequestItemSnapshot item = new DistributionRequestItemSnapshot();
        item.setObjectType("SW");
        item.setObjectId("OBJ-1");
        item.setFileNo("1");
        item.setMaterialNo("TD-001");
        item.setMaterialName("2D drawing");
        item.setOriginalFileName("drawing.pdf");
        item.setGradeCd("GENERAL");
        item.setGradeNm("General");
        item.setParentTreeCd("TRB000002");
        item.setParentTreeNm("Drawing");
        item.setTreeCd("TRB000013");
        item.setTreeNm("2D");
        DistributionDocumentBundle bundle = new DistributionDocumentBundle();
        bundle.setObjectId("OBJ-1");
        bundle.setMaterialNo("TD-001");
        bundle.setMaterialName("2D drawing");
        bundle.setParentTreeNm("Drawing");
        bundle.setTreeNm("2D");
        bundle.setMainFileCount(1);
        bundle.setSubFileCount(0);
        bundle.setTotalFileCount(1);
        bundle.setFiles(Collections.singletonList(item));
        when(service.catalog("TRB000013")).thenReturn(Collections.singletonList(bundle));

        mockMvc.perform(get("/general/distribution/workflow/api/catalog")
                .param("treeCd", "TRB000013"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].objectId").value("OBJ-1"))
            .andExpect(jsonPath("$[0].materialNo").value("TD-001"))
            .andExpect(jsonPath("$[0].parentTreeNm").value("Drawing"))
            .andExpect(jsonPath("$[0].treeNm").value("2D"))
            .andExpect(jsonPath("$[0].totalFileCount").value(1))
            .andExpect(jsonPath("$[0].files[0].objectType").value("SW"))
            .andExpect(jsonPath("$[0].files[0].fileNo").value("1"));
    }

    @Test
    void malformedJsonReturnsStableJsonErrorInsteadOfAnHtmlErrorPage() throws Exception {
        mockMvc.perform(post("/general/distribution/workflow/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("INVALID_DISTRIBUTION_API_REQUEST"));

        verify(service, never()).create(any());
    }

    @Test
    void directoryEndpointsExposeTheStableUiOptionFields() throws Exception {
        DistributionPartnerOption partner = new DistributionPartnerOption();
        partner.setPartnerCompanyId(1L);
        partner.setCode("PARTNER-001");
        partner.setName("Partner One");
        DistributionRecipientOption recipient = new DistributionRecipientOption();
        recipient.setPartnerCompanyId(1L);
        recipient.setPartnerUserId(11L);
        recipient.setUserName("Representative");
        recipient.setRepresentativeYn("Y");
        DistributionApproverOption approver = new DistributionApproverOption();
        approver.setApproverUserCd("USER-2");
        approver.setUserId("approver");
        approver.setUserName("Approver");
        when(service.partners()).thenReturn(Collections.singletonList(partner));
        when(service.recipients(1L)).thenReturn(Collections.singletonList(recipient));
        when(service.approvers()).thenReturn(Collections.singletonList(approver));

        mockMvc.perform(get("/general/distribution/workflow/api/directory/partners"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].partnerCompanyId").value(1))
            .andExpect(jsonPath("$[0].code").value("PARTNER-001"))
            .andExpect(jsonPath("$[0].name").value("Partner One"));
        mockMvc.perform(get("/general/distribution/workflow/api/directory/partners/1/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].partnerUserId").value(11))
            .andExpect(jsonPath("$[0].representativeYn").value("Y"));
        mockMvc.perform(get("/general/distribution/workflow/api/directory/approvers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].approverUserCd").value("USER-2"))
            .andExpect(jsonPath("$[0].userName").value("Approver"));
    }

    @Test
    void invalidPagingTypeReturnsStableJsonError() throws Exception {
        mockMvc.perform(get("/general/distribution/workflow/api/requests")
                .param("limit", "not-a-number"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_DISTRIBUTION_API_REQUEST"));
    }

    @Test
    void workflowAndAccessFailuresKeepTheirStatusAndMachineReadableCode() throws Exception {
        when(service.detail(91L)).thenThrow(DistributionWorkflowException.forbidden(
            "DISTRIBUTION_REQUEST_ACCESS_DENIED", "Request access denied."));
        when(service.detail(92L)).thenThrow(DistributionWorkflowException.conflict(
            "INVALID_DISTRIBUTION_STATUS_TRANSITION", "Invalid status."));

        mockMvc.perform(get("/general/distribution/workflow/api/requests/91"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("DISTRIBUTION_REQUEST_ACCESS_DENIED"));
        mockMvc.perform(get("/general/distribution/workflow/api/requests/92"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("INVALID_DISTRIBUTION_STATUS_TRANSITION"));
    }

    private DistributionRequestDetail detail() {
        DistributionRequestRecord request = new DistributionRequestRecord();
        request.setRequestId(31L);
        request.setRequestNo("DREQ-20260801-00000031");
        request.setTitle("Payload package");
        request.setPurpose("Delivery");
        request.setStatus("DRAFT");
        request.setRequestedByUserCd("USER-1");
        request.setRequestedByUserId("requester");
        request.setRequestedByUserNm("Requester");
        request.setCreatedAt("2026-08-01 09:00:00");
        request.setUpdatedAt("2026-08-01 09:00:00");
        request.setItemCount(1);

        DistributionRequestItemSnapshot item = new DistributionRequestItemSnapshot();
        item.setItemId(1L);
        item.setRequestId(31L);
        item.setLineNo(1);
        item.setObjectType("SW");
        item.setObjectId("OBJ-1");
        item.setFileNo("1");
        item.setMaterialNo("TD-001");
        item.setMaterialName("Drawing");
        item.setOriginalFileName("drawing.pdf");
        item.setFileSize(1234L);
        item.setGradeCd("GENERAL");

        DistributionRequestEventRecord event = new DistributionRequestEventRecord();
        event.setEventId(1L);
        event.setRequestId(31L);
        event.setToStatus("DRAFT");
        event.setEventType("CREATE");
        event.setActorUserCd("USER-1");
        event.setActorUserId("requester");
        event.setActorUserNm("Requester");
        event.setOccurredAt("2026-08-01 09:00:00");

        DistributionRequestDetail detail = new DistributionRequestDetail();
        detail.setRequest(request);
        detail.setItems(Collections.singletonList(item));
        detail.setEvents(Collections.singletonList(event));
        return detail;
    }
}

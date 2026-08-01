package kr.esob.tdms.controller.general.distribution.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.commonlogic.value.Constant;
import kr.esob.tdms.controller.general.distribution.swrequest.SwRequestService;
import kr.esob.tdms.controller.login.UserVO;

class DistributionWorkflowPageContractTest {
    private static final Path ROOT = Paths.get(System.getProperty("user.dir"));
    private static final Path VIEW_ROOT = ROOT.resolve(
        "src/main/webapp/WEB-INF/views/general/distribution/workflow");
    private static final Path SCRIPT = ROOT.resolve(
        "src/main/resources/static/js/views/general/distribution/workflow/distribution-workflow.js");
    private static final Path STYLE = ROOT.resolve(
        "src/main/resources/static/css/pages/distribution-workflow.css");

    private SecurityAclService aclService;
    private SwRequestService swRequestService;
    private DistributionWorkflowService workflowService;
    private DistributionWorkflowPageController controller;
    private UserVO actor;

    @BeforeEach
    void setUp() {
        aclService = mock(SecurityAclService.class);
        swRequestService = mock(SwRequestService.class);
        workflowService = mock(DistributionWorkflowService.class);
        actor = new UserVO();
        actor.setUserCd("USER-1");
        actor.setRoleGroup("RG_USER");
        when(aclService.requireCurrentUser()).thenReturn(actor);
        when(workflowService.selectionPreview(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<DistributionRequestItemRef> refs = invocation.getArgument(0);
            java.util.ArrayList<DistributionRequestItemSnapshot> snapshots =
                new java.util.ArrayList<DistributionRequestItemSnapshot>();
            for (DistributionRequestItemRef ref : refs) {
                DistributionRequestItemSnapshot snapshot = new DistributionRequestItemSnapshot();
                snapshot.setObjectType(ref.getObjectType());
                snapshot.setObjectId(ref.getObjectId());
                snapshot.setFileNo(ref.getFileNo());
                snapshots.add(snapshot);
            }
            return snapshots;
        });
        controller = new DistributionWorkflowPageController(
            aclService, swRequestService, workflowService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void newRequestAcceptsRepeatedItemParametersAndDropsUnsafeOrDuplicateItems() {
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.newRequest(
            Arrays.asList("sw", "SW_SUB", "SW", "DOCUMENT"),
            Arrays.asList("OBJ-1", "OBJ_2", "OBJ-1", "<script>"),
            Arrays.asList("1", "2", "1", "3"), model);

        assertEquals("/general/distribution/workflow/myRequests", view);
        assertEquals(Boolean.TRUE, model.get("workflowOpenCreate"));
        List<DistributionRequestItemSnapshot> items =
            (List<DistributionRequestItemSnapshot>) model.get("initialItems");
        assertEquals(2, items.size());
        assertEquals("SW", items.get(0).getObjectType());
        assertEquals("OBJ-1", items.get(0).getObjectId());
        assertEquals("SW_SUB", items.get(1).getObjectType());
        assertEquals("2", items.get(1).getFileNo());
    }

    @Test
    void requestsPageSupportsNoPreselectedItemsAndDoesNotForceTheEditorOpen() {
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.requests(null, null, null, model);

        assertEquals("/general/distribution/workflow/myRequests", view);
        assertEquals(Boolean.FALSE, model.get("workflowOpenCreate"));
        assertEquals(Collections.emptyList(), model.get("initialItems"));
    }

    @Test
    void newRequestRouteBindsRepeatedTechnicalDataParameters() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/general/distribution/workflow/requests/new")
                .param("objectType", "SW", "SW_SUB")
                .param("objectId", "OBJ-1", "OBJ-2")
                .param("fileNo", "1", "2"))
            .andExpect(status().isOk())
            .andExpect(view().name("/general/distribution/workflow/myRequests"))
            .andExpect(model().attribute("workflowOpenCreate", true))
            .andExpect(model().attributeExists("initialItems"));
    }

    @Test
    void approvalPageRequiresTheAdministratorRole() {
        assertThrows(AccessDeniedException.class,
            () -> controller.approval(new ExtendedModelMap()));

        actor.setRoleGroup(Constant.GROUP_CD_ADMIN);
        assertEquals("/general/distribution/workflow/approvalQueue",
            controller.approval(new ExtendedModelMap()));
    }

    @Test
    void viewsShareOneApiClientAndExposeAllThreeLifecycleScreens() throws Exception {
        String myRequests = read(VIEW_ROOT.resolve("myRequests.jsp"));
        String approvalQueue = read(VIEW_ROOT.resolve("approvalQueue.jsp"));
        String approvedList = read(VIEW_ROOT.resolve("approvedList.jsp"));
        String dialog = read(VIEW_ROOT.resolve("workflowDialog.jspf"));
        String script = read(SCRIPT);
        String style = read(STYLE);

        assertTrue(myRequests.contains("mode: 'mine'"));
        assertTrue(approvalQueue.contains("mode: 'approval'"));
        assertTrue(approvedList.contains("mode: 'approved'"));
        assertTrue(myRequests.contains("workflowInitialItems"));
        assertTrue(myRequests.contains("workflowCategoryOptions"));
        assertTrue(myRequests.contains("dw-category-parent-option"));
        assertTrue(myRequests.contains("dw-category-child-option"));
        assertTrue(myRequests.contains("data-parent-tree-cd"));
        assertTrue(myRequests.contains("data-material-no"));
        assertTrue(myRequests.indexOf("distribution-workflow.js") > myRequests.indexOf("</main>"));
        assertTrue(approvalQueue.indexOf("distribution-workflow.js") > approvalQueue.indexOf("</main>"));
        assertTrue(approvedList.indexOf("distribution-workflow.js") > approvedList.indexOf("</main>"));
        assertTrue(dialog.contains("workflowSaveDraft"));
        assertTrue(dialog.contains("workflowApprove"));
        assertTrue(dialog.contains("workflowReject"));
        assertTrue(dialog.contains("workflowEventsBody"));
        assertTrue(dialog.contains("workflowPartnerCompany"));
        assertTrue(dialog.contains("workflowRecipients"));
        assertTrue(dialog.contains("workflowApprover"));
        assertTrue(dialog.contains("workflowDistributionStartDate"));
        assertTrue(dialog.contains("workflowDistributionEndDate"));
        assertTrue(dialog.contains("feature.distributionWorkflow.column.fileBundle"));
        assertTrue(dialog.contains("data-i18n-key="));

        assertTrue(script.contains("/general/distribution/workflow/api"));
        assertTrue(script.contains("/approval-queue?"));
        assertTrue(script.contains("/approved?"));
        assertTrue(script.contains("'/submit'"));
        assertTrue(script.contains("decide('approve')"));
        assertTrue(script.contains("decide('reject')"));
        assertTrue(script.contains("response.status === 403"));
        assertTrue(script.contains("message.accessDenied"));
        assertTrue(script.contains("record.requestedByUserCd === window.USER_CD"));
        assertTrue(script.contains(".dw-item-category-parent"));
        assertTrue(script.contains(".dw-item-category-child"));
        assertTrue(script.contains("/catalog?treeCd="));
        assertTrue(script.contains("function documentBundles(items)"));
        assertTrue(script.contains("function bundleSummary(bundle)"));
        assertTrue(script.contains("recipientUserIds: recipientUserIds"));
        assertTrue(script.contains("approverUserCd: approverUserCd"),
            "distribution payload must include the selected approver");
        assertTrue(script.contains("distributionStartDate: startDate"));
        assertTrue(script.contains("documents: documents"));
        assertTrue(script.contains("/directory/partners"));
        assertFalse(script.contains(".dw-item-file-select"));
        assertTrue(script.contains("feature.techList.tree.category."));
        assertTrue(script.contains("String(bundle.objectId)"));
        assertFalse(script.contains("['SW', 'SW_SUB'].forEach"));
        assertFalse(script.contains("demo.esob.kr"));
        assertFalse(script.contains("RestTemplate"));
        assertTrue(style.contains(".dw-card"));
        assertTrue(style.contains(".dw-table"));
        assertTrue(style.contains(".dw-status-chip"));
        assertTrue(style.contains(".dw-recipient-list"));
        assertTrue(style.contains(".dw-item-bundle"));
        assertTrue(style.contains(".dw-dialog__header > div"));
        assertTrue(style.contains(".dw-items__header > div"));
        assertTrue(style.contains("text-align: left"));
    }

    private String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}

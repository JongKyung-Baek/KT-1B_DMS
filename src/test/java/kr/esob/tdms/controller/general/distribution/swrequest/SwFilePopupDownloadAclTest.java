package kr.esob.tdms.controller.general.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import kr.esob.tdms.commonlogic.securityacl.FileAccessDecisionVO;
import kr.esob.tdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;

class SwFilePopupDownloadAclTest {
    private SwRequestService requestService;
    private SecurityAclService aclService;
    private SwRequestController controller;

    @BeforeEach
    void setUp() {
        requestService = mock(SwRequestService.class);
        aclService = mock(SecurityAclService.class);
        controller = new SwRequestController();
        controller.service = requestService;
        controller.securityAclService = aclService;

        when(requestService.getSwFileDownloadInfo("SW-1", null)).thenReturn(map(
            "objectId", "SW-1", "aclObjectId", "SW-1", "aclObjectType", "SW", "fileNo", "1"));
        when(requestService.selectSwDetailInfo("SW-1")).thenReturn(Collections.emptyMap());
        when(aclService.requireAccess(any(FileAccessRequest.class))).thenReturn(decision(true));
        when(aclService.hasCurrentUserActionPermission(SecurityAclService.DOWNLOAD_ORIGINAL)).thenReturn(true);
    }

    @Test
    void mainAndSupportingButtonsReflectTheirOwnAccessibleFiles() {
        when(requestService.selectMainFileInfo("SW-1")).thenReturn(rows(
            map("objectId", "SW-1", "fileNo", "1", "orgFileNm", "main.pdf")));
        when(requestService.selectSubFileInfo("SW-1")).thenReturn(rows(
            map("objectId", "SUB-1", "parentObjectId", "SW-1", "fileNo", "2",
                "orgFileNm", "support.xlsx")));
        when(aclService.checkAccessForDisplay(any(FileAccessRequest.class))).thenAnswer(invocation -> {
            FileAccessRequest access = invocation.getArgument(0);
            if (SecurityAclService.VIEW.equals(access.getActionCd())) return decision(true);
            return decision("SW".equals(access.getObjectType()) && "1".equals(access.getFileNo()));
        });

        ExtendedModelMap model = openPopup();

        assertEquals(Boolean.TRUE, model.get("mainDownloadAllowed"));
        assertEquals(Boolean.FALSE, model.get("subDownloadAllowed"));
        assertTrue(rowDownloadAllowed(model, "mainFileList"));
        assertFalse(rowDownloadAllowed(model, "subFileList"));
        verify(aclService, atLeastOnce()).checkAccessForDisplay(any(FileAccessRequest.class));
        verify(aclService).requireAccess(any(FileAccessRequest.class));
    }

    @Test
    void generalHanWithOnlyGlobalPermissionDoesNotSeeEitherDownloadButton() {
        when(requestService.selectMainFileInfo("SW-1")).thenReturn(rows(
            map("objectId", "SW-1", "fileNo", "1", "orgFileNm", "main.pdf")));
        when(requestService.selectSubFileInfo("SW-1")).thenReturn(rows(
            map("objectId", "SUB-1", "parentObjectId", "SW-1", "fileNo", "2",
                "orgFileNm", "support.xlsx")));
        when(aclService.checkAccessForDisplay(any(FileAccessRequest.class))).thenAnswer(invocation -> {
            FileAccessRequest access = invocation.getArgument(0);
            return decision(SecurityAclService.VIEW.equals(access.getActionCd()));
        });

        ExtendedModelMap model = openPopup();

        assertEquals(Boolean.FALSE, model.get("mainDownloadAllowed"));
        assertEquals(Boolean.FALSE, model.get("subDownloadAllowed"));
        assertFalse(rowDownloadAllowed(model, "mainFileList"));
        assertFalse(rowDownloadAllowed(model, "subFileList"));
    }

    private ExtendedModelMap openPopup() {
        SwRequestParam param = new SwRequestParam();
        param.setObjectId("SW-1");
        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals("general/distribution/swFilePopup", controller.swFilePopup(param, model));
        return model;
    }

    @SuppressWarnings("unchecked")
    private boolean rowDownloadAllowed(ExtendedModelMap model, String attribute) {
        List<Map<String, Object>> rows = (List<Map<String, Object>>) model.get(attribute);
        return Boolean.TRUE.equals(rows.get(0).get("downloadAllowed"));
    }

    private FileAccessDecisionVO decision(boolean allowed) {
        FileAccessDecisionVO decision = new FileAccessDecisionVO();
        decision.setAllowed(allowed);
        return decision;
    }

    private List<Map<String, Object>> rows(Map<String, Object> row) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        rows.add(row);
        return rows;
    }

    private Map<String, Object> map(Object... values) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (int index = 0; index < values.length; index += 2) {
            result.put(String.valueOf(values[index]), values[index + 1]);
        }
        return result;
    }
}

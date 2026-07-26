package kr.esob.fdms.commonlogic.securityacl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.fdms.controller.login.UserVO;

class DocumentUserPermissionServiceTest {
    private SecurityAclDao dao;
    private SecurityAuditWriter auditWriter;
    private SecurityAclService service;

    @BeforeEach
    void setUp() {
        dao = mock(SecurityAclDao.class);
        auditWriter = mock(SecurityAuditWriter.class);
        service = new SecurityAclService(
            dao, auditWriter, new ObjectMapper());

        UserVO actor = new UserVO();
        actor.setUserCd("ADMIN");
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(actor, null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(dao.hasActionPermission("ADMIN", SecurityAclService.MANAGE_ACL)).thenReturn(true);
        when(dao.countResource(any(FileSecurityLabelVO.class))).thenReturn(1);
        when(dao.selectEffectiveFileGradeCd(any(FileAccessRequest.class))).thenReturn("GENERAL");
        when(dao.upsertDocumentUserPermission(
            any(FileAccessRequest.class), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void viewGrantWritesListDetailAndViewForTheParentDocument() {
        FileUserPermissionVO permission = new FileUserPermissionVO();
        permission.setUserCd("USER-1");
        permission.setViewYn("Y");

        FileUserPermissionSaveRequest request = request("DRAWING_SUB");
        request.setPermissions(Collections.singletonList(permission));

        service.saveFileUserPermissions(request);

        InOrder ordered = inOrder(dao);
        ordered.verify(dao).deleteDocumentUserPermissions("DRAWING", "OBJ-1", "USER-1");
        ordered.verify(dao).upsertDocumentUserPermission(
            any(FileAccessRequest.class), eq("USER-1"), eq(SecurityAclService.LIST),
            eq("업무 담당자 지정"), eq("ADMIN"));
        ordered.verify(dao).upsertDocumentUserPermission(
            any(FileAccessRequest.class), eq("USER-1"), eq(SecurityAclService.DETAIL),
            eq("업무 담당자 지정"), eq("ADMIN"));
        ordered.verify(dao).upsertDocumentUserPermission(
            any(FileAccessRequest.class), eq("USER-1"), eq(SecurityAclService.VIEW),
            eq("업무 담당자 지정"), eq("ADMIN"));
        verify(auditWriter).writeInCurrentTransaction(
            any(UserVO.class), eq("ACL_CHANGE"), eq("MANAGE_DOCUMENT_PERMISSION"),
            eq("SUCCESS"), isNull(), eq("업무 담당자 지정"),
            eq("DRAWING_SUB"), eq("OBJ-1"), eq("1"), isNull(), eq("GENERAL"),
            anyString());
    }

    @Test
    void gradeChangeUsesStableActionAndDoesNotPersistALocalizedSuccessMessage() {
        when(dao.upsertGrade(any(SecurityGradeVO.class), eq("ADMIN"))).thenReturn(1);
        SecurityGradeVO grade = new SecurityGradeVO();
        grade.setGradeCd("GENERAL");
        grade.setGradeNm("일반");
        grade.setGradeLevel(Integer.valueOf(0));

        service.saveGrade(grade);

        verify(auditWriter).writeInCurrentTransaction(
            any(UserVO.class), eq("ACL_CHANGE"), eq("MANAGE_GRADE"),
            eq("SUCCESS"), isNull(), isNull(), isNull(), eq("GENERAL"),
            isNull(), isNull(), eq("GENERAL"), eq("{}"));
    }

    @Test
    void downloadOrPrintCannotBeGrantedWithoutView() {
        FileUserPermissionVO permission = new FileUserPermissionVO();
        permission.setUserCd("USER-1");
        permission.setDownloadOriginalYn("Y");

        FileUserPermissionSaveRequest request = request("DOCUMENT");
        request.setPermissions(Collections.singletonList(permission));

        assertThrows(IllegalArgumentException.class,
            () -> service.saveFileUserPermissions(request));
        verify(dao, never()).deleteDocumentUserPermissions(anyString(), anyString(), anyString());
    }

    @Test
    void accessDecisionUsesTheParentDocumentPermissionSubject() {
        FileAccessDecisionVO decision = new FileAccessDecisionVO();
        decision.setAllowed(true);
        decision.setReasonCd("ALLOW");
        when(dao.selectDecision(any(FileAccessRequest.class))).thenReturn(decision);

        FileAccessRequest request = new FileAccessRequest();
        request.setActionCd(SecurityAclService.VIEW);
        request.setObjectType("PRODUCT_DOCUMENT_SUB");
        request.setObjectId("OBJ-1");
        request.setFileNo("11");

        service.checkAccess(request);

        ArgumentCaptor<FileAccessRequest> captor =
            ArgumentCaptor.forClass(FileAccessRequest.class);
        verify(dao).selectDecision(captor.capture());
        assertEquals("PRODUCT_DOCUMENT", captor.getValue().getPermissionObjectType());
        assertEquals("PRODUCT_DOCUMENT_SUB", captor.getValue().getObjectType());
    }

    private FileUserPermissionSaveRequest request(String objectType) {
        FileUserPermissionSaveRequest request = new FileUserPermissionSaveRequest();
        request.setObjectType(objectType);
        request.setObjectId("OBJ-1");
        request.setFileNo("1");
        request.setChangeReason("업무 담당자 지정");
        return request;
    }
}

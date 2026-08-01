package kr.esob.tdms.commonlogic.securityacl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.esob.tdms.controller.login.UserVO;

class SecurityAclRegistrationBootstrapTest {
    private SecurityAclDao dao;
    private SecurityAuditWriter auditWriter;
    private SecurityAclService service;

    @BeforeEach
    void setUp() {
        dao = mock(SecurityAclDao.class);
        auditWriter = mock(SecurityAuditWriter.class);
        service = new SecurityAclService(dao, auditWriter, new ObjectMapper());

        UserVO registrant = new UserVO();
        registrant.setUserCd("REGISTRANT");
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                registrant, null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(dao.countResource(any(FileSecurityLabelVO.class))).thenReturn(1);
        when(dao.upsertFileLabel(any(FileSecurityLabelVO.class), eq("REGISTRANT")))
            .thenReturn(1);
        when(dao.upsertDocumentUserPermission(
            any(FileAccessRequest.class), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrationUsesActiveDefaultGradeAndAuthenticatedRegistrantOnly() {
        when(dao.selectGrades()).thenReturn(Arrays.asList(
            grade("INACTIVE_DEFAULT", "Y", "N"),
            grade("GENERAL", "Y", "Y"),
            grade("INTERNAL", "N", "Y")));
        when(dao.hasActionPermission("REGISTRANT", SecurityAclService.DOWNLOAD_ORIGINAL))
            .thenReturn(true);
        when(dao.hasActionPermission("REGISTRANT", SecurityAclService.PRINT))
            .thenReturn(false);

        service.initializeRegisteredSwAcl(" SW-OBJECT-1 ");

        ArgumentCaptor<FileSecurityLabelVO> labelCaptor =
            ArgumentCaptor.forClass(FileSecurityLabelVO.class);
        verify(dao).upsertFileLabel(labelCaptor.capture(), eq("REGISTRANT"));
        assertEquals("SW", labelCaptor.getValue().getObjectType());
        assertEquals("SW-OBJECT-1", labelCaptor.getValue().getObjectId());
        assertEquals("*", labelCaptor.getValue().getFileNo());
        assertEquals("GENERAL", labelCaptor.getValue().getGradeCd());
        assertEquals("REGISTER_DEFAULT_GRADE", labelCaptor.getValue().getLabelReason());

        ArgumentCaptor<String> actionCaptor = ArgumentCaptor.forClass(String.class);
        verify(dao, times(4)).upsertDocumentUserPermission(
            any(FileAccessRequest.class), eq("REGISTRANT"), actionCaptor.capture(),
            eq("REGISTERED_DOCUMENT_OWNER"), eq("REGISTRANT"));
        assertEquals(
            Arrays.asList(
                SecurityAclService.LIST,
                SecurityAclService.DETAIL,
                SecurityAclService.VIEW,
                SecurityAclService.DOWNLOAD_ORIGINAL),
            actionCaptor.getAllValues());
        verify(dao, never()).upsertDocumentUserPermission(
            any(FileAccessRequest.class), eq("ADMIN"), anyString(), anyString(), anyString());
    }

    @Test
    void registrationFailsClosedWhenNoActiveDefaultGradeExists() {
        when(dao.selectGrades()).thenReturn(Arrays.asList(
            grade("GENERAL", "Y", "N"),
            grade("INTERNAL", "N", "Y")));

        assertThrows(
            IllegalStateException.class,
            () -> service.initializeRegisteredSwAcl("SW-OBJECT-1"));

        verify(dao, never()).upsertFileLabel(any(FileSecurityLabelVO.class), anyString());
        verify(dao, never()).upsertDocumentUserPermission(
            any(FileAccessRequest.class), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void registrationFailsClosedWhenMultipleActiveDefaultsExist() {
        when(dao.selectGrades()).thenReturn(Arrays.asList(
            grade("GENERAL", "Y", "Y"),
            grade("INTERNAL", "Y", "Y")));

        assertThrows(
            IllegalStateException.class,
            () -> service.initializeRegisteredSwAcl("SW-OBJECT-1"));

        verify(dao, never()).upsertFileLabel(any(FileSecurityLabelVO.class), anyString());
    }

    private SecurityGradeVO grade(String gradeCd, String defaultYn, String useYn) {
        SecurityGradeVO grade = new SecurityGradeVO();
        grade.setGradeCd(gradeCd);
        grade.setDefaultYn(defaultYn);
        grade.setUseYn(useYn);
        return grade;
    }
}

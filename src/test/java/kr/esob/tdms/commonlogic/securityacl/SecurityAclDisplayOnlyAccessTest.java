package kr.esob.tdms.commonlogic.securityacl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

class SecurityAclDisplayOnlyAccessTest {
    private SecurityAclDao dao;
    private SecurityAuditWriter auditWriter;
    private SecurityAclService service;

    @BeforeEach
    void setUp() {
        dao = mock(SecurityAclDao.class);
        auditWriter = mock(SecurityAuditWriter.class);
        service = new SecurityAclService(dao, auditWriter, new ObjectMapper());

        UserVO user = new UserVO();
        user.setUserCd("USER_GENERAL_HAN");
        user.setUserId("general.han");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(user, null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void displayOnlyDecisionUsesTheAuthenticatedFileAclWithoutWritingAudit() {
        FileAccessDecisionVO storedDecision = new FileAccessDecisionVO();
        storedDecision.setAllowed(true);
        storedDecision.setReasonCd("DOCUMENT_PERMISSION");
        when(dao.selectDecision(any(FileAccessRequest.class))).thenReturn(storedDecision);

        FileAccessRequest request = new FileAccessRequest();
        request.setActionCd(SecurityAclService.DOWNLOAD_ORIGINAL);
        request.setObjectType("sw_sub");
        request.setObjectId("SW-OBJECT-1");
        request.setFileNo("2");

        FileAccessDecisionVO decision = service.checkAccessForDisplay(request);

        assertTrue(decision.isAllowed());
        assertEquals("USER_GENERAL_HAN", decision.getActorUserCd());
        assertEquals(SecurityAclService.DOWNLOAD_ORIGINAL, decision.getActionCd());
        assertEquals("SW_SUB", decision.getObjectType());
        assertEquals("SW-OBJECT-1", decision.getObjectId());
        assertEquals("2", decision.getFileNo());

        ArgumentCaptor<FileAccessRequest> normalized = ArgumentCaptor.forClass(FileAccessRequest.class);
        verify(dao).selectDecision(normalized.capture());
        assertEquals("USER_GENERAL_HAN", normalized.getValue().getActorUserCd());
        verifyNoInteractions(auditWriter);
    }
}

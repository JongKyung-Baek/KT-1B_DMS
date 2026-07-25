package kr.esob.fdms.commonlogic.securityacl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.fdms.controller.login.UserVO;

class SecurityAuditTransactionTest {
    @Test
    void auditWriterUsesAnIndependentTransaction() throws Exception {
        Method write = SecurityAuditWriter.class.getMethod("write",
                UserVO.class, String.class, String.class, String.class,
                String.class, String.class, String.class, String.class,
                String.class, String.class, String.class, String.class);
        Transactional transactional = write.getAnnotation(Transactional.class);

        assertNotNull(transactional);
        assertEquals(Propagation.REQUIRES_NEW, transactional.propagation());
    }

    @Test
    void auditInsertMustAffectExactlyOneRow() {
        SecurityAclDao dao = mock(SecurityAclDao.class);
        when(dao.insertAudit(any(AccessAuditEventVO.class))).thenReturn(1);
        SecurityAuditWriter writer = new SecurityAuditWriter(dao);
        UserVO actor = new UserVO();
        actor.setUserCd("USER-1");

        writer.write(actor, "FILE_ACCESS", "VIEW", "DENY",
                "CLEARANCE_TOO_LOW", null, "DOCUMENT", "DOC-1",
                "FILE-1", "REQ-1", "RESTRICTED", "{}");

        verify(dao).insertAudit(any(AccessAuditEventVO.class));
    }
}
